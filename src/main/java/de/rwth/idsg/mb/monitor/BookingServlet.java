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
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.ConsumptionRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.dto.BookingInfoDTO;
import de.rwth.idsg.mb.controller.ProviderLookupTable;
import de.rwth.idsg.mb.monitor.dto.BookingInfoParams;
import de.rwth.idsg.mb.monitor.dto.ConsumptionParamsDate;
import de.rwth.idsg.mb.utils.MonitorUtils;
import jooq.db.ixsi.tables.records.BookingChangeRecord;
import jooq.db.ixsi.tables.records.BookingCreateRecord;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 17.12.2015
 */
@Slf4j
@WebServlet(name = "BookingServlet", value = "/bookings/*")
public class BookingServlet extends HttpServlet {
    private static final long serialVersionUID = -6066473922361365939L;

    @Inject private BookingRepository bookingRepository;
    @Inject private ProviderLookupTable providerLookupTable;
    @Inject private ConsumptionRepository consumptionRepository;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String command = request.getPathInfo();
        if (Strings.isNullOrEmpty(command) || "/".equals(command)) {
            printBookingOverview(request, response);

        } else if (command.startsWith("/details")) {
            printSingleBooking(request, response);
        }
    }

    private void printBookingOverview(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        BookingInfoParams params = new BookingInfoParams(request);

        Map<String, List<BookingInfoDTO>> results =
                bookingRepository.getInfo(params)
                                 .stream()
                                 .collect(Collectors.groupingBy(
                                         BookingInfoDTO::getVehicleId,
                                         LinkedHashMap::new,
                                         Collectors.mapping(Function.identity(), Collectors.toList())
                                 ));

        request.setAttribute("params", params);
        request.setAttribute("results", results);
        request.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(request, response);
    }

    private void printSingleBooking(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String bookingId = MonitorUtils.trim(request.getParameter("bookingId"));

        BookingCreateRecord createRecord = bookingRepository.getCreateRecord(bookingId);

        String providerName;
        List<BookingChangeRecord> bookingChanges;
        List<ConsumptionParamsDate> bookingConsumptions;

        // prevent NPE in jsp
        //
        if (createRecord == null) {
            providerName = "";
            createRecord = new BookingCreateRecord();
            bookingChanges = Collections.emptyList();
            bookingConsumptions = Collections.emptyList();

        } else {
            providerName = providerLookupTable.getProviderName(createRecord.getProviderId());
            bookingChanges = bookingRepository.getChangeRecords(createRecord.getProviderId(), bookingId);
            bookingConsumptions = consumptionRepository.getRecords(providerName, bookingId)
                                                       .stream()
                                                       .map(ConsumptionParamsDate::new)
                                                       .collect(Collectors.toList());
        }

        request.setAttribute("bookingId", bookingId);
        request.setAttribute("providerName", providerName);
        request.setAttribute("bookingCreate", createRecord);
        request.setAttribute("bookingChanges", bookingChanges);
        request.setAttribute("bookingConsumptions", bookingConsumptions);
        request.getRequestDispatcher("/WEB-INF/views/bookingDetails.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doGet(request, response);
    }

}
