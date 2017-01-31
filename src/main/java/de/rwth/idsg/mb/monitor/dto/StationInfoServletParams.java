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
package de.rwth.idsg.mb.monitor.dto;

import de.rwth.idsg.mb.utils.MonitorUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 23.09.2016
 */
@Getter
@Setter
@NoArgsConstructor
public class StationInfoServletParams  extends StationServletParams {

    private String vehicleId;
    private String stopId;

    public StationInfoServletParams(HttpServletRequest request) {
        super(request);

        vehicleId = MonitorUtils.trim(request.getParameter("vehicleId"));
        stopId = MonitorUtils.trim(request.getParameter("stopId"));
    }
}
