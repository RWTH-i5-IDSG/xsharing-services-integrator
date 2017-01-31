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
package de.rwth.idsg.mb.controller.repository;

import de.ivu.realtime.modules.ura.data.response.StopPoint;
import de.rwth.idsg.mb.pg.TsRange;

import java.util.List;

/**
 * Created by Wolfgang Kluth on 03/02/15.
 */

public interface StopInfoRepository {

    List<StopPoint> find(List<String> stationIds, List<String> providers, List<String> stopPointTypes,
                         Double latitude, Double longitude, Double radius,
                         TsRange timePeriod);
}
