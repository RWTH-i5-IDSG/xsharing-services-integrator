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
package de.rwth.idsg.mb.adapter.ixsi.processor.subscription.push;

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.InContext;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.PushMessageProcessor;
import de.rwth.idsg.mb.adapter.ixsi.processor.ConsumptionService;
import de.rwth.idsg.mb.adapter.ixsi.repository.ConsumptionRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.dto.ConsumptionDTO;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.ConsumptionSubscriptionStore;
import de.rwth.idsg.mb.regioIT.service.RegioITPushService;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.ConsumptionPushMessageType;
import xjc.schema.ixsi.ConsumptionType;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.11.2014
 */
@Slf4j
@Stateless
public class ConsumptionPushProcessor
        implements PushMessageProcessor<ConsumptionPushMessageType> {

    @EJB private ConsumptionSubscriptionStore subscriptionStore;
    @EJB private ConsumptionRepository consumptionRepository;
    @EJB private RegioITPushService regioITPushService;
    @EJB private ConsumptionService consumptionService;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.S_Consumption;
    }

    @Override
    public Class<ConsumptionPushMessageType> getProcessingClass() {
        return ConsumptionPushMessageType.class;
    }

    @Override
    public void process(InContext<ConsumptionPushMessageType> context) {
        if (!context.getIncoming().isSetConsumption()) {
            return;
        }

        List<ConsumptionType> actual = getSubscribed(context);

        PartnerContext pctx = context.getPartnerContext();

        consumptionService.unSubscribeFinalized(pctx, actual);

        List<ConsumptionDTO> list = consumptionRepository.insertConsumptionList(pctx.getConfig().getPartnerId(), actual);

        if (!list.isEmpty()) {
            regioITPushService.pushConsumptionData(pctx.getConfig().getPartnerId(), list);
        }
    }

    /**
     * Look at what we subscribed, and what arrived with the message. Return only the ones that we
     * subscribed to be processed further, discard the others.
     */
    private List<ConsumptionType> getSubscribed(InContext<ConsumptionPushMessageType> context) {
        int partnerId = context.getPartnerContext().getConfig().getPartnerId();
        Set<String> subscriptions = subscriptionStore.getSubscriptions(partnerId);

        List<ConsumptionType> arrived = context.getIncoming().getConsumption();
        List<ConsumptionType> actual = new ArrayList<>(arrived.size());

        for (ConsumptionType item : arrived) {
            if (subscriptions.contains(item.getBookingID())) {
                actual.add(item);
            } else {
                log.error("We did not subscribe to {}", item);
            }
        }
        return actual;
    }
}
