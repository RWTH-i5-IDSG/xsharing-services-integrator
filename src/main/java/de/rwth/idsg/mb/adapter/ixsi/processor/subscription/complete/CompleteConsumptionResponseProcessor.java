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
package de.rwth.idsg.mb.adapter.ixsi.processor.subscription.complete;

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.InOutContext;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.intercept.ErrorLog;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.SubscriptionResponseMessageProcessor;
import de.rwth.idsg.mb.adapter.ixsi.processor.ConsumptionService;
import de.rwth.idsg.mb.adapter.ixsi.repository.ConsumptionRepository;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.CompleteConsumptionRequestType;
import xjc.schema.ixsi.CompleteConsumptionResponseType;
import xjc.schema.ixsi.ConsumptionType;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 18.11.2014
 */
@Slf4j
@Stateless
public class CompleteConsumptionResponseProcessor
        implements SubscriptionResponseMessageProcessor<CompleteConsumptionRequestType,
                                                        CompleteConsumptionResponseType> {

    @EJB private ConsumptionRepository consumptionRepository;
    @EJB private ConsumptionService consumptionService;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.S_Consumption;
    }

    @Override
    public Class<CompleteConsumptionResponseType> getProcessingClass() {
        return CompleteConsumptionResponseType.class;
    }

    @Override
    @ErrorLog
    public void process(InOutContext<CompleteConsumptionRequestType, CompleteConsumptionResponseType> context) {
        updateState(context.getPartnerContext());
        CompleteConsumptionResponseType response = context.getIncoming();

        log.debug("CompleteConsumption message arrived. messageBlockID={}, isLastPart={}",
                response.getMessageBlockID(), response.isSetLast());

        if (response.isSetConsumption()) {
            int partnerId = context.getPartnerContext().getConfig().getPartnerId();
            List<ConsumptionType> consumptions = computeConsumptionsToInsert(partnerId, response);
            consumptionRepository.insertConsumptionList(partnerId, consumptions);
            consumptionService.unSubscribeFinalized(context.getPartnerContext(), response.getConsumption());
        } else {
            log.debug("No consumption set.");
        }
    }

    /**
     * Notify that we finished SETUP for this subscription
     */
    private void updateState(PartnerContext ctx) {
        ctx.finished(getRelatedFeature());
    }

    /**
     * In order to prevent multiple, redundant consumption entries in db, we filter them. We compare the consumptions
     * that we have in db that are marked as 'final' with the newly arrived list. We DROP a newly arrived consumption
     * record if it was 'final' in db, and only insert the others.
     */
    private List<ConsumptionType> computeConsumptionsToInsert(int partnerId, CompleteConsumptionResponseType response) {
        List<String> dbList = consumptionRepository.getFinalized(partnerId);

        Set<String> responseList = response.getConsumption()
                                           .stream()
                                           .map(ConsumptionType::getBookingID)
                                           .collect(Collectors.toSet());

        // Remove all 'final' consumptions from this set!
        //
        responseList.removeAll(dbList);

        log.info("Booking ids of consumptions to be inserted (not final) into db: {}", responseList);

        return response.getConsumption()
                       .stream()
                       .filter(consumption -> responseList.contains(consumption.getBookingID()))
                       .collect(Collectors.toList());
    }
}
