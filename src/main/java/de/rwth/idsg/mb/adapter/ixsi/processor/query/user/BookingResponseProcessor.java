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
import de.rwth.idsg.mb.adapter.ixsi.IxsiProcessingException;
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import de.rwth.idsg.mb.adapter.ixsi.intercept.ErrorLog;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.UserResponseProcessor;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingTargetRepository;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledSubscriptionService;
import de.rwth.idsg.mb.regioIT.resource.BookingResource;
import de.rwth.idsg.mb.utils.RegioITUtils;
import jooq.db.ixsi.enums.EventOrigin;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.BookingRequestType;
import xjc.schema.ixsi.BookingResponseType;
import xjc.schema.ixsi.BookingType;
import xjc.schema.ixsi.TimePeriodType;
import xjc.schema.ixsi.UserInfoType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.09.2014
 */
@Slf4j
@Stateless
public class BookingResponseProcessor
        implements UserResponseProcessor<BookingRequestType, BookingResponseType> {

    @Inject private EnabledSubscriptionService subscriptionService;
    @Inject private BookingResource bookingResource;
    @Inject private BookingRepository bookingRepository;
    @Inject private BookingTargetRepository bookingTargetRepository;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.QU_Booking;
    }

    @Override
    public Class<BookingResponseType> getProcessingClass() {
        return BookingResponseType.class;
    }

    @Override
    @ErrorLog
    public void process(UserInOutContext<BookingRequestType, BookingResponseType> context) {
        if (context.getIncoming().isSetError()) {
            bookingResource.asyncErrorResponse(context.getIncoming().getError(), context.getAsyncResponse());
        } else {
            proceedCreate(context);
        }
    }

    private void proceedCreate(UserInOutContext<BookingRequestType, BookingResponseType> context) {
        BookingRequestType request = context.getOutgoing();
        BookingResponseType response = context.getIncoming();
        BookingType bookingType = response.getBooking();

        if (bookingType == null) {
            bookingResource.asyncErrorResponse(
                    RegioITUtils.buildAdapterError("Received incorrect response from provider"),
                    context.getAsyncResponse());
            return;
        }

        ArrayList<String> bookings = new ArrayList<>(1);
        bookings.add(bookingType.getID());

        TimePeriodType timePeriod = bookingType.getTimePeriod();
        if (timePeriod == null) {
            timePeriod = request.getTimePeriodProposal();
        }

        bookingResource.asyncBookingResponse(response.getBooking(), context.getAsyncResponse());

        bookingRepository.create(
                EventOrigin.INTERNAL,
                context.getTransaction().getTimeStamp(),
                response.getBooking().getID(),
                request.getBookingTargetID(),
                getUserId(context.getUserInfo()),
                timePeriod,
                request.getOrigin(),
                request.getDest()
        );

        subscriptionService.bookingAlertSub(context.getPartnerContext(), bookings);
        subscriptionService.consumptionSub(context.getPartnerContext(), bookings);
    }

    private String getUserId(List<UserInfoType> userInfo) {
        int size = userInfo.size();

        if (size == 0) {
            throw new IxsiProcessingException("No user info set (this is unexpected)");
        } else if (size > 1) {
            log.warn("There are {} user info elements set (this is unexpected). Considering only the first...", size);
            return userInfo.get(0).getUserID();
        } else {
            return userInfo.get(0).getUserID();
        }
    }
}
