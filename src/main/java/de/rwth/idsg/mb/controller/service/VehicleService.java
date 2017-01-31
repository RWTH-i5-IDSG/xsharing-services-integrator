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

import de.ivu.realtime.modules.ura.data.response.StopPoint;
import de.ivu.realtime.modules.ura.data.response.VehicleMessage;
import de.rwth.idsg.mb.controller.ProviderLookupTable;
import de.rwth.idsg.mb.controller.dto.VehicleDTO;
import de.rwth.idsg.mb.controller.repository.VehicleRepository;
import de.rwth.idsg.mb.pg.TsRange;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Wolfgang Kluth on 12/03/15.
 */

@Slf4j
@Stateless
public class VehicleService {

    @EJB private VehicleRepository vehicleRepository;
    @EJB private ProviderLookupTable providerLookupTable;

    public List<VehicleMessage> getVehicleMessages(StopPoint stopPoint, TsRange timePeriod) {
        String provider = stopPoint.getProvider();

        // 1. IXSI
        //
        try {
            // StopPoint uses provider name, but our vehicle DB queries require the provider id
            String providerId = providerLookupTable.getIxsiInfo(provider).getProviderId();
            if (providerId != null) {
                return getIxsi(providerId, stopPoint.getStopPointId(), timePeriod);
            }
        } catch (Exception e) {
            log.error("Exception happened", e);
        }

        // 2. As last resort
        //
        return Collections.emptyList();
    }

    private List<VehicleMessage> getIxsi(String providerId, String stopPointId, TsRange timePeriod) {
        List<VehicleMessage> vehicleMessages = new ArrayList<>();

        for (VehicleDTO vehicleDTO : vehicleRepository.find(providerId, stopPointId, timePeriod)) {
            vehicleMessages.add(
                    new VehicleMessage.Builder()
                            .withVehicleId(vehicleDTO.getVehicleId())
                            .withName(vehicleDTO.getVehicleName())
                            .withModalType(vehicleDTO.getVehicleType())
                            .build()
            );
        }

        return vehicleMessages;
    }

}
