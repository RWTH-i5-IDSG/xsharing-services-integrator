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
package de.rwth.idsg.mb.adapter.ixsi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xjc.schema.ixsi.AvailabilityRequestType;
import xjc.schema.ixsi.AvailabilitySubscriptionRequestType;
import xjc.schema.ixsi.BookingAlertSubscriptionRequestType;
import xjc.schema.ixsi.BookingRequestType;
import xjc.schema.ixsi.BookingTargetsInfoRequestType;
import xjc.schema.ixsi.ChangeBookingRequestType;
import xjc.schema.ixsi.ChangeBookingStateRequestType;
import xjc.schema.ixsi.ChangeUserRequestType;
import xjc.schema.ixsi.ChangedProvidersRequestType;
import xjc.schema.ixsi.CloseSessionRequestType;
import xjc.schema.ixsi.ConsumptionSubscriptionRequestType;
import xjc.schema.ixsi.CreateUserRequestType;
import xjc.schema.ixsi.ExternalBookingSubscriptionRequestType;
import xjc.schema.ixsi.HeartBeatRequestType;
import xjc.schema.ixsi.OpenSessionRequestType;
import xjc.schema.ixsi.PlaceAvailabilityRequestType;
import xjc.schema.ixsi.PlaceAvailabilitySubscriptionRequestType;
import xjc.schema.ixsi.PriceInformationRequestType;
import xjc.schema.ixsi.TokenGenerationRequestType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;

import static de.rwth.idsg.mb.adapter.ixsi.IxsiFeatureGroup.QueryStatic;
import static de.rwth.idsg.mb.adapter.ixsi.IxsiFeatureGroup.QueryUser;
import static de.rwth.idsg.mb.adapter.ixsi.IxsiFeatureGroup.Subscription;
import static de.rwth.idsg.mb.adapter.ixsi.IxsiFeatureGroup.SubscriptionAdmin;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 07.01.2016
 */
@Getter
@RequiredArgsConstructor
public enum IxsiFeature {

    QS_BookingTargetsInfo(QueryStatic, get(BookingTargetsInfoRequestType.class)),
    QS_ChangedProviders(QueryStatic, get(ChangedProvidersRequestType.class)),

    QU_OpenSession(QueryUser, get(OpenSessionRequestType.class)),
    QU_CloseSession(QueryUser, get(CloseSessionRequestType.class)),
    QU_TokenGeneration(QueryUser, get(TokenGenerationRequestType.class)),
    QU_Availability(QueryUser, get(AvailabilityRequestType.class)),
    QU_PlaceAvailability(QueryUser, get(PlaceAvailabilityRequestType.class)),
    QU_PriceInformation(QueryUser, get(PriceInformationRequestType.class)),
    QU_Booking(QueryUser, get(BookingRequestType.class)),
    QU_ChangeBooking(QueryUser, get(ChangeBookingRequestType.class)),
    QU_ChangeBookingState(QueryUser, get(ChangeBookingStateRequestType.class)),
    QU_CreateUser(QueryUser, get(CreateUserRequestType.class)),
    QU_ChangeUser(QueryUser, get(ChangeUserRequestType.class)),

    SA_HeartBeat(SubscriptionAdmin, get(HeartBeatRequestType.class)),

    S_Availability(Subscription, get(AvailabilitySubscriptionRequestType.class)),
    S_PlaceAvailability(Subscription, get(PlaceAvailabilitySubscriptionRequestType.class)),
    S_BookingAlert(Subscription, get(BookingAlertSubscriptionRequestType.class)),
    S_Consumption(Subscription, get(ConsumptionSubscriptionRequestType.class)),
    S_ExternalBooking(Subscription, get(ExternalBookingSubscriptionRequestType.class));

    private final IxsiFeatureGroup group;
    private final String name;

    public static final EnumMap<IxsiFeatureGroup, EnumSet<IxsiFeature>> FEATURES_BY_GROUP;
    public static final EnumSet<IxsiFeature> QUERY_STATIC_VALUES;
    public static final EnumSet<IxsiFeature> QUERY_USER_VALUES;
    public static final EnumSet<IxsiFeature> SUBSCRIPTION_ADMIN_VALUES;
    public static final EnumSet<IxsiFeature> SUBSCRIPTION_VALUES;

    static {
        FEATURES_BY_GROUP = Arrays.stream(IxsiFeature.values()).collect(groupingBy(
                IxsiFeature::getGroup,
                () -> new EnumMap<>(IxsiFeatureGroup.class),
                toCollection(() -> EnumSet.noneOf(IxsiFeature.class))
        ));
        QUERY_STATIC_VALUES = FEATURES_BY_GROUP.get(QueryStatic);
        QUERY_USER_VALUES = FEATURES_BY_GROUP.get(QueryUser);
        SUBSCRIPTION_ADMIN_VALUES = FEATURES_BY_GROUP.get(SubscriptionAdmin);
        SUBSCRIPTION_VALUES = FEATURES_BY_GROUP.get(Subscription);
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    public static IxsiFeature fromValues(String group, String name) {
        final IxsiFeatureGroup featureGroup = IxsiFeatureGroup.fromName(group);
        final EnumSet<IxsiFeature> ixsiFeatures = FEATURES_BY_GROUP.get(featureGroup);
        return ixsiFeatures.stream()
                           .filter(f -> f.getName()
                                         .equalsIgnoreCase(name))
                           .findAny()
                           .orElseThrow(() -> new IllegalArgumentException(group + "," + name));
    }

    private static final String REQUEST_TYPE = "RequestType";
    private static final String SUBSCRIPTION = "Subscription";

    private static String get(Class<?> clazz) {
        String s = clazz.getSimpleName();

        if (s.endsWith(REQUEST_TYPE)) {
            s = s.substring(0, s.length() - REQUEST_TYPE.length());
        }

        if (s.endsWith(SUBSCRIPTION)) {
            s = s.substring(0, s.length() - SUBSCRIPTION.length());
        }

        return s;
    }
}
