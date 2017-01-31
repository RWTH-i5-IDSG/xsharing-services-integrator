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
package de.rwth.idsg.mb.adapter.ixsi.service;

import de.rwth.idsg.ixsi.jaxb.RequestMessageGroup;
import de.rwth.idsg.ixsi.jaxb.StaticDataRequestGroup;
import de.rwth.idsg.ixsi.jaxb.SubscriptionRequestGroup;
import de.rwth.idsg.ixsi.jaxb.UserTriggeredRequestChoice;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import xjc.schema.ixsi.AuthType;
import xjc.schema.ixsi.Language;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.11.2014
 */
public interface IxsiService {

    void sendStaticRequest(PartnerContext pctx, StaticDataRequestGroup staticRequest, Language lan);

    UserInOutContext sendUserRequest(PartnerContext pctx, UserTriggeredRequestChoice userRequest,
                                     AuthType auth, Language lan);

    void getHeartbeat(PartnerContext pctx);

    void sendSubscriptionRequest(PartnerContext pctx, SubscriptionRequestGroup subscriptionRequest);

    void sendSubscriptionCompleteRequest(PartnerContext pctx, RequestMessageGroup request);
}
