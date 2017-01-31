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
import com.google.common.collect.Maps;
import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.repository.ServerSystemRepository;
import de.rwth.idsg.mb.monitor.dto.ServerSystemForm;
import jooq.db.ixsi.tables.records.ServerSystemRecord;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 17.12.2015
 */
@Slf4j
@WebServlet(name = "ServerSystemServlet", value = "/systems/*")
public class ServerSystemServlet extends HttpServlet {
    private static final long serialVersionUID = -6066473922361365939L;

    @Inject private ServerSystemRepository serverSystemRepository;
    @Inject private MonitorServlet monitorServlet;

    private static final String ADD = "/add";
    private static final String UPDATE = "/update";
    private static final String DELETE = "/delete";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String command = request.getPathInfo();

        if (Strings.isNullOrEmpty(command) || "/".equals(command)) {
            printHome(request, response);

        } else if (command.startsWith(ADD)) {
            printAdd(request, response);

        } else if (command.startsWith(UPDATE)) {
            printUpdate(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String command = request.getPathInfo();

        if (Strings.isNullOrEmpty(command) || "/".equals(command)) {
            // No-op

        } else if (command.startsWith(ADD)) {
            processAdd(request);

        } else if (command.startsWith(UPDATE)) {
            processUpdate(request);

        } else if (command.startsWith(DELETE)) {
            processDelete(request);
        }

        printHome(request, response);
    }

    // -------------------------------------------------------------------------
    // GET
    // -------------------------------------------------------------------------

    private void printHome(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("serverSystemList", serverSystemRepository.getServerSystems());
        request.getRequestDispatcher("/WEB-INF/views/serverSystem.jsp").forward(request, response);
    }

    private void printAdd(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setIxsiFeatureMap(request, Collections.emptySet());

        request.setAttribute("operationType", "add");
        request.getRequestDispatcher("/WEB-INF/views/serverSystem-save.jsp").forward(request, response);
    }

    private void printUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String partnerIdString = request.getPathInfo().replaceFirst(UPDATE + "/", "");
        int partnerId = Integer.parseInt(partnerIdString);

        ServerSystemRecord r = serverSystemRepository.getServerSystem(partnerId);

        setIxsiFeatureMap(request, serverSystemRepository.getFeatures(partnerId));

        request.setAttribute("operationType", "update");
        request.setAttribute("system", r);
        request.getRequestDispatcher("/WEB-INF/views/serverSystem-save.jsp").forward(request, response);
    }

    // -------------------------------------------------------------------------
    // POST
    // -------------------------------------------------------------------------

    private void processAdd(HttpServletRequest request) {
        ServerSystemForm form = buildForm(request);
        serverSystemRepository.addServerSystem(form);
        monitorServlet.refreshPartners();
    }

    private void processUpdate(HttpServletRequest request) {
        ServerSystemForm form = buildForm(request);
        serverSystemRepository.updateServerSystem(form);
        monitorServlet.refreshPartners();
    }

    private void processDelete(HttpServletRequest request) {
        int partnerId = Integer.parseInt(request.getParameter("partnerId"));
        serverSystemRepository.deleteServerSystem(partnerId);
        monitorServlet.refreshPartners();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static boolean getBoolean(String param) {
        return "on".equalsIgnoreCase(param);
    }

    private ServerSystemForm buildForm(HttpServletRequest request) {
        String partnerIdString = request.getParameter("partnerId");
        String partnerName = request.getParameter("partnerName");
        String basePath = request.getParameter("basePath");
        Integer numOfConnections = Integer.valueOf(request.getParameter("numOfConnections"));
        boolean enabled = getBoolean(request.getParameter("enable"));

        EnumSet<IxsiFeature> ixsiFeatures = getFeatures(request, "queryStatic");
        ixsiFeatures.addAll(getFeatures(request, "queryUser"));
        ixsiFeatures.addAll(getFeatures(request, "subscriptionAdmin"));
        ixsiFeatures.addAll(getFeatures(request, "subscription"));

        Integer partnerId = null;
        if (partnerIdString != null && !partnerIdString.isEmpty()) {
            partnerId = Integer.valueOf(partnerIdString);
        }

        if (numOfConnections <= 0) {
            throw new IllegalArgumentException("# of connections must be > 0!");
        }

        return ServerSystemForm.builder()
                               .partnerId(partnerId)
                               .partnerName(partnerName)
                               .basePath(basePath)
                               .numberOfConnections(numOfConnections)
                               .enabled(enabled)
                               .ixsiFeatures(ixsiFeatures)
                               .build();
    }

    private void setIxsiFeatureMap(HttpServletRequest request, Set<IxsiFeature> sif) {
        request.setAttribute("queryStatic", getFeatureCheckMap(sif, IxsiFeature.QUERY_STATIC_VALUES));
        request.setAttribute("queryUser", getFeatureCheckMap(sif, IxsiFeature.QUERY_USER_VALUES));
        request.setAttribute("subscriptionAdmin", getFeatureCheckMap(sif, IxsiFeature.SUBSCRIPTION_ADMIN_VALUES));
        request.setAttribute("subscription", getFeatureCheckMap(sif, IxsiFeature.SUBSCRIPTION_VALUES));
    }

    private Map<IxsiFeature, Boolean> getFeatureCheckMap(Set<IxsiFeature> selectedInDB,
                                                         Set<IxsiFeature> featureGroup) {
        return new EnumMap<>(Maps.asMap(featureGroup, selectedInDB::contains));
    }

    private EnumSet<IxsiFeature> getFeatures(HttpServletRequest request, String group) {
        String[] nameList = request.getParameterValues(group);

        // If nothing is selected
        if (nameList == null || nameList.length == 0) {
            // return a mutable instance since we want to add further elements to the result set in the future
            return EnumSet.noneOf(IxsiFeature.class);
        }

        return Arrays.stream(nameList)
                     .map(name -> IxsiFeature.fromValues(group, name))
                     .collect(toCollection(() -> EnumSet.noneOf(IxsiFeature.class)));
    }

}
