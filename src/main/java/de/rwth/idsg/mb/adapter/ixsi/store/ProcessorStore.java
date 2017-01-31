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
package de.rwth.idsg.mb.adapter.ixsi.store;

import de.rwth.idsg.ixsi.jaxb.PushMessageGroup;
import de.rwth.idsg.ixsi.jaxb.ResponseMessageGroup;
import de.rwth.idsg.ixsi.jaxb.StaticDataResponseGroup;
import de.rwth.idsg.ixsi.jaxb.SubscriptionResponseGroup;
import de.rwth.idsg.ixsi.jaxb.UserTriggeredResponseChoice;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.PushMessageProcessor;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.StaticResponseProcessor;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.SubscriptionResponseMessageProcessor;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.SubscriptionResponseProcessor;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.UserResponseProcessor;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.11.2014
 */
public interface ProcessorStore {
    StaticResponseProcessor find(StaticDataResponseGroup s);
    UserResponseProcessor find(UserTriggeredResponseChoice s);
    PushMessageProcessor find(PushMessageGroup s);
    SubscriptionResponseProcessor find(SubscriptionResponseGroup s);
    SubscriptionResponseMessageProcessor find(ResponseMessageGroup s);
}
