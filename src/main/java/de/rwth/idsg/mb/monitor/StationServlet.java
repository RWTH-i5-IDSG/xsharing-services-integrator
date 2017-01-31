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
import de.ivu.realtime.modules.ura.data.response.StopPoint;
import de.ivu.realtime.modules.ura.data.response.VehicleMessage;
import de.rwth.idsg.mb.controller.service.StationService;
import de.rwth.idsg.mb.controller.service.VehicleService;
import de.rwth.idsg.mb.monitor.dto.StationInfoServletParams;
import de.rwth.idsg.mb.monitor.dto.StationServletParams;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * Created by igor on 30.06.2016.
 */
@Slf4j
@WebServlet(name = "StationServlet", value = "/stations/*")
public class StationServlet extends HttpServlet {
    private static final long serialVersionUID = -6066473922361365939L;

    @EJB private VehicleService vehicleService;
    @EJB private StationService stationService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String command = request.getPathInfo();
        if (Strings.isNullOrEmpty(command) || "/".equals(command)) {
            printStationOverview(request, response);

        } else if (command.startsWith("/details")) {
            printSingleStation(request, response);
        }
    }

    private void printStationOverview(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StationServletParams params = new StationServletParams(request);

        String providerName = params.getProviderName();
        List<String> providerList;
        if (providerName.isEmpty()) {
            providerList = Collections.emptyList();
        } else {
            providerList = Collections.singletonList(providerName);
        }

        Map<String, List<StopPoint>> stopPointsMap =
                stationService.getStopPoints(emptyList(), providerList, emptyList(), emptyList(), params.getTsRange())
                              .stream()
                              .collect(Collectors.groupingBy(StopPoint::getProvider));

        request.setAttribute("params", params);
        request.setAttribute("stopPointsMap", stopPointsMap);
        request.getRequestDispatcher("/WEB-INF/views/station.jsp").forward(request, response);
    }

    private void printSingleStation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StationInfoServletParams params = new StationInfoServletParams(request);

        StopPoint stopPoint = new StopPoint.Builder().withStopPointId(params.getStopId())
                                                     .withProvider(params.getProviderName())
                                                     .build();

        List<VehicleMessage> vehicles = vehicleService.getVehicleMessages(stopPoint, params.getTsRange());

        request.setAttribute("params", params);
        request.setAttribute("vehicles", vehicles);
        request.getRequestDispatcher("/WEB-INF/views/stationDetails.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doGet(request, response);

    }

}
