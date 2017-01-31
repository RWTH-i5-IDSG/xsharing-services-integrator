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
package de.rwth.idsg.mb.monitor;

import com.google.common.base.Strings;
import de.rwth.idsg.mb.adapter.ixsi.Engine;
import de.rwth.idsg.mb.adapter.ixsi.TransactionCounter;
import de.rwth.idsg.mb.adapter.ixsi.client.WebSocketClientEndpoint;
import de.rwth.idsg.mb.adapter.ixsi.context.InOutContext;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import de.rwth.idsg.mb.adapter.ixsi.repository.ServerSystemRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.dto.ServerSystem;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledQueryService;
import de.rwth.idsg.mb.adapter.ixsi.store.InOutContextStore;
import de.rwth.idsg.mb.adapter.ixsi.store.UserInOutContextStore;
import de.rwth.idsg.mb.adapter.ixsi.store.WebSocketClientEndpointStore;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.AvailabilitySubscriptionStore;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.BookingAlertSubscriptionStore;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.ConsumptionSubscriptionStore;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.ExternalBookingSubscriptionStore;
import de.rwth.idsg.mb.adapter.ixsi.store.subscription.PlaceAvailabilitySubscriptionStore;
import de.rwth.idsg.mb.controller.ProviderLookupTable;
import de.rwth.idsg.mb.monitor.dto.EndpointDTO;
import de.rwth.idsg.mb.monitor.dto.IxsiCommand;
import de.rwth.idsg.mb.monitor.dto.OtherCommand;
import de.rwth.idsg.mb.monitor.dto.StoreDTO;
import de.rwth.idsg.mb.monitor.dto.StoreItem;
import de.rwth.idsg.mb.regioIT.service.RegioITPushService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.ProviderPlaceIDType;
import xjc.schema.ixsi.TransactionType;
import xjc.schema.ixsi.UserInfoType;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.04.2015
 */
@Slf4j
@WebServlet(name = "MonitorServlet", value = "/monitor/*", loadOnStartup = 3)
public class MonitorServlet extends HttpServlet {
    private static final long serialVersionUID = 9154350019595715629L;

    @Inject private Engine ixsiEngine;
    @Inject private WebSocketClientEndpointStore webSocketClientEndpointStore;
    @Inject private ServerSystemRepository serverSystemRepository;
    @Inject private FileProvider fileProvider;

    @Inject private UserInOutContextStore userInOutContextStore;
    @Inject private InOutContextStore inOutContextStore;

    @Inject private ProviderLookupTable providerLookupTable;
    @Inject private RegioITPushService regioITPushService;

    @EJB private EnabledQueryService queryService;
    @EJB private AvailabilitySubscriptionStore availabilitySubscriptionStore;
    @EJB private BookingAlertSubscriptionStore bookingAlertSubscriptionStore;
    @EJB private ConsumptionSubscriptionStore consumptionSubscriptionStore;
    @EJB private PlaceAvailabilitySubscriptionStore placeAvailabilitySubscriptionStore;
    @EJB private ExternalBookingSubscriptionStore externalBookingSubscriptionStore;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    private static final PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder()
            .printZeroNever()
            .appendYears().appendSuffix(" year", " years").appendSeparator(" ")
            .appendMonths().appendSuffix(" month", " months").appendSeparator(" ")
            .appendWeeks().appendSuffix(" week", " weeks").appendSeparator(" ")
            .appendDays().appendSuffix(" day", " days").appendSeparator(" ")
            .appendHours().appendSuffix(" hour", " hours").appendSeparator(" ")
            .appendMinutes().appendSuffix(" minute", " minutes").appendSeparator(" ")
            .appendSeconds().appendSuffix(" second", " seconds")
            .toFormatter();

    private static Map<String, List<String>> ixsiCommandList;
    private static List<String> otherCommandList;
    private static List<ServerSystem.Monitor> partnerList;

    // -------------------------------------------------------------------------
    // Paths
    // -------------------------------------------------------------------------

    private static final String OTHER_COMMANDS              = "/other-commands";

    private static final String IXSI_BASIC_COMMANDS         = "/ixsi/basic-commands";

    private static final String IXSI_XSD                    = "/ixsi/xsd";
    private static final String IXSI_STORE_AVAIL            = "/ixsi/store/avail";
    private static final String IXSI_STORE_PLACE_AVAIL      = "/ixsi/store/place-avail";
    private static final String IXSI_STORE_BOOKING_ALERT    = "/ixsi/store/booking-alert";
    private static final String IXSI_STORE_CONSUMPTION      = "/ixsi/store/consumption";
    private static final String IXSI_STORE_EX_BOOK          = "/ixsi/store/external-book";

    private static final String IXSI_CONTEXT_INOUT          = "/ixsi/store/context/in-out";
    private static final String IXSI_CONTEXT_USER_INOUT     = "/ixsi/store/context/user-in-out";

    private static final String COMMIT_INFO = "/commit-info";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        partnerList = serverSystemRepository.getPartners();

        ixsiCommandList = new LinkedHashMap<>(3);
        ixsiCommandList.put("Connection Management", IxsiCommand.getConnectionValues());
        ixsiCommandList.put("Static Data", IxsiCommand.getStaticDataValues());
        ixsiCommandList.put("Subscriptions", IxsiCommand.getSubscriptionValues());

        otherCommandList = OtherCommand.getValues();

        ixsiEngine.start();

        log.debug("Ready");
    }

    @Override
    public void destroy() {
        log.debug("Being destroyed");
    }

    void refreshPartners() {
        partnerList = serverSystemRepository.getPartners();
    }

    // -------------------------------------------------------------------------
    // HTTP methods
    // -------------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            internalGet(request, response);
        } catch (Exception e) {
            log.error("Error occurred", e);
            response.setContentType("text/plain");
            e.printStackTrace(response.getWriter());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            internalPost(request, response);
        } catch (Exception e) {
            log.error("Error occurred", e);
            response.setContentType("text/plain");
            e.printStackTrace(response.getWriter());
        }
    }

    private void internalGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String command = request.getPathInfo();
        //String command = request.getParameter("path");

        if (Strings.isNullOrEmpty(command) || "/".equals(command)) {
            printHome(request, response);

        } else if (command.startsWith("/ixsi")) {
            printIxsi(request, response, command);

        } else if (COMMIT_INFO.equals(command)) {
            response.setContentType("text/plain");
            response.getWriter().print(fileProvider.getGitProperties());
        }
    }

    private void internalPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String command = request.getPathInfo();
        //String command = request.getParameter("path");

        if (Strings.isNullOrEmpty(command) || "/".equals(command)) {
            // No-op

        } else if (IXSI_BASIC_COMMANDS.equals(command)) {
            processIxsi(request);

        } else if (OTHER_COMMANDS.equals(command)) {
            processOther(request);

        }

        printHome(request, response);
    }

    private void processIxsi(HttpServletRequest request) throws IOException {
        int partnerId;
        try {
            partnerId = Integer.parseInt(request.getParameter("partnerId"));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Partner not selected", e);
        }

        IxsiCommand ic = IxsiCommand.fromValue(request.getParameter("ixsiBasicCommand"));
        switch (ic) {
            case CONNECT:
                ixsiEngine.start(partnerId);
                break;

            case CANCEL_RECONNECT:
                ixsiEngine.cancelReconnectJobs(partnerId);
                break;

            case DESTROY:
                ixsiEngine.stop(partnerId);
                break;

            case SUB:
                ixsiEngine.subscribeAll(partnerId);
                break;

            case SUB_STATUS:
                ixsiEngine.subscribeStatusAll(partnerId);
                break;

            case SUB_COMPLETE:
                ixsiEngine.subscribeGetCompleteAll(partnerId);
                break;

            case STATIC_GET:
                // For now, no provider filter. Just update all providers.
                queryService.getBookingTargetsInfo(webSocketClientEndpointStore.getPartnerContext(partnerId), null, null);
                break;

            case STATIC_UPDATE:
                DateTime dt = serverSystemRepository.getBTIRDeliveryTimestamp(partnerId);
                queryService.getChangedProviders(webSocketClientEndpointStore.getPartnerContext(partnerId), dt, null);
                break;

            default:
                throw new RuntimeException("Unknown command");
        }
    }

    private void processOther(HttpServletRequest request) throws IOException {
        switch (OtherCommand.fromValue(request.getParameter("otherCommand"))) {
            case REFRESH_PARTNERS:
                refreshPartners();
                break;

            case REFRESH_PROVIDER_LOOKUP:
                providerLookupTable.retrieve();
                break;

            case TOGGLE_PUSH:
                regioITPushService.toggleDisabled();
                break;
        }
    }

    private void printIxsi(HttpServletRequest request, HttpServletResponse response, String command)
            throws ServletException, IOException {

        switch (command) {
            case IXSI_XSD:
                response.setContentType("application/xml");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().print(fileProvider.getIxsiSchema());
                return; // Skip forwarding to jsp (no need to render anything)!

            case IXSI_STORE_AVAIL:
                StoreDTO<Integer, BookingTargetIDType> avail = new StoreDTO<>();
                avail.setName("Availability Store");
                avail.setItemDescription(BookingTargetIDType.class.getSimpleName());
                avail.setItemList(toList(availabilitySubscriptionStore.getLookupTable()));

                request.setAttribute("store", avail);
                break;

            case IXSI_STORE_PLACE_AVAIL:
                StoreDTO<Integer, ProviderPlaceIDType> placeAvail = new StoreDTO<>();
                placeAvail.setName("Place Availability Store");
                placeAvail.setItemDescription(ProviderPlaceIDType.class.getSimpleName());
                placeAvail.setItemList(toList(placeAvailabilitySubscriptionStore.getLookupTable()));

                request.setAttribute("store", placeAvail);
                break;

            case IXSI_STORE_BOOKING_ALERT:
                StoreDTO<Integer, String> bookingAlert = new StoreDTO<>();
                bookingAlert.setName("Booking Alert Store");
                bookingAlert.setItemDescription("Booking Id");
                bookingAlert.setItemList(toList(bookingAlertSubscriptionStore.getLookupTable()));

                request.setAttribute("store", bookingAlert);
                break;

            case IXSI_STORE_CONSUMPTION:
                StoreDTO<Integer, String> consumption = new StoreDTO<>();
                consumption.setName("Consumption Store");
                consumption.setItemDescription("Booking Id");
                consumption.setItemList(toList(consumptionSubscriptionStore.getLookupTable()));

                request.setAttribute("store", consumption);
                break;

            case IXSI_STORE_EX_BOOK:
                StoreDTO<Integer, UserInfoType> exBook = new StoreDTO<>();
                exBook.setName("External Booking Store");
                exBook.setItemDescription(UserInfoType.class.getSimpleName());
                exBook.setItemList(toList(externalBookingSubscriptionStore.getLookupTable()));

                request.setAttribute("store", exBook);
                break;

            case IXSI_CONTEXT_INOUT:
                StoreDTO<TransactionType, InOutContext> inOut = new StoreDTO<>("TransactionType");
                inOut.setName("InOut Context Store");
                inOut.setItemDescription(InOutContext.class.getSimpleName());
                inOut.setItemList(contextToList(inOutContextStore.getACopy()));

                request.setAttribute("store", inOut);
                break;

            case IXSI_CONTEXT_USER_INOUT:
                StoreDTO<TransactionType, UserInOutContext> userInOut = new StoreDTO<>("TransactionType");
                userInOut.setName("UserInOut Context Store");
                userInOut.setItemDescription(UserInOutContext.class.getSimpleName());
                userInOut.setItemList(contextToList(userInOutContextStore.getACopy()));

                request.setAttribute("store", userInOut);
                break;

            default:
                throw new RuntimeException("No command");
        }

        request.getRequestDispatcher("/WEB-INF/views/ixsi-store.jsp").forward(request, response);
    }

    private void printHome(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("partnerList", partnerList);
        request.setAttribute("ixsiCommandList", ixsiCommandList);
        request.setAttribute("otherCommandList", otherCommandList);

        request.setAttribute("ixsiConnectionStatus", ixsiConnectionStatus());
        request.setAttribute("ixsiStoreList", populateIxsiStoreList());
        request.setAttribute("currentTime", new DateTime());

        request.setAttribute("numberOfRequests", TransactionCounter.INSTANCE.getRequestCount());
        request.setAttribute("numberOfResponses", TransactionCounter.INSTANCE.getResponseCount());
        request.setAttribute("userInOutContextStoreSize", userInOutContextStore.getSize());
        request.setAttribute("inOutContextStoreSize", inOutContextStore.getSize());
        request.setAttribute("isPushServiceDisabled", regioITPushService.isDisabled());

        request.getRequestDispatcher("/WEB-INF/views/monitor.jsp").forward(request, response);
    }

    private List<StoreDTO> populateIxsiStoreList() {
        StoreDTO avail = new StoreDTO<>();
        avail.setName("Availability Store");
        avail.setLink(IXSI_STORE_AVAIL);
        avail.setSize(availabilitySubscriptionStore.getSize());

        StoreDTO placeAvail = new StoreDTO<>();
        placeAvail.setName("Place Availability Store");
        placeAvail.setLink(IXSI_STORE_PLACE_AVAIL);
        placeAvail.setSize(placeAvailabilitySubscriptionStore.getSize());

        StoreDTO bookingAlert = new StoreDTO<>();
        bookingAlert.setName("Booking Alert Store");
        bookingAlert.setLink(IXSI_STORE_BOOKING_ALERT);
        bookingAlert.setSize(bookingAlertSubscriptionStore.getSize());

        StoreDTO consumption = new StoreDTO<>();
        consumption.setName("Consumption Store");
        consumption.setLink(IXSI_STORE_CONSUMPTION);
        consumption.setSize(consumptionSubscriptionStore.getSize());

        StoreDTO exBook = new StoreDTO<>();
        exBook.setName("External Booking Store");
        exBook.setLink(IXSI_STORE_EX_BOOK);
        exBook.setSize(externalBookingSubscriptionStore.getSize());

        List<StoreDTO> ixsiStoreList = new ArrayList<>();
        ixsiStoreList.add(avail);
        ixsiStoreList.add(placeAvail);
        ixsiStoreList.add(bookingAlert);
        ixsiStoreList.add(consumption);
        ixsiStoreList.add(exBook);
        return ixsiStoreList;
    }

    private List<EndpointDTO> ixsiConnectionStatus() {
        List<WebSocketClientEndpoint> endpointList = webSocketClientEndpointStore.getAll();
        List<EndpointDTO> dtoList = new ArrayList<>(endpointList.size());

        DateTime now = new DateTime();

        for (WebSocketClientEndpoint e : endpointList) {
            DateTime start = new DateTime(e.getStartTime());
            PartnerContext.Config config = e.getPartnerContext().getConfig();
            dtoList.add(
                    EndpointDTO.builder()
                               .partnerName(config.getPartnerName())
                               .partnerId(config.getPartnerId())
                               .sessionId(e.getSessionId())
                               .openSince(DATE_FORMATTER.print(start))
                               .openFor(PERIOD_FORMATTER.print(new Period(start, now)))
                               .state(e.getPartnerContext().getState())
                               .build()
            );
        }
        return dtoList;
    }

    private <T> List<StoreItem<Integer, T>> toList(Map<Integer, Set<T>> lookupTable) {
        List<StoreItem<Integer, T>> itemList = new ArrayList<>();
        for (Map.Entry<Integer, Set<T>> entry : lookupTable.entrySet()) {
            for (T item : entry.getValue()) {
                itemList.add(new StoreItem<>(entry.getKey(), item));
            }
        }
        return itemList;
    }

    private <T> List<StoreItem<TransactionType, T>> contextToList(Map<TransactionType, T> lookupTable) {
        return lookupTable.entrySet()
                          .stream()
                          .map(entry -> new StoreItem<>(entry.getKey(), entry.getValue()))
                          .sorted(Comparator.comparing(o -> o.getKey().getTimeStamp()))
                          .collect(Collectors.toList());
    }
}
