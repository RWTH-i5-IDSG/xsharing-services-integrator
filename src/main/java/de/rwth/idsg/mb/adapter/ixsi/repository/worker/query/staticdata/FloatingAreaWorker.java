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
import de.rwth.idsg.mb.utils.BasicUtils;
import jooq.db.ixsi.enums.Status;
import lombok.RequiredArgsConstructor;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import xjc.schema.ixsi.FloatingAreaType;

import java.util.List;

import static jooq.db.ixsi.tables.FloatingArea.FLOATING_AREA;

/**
 * Created by max on 18/12/14.
 */
@RequiredArgsConstructor
public class FloatingAreaWorker implements ParentWorker<FloatingAreaType> {
    private final DSLContext ctx;
    private final List<String> dbList;

    private BatchBindStep insertBatch;
    private BatchBindStep updateBatch;

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(FLOATING_AREA,
                        FLOATING_AREA.STATUS,
                        FLOATING_AREA.FLOATING_AREA_ID,
                        FLOATING_AREA.PROVIDER_ID,
                        FLOATING_AREA.DURATION_IN_SECONDS,
                        FLOATING_AREA.DURATION_VARIANCE_IN_SECONDS)
                        .values(null, "", null, null, null)
        );

        updateBatch = ctx.batch(
                ctx.update(FLOATING_AREA)
                .set(FLOATING_AREA.STATUS, (Status) null)
                .set(FLOATING_AREA.PROVIDER_ID, "")
                .set(FLOATING_AREA.DURATION_IN_SECONDS, (Integer) null)
                .set(FLOATING_AREA.DURATION_VARIANCE_IN_SECONDS, (Integer) null)
                .where(FLOATING_AREA.FLOATING_AREA_ID.equal(""))
        );
    }

    @Override
    public void bind(FloatingAreaType item) {
        String floatingAreaId = item.getID();

        if (dbList.contains(floatingAreaId)) {
            updateBatch.bind(
                    Status.ACTIVE,
                    item.getProviderID(),
                    BasicUtils.toSeconds(item.getDuration()),
                    BasicUtils.toSeconds(item.getVariance()),
                    floatingAreaId
            );
        } else {
            insertBatch.bind(
                    Status.ACTIVE,
                    floatingAreaId,
                    item.getProviderID(),
                    BasicUtils.toSeconds(item.getDuration()),
                    BasicUtils.toSeconds(item.getVariance())
            );
        }
    }

    @Override
    public void execute() {
        insertBatch.execute();
        updateBatch.execute();
    }
}
