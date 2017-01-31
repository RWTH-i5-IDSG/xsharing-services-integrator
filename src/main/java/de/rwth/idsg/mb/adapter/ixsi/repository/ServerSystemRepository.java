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

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.repository.dto.ServerSystem;
import de.rwth.idsg.mb.monitor.dto.ServerSystemForm;
import jooq.db.ixsi.tables.records.ServerSystemRecord;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.11.2014
 */
public interface ServerSystemRepository {
    List<ServerSystemRecord> getAll();
    List<ServerSystemRecord> getAll(int partnerId);
    Set<IxsiFeature> getFeatures(int partnerId);

    List<ServerSystem.Monitor> getPartners();
    String getTableName();
    void updateBTIRDeliveryTimestamp(int partnerId, DateTime dateTime);
    DateTime getBTIRDeliveryTimestamp(int partnerId);

    List<ServerSystemRecord> getServerSystems();
    ServerSystemRecord getServerSystem(int partnerId);
    void addServerSystem(ServerSystemForm form);
    void updateServerSystem(ServerSystemForm form);
    void deleteServerSystem(int partnerId);
}
