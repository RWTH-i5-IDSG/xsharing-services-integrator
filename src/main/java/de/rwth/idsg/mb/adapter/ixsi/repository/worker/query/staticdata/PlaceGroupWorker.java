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
package de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata;

import de.rwth.idsg.mb.adapter.ixsi.repository.worker.ParentWorker;
import jooq.db.ixsi.enums.Status;
import lombok.RequiredArgsConstructor;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import xjc.schema.ixsi.PlaceGroupType;

import java.util.List;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.getProbabilityOrNull;
import static jooq.db.ixsi.tables.Placegroup.PLACEGROUP;

/**
 * Created by max on 17/12/14.
 */
@RequiredArgsConstructor
public class PlaceGroupWorker implements ParentWorker<PlaceGroupType> {
    private final DSLContext ctx;
    private final List<String> dbList;
    private final int partnerId;

    private BatchBindStep insertBatch;
    private BatchBindStep updateBatch;

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(PLACEGROUP,
                        PLACEGROUP.STATUS,
                        PLACEGROUP.PARTNER_ID,
                        PLACEGROUP.PLACEGROUP_ID,
                        PLACEGROUP.PROBABILITY)
                        .values(null, 0, "", null)
        );

        updateBatch = ctx.batch(
                ctx.update(PLACEGROUP)
                   .set(PLACEGROUP.STATUS, (Status) null)
                   .set(PLACEGROUP.PROBABILITY, (Integer) null)
                   .where(PLACEGROUP.PARTNER_ID.equal(0))
                   .and(PLACEGROUP.PLACEGROUP_ID.equal(""))
        );
    }

    @Override
    public void bind(PlaceGroupType item) {
        String placeGroupId = item.getID();

        if (dbList.contains(placeGroupId)) {
            updateBatch.bind(
                    Status.ACTIVE,
                    getProbabilityOrNull(item.getProbability()),
                    partnerId,
                    placeGroupId
            );
        } else {
            insertBatch.bind(
                    Status.ACTIVE,
                    partnerId,
                    placeGroupId,
                    getProbabilityOrNull(item.getProbability())
            );
        }
    }

    @Override
    public void execute() {
        insertBatch.execute();
        updateBatch.execute();
    }

}
