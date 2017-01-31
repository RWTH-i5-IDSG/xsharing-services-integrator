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
package de.rwth.idsg.mb.adapter.ixsi.processor.subscription;

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.InOutContext;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.SubscriptionAdminProcessor;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.HeartBeatRequestType;
import xjc.schema.ixsi.HeartBeatResponseType;

import javax.ejb.Stateless;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.11.2014
 */
@Slf4j
@Stateless
public class HeartBeatResponseProcessor
        implements SubscriptionAdminProcessor<HeartBeatRequestType,
                                              HeartBeatResponseType> {

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.SA_HeartBeat;
    }

    @Override
    public Class<HeartBeatResponseType> getProcessingClass() {
        return HeartBeatResponseType.class;
    }

    public void process(InOutContext<HeartBeatRequestType, HeartBeatResponseType> context) {
        log.debug("Got heartbeat response from partner '{}'", context.getPartnerContext().getConfig().getPartnerId());
    }
}
