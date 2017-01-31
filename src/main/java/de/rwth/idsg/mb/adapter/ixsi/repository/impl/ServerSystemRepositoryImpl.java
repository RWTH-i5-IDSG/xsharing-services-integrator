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
package de.rwth.idsg.mb.adapter.ixsi.repository.impl;

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.IxsiProcessingException;
import de.rwth.idsg.mb.adapter.ixsi.repository.ServerSystemRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.dto.ServerSystem;
import de.rwth.idsg.mb.monitor.dto.ServerSystemForm;
import jooq.db.ixsi.tables.records.ServerSystemRecord;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.jooq.DSLContext;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static jooq.db.ixsi.tables.ServerFeature.SERVER_FEATURE;
import static jooq.db.ixsi.tables.ServerSystem.SERVER_SYSTEM;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.11.2014
 */
@Slf4j
@Stateless
public class ServerSystemRepositoryImpl implements ServerSystemRepository {

    @Inject private DSLContext ctx;

    @Override
    public List<ServerSystemRecord> getAll() {
        return ctx.selectFrom(SERVER_SYSTEM)
                  .where(SERVER_SYSTEM.ENABLED.isTrue())
                  .and(SERVER_SYSTEM.NUMBER_OF_CONNECTIONS.greaterThan(0))
                  .fetch();
    }

    @Override
    public List<ServerSystemRecord> getAll(int partnerId) {
        return ctx.selectFrom(SERVER_SYSTEM)
                  .where(SERVER_SYSTEM.ENABLED.isTrue())
                  .and(SERVER_SYSTEM.NUMBER_OF_CONNECTIONS.greaterThan(0))
                  .and(SERVER_SYSTEM.PARTNER_ID.eq(partnerId))
                  .fetch();
    }

    @Override
    public Set<IxsiFeature> getFeatures(int partnerId) {
        List<IxsiFeature> ff = ctx.selectFrom(SERVER_FEATURE)
                                  .where(SERVER_FEATURE.PARTNER_ID.eq(partnerId))
                                  .fetch()
                                  .map(r -> IxsiFeature.fromValues(r.getFeatureGroup(), r.getFeatureName()));

        if (ff.isEmpty()) {
            return EnumSet.noneOf(IxsiFeature.class);
        } else {
            return EnumSet.copyOf(ff);
        }
    }

    @Override
    public List<ServerSystem.Monitor> getPartners() {
        return ctx.select(SERVER_SYSTEM.PARTNER_ID, SERVER_SYSTEM.PARTNER_NAME)
                  .from(SERVER_SYSTEM)
                  .where(SERVER_SYSTEM.ENABLED.isTrue())
                  .and(SERVER_SYSTEM.NUMBER_OF_CONNECTIONS.greaterThan(0))
                  .fetch()
                  .map(record -> new ServerSystem.Monitor(record.value1(), record.value2()));
    }

    @Override
    public String getTableName() {
        return SERVER_SYSTEM.getSchema().getName() + "." + SERVER_SYSTEM.getName();
    }

    /**
     * Write Timestamp of BookingTargetsInfoResponse in DB
     */
    @Override
    @Transactional
    public void updateBTIRDeliveryTimestamp(int partnerId, DateTime dateTime) {
        ctx.update(SERVER_SYSTEM)
           .set(SERVER_SYSTEM.BTIR_DELIVERY_TIMESTAMP, new Timestamp(dateTime.getMillis()))
           .where(SERVER_SYSTEM.PARTNER_ID.eq(partnerId))
           .execute();
    }

    @Override
    public DateTime getBTIRDeliveryTimestamp(int partnerId) {
        Timestamp timestamp = ctx.select(SERVER_SYSTEM.BTIR_DELIVERY_TIMESTAMP)
                                 .from(SERVER_SYSTEM)
                                 .where(SERVER_SYSTEM.PARTNER_ID.eq(partnerId))
                                 .fetchOne()
                                 .value1();

        if (timestamp == null) {
            throw new IxsiProcessingException("Cannot query ChangedProviders (updates) "
                    + "before querying BookingTargetsInfo (complete static data)");
        }

        return new DateTime(timestamp.getTime());
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    @Override
    public List<ServerSystemRecord> getServerSystems() {
        return ctx.selectFrom(SERVER_SYSTEM)
                  .orderBy(SERVER_SYSTEM.PARTNER_ID)
                  .fetch();
    }

    @Override
    public ServerSystemRecord getServerSystem(int partnerId) {
        return ctx.selectFrom(SERVER_SYSTEM)
                  .where(SERVER_SYSTEM.PARTNER_ID.eq(partnerId))
                  .fetchOne();
    }

    @Override
    @Transactional
    public void addServerSystem(ServerSystemForm form) {
        int partnerId = ctx.insertInto(SERVER_SYSTEM)
                           .set(SERVER_SYSTEM.PARTNER_NAME, form.getPartnerName())
                           .set(SERVER_SYSTEM.BASE_PATH, form.getBasePath())
                           .set(SERVER_SYSTEM.NUMBER_OF_CONNECTIONS, form.getNumberOfConnections())
                           .set(SERVER_SYSTEM.ENABLED, form.isEnabled())
                           .returning(SERVER_SYSTEM.PARTNER_ID)
                           .fetchOne()
                           .getPartnerId();

        form.setPartnerId(partnerId);
        insertFeatures(form);
    }

    @Override
    @Transactional
    public void updateServerSystem(ServerSystemForm form) {
        ctx.update(SERVER_SYSTEM)
           .set(SERVER_SYSTEM.PARTNER_NAME, form.getPartnerName())
           .set(SERVER_SYSTEM.BASE_PATH, form.getBasePath())
           .set(SERVER_SYSTEM.NUMBER_OF_CONNECTIONS, form.getNumberOfConnections())
           .set(SERVER_SYSTEM.ENABLED, form.isEnabled())
           .where(SERVER_SYSTEM.PARTNER_ID.eq(form.getPartnerId()))
           .execute();

        ctx.delete(SERVER_FEATURE)
           .where(SERVER_FEATURE.PARTNER_ID.eq(form.getPartnerId()))
           .execute();

        insertFeatures(form);
    }

    @Override
    public void deleteServerSystem(int partnerId) {
        ctx.delete(SERVER_FEATURE)
           .where(SERVER_FEATURE.PARTNER_ID.eq(partnerId))
           .execute();

        ctx.delete(SERVER_SYSTEM)
           .where(SERVER_SYSTEM.PARTNER_ID.eq(partnerId))
           .execute();
    }

    private void insertFeatures(ServerSystemForm form) {
        ctx.batchInsert(
                form.getIxsiFeatures()
                    .stream()
                    .map(f ->
                        ctx.newRecord(SERVER_FEATURE)
                           .setPartnerId(form.getPartnerId())
                           .setFeatureGroup(f.getGroup().name())
                           .setFeatureName(f.getName()))
                    .collect(toList()))
           .execute();
    }

}
