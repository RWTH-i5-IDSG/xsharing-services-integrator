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
package de.rwth.idsg.mb.regioIT.service;

import com.google.common.base.Joiner;
import de.rwth.idsg.mb.AppConfiguration;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingTargetRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.ConsumptionRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.dto.ConsumptionDTO;
import de.rwth.idsg.mb.controller.ProviderLookupTable;
import de.rwth.idsg.mb.controller.repository.IxsiProviderRepository;
import de.rwth.idsg.mb.regioIT.client.rest.api.BookingClient;
import de.rwth.idsg.mb.regioIT.client.rest.api.ConsumptionClient;
import de.rwth.idsg.mb.regioIT.client.rest.params.BookingAlertParams;
import de.rwth.idsg.mb.regioIT.client.rest.params.ConsumptionParams;
import de.rwth.idsg.mb.regioIT.client.rest.params.ExternalBookingParams;
import de.rwth.idsg.mb.utils.BasicUtils;
import jooq.db.ixsi.tables.records.ConsumptionRecord;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.AttributeClassType;
import xjc.schema.ixsi.BookingChangeType;
import xjc.schema.ixsi.ExternalBookingType;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.isTrue;
import static de.rwth.idsg.mb.regioIT.client.rest.params.BookingAlertParams.AlertType.CANCELLED;
import static de.rwth.idsg.mb.regioIT.client.rest.params.BookingAlertParams.AlertType.IMPOSSIBLE;
import static de.rwth.idsg.mb.regioIT.client.rest.params.BookingAlertParams.AlertType.NEW_PERIOD;
import static de.rwth.idsg.mb.regioIT.client.rest.params.BookingAlertParams.AlertType.NOTIFICATION;
import static de.rwth.idsg.mb.regioIT.client.rest.params.BookingAlertParams.AlertType.POSSIBLE_AGAIN;
import static de.rwth.idsg.mb.utils.BasicUtils.toSeconds;
import static de.rwth.idsg.mb.utils.ConsumptionConverterUtils.toDto;
import static de.rwth.idsg.mb.utils.ConsumptionConverterUtils.toParams;

/**
 * General sequence of steps:
 * <p/>
 * 1. Convert IXSI-Types into Params
 * 2. Call RegioIT's REST API and get response
 *
 * @author Wolfgang Kluth <kluth@dbis.rwth-aachen.de>
 * @since 20.04.2015
 */
@Slf4j
@Singleton
public class RegioITPushService {

    @Inject private AppConfiguration appConfiguration;
    @Inject private BookingClient bookingClient;
    @Inject private ConsumptionClient consumptionClient;
    @Inject private ProviderLookupTable providerLookupTable;
    @Inject private IxsiProviderRepository ixsiProviderRepository;
    @Inject private BookingTargetRepository bookingTargetRepository;
    @Inject private ConsumptionRepository consumptionRepository;
    @Inject private BookingRepository bookingRepository;

    private static final Joiner COMMA_JOINER = Joiner.on(", ");

    // The switch to enable/disable pushing data to RegioIT
    @Getter
    private boolean isDisabled;

    @PostConstruct
    private void init() {
        isDisabled = !appConfiguration.getRegioIT().isPushEnabled();
    }

    public void toggleDisabled() {
        isDisabled = !isDisabled;
    }

    // -------------------------------------------------------------------------
    // Consumption
    // -------------------------------------------------------------------------

    @Asynchronous
    public void pushConsumptionData(int partnerId, List<ConsumptionDTO> consumptions) {
        if (isDisabled) {
            return;
        }

        Set<String> bookingIdSet = consumptions.stream()
                                               .map(c -> c.getConsumption().getBookingID())
                                               .collect(Collectors.toSet());

        Map<String, String> lookupMap = ixsiProviderRepository.getBookingProviderMap(bookingIdSet);

        pushConsumptionData(partnerId, consumptions, lookupMap);
    }

    private void pushConsumptionData(int partnerId, List<ConsumptionDTO> consumptions, Map<String, String> lookupMap) {
        if (isDisabled) {
            return;
        }

        boolean success;
        try {
            pushConsumptionDataInternal(consumptions, lookupMap);
            success = true;
        } catch (Exception e) {
            log.error("Error occurred", e);
            success = false;
        }

        consumptionRepository.insertPushEvent(partnerId, consumptions, success);
    }

    /**
     * Cron job that runs at fixed intervals
     */
    @Schedule(minute = "*/2", hour = "*", persistent = false)
    public void retryConsumptionPush() {
        if (isDisabled) {
            return;
        }

        List<ConsumptionRecord> list = consumptionRepository.getRecordsToPush();

        if (list.isEmpty()) {
            return;
        }

        Set<String> bookingIdSet = list.stream()
                                       .map(ConsumptionRecord::getBookingId)
                                       .collect(Collectors.toSet());

        Map<String, String> lookupMap = ixsiProviderRepository.getBookingProviderMap(bookingIdSet);

        list.stream()
            .collect(Collectors.groupingBy(ConsumptionRecord::getPartnerId,
                     Collectors.groupingBy(ConsumptionRecord::getBookingId)))
            .entrySet()
            .stream()
            .forEach(item -> {
                int partnerId = item.getKey();
                for (Map.Entry<String, List<ConsumptionRecord>> k : item.getValue().entrySet()) {
                    String bookingId = k.getKey();
                    List<ConsumptionRecord> records = k.getValue();

                    log.info("Retrying to push {} consumptions for booking {} of partner {}",
                            records.size(), bookingId, partnerId);
                    pushConsumptionData(partnerId, toDto(records), lookupMap);
                }
            });
    }

    private void pushConsumptionDataInternal(List<ConsumptionDTO> list, Map<String, String> lookupMap) {
        List<ConsumptionParams> consumptionParamsList = toParams(list, lookupMap);
        Response.Status status = consumptionClient.postConsumptionData(consumptionParamsList);
        checkResponseStatus(consumptionParamsList, status, "Booking reference in consumption data is not found in RegioIT system");
    }

    // -------------------------------------------------------------------------
    // External Booking
    // -------------------------------------------------------------------------

    @Asynchronous
    public void pushExternalBooking(int partnerId, ExternalBookingType externalBooking) {
        if (isDisabled) {
            return;
        }

        boolean success;
        try {
            pushExternalBookingInternal(externalBooking);
            success = true;
        } catch (Exception e) {
            log.error("Error occurred", e);
            success = false;
        }

        bookingRepository.insertPushEvent(partnerId, externalBooking, success);
    }

    @Asynchronous
    public void postBookingAlert(List<BookingChangeType> bookingChangeList) {
        if (isDisabled) {
            return;
        }

        try {
            postBookingAlertInternal(bookingChangeList);
        } catch (Exception e) {
            log.error("Error occurred", e);
        }
    }

    private void pushExternalBookingInternal(ExternalBookingType externalBooking) {
        String userId = externalBooking.getUserInfo().getUserID();
        String bookingId = externalBooking.getBookingID();
        String bookingTargetId = externalBooking.getBookingTargetID().getBookeeID();
        String providerId = externalBooking.getUserInfo().getProviderID();

        AttributeClassType mobilityType = bookingTargetRepository.getAttributeClassType(bookingTargetId, providerId);

        ExternalBookingParams externalBookingParams = ExternalBookingParams
                .builder()
                .startDateTime(toSeconds(externalBooking.getTimePeriod().getBegin()))
                .endDateTime(toSeconds(externalBooking.getTimePeriod().getEnd()))
                .resourceId(bookingTargetId)
                .fromStation(null) // TODO: Missing field in ExternalBookingType spec
                .userId(userId)
                .mobilityType(mobilityType.value())
                .build();

        Response.Status status = bookingClient.putExternalBooking(
                providerLookupTable.getProviderName(providerId),
                bookingId,
                externalBookingParams
        );

        checkResponseStatus(externalBookingParams, status, "userId '" + userId + "' is not found in RegioIT system");
    }

    private void postBookingAlertInternal(List<BookingChangeType> bookingChangeList) {
        List<BookingAlertParams> bookingAlertParams = new ArrayList<>();
        for (BookingChangeType bc : bookingChangeList) {
            String bookingId = bc.getBookingID();

            BookingAlertParams params = BookingAlertParams
                    .builder()
                    .providerName(ixsiProviderRepository.getProviderName(bookingId))
                    .bookingNo(bookingId)
                    .reason(COMMA_JOINER.join(BasicUtils.extractGermanTextList(bc.getReason())))
                    .build();

            if (isTrue(bc.isCancelled())) {
                params.setAlertType(CANCELLED);

            } else if (bc.isSetNewPeriod()) {
                params.setAlertType(NEW_PERIOD);
                params.setNewBeginBooking(toSeconds(bc.getNewPeriod().getBegin()));
                params.setNewEndBooking(toSeconds(bc.getNewPeriod().getEnd()));

            } else if (isTrue(bc.isNotification())) {
                params.setAlertType(NOTIFICATION);

            } else if (isTrue(bc.isImpossible())) {
                params.setAlertType(IMPOSSIBLE);

            } else if (isTrue(bc.isRepossiblized())) {
                params.setAlertType(POSSIBLE_AGAIN);
            }

            bookingAlertParams.add(params);
        }

        Response.Status status = bookingClient.postBookingAlert(bookingAlertParams);
        checkResponseStatus(bookingAlertParams, status, "Booking reference is not found in RegioIT system");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void checkResponseStatus(Object request, Response.Status status, String errorReason) {
        if (status == Response.Status.OK) {
            log.info("Pushed successfully {}", request.toString());

        } else if (status == Response.Status.NOT_FOUND) {
            throw new RuntimeException(errorReason);

        } else {
            throw new RuntimeException("Received unexpected status code '" + status.getStatusCode() + "' from RegioIT");
        }
    }
}
