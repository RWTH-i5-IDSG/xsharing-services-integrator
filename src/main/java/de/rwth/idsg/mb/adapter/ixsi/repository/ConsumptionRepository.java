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
package de.rwth.idsg.mb.adapter.ixsi.repository;

import de.rwth.idsg.mb.adapter.ixsi.repository.dto.ConsumptionDTO;
import de.rwth.idsg.mb.regioIT.client.rest.params.ConsumptionParams;
import jooq.db.ixsi.tables.records.ConsumptionRecord;
import xjc.schema.ixsi.ConsumptionType;

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 13.01.2015
 */
public interface ConsumptionRepository {
    List<String> getBookingIdsWithNonFinalConsumption(int partnerId);

    List<ConsumptionDTO> insertConsumptionList(int partnerId, List<ConsumptionType> consumptionList);

    void insertPushEvent(int partnerId, List<ConsumptionDTO> consumptionList, boolean success);

    List<ConsumptionRecord> getRecordsToPush();

    List<ConsumptionParams> getRecords(String providerName, String bookingId);

    List<String> getFinalized(int partnerId);

}
