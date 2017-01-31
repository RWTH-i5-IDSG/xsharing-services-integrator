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

import de.rwth.idsg.mb.adapter.ixsi.repository.FloatingAreaRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.FloatingAreaAreaWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.FloatingAreaAttributeWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.FloatingAreaDescriptionWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.FloatingAreaNameWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.FloatingAreaStopLinkWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.FloatingAreaSubAreaWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.FloatingAreaWorker;
import de.rwth.idsg.mb.utils.ItemIdComparator;
import jooq.db.ixsi.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import xjc.schema.ixsi.DensityAreaType;
import xjc.schema.ixsi.FloatingAreaType;
import xjc.schema.ixsi.IncExcGeoAreaType;
import xjc.schema.ixsi.StopLinkType;
import xjc.schema.ixsi.TextType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static jooq.db.ixsi.tables.FloatingArea.FLOATING_AREA;
import static jooq.db.ixsi.tables.Provider.PROVIDER;

/**
 * Created by max on 10/12/14.
 */
@Slf4j
@Stateless
public class FloatingAreaRepositoryImpl implements FloatingAreaRepository {

    @Inject private DSLContext ctx;

    @Override
    public List<String> getIds(int partnerId) {
        return ctx.select(FLOATING_AREA.FLOATING_AREA_ID)
                  .from(FLOATING_AREA)
                  .join(PROVIDER)
                  .on(PROVIDER.PROVIDER_ID.eq(FLOATING_AREA.PROVIDER_ID))
                  .and(PROVIDER.PARTNER_ID.eq(partnerId))
                  .fetch(FLOATING_AREA.FLOATING_AREA_ID);
    }

    @Override
    @Transactional
    public void upsertFloatingAreaList(int partnerId, final List<FloatingAreaType> floatingAreaList) {
        log.debug("Size of the arrived place list: {}", floatingAreaList.size());

        List<String> dbList = getIds(partnerId);

        // -------------------------------------------------------------------------
        // Prepare statements
        // -------------------------------------------------------------------------

        FloatingAreaWorker floatingAreaWorker = new FloatingAreaWorker(ctx, dbList);
        floatingAreaWorker.prepare();

        FloatingAreaAttributeWorker floatingAreaAttributeWorker = new FloatingAreaAttributeWorker(ctx);
        floatingAreaAttributeWorker.prepare();

        FloatingAreaDescriptionWorker floatingAreaDescriptionWorker = new FloatingAreaDescriptionWorker(ctx);
        floatingAreaDescriptionWorker.prepare();

        FloatingAreaAreaWorker floatingAreaAreaWorker = new FloatingAreaAreaWorker(ctx);
        floatingAreaAreaWorker.prepare();

        FloatingAreaNameWorker floatingAreaNameWorker = new FloatingAreaNameWorker(ctx);
        floatingAreaNameWorker.prepare();

        FloatingAreaStopLinkWorker floatingAreaStopLinkWorker = new FloatingAreaStopLinkWorker(ctx);
        floatingAreaStopLinkWorker.prepare();

        FloatingAreaSubAreaWorker floatingAreaSubAreaWorker = new FloatingAreaSubAreaWorker(ctx);
        floatingAreaSubAreaWorker.prepare();

        // -------------------------------------------------------------------------
        // Bind values
        // -------------------------------------------------------------------------

        for (FloatingAreaType floatingArea : floatingAreaList) {
            floatingAreaWorker.bind(floatingArea);
            String floatingAreaId = floatingArea.getID();

            if (floatingArea.isSetAttributeID()) {
                floatingAreaAttributeWorker.bindDelete(floatingAreaId);
                for (String att : floatingArea.getAttributeID()) {
                    floatingAreaAttributeWorker.bindInsert(floatingAreaId, att);
                }
            }

            if (floatingArea.isSetDescription()) {
                floatingAreaDescriptionWorker.bindDelete(floatingAreaId);
                for (TextType desc : floatingArea.getDescription()) {
                    floatingAreaDescriptionWorker.bindInsert(floatingAreaId, desc);
                }
            }

            if (floatingArea.isSetArea()) {
                floatingAreaAreaWorker.bindDelete(floatingAreaId);
                for (IncExcGeoAreaType geoArea : floatingArea.getArea()) {
                    floatingAreaAreaWorker.bindInsert(floatingAreaId, geoArea);
                }
            }

            if (floatingArea.isSetName()) {
                floatingAreaNameWorker.bindDelete(floatingAreaId);
                for (TextType name : floatingArea.getName()) {
                    floatingAreaNameWorker.bindInsert(floatingAreaId, name);
                }
            }

            if (floatingArea.isSetStopLink()) {
                floatingAreaStopLinkWorker.bindDelete(floatingAreaId);
                for (StopLinkType stopLink : floatingArea.getStopLink()) {
                    floatingAreaStopLinkWorker.bindInsert(floatingAreaId, stopLink);
                }
            }

            if (floatingArea.isSetSubArea()) {
                floatingAreaSubAreaWorker.bindDelete(floatingAreaId);
                for (DensityAreaType densityArea : floatingArea.getSubArea()) {
                    floatingAreaSubAreaWorker.bindInsert(floatingAreaId, densityArea);
                }
            }
        }

        // -------------------------------------------------------------------------
        // Execute - the order is important
        // -------------------------------------------------------------------------

        floatingAreaWorker.execute();
        floatingAreaAttributeWorker.execute();
        floatingAreaDescriptionWorker.execute();
        floatingAreaAreaWorker.execute();
        floatingAreaNameWorker.execute();
        floatingAreaStopLinkWorker.execute();
        floatingAreaSubAreaWorker.execute();

        // -------------------------------------------------------------------------
        // Set others to inactive
        // -------------------------------------------------------------------------

        List<String> stringList = new ArrayList<>(floatingAreaList.size());
        for (FloatingAreaType at : floatingAreaList) {
            stringList.add(at.getID());
        }

        ItemIdComparator<String> comparator = new ItemIdComparator<>();
        comparator.setDatabaseList(dbList);
        comparator.setNewList(stringList);

        updateStatus(partnerId, comparator);
    }

    private void updateStatus(int partnerId, ItemIdComparator<String> comparator) {
        List<String> toDelete = comparator.getForDelete();

        // TODO: theoretically not correct. we must also incorporate providerId in where clause

        ctx.update(FLOATING_AREA)
           .set(FLOATING_AREA.STATUS, Status.INACTIVE)
           .where(FLOATING_AREA.FLOATING_AREA_ID.in(toDelete))
           .execute();
    }
}
