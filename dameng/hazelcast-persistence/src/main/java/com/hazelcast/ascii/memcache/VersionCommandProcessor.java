/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.ascii.memcache;

import com.hazelcast.ascii.AbstractTextCommandProcessor;
import com.hazelcast.ascii.TextCommandProcessor;
import com.hazelcast.ascii.TextCommandServiceImpl;

/**
 * User: sancar
 * Date: 3/7/13
 * Time: 10:31 AM
 */
public class VersionCommandProcessor extends MemcacheCommandProcessor<VersionCommand> {
    public VersionCommandProcessor(TextCommandServiceImpl textCommandService) {
        super(textCommandService);
    }

    public void handle(VersionCommand request) {
        textCommandService.sendResponse(request);
    }

    public void handleRejection(VersionCommand request) {
        handle(request);
    }
}
