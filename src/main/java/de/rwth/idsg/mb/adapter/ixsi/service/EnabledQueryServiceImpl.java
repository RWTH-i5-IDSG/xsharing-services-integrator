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

import de.rwth.idsg.mb.adapter.ixsi.BeanProducer;
import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.IxsiProcessingException;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import org.joda.time.DateTime;
import xjc.schema.ixsi.AuthType;
import xjc.schema.ixsi.BookingStateType;
import xjc.schema.ixsi.BookingTargetPropertiesType;
import xjc.schema.ixsi.GeoCircleType;
import xjc.schema.ixsi.GeoRectangleType;
import xjc.schema.ixsi.Language;
import xjc.schema.ixsi.OriginDestType;
import xjc.schema.ixsi.ProviderPlaceIDType;
import xjc.schema.ixsi.UserInfoType;
import xjc.schema.ixsi.UserType;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

/**
 * Wrapper around the actual business logic service to only trigger actions for enabled IXSI features
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 11.02.2016
 */
@Stateless
public class EnabledQueryServiceImpl implements EnabledQueryService {

    @Inject private BeanProducer beanProducer;

    private QueryService queryService;

    @PostConstruct
    private void init() {
        queryService = beanProducer.getQueryService();
    }

    // -------------------------------------------------------------------------
    // Static
    // -------------------------------------------------------------------------

    @Override
    public void getBookingTargetsInfo(PartnerContext pctx, List<String> providerIds, Language lan) {
        if (isEnabled(pctx, IxsiFeature.QS_BookingTargetsInfo)) {
            queryService.getBookingTargetsInfo(pctx, providerIds, lan);
        }
    }

    @Override
    public void getChangedProviders(PartnerContext pctx, DateTime timestamp, Language lan) {
        if (isEnabled(pctx, IxsiFeature.QS_ChangedProviders)) {
            queryService.getChangedProviders(pctx, timestamp, lan);
        }
    }

    // -------------------------------------------------------------------------
    // User
    // -------------------------------------------------------------------------

    @Override
    public UserInOutContext openSession(PartnerContext pctx, UserInfoType userInfo,
                                        Integer inactivityTimeoutInMinutes) {
        assertEnabled(pctx, IxsiFeature.QU_OpenSession);
        return queryService.openSession(pctx, userInfo, inactivityTimeoutInMinutes);
    }

    @Override
    public UserInOutContext closeSession(PartnerContext pctx, String sessionId) {
        assertEnabled(pctx, IxsiFeature.QU_CloseSession);
        return queryService.closeSession(pctx, sessionId);
    }

    @Override
    public UserInOutContext generateToken(PartnerContext pctx, UserInfoType userInfo) {
        assertEnabled(pctx, IxsiFeature.QU_TokenGeneration);
        return queryService.generateToken(pctx, userInfo);
    }

    @Override
    public UserInOutContext getAvailability(PartnerContext pctx, List<BookingTargetPropertiesType> targetList,
                                            AuthType auth, Language lan) {
        assertEnabled(pctx, IxsiFeature.QU_Availability);
        return queryService.getAvailability(pctx, targetList, auth, lan);
    }

    @Override
    public UserInOutContext getAvailability(PartnerContext pctx, GeoCircleType circle, AuthType auth,
                                            Language lan) {
        assertEnabled(pctx, IxsiFeature.QU_Availability);
        return queryService.getAvailability(pctx, circle, auth, lan);
    }

    @Override
    public UserInOutContext getAvailability(PartnerContext pctx, GeoRectangleType rectangle, AuthType auth,
                                            Language lan) {
        assertEnabled(pctx, IxsiFeature.QU_Availability);
        return queryService.getAvailability(pctx, rectangle, auth, lan);
    }

    @Override
    public UserInOutContext getPlaceAvailability(PartnerContext pctx, List<ProviderPlaceIDType> placeIDList,
                                                 AuthType auth, Language lan) {
        assertEnabled(pctx, IxsiFeature.QU_PlaceAvailability);
        return queryService.getPlaceAvailability(pctx, placeIDList, auth, lan);
    }

    @Override
    public UserInOutContext getPlaceAvailability(PartnerContext pctx, GeoCircleType circle, AuthType auth,
                                                 Language lan) {
        assertEnabled(pctx, IxsiFeature.QU_PlaceAvailability);
        return queryService.getPlaceAvailability(pctx, circle, auth, lan);
    }

    @Override
    public UserInOutContext getPlaceAvailability(PartnerContext pctx, GeoRectangleType rectangle,
                                                 AuthType auth, Language lan) {
        assertEnabled(pctx, IxsiFeature.QU_PlaceAvailability);
        return queryService.getPlaceAvailability(pctx, rectangle, auth, lan);
    }

    @Override
    public UserInOutContext getPriceInfo(PartnerContext pctx) {
        assertEnabled(pctx, IxsiFeature.QU_PriceInformation);
        return queryService.getPriceInfo(pctx);
    }

    @Override
    public UserInOutContext createBooking(PartnerContext pctx, String providerId, String bookingTargetId,
                                          OriginDestType origin, OriginDestType destination,
                                          DateTime begin, DateTime end, Integer maxWaitInMinutes,
                                          AuthType auth, Language lan) {
        assertEnabled(pctx, IxsiFeature.QU_Booking);
        return queryService.createBooking(pctx, providerId, bookingTargetId, origin, destination,
                begin, end, maxWaitInMinutes, auth, lan);
    }

    @Override
    public UserInOutContext changeBookingTime(PartnerContext pctx, String bookingID,
                                              DateTime begin, DateTime end, Integer maxWaitInMinutes,
                                              AuthType auth, Language lan) {
        assertEnabled(pctx, IxsiFeature.QU_ChangeBooking);
        return queryService.changeBookingTime(pctx, bookingID, begin, end, maxWaitInMinutes, auth, lan);
    }

    @Override
    public UserInOutContext cancelBooking(PartnerContext pctx, String bookingID, AuthType auth,
                                          Language lan) {
        assertEnabled(pctx, IxsiFeature.QU_ChangeBooking);
        return queryService.cancelBooking(pctx, bookingID, auth, lan);
    }

    @Override
    public UserInOutContext changeBookingState(PartnerContext pctx, String bookingID, BookingStateType state,
                                               AuthType auth, Language lan) {
        assertEnabled(pctx, IxsiFeature.QU_ChangeBookingState);
        return queryService.changeBookingState(pctx, bookingID, state, auth, lan);
    }

    @Override
    public UserInOutContext createUser(PartnerContext pctx, List<UserType> userList, AuthType auth,
                                       Language lan) {
        assertEnabled(pctx, IxsiFeature.QU_CreateUser);
        return queryService.createUser(pctx, userList, auth, lan);
    }

    @Override
    public UserInOutContext changeUser(PartnerContext pctx, List<UserType> userList, AuthType auth,
                                       Language lan) {
        assertEnabled(pctx, IxsiFeature.QU_ChangeUser);
        return queryService.changeUser(pctx, userList, auth, lan);
    }

    private IxsiProcessingException notEnabled() {
        return new IxsiProcessingException("This feature is not enabled for this partner/provider");
    }
}
