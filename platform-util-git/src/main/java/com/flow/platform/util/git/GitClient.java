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

package com.flow.platform.util.git;

import com.flow.platform.util.git.model.GitCommit;
import java.util.Collection;
import org.eclipse.jgit.lib.Ref;

/**
 * @author yang
 */
public interface GitClient {

    /**
     * Git pull with depth
     * @param depth git pull depth, can be set to null
     */
    void pull(Integer depth);

    /**
     * Load all branches from git
     */
    Collection<Ref> branches();

    /**
     * Load all tags from git
     */
    Collection<Ref> tags();

    /**
     * Git latest commit from ref
     */
    GitCommit commit(String refName);
}