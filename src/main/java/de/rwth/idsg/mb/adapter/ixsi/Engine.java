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

import de.rwth.idsg.mb.adapter.ixsi.client.PingService;
import de.rwth.idsg.mb.adapter.ixsi.client.api.Consumer;
import de.rwth.idsg.mb.adapter.ixsi.client.connect.ConnectionManager;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingTargetRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.ConsumptionRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.PlaceRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.ServerSystemRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.UserRepository;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledQueryService;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledSubscriptionService;
import de.rwth.idsg.mb.adapter.ixsi.store.WebSocketClientEndpointStore;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.AvailabilitySubscriptionStore;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.BookingAlertSubscriptionStore;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.ConsumptionSubscriptionStore;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.ExternalBookingSubscriptionStore;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.PlaceAvailabilitySubscriptionStore;
import de.rwth.idsg.mb.notification.Mailer;
import jooq.db.ixsi.tables.records.ServerSystemRecord;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.ProviderPlaceIDType;
import xjc.schema.ixsi.UserInfoType;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 30.10.2014
 */
@Slf4j
@ApplicationScoped
public class Engine {

    @EJB private EnabledQueryService queryService;
    @EJB private AvailabilitySubscriptionStore availabilitySubscriptionStore;
    @EJB private BookingAlertSubscriptionStore bookingAlertSubscriptionStore;
    @EJB private ConsumptionSubscriptionStore consumptionSubscriptionStore;
    @EJB private ExternalBookingSubscriptionStore externalBookingSubscriptionStore;
    @EJB private PlaceAvailabilitySubscriptionStore placeAvailabilitySubscriptionStore;

    @Inject private EnabledSubscriptionService subscriptionService;
    @Inject private ConnectionManager ixsiConnectionManager;

    @Inject private BookingTargetRepository bookingTargetRepository;
    @Inject private PlaceRepository placeRepository;
    @Inject private UserRepository userRepository;
    @Inject private BookingRepository bookingRepository;
    @Inject private ConsumptionRepository consumptionRepository;
    @Inject private ServerSystemRepository serverSystemRepository;

    @Inject private WebSocketClientEndpointStore endpointStore;
    @Inject private Consumer consumer;
    @Inject private PingService pingService;
    @Inject private Mailer mailer;

    @Resource private ManagedScheduledExecutorService executorService;

    public void start() {
        executeAsync(() -> {
            List<ServerSystemRecord> recordList = serverSystemRepository.getAll();
            startInternal(recordList);
        });
    }

    public void start(int partnerId) {
        int connections = endpointStore.getSize(partnerId);
        if (connections > 0) {
            log.info("There are already {} connections for partner {}. Will not establish new connections!",
                    connections, partnerId);
            return;
        }

        List<Long> contextIds = ixsiConnectionManager.getActiveConnectContextIds(partnerId);
        if (!contextIds.isEmpty()) {
            log.info("There are already active connect contexts {} for partner {}. Will not initiate new contexts!",
                    contextIds, partnerId);
            return;
        }

        List<ServerSystemRecord> recordList = serverSystemRepository.getAll(partnerId);
        startInternal(recordList);
    }

    public void cancelReconnectJobs(int partnerId) {
        ixsiConnectionManager.cancelReconnectJobs(partnerId);
    }

    public void stop(int partnerId) {
        ixsiConnectionManager.destroy(partnerId);
    }

    public void setup(PartnerContext pctx) {
        executeAsync(() -> {
            mailer.connectedAll(pctx);
            queryService.getBookingTargetsInfo(pctx, null, null);
        });
    }

    public void clearStores(PartnerContext partnerContext) {
        executeAsync(() -> {
            int partnerId = partnerContext.getConfig().getPartnerId();
            log.debug("Clearing all the subscription stores for the server system with partnerId '{}'", partnerId);

            // Let's not check whether the feature is enabled for the partner. It doesn't hurt
            // to call all since the method does nothing if the key is not in the map.
            availabilitySubscriptionStore.unsubscribeAll(partnerId);
            bookingAlertSubscriptionStore.unsubscribeAll(partnerId);
            consumptionSubscriptionStore.unsubscribeAll(partnerId);
            externalBookingSubscriptionStore.unsubscribeAll(partnerId);
            placeAvailabilitySubscriptionStore.unsubscribeAll(partnerId);

            mailer.disconnectedAll(partnerContext);
        });
    }

    public void subscribeAll(int partnerId) {
        subscribeAll(endpointStore.getPartnerContext(partnerId));
    }

    public void subscribeAll(PartnerContext pctx) {
        executeAsync(() -> {
            int partnerId = pctx.getConfig().getPartnerId();
            Set<IxsiFeature> ff = pctx.getConfig().getFeatures();

            log.debug("Sending 'subscribe' requests to the server system with partnerId '{}'", partnerId);

            if (ff.contains(IxsiFeature.S_Availability)) {
                List<BookingTargetIDType> bookingTargetList = bookingTargetRepository.getActiveIds(partnerId);
                subscriptionService.availabilitySub(pctx, bookingTargetList, null);
            }

            if (ff.contains(IxsiFeature.S_PlaceAvailability)) {
                List<ProviderPlaceIDType> placeList = placeRepository.getActiveIds(partnerId);
                subscriptionService.placeAvailabilitySub(pctx, placeList);
            }

            if (ff.contains(IxsiFeature.S_ExternalBooking)) {
                List<UserInfoType> userInfoList = userRepository.getUserInfos(partnerId);
                subscriptionService.externalBookingSub(pctx, userInfoList);
            }

            List<String> nonFinalBookings = consumptionRepository.getBookingIdsWithNonFinalConsumption(partnerId);

            if (ff.contains(IxsiFeature.S_Consumption)) {
                subscriptionService.consumptionSub(pctx, nonFinalBookings);
            }

            if (ff.contains(IxsiFeature.S_BookingAlert)) {
                // Here we assume that bookings for which we did not receive "final" consumptions,
                // are still in some capacity ongoing/active and subscribe to them for alerts
                subscriptionService.bookingAlertSub(pctx, nonFinalBookings);
            }
        });
    }

    public void subscribeStatusAll(int partnerId) {
        PartnerContext pctx = endpointStore.getPartnerContext(partnerId);
        log.debug("Sending 'status' requests to the server system with partnerId '{}'", partnerId);

        subscriptionService.availabilitySubStatus(pctx);
        subscriptionService.placeAvailabilitySubStatus(pctx);
        subscriptionService.externalBookingSubStatus(pctx);
        subscriptionService.consumptionSubStatus(pctx);
        subscriptionService.bookingAlertSubStatus(pctx);
    }

    public void subscribeGetCompleteAll(int partnerId) {
        PartnerContext pctx = endpointStore.getPartnerContext(partnerId);
        log.debug("Sending 'get complete' requests to the server system with partnerId '{}'", partnerId);

        subscriptionService.getCompleteAvailability(pctx, null);
        subscriptionService.getCompletePlaceAvailability(pctx, null);
        subscriptionService.getCompleteExternalBooking(pctx, null);
        subscriptionService.getCompleteConsumption(pctx, null);
        subscriptionService.getCompleteBookingAlert(pctx, null);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void startInternal(List<ServerSystemRecord> recordList) {
        List<PartnerContext> contextList = new ArrayList<>(recordList.size());

        for (ServerSystemRecord r : recordList) {
            PartnerContext.Config pc =
                    PartnerContext.Config.builder()
                                         .partnerId(r.getPartnerId())
                                         .partnerName(r.getPartnerName())
                                         .basePath(r.getBasePath())
                                         .numberOfConnections(r.getNumberOfConnections())
                                         .features(serverSystemRepository.getFeatures(r.getPartnerId()))
                                         .build();

            contextList.add(PartnerContext.builder()
                                          .config(pc)
                                          .consumer(consumer)
                                          .pingService(pingService)
                                          .engine(this)
                                          .connectionManager(ixsiConnectionManager)
                                          .endpointStore(endpointStore)
                                          .build());
        }

        ixsiConnectionManager.connect(contextList);
    }

    private void executeAsync(Runnable r) {
        executorService.execute(r);
    }
}
