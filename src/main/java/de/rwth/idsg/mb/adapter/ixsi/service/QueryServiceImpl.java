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

import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Period;
import xjc.schema.ixsi.AuthType;
import xjc.schema.ixsi.AvailabilityRequestType;
import xjc.schema.ixsi.BookingRequestType;
import xjc.schema.ixsi.BookingStateType;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.BookingTargetPropertiesType;
import xjc.schema.ixsi.BookingTargetsInfoRequestType;
import xjc.schema.ixsi.ChangeBookingRequestType;
import xjc.schema.ixsi.ChangeBookingStateRequestType;
import xjc.schema.ixsi.ChangeUserRequestType;
import xjc.schema.ixsi.ChangedProvidersRequestType;
import xjc.schema.ixsi.CloseSessionRequestType;
import xjc.schema.ixsi.CreateUserRequestType;
import xjc.schema.ixsi.GeoCircleType;
import xjc.schema.ixsi.GeoRectangleType;
import xjc.schema.ixsi.Language;
import xjc.schema.ixsi.OpenSessionRequestType;
import xjc.schema.ixsi.OriginDestType;
import xjc.schema.ixsi.PlaceAvailabilityRequestType;
import xjc.schema.ixsi.ProviderPlaceIDType;
import xjc.schema.ixsi.TimePeriodProposalType;
import xjc.schema.ixsi.TokenGenerationRequestType;
import xjc.schema.ixsi.UserInfoType;
import xjc.schema.ixsi.UserType;

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.11.2014
 */
@Slf4j
@RequiredArgsConstructor
public class QueryServiceImpl implements QueryService {

    private final IxsiService ixsiService;

    // -------------------------------------------------------------------------
    // Static
    // -------------------------------------------------------------------------

    @Override
    public void getBookingTargetsInfo(PartnerContext pctx, List<String> providerIds, Language lan) {
        BookingTargetsInfoRequestType request = new BookingTargetsInfoRequestType().withProviderFilter(providerIds);
        ixsiService.sendStaticRequest(pctx, request, lan);
    }

    @Override
    public void getChangedProviders(PartnerContext pctx, DateTime timestamp, Language lan) {
        ChangedProvidersRequestType request = new ChangedProvidersRequestType().withTimestamp(timestamp);
        ixsiService.sendStaticRequest(pctx, request, lan);
    }

    // -------------------------------------------------------------------------
    // User
    // -------------------------------------------------------------------------

    @Override
    public UserInOutContext openSession(PartnerContext pctx, UserInfoType userInfo,
                                        Integer inactivityTimeoutInMinutes) {
        AuthType auth = new AuthType().withUserInfo(userInfo);
        if (inactivityTimeoutInMinutes != null) {
            Period timeout = Period.minutes(inactivityTimeoutInMinutes);
            auth.setSessionTimeout(timeout);
        }
        return ixsiService.sendUserRequest(pctx, new OpenSessionRequestType(), auth, null);
    }

    @Override
    public UserInOutContext closeSession(PartnerContext pctx, String sessionId) {
        AuthType auth = new AuthType().withSessionID(sessionId);
        return ixsiService.sendUserRequest(pctx, new CloseSessionRequestType(), auth, null);
    }

    @Override
    public UserInOutContext generateToken(PartnerContext pctx, UserInfoType userInfo) {
        AuthType auth = new AuthType().withUserInfo(userInfo);
        return ixsiService.sendUserRequest(pctx, new TokenGenerationRequestType(), auth, null);
    }

    @Override
    public UserInOutContext getAvailability(PartnerContext pctx, List<BookingTargetPropertiesType> targetList,
                                            AuthType auth, Language lan) {
        AvailabilityRequestType request = new AvailabilityRequestType().withBookingTarget(targetList);
        return ixsiService.sendUserRequest(pctx, request, auth, lan);
    }

    @Override
    public UserInOutContext getAvailability(PartnerContext pctx, GeoCircleType circle, AuthType auth, Language lan) {
        AvailabilityRequestType request = new AvailabilityRequestType().withCircle(circle);
        return ixsiService.sendUserRequest(pctx, request, auth, lan);
    }

    @Override
    public UserInOutContext getAvailability(PartnerContext pctx, GeoRectangleType rectangle, AuthType auth,
                                            Language lan) {
        AvailabilityRequestType request = new AvailabilityRequestType().withGeoRectangle(rectangle);
        return ixsiService.sendUserRequest(pctx, request, auth, lan);
    }

    @Override
    public UserInOutContext getPlaceAvailability(PartnerContext pctx, List<ProviderPlaceIDType> placeIDList,
                                                 AuthType auth, Language lan) {
        PlaceAvailabilityRequestType request = new PlaceAvailabilityRequestType().withPlaceID(placeIDList);
        return ixsiService.sendUserRequest(pctx, request, auth, lan);
    }

    @Override
    public UserInOutContext getPlaceAvailability(PartnerContext pctx, GeoCircleType circle, AuthType auth,
                                                 Language lan) {
        PlaceAvailabilityRequestType request = new PlaceAvailabilityRequestType().withCircle(circle);
        return ixsiService.sendUserRequest(pctx, request, auth, lan);
    }

    @Override
    public UserInOutContext getPlaceAvailability(PartnerContext pctx, GeoRectangleType rectangle,
                                                 AuthType auth, Language lan) {
        PlaceAvailabilityRequestType request = new PlaceAvailabilityRequestType().withGeoRectangle(rectangle);
        return ixsiService.sendUserRequest(pctx, request, auth, lan);
    }

    @Override
    public UserInOutContext getPriceInfo(PartnerContext pctx) {
        log.warn("Will not send the message (This feature is not supported)");
        return null;
    }

    @Override
    public UserInOutContext createBooking(PartnerContext pctx, String providerId, String bookingTargetId,
                                          OriginDestType origin, OriginDestType destination,
                                          DateTime begin, DateTime end, Integer maxWaitInMinutes,
                                          AuthType auth, Language lan) {

        TimePeriodProposalType tp = buildTimePeriod(begin, end, maxWaitInMinutes);

        BookingTargetIDType bookingTargetIDType = new BookingTargetIDType()
                .withBookeeID(bookingTargetId)
                .withProviderID(providerId);

        BookingRequestType request = new BookingRequestType()
                .withBookingTargetID(bookingTargetIDType)
                .withOrigin(origin)
                .withDest(destination)
                .withTimePeriodProposal(tp);

        return ixsiService.sendUserRequest(pctx, request, auth, lan);
    }

    @Override
    public UserInOutContext changeBookingTime(PartnerContext pctx, String bookingID,
                                              DateTime begin, DateTime end, Integer maxWaitInMinutes,
                                              AuthType auth, Language lan) {

        TimePeriodProposalType tp = buildTimePeriod(begin, end, maxWaitInMinutes);

        ChangeBookingRequestType request = new ChangeBookingRequestType()
                .withBookingID(bookingID)
                .withNewTimePeriodProposal(tp);

        return ixsiService.sendUserRequest(pctx, request, auth, lan);
    }

    @Override
    public UserInOutContext cancelBooking(PartnerContext pctx, String bookingID, AuthType auth, Language lan) {
        ChangeBookingRequestType request = new ChangeBookingRequestType()
                .withBookingID(bookingID)
                .withCancel(true);

        return ixsiService.sendUserRequest(pctx, request, auth, lan);
    }

    @Override
    public UserInOutContext changeBookingState(PartnerContext pctx, String bookingID, BookingStateType state,
                                               AuthType auth, Language lan) {
        ChangeBookingStateRequestType request = new ChangeBookingStateRequestType()
                .withBookingID(bookingID)
                .withBookingState(state);

        return ixsiService.sendUserRequest(pctx, request, auth, lan);
    }

    @Override
    public UserInOutContext createUser(PartnerContext pctx, List<UserType> userList, AuthType auth, Language lan) {
        CreateUserRequestType request = new CreateUserRequestType().withUser(userList);
        return ixsiService.sendUserRequest(pctx, request, auth, lan);
    }

    @Override
    public UserInOutContext changeUser(PartnerContext pctx, List<UserType> userList, AuthType auth, Language lan) {
        ChangeUserRequestType request = new ChangeUserRequestType().withUser(userList);
        return ixsiService.sendUserRequest(pctx, request, auth, lan);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private TimePeriodProposalType buildTimePeriod(DateTime begin, DateTime end, Integer maxWaitInMinutes) {
        TimePeriodProposalType proposal = new TimePeriodProposalType()
                .withBegin(begin)
                .withEnd(end);

        if (maxWaitInMinutes != null) {
            Period duration = Period.minutes(maxWaitInMinutes);
            proposal.setMaxWait(duration);
        }

        return proposal;
    }
}
