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
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingTargetRepository;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.AvailabilitySubscriptionStore;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.AvailabilityPushMessageType;
import xjc.schema.ixsi.BookingTargetChangeAvailabilityType;
import xjc.schema.ixsi.BookingTargetIDType;

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
public class AvailabilityPushProcessor
        implements PushMessageProcessor<AvailabilityPushMessageType> {

    @EJB private AvailabilitySubscriptionStore subscriptionStore;
    @EJB private BookingTargetRepository bookingTargetRepository;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.S_Availability;
    }

    @Override
    public Class<AvailabilityPushMessageType> getProcessingClass() {
        return AvailabilityPushMessageType.class;
    }

    @Override
    public void process(InContext<AvailabilityPushMessageType> context) {
        if (!context.getIncoming().isSetAvailabilityChange()) {
            return;
        }

        List<BookingTargetChangeAvailabilityType> actual = getSubscribed(context);

        // -> update db
        bookingTargetRepository.updateBookingTargetChangeAvailabilityList(actual);
    }

    /**
     * Look at what we subscribed, and what arrived with the message. Return only the ones that we
     * subscribed to be processed further, discard the others.
     */
    private List<BookingTargetChangeAvailabilityType> getSubscribed(InContext<AvailabilityPushMessageType> context) {
        int partnerId = context.getPartnerContext().getConfig().getPartnerId();
        Set<BookingTargetIDType> subscriptions = subscriptionStore.getSubscriptions(partnerId);

        List<BookingTargetChangeAvailabilityType> arrived = context.getIncoming().getAvailabilityChange();
        List<BookingTargetChangeAvailabilityType> actual = new ArrayList<>(arrived.size());

        for (BookingTargetChangeAvailabilityType item : arrived) {
            if (subscriptions.contains(item.getID())) {
                actual.add(item);
            } else {
                log.error("We did not subscribe to {}", item);
            }
        }
        return actual;
    }
}
