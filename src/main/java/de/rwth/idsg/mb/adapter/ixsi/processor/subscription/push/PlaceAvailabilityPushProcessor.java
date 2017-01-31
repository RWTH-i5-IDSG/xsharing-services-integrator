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
import de.rwth.idsg.mb.adapter.ixsi.processor.api.PushMessageProcessor;
import de.rwth.idsg.mb.adapter.ixsi.repository.PlaceRepository;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.PlaceAvailabilitySubscriptionStore;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.PlaceAvailabilityPushMessageType;
import xjc.schema.ixsi.PlaceAvailabilityType;
import xjc.schema.ixsi.ProviderPlaceIDType;

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
public class PlaceAvailabilityPushProcessor
        implements PushMessageProcessor<PlaceAvailabilityPushMessageType> {

    @EJB private PlaceAvailabilitySubscriptionStore subscriptionStore;
    @EJB private PlaceRepository placeRepository;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.S_PlaceAvailability;
    }

    @Override
    public Class<PlaceAvailabilityPushMessageType> getProcessingClass() {
        return PlaceAvailabilityPushMessageType.class;
    }

    @Override
    public void process(InContext<PlaceAvailabilityPushMessageType> context) {
        if (!context.getIncoming().isSetPlaceAvailability()) {
            return;
        }

        List<PlaceAvailabilityType> actual = getSubscribed(context);

        placeRepository.updatePlaceAvailabilityList(actual);
    }

    /**
     * Look at what we subscribed, and what arrived with the message. Return only the ones that we
     * subscribed to be processed further, discard the others.
     */
    private List<PlaceAvailabilityType> getSubscribed(InContext<PlaceAvailabilityPushMessageType> context) {
        int partnerId = context.getPartnerContext().getConfig().getPartnerId();
        Set<ProviderPlaceIDType> subscriptions = subscriptionStore.getSubscriptions(partnerId);

        List<PlaceAvailabilityType> arrived = context.getIncoming().getPlaceAvailability();
        List<PlaceAvailabilityType> actual = new ArrayList<>(arrived.size());

        for (PlaceAvailabilityType item : arrived) {
            if (subscriptions.contains(item.getID())) {
                actual.add(item);
            } else {
                log.error("We did not subscribe to {}", item);
            }
        }
        return actual;
    }
}
