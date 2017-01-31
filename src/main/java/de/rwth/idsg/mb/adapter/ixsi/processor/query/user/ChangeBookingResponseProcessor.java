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
package de.rwth.idsg.mb.adapter.ixsi.processor.query.user;


import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import de.rwth.idsg.mb.adapter.ixsi.intercept.ErrorLog;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.UserResponseProcessor;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingTargetRepository;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledSubscriptionService;
import de.rwth.idsg.mb.regioIT.resource.BookingResource;
import de.rwth.idsg.mb.utils.RegioITUtils;
import jooq.db.ixsi.enums.EventOrigin;
import xjc.schema.ixsi.BookingType;
import xjc.schema.ixsi.ChangeBookingRequestType;
import xjc.schema.ixsi.ChangeBookingResponseType;
import xjc.schema.ixsi.TimePeriodType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.isTrue;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.09.2014
 */
@Stateless
public class ChangeBookingResponseProcessor
        implements UserResponseProcessor<ChangeBookingRequestType, ChangeBookingResponseType> {

    @Inject private EnabledSubscriptionService subscriptionService;
    @Inject private BookingResource bookingResource;
    @Inject private BookingRepository bookingRepository;
    @Inject private BookingTargetRepository bookingTargetRepository;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.QU_ChangeBooking;
    }

    @Override
    public Class<ChangeBookingResponseType> getProcessingClass() {
        return ChangeBookingResponseType.class;
    }

    @Override
    @ErrorLog
    public void process(UserInOutContext<ChangeBookingRequestType, ChangeBookingResponseType> context) {
        if (context.getIncoming().isSetError()) {
            bookingResource.asyncErrorResponse(context.getIncoming().getError(), context.getAsyncResponse());
            return;
        }

        if (context.getOutgoing().isSetNewTimePeriodProposal()) {
            proceedChange(context);

        } else if (isTrue(context.getOutgoing().isCancel())) {
            proceedCancel(context);
        }
    }

    private void proceedChange(UserInOutContext<ChangeBookingRequestType, ChangeBookingResponseType> context) {
        ChangeBookingRequestType request = context.getOutgoing();
        ChangeBookingResponseType response = context.getIncoming();
        BookingType responseBooking = response.getBooking();

        if (responseBooking == null) {
            bookingResource.asyncErrorResponse(
                    RegioITUtils.buildAdapterError("Received incorrect response from provider"),
                    context.getAsyncResponse());
            return;
        }

        TimePeriodType newTimePeriod = responseBooking.getTimePeriod();
        if (newTimePeriod == null) {
            newTimePeriod = request.getNewTimePeriodProposal();
        }

        bookingResource.asyncBookingResponse(response.getBooking(), context.getAsyncResponse());

        bookingRepository.update(EventOrigin.INTERNAL, context.getTransaction().getTimeStamp(),
                                 request.getBookingID(), newTimePeriod);
    }

    private void proceedCancel(UserInOutContext<ChangeBookingRequestType, ChangeBookingResponseType> context) {
        ChangeBookingRequestType request = context.getOutgoing();
        ChangeBookingResponseType response = context.getIncoming();

        ArrayList<String> bookings = new ArrayList<>(1);
        BookingType responseBooking = response.getBooking();
        if (responseBooking == null) {
            bookings.add(request.getBookingID());
        } else {
            bookings.add(responseBooking.getID());
        }

        bookingResource.asyncOkResponse(context.getAsyncResponse());

        // Only one element in the list anyway
        bookingRepository.cancel(EventOrigin.INTERNAL, context.getTransaction().getTimeStamp(), bookings.get(0));

        subscriptionService.bookingAlertUnSub(context.getPartnerContext(), bookings);
        subscriptionService.consumptionUnSub(context.getPartnerContext(), bookings);
    }

}
