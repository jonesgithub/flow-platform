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

package com.flow.platform.api.domain.envs;

/**
 * @author yang
 */
public enum FlowEnvs implements EnvKey {

    /**
     * Indicate flow is ready to run
     */
    FLOW_STATUS,

    /**
     * Indicate flow yml loading, ready
     */
    FLOW_YML_STATUS;

    public enum Value implements EnvValue {

        FLOW_STATUS_READY("READY"),

        FLOW_STATUS_PENDING("PENDING"),


        FLOW_YML_STATUS_LOADING("LOADING"),

        FLOW_YML_STATUS_NOT_FOUND("NOT_FOUND"),

        FLOW_YML_STATUS_FOUND("FOUND"),

        FLOW_YML_STATUS_ERROR("ERROR");

        private String value;

        Value(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}