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
import de.rwth.idsg.mb.adapter.ixsi.repository.PlaceRepository;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.CompletePlaceAvailabilityRequestType;
import xjc.schema.ixsi.CompletePlaceAvailabilityResponseType;

import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.09.2014
 */
@Slf4j
@Stateless
public class CompletePlaceAvailabilityResponseProcessor
        implements SubscriptionResponseMessageProcessor<CompletePlaceAvailabilityRequestType,
                                                        CompletePlaceAvailabilityResponseType> {

    @EJB private PlaceRepository placeRepository;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.S_PlaceAvailability;
    }

    @Override
    public Class<CompletePlaceAvailabilityResponseType> getProcessingClass() {
        return CompletePlaceAvailabilityResponseType.class;
    }

    @Override
    @ErrorLog
    public void process(InOutContext<CompletePlaceAvailabilityRequestType,
                                     CompletePlaceAvailabilityResponseType> context) {

        updateState(context.getPartnerContext());
        CompletePlaceAvailabilityResponseType response = context.getIncoming();

        log.debug("CompletePlaceAvailablitityResponse message arrived. messageBlockID={}, isLastPart={}",
                response.getMessageBlockID(), response.isSetLast());

        if (response.isSetPlaceAvailability()) {
            placeRepository.updatePlaceAvailabilityList(response.getPlaceAvailability());
        } else {
            log.debug("No place availability set.");
        }
    }

    /**
     * Notify that we finished SETUP for this subscription
     */
    private void updateState(PartnerContext ctx) {
        ctx.finished(getRelatedFeature());
    }
}
