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

import de.rwth.idsg.mb.adapter.ixsi.repository.PlaceGroupRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.PlaceGroupPlaceWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.PlaceGroupWorker;
import de.rwth.idsg.mb.utils.ItemIdComparator;
import jooq.db.ixsi.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import xjc.schema.ixsi.PlaceGroupType;
import xjc.schema.ixsi.ProbabilityPlaceIDType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static jooq.db.ixsi.tables.Placegroup.PLACEGROUP;

/**
 * Created by max on 08/12/14.
 */
@Slf4j
@Stateless
public class PlaceGroupRepositoryImpl implements PlaceGroupRepository {

    @Inject private DSLContext ctx;

    @Override
    public List<String> getIds(int partnerId) {
        return ctx.select(PLACEGROUP.PLACEGROUP_ID)
                  .from(PLACEGROUP)
                  .where(PLACEGROUP.PARTNER_ID.eq(partnerId))
                  .fetch(PLACEGROUP.PLACEGROUP_ID);
    }

    @Override
    @Transactional
    public void upsertPlaceGroupList(int partnerId, final List<PlaceGroupType> placeGroupList) {
        log.debug("Size of the arrived place group list: {}", placeGroupList.size());

        List<String> dbList = getIds(partnerId);

        // -------------------------------------------------------------------------
        // Prepare environment
        // -------------------------------------------------------------------------

        PlaceGroupWorker placeGroupWorker = new PlaceGroupWorker(ctx, dbList, partnerId);
        placeGroupWorker.prepare();

        PlaceGroupPlaceWorker placeGroupPlaceWorker = new PlaceGroupPlaceWorker(ctx);
        placeGroupPlaceWorker.prepare();

        // -------------------------------------------------------------------------
        // Bind values
        // -------------------------------------------------------------------------

        for (PlaceGroupType pg : placeGroupList) {
            placeGroupWorker.bind(pg);

            String placeGroupId = pg.getID();
            placeGroupPlaceWorker.bindDelete(placeGroupId);

            for (ProbabilityPlaceIDType pid : pg.getPlaceID()) {
                placeGroupPlaceWorker.bindInsert(placeGroupId, pid);
            }
        }

        // -------------------------------------------------------------------------
        // Execute
        // -------------------------------------------------------------------------

        placeGroupWorker.execute();
        placeGroupPlaceWorker.execute();

        // -------------------------------------------------------------------------
        // Set others to inactive
        // -------------------------------------------------------------------------

        List<String> stringList = new ArrayList<>(placeGroupList.size());
        for (PlaceGroupType at : placeGroupList) {
            stringList.add(at.getID());
        }

        ItemIdComparator<String> comparator = new ItemIdComparator<>();
        comparator.setDatabaseList(dbList);
        comparator.setNewList(stringList);

        updateStatus(partnerId, comparator);
    }

    private void updateStatus(int partnerId, ItemIdComparator<String> comparator) {
        List<String> toDelete = comparator.getForDelete();

        ctx.update(PLACEGROUP)
           .set(PLACEGROUP.STATUS, Status.INACTIVE)
           .where(PLACEGROUP.PARTNER_ID.eq(partnerId))
           .and(PLACEGROUP.PLACEGROUP_ID.in(toDelete))
           .execute();
    }
}
