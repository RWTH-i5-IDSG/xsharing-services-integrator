/*
 * Copyright (C) 2014-2017 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group.
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.rwth.idsg.mb.adapter.ixsi.processor.api;

import de.rwth.idsg.ixsi.jaxb.UserTriggeredRequestChoice;
import de.rwth.idsg.ixsi.jaxb.UserTriggeredResponseChoice;
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.10.2014
 */
public interface UserResponseProcessor<T1 extends UserTriggeredRequestChoice,
                                       T2 extends UserTriggeredResponseChoice> extends ClassAwareProcessor<T2> {

    void process(UserInOutContext<T1, T2> context);

    default void processIfEnabled(UserInOutContext<T1, T2> context) {
        if (isEnabled(context.getPartnerContext())) {
            process(context);
        }
    }
}
