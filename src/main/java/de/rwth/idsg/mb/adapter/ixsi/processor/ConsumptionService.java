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
package de.rwth.idsg.mb.adapter.ixsi.processor;

import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledSubscriptionService;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.ConsumptionSubscriptionStore;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.ConsumptionType;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.isTrue;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.11.2015
 */
@Slf4j
@Stateless
public class ConsumptionService {

    @EJB private ConsumptionSubscriptionStore subscriptionStore;
    @Inject private EnabledSubscriptionService subscriptionService;

    public void unSubscribeFinalized(PartnerContext pctx, List<ConsumptionType> actual) {
        List<String> finalized = getFinalizedBookings(actual);
        subscriptionService.bookingAlertUnSub(pctx, finalized);
        subscriptionService.consumptionUnSub(pctx, finalized);
    }

    private List<String> getFinalizedBookings(List<ConsumptionType> changes) {
        List<String> finalized = new ArrayList<>(changes.size());
        for (ConsumptionType bt : changes) {
            if (isTrue(bt.isFinal())) {
                finalized.add(bt.getBookingID());
            }
        }
        return finalized;
    }



}
