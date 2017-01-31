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

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.11.2014
 */
public interface QueryService {

    // -------------------------------------------------------------------------
    // Static
    // -------------------------------------------------------------------------

    void getBookingTargetsInfo(PartnerContext pctx, List<String> providerIds, Language lan);

    void getChangedProviders(PartnerContext pctx, DateTime timestamp, Language lan);

    // -------------------------------------------------------------------------
    // User
    // -------------------------------------------------------------------------

    UserInOutContext openSession(PartnerContext pctx, UserInfoType userInfo, Integer inactivityTimeout);

    UserInOutContext closeSession(PartnerContext pctx, String sessionId);

    UserInOutContext generateToken(PartnerContext pctx, UserInfoType userInfo);

    UserInOutContext getAvailability(PartnerContext pctx, List<BookingTargetPropertiesType> targetList,
                                     AuthType auth, Language lan);

    UserInOutContext getAvailability(PartnerContext pctx, GeoCircleType circle, AuthType auth, Language lan);

    UserInOutContext getAvailability(PartnerContext pctx, GeoRectangleType rectangle, AuthType auth, Language lan);

    UserInOutContext getPlaceAvailability(PartnerContext pctx, List<ProviderPlaceIDType> placeIDList,
                                          AuthType auth, Language lan);

    UserInOutContext getPlaceAvailability(PartnerContext pctx, GeoCircleType circle, AuthType auth, Language lan);

    UserInOutContext getPlaceAvailability(PartnerContext pctx, GeoRectangleType rectangle, AuthType auth, Language lan);

    UserInOutContext getPriceInfo(PartnerContext pctx);

    UserInOutContext createBooking(PartnerContext pctx, String providerId, String bookingTargetId,
                                   OriginDestType origin, OriginDestType destination,
                                   DateTime begin, DateTime end, Integer maxWaitInMinutes,
                                   AuthType auth, Language lan);

    UserInOutContext changeBookingTime(PartnerContext pctx, String bookingID,
                                       DateTime begin, DateTime end, Integer maxWait,
                                       AuthType auth, Language lan);

    UserInOutContext cancelBooking(PartnerContext pctx, String bookingID, AuthType auth, Language lan);

    UserInOutContext changeBookingState(PartnerContext pctx, String bookingID, BookingStateType state,
                                        AuthType auth, Language lan);

    UserInOutContext createUser(PartnerContext pctx, List<UserType> userList, AuthType auth, Language lan);

    UserInOutContext changeUser(PartnerContext pctx, List<UserType> userList, AuthType auth, Language lan);
}
