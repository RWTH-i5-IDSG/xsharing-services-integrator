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
package de.rwth.idsg.mb.controller.service;

import de.ivu.realtime.modules.ura.data.json.Ura2Converter;
import de.ivu.realtime.modules.ura.data.request.UraRequestParseException;
import de.ivu.realtime.modules.ura.data.response.StopPoint;
import de.ivu.realtime.modules.ura.data.response.UraEntity;
import de.ivu.realtime.modules.ura.data.response.UraResponse;
import de.ivu.realtime.modules.ura.data.response.UraVersion;
import de.ivu.realtime.modules.ura.data.response.VehicleMessage;
import de.rwth.idsg.mb.controller.NonIxsiProvider;
import de.rwth.idsg.mb.controller.ProviderLookupTable;
import de.rwth.idsg.mb.controller.parse.UraStringParser;
import de.rwth.idsg.mb.pg.TsRange;
import de.rwth.idsg.mb.utils.BasicUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wolfgang Kluth on 03/02/15.
 */

@Slf4j
@Stateless
public class UraService {

    @EJB private StationService stationService;
    @EJB private VehicleService vehicleService;
    @EJB private AseagService aseagService;
    @EJB private ProviderLookupTable providerLookupTable;

    public String fetchUraResponseString(String uraParams) throws UraRequestParseException {
        UraStringParser uraStringParser = new UraStringParser(uraParams, providerLookupTable.getLocalProviders());

        List<StopPoint> stopPoints = new ArrayList<>();
        List<VehicleMessage> vehicleMessages = new ArrayList<>();

        // 1. Local database query
        //
        if (uraStringParser.isLocalRelevant()) {
            List<String> providers = uraStringParser.getSupportedProviders();
            List<String> stopPointTypes = uraStringParser.getStopPointTypes();
            List<String> stopIdList = uraStringParser.getStopId();
            List<Double> circleFilter = uraStringParser.getCircleFilter();
            TsRange timePeriod = uraStringParser.getEstimatedTimeFilter();

            stopPoints.addAll(
                    stationService.getStopPoints(
                            stopIdList, providers, stopPointTypes,
                            circleFilter, timePeriod));

            // also find vehicles to a stopPointID
            if (BasicUtils.hasElements(stopIdList)) {
                for (StopPoint stopPoint : stopPoints) {
                    vehicleMessages.addAll(vehicleService.getVehicleMessages(stopPoint, timePeriod));
                }
            }
        }

        UraResponse uraResponse = new UraResponse.Builder()
                .withStops(stopPoints)
                .withVehicleMessages(vehicleMessages)
                .withUraVersion(
                        new UraVersion.Builder().withMajor(2).withMinor(0).withTimestamp(new DateTime()).build())
                .build();

        // 2. Remote query
        //
        if (uraStringParser.isAseagRelevant()) {
            UraEntity entity = aseagService.get(uraStringParser);
            if (entity != null) {
                UraResponse aseagResponse = (UraResponse) entity;
                uraResponse = mergeUraResponse(uraResponse, aseagResponse);
            }
        }

        return new Ura2Converter(uraStringParser.getReturnStringList())
                .convert(uraResponse);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private UraResponse mergeUraResponse(UraResponse localResponse, UraResponse aseagResponse) {
        fillValues(aseagResponse.getStops());

        aseagResponse.addStops(localResponse.getStops());
        aseagResponse.addVehicleMessages(localResponse.getVehicleMessages());
        aseagResponse.setUraVersion(localResponse.getUraVersion());
        return aseagResponse;
    }

    private void fillValues(List<StopPoint> aseagStops) {
        for (StopPoint sp : aseagStops) {
            if (sp.getProvider() == null) {
                sp.setProvider(NonIxsiProvider.ASEAG.value());
            }
            if (sp.getStopPointType() == null) {
                sp.setStopPointType("bus_stop");
            }
        }
    }
}
