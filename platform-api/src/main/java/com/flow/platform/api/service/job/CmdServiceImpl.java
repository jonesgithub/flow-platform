/*
 * Copyright 2017 flow.ci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flow.platform.api.service.job;

import com.flow.platform.api.domain.job.Job;
import com.flow.platform.api.domain.node.Node;
import com.flow.platform.core.exception.HttpException;
import com.flow.platform.core.exception.IllegalStatusException;
import com.flow.platform.core.util.HttpUtil;
import com.flow.platform.domain.Cmd;
import com.flow.platform.domain.CmdInfo;
import com.flow.platform.domain.CmdType;
import com.flow.platform.domain.Jsonable;
import com.flow.platform.util.Logger;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Response for send cmd to control center
 *
 * @author yang
 */
@Service
public class CmdServiceImpl implements CmdService {

    private final static Logger LOGGER = new Logger(CmdService.class);

    @Value(value = "${platform.zone}")
    private String zone;

    @Value(value = "${domain}")
    private String domain;

    @Value(value = "${platform.queue.url}")
    private String queueUrl;

    @Value(value = "${platform.cmd.url}")
    private String cmdUrl;

    @Override
    public String createSession(Job job) {
        CmdInfo cmdInfo = new CmdInfo(zone, null, CmdType.CREATE_SESSION, null);
        cmdInfo.setWebhook(buildCmdWebhook(job));
        LOGGER.traceMarker("CreateSession", "job id - %s", job.getId());

        // create session
        Cmd cmd = sendToQueue(cmdInfo, 5);
        if (cmd == null) {
            throw new IllegalStatusException("Unable to create session since cmd return null");
        }

        return cmd.getId();
    }

    /**
     * delete sessionId
     */
    @Override
    public void deleteSession(Job job) {
        CmdInfo cmdInfo = new CmdInfo(zone, null, CmdType.DELETE_SESSION, null);
        cmdInfo.setSessionId(job.getSessionId());

        LOGGER.traceMarker("DeleteSession", "sessionId - %s", job.getSessionId());

        // delete session
        sendToQueue(cmdInfo, 5);
    }

    @Override
    public String runShell(Job job, Node node) {
        CmdInfo cmdInfo = new CmdInfo(zone, null, CmdType.RUN_SHELL, node.getScript());
        cmdInfo.setInputs(node.getEnvs());
        cmdInfo.setWebhook(buildCmdWebhook(job));
        cmdInfo.setOutputEnvFilter("FLOW_");
        cmdInfo.setSessionId(job.getSessionId());
        cmdInfo.setExtra(node.getPath()); // use cmd.extra to keep node path info

        try {
            LOGGER.traceMarker("RunShell", "step name - %s, node path - %s", node.getName(), node.getPath());
            String res = HttpUtil.post(cmdUrl, cmdInfo.toJson());

            if (res == null) {
                LOGGER.warn("Post cmd error, cmdUrl: %s, cmdInfo: %s", cmdUrl, cmdInfo.toJson());
                throw new HttpException(
                    String.format("Post Cmd Error, Node Name - %s, CmdInfo - %s", node.getName(), cmdInfo.toJson()));
            }

            Cmd cmd = Jsonable.parse(res, Cmd.class);
            return cmd.getId();
        } catch (Throwable ignore) {
            LOGGER.warnMarker("RunShell", "Unexpected exception", ignore);
            return null;
        }
    }

    /**
     * send cmd to control center cmd queue
     */
    private Cmd sendToQueue(CmdInfo cmdInfo, Integer retry) {
        final StringBuilder stringBuilder = new StringBuilder(queueUrl);
        stringBuilder.append("?priority=1&retry=").append(retry);

        try {
            String res = HttpUtil.post(stringBuilder.toString(), cmdInfo.toJson());

            if (res == null) {
                String message = String
                    .format("post session to queue error, cmdUrl: %s, cmdInfo: %s", stringBuilder, cmdInfo.toJson());

                LOGGER.warn(message);
                throw new HttpException(message);
            }

            return Jsonable.parse(res, Cmd.class);
        } catch (Throwable ignore) {
            LOGGER.warnMarker("SendToQueue", "Unexpected exception", ignore);
            return null;
        }
    }

    /**
     * Build cmd callback webhook url with job id as identifier
     */
    private String buildCmdWebhook(Job job) {
        return domain + "/hooks/cmd?identifier=" + HttpUtil.urlEncode(job.getId().toString());
    }
}
