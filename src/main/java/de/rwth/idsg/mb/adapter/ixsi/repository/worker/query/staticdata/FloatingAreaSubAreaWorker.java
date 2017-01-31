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

import de.rwth.idsg.mb.adapter.ixsi.repository.worker.ChildWorker;
import de.rwth.idsg.mb.utils.BasicUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.postgresql.geometric.PGpolygon;
import xjc.schema.ixsi.DensityAreaType;

import java.util.ArrayList;
import java.util.List;

import static jooq.db.ixsi.tables.FloatingAreaSubArea.FLOATING_AREA_SUB_AREA;

/**
 * Created by max on 18/12/14.
 */
@Slf4j
@RequiredArgsConstructor
public class FloatingAreaSubAreaWorker implements ChildWorker<String, DensityAreaType> {
    private final DSLContext ctx;
    private BatchBindStep insertBatch;
    private List<String> itemIdList = new ArrayList<>();

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(FLOATING_AREA_SUB_AREA,
                        FLOATING_AREA_SUB_AREA.FLOATING_AREA_ID,
                        FLOATING_AREA_SUB_AREA.GPS_AREA,
                        FLOATING_AREA_SUB_AREA.DURATION_IN_SECONDS,
                        FLOATING_AREA_SUB_AREA.DURATION_VARIANCE_IN_SECONDS)
                   .values("", null, null, null)
        );
    }

    @Override
    public void bindDelete(String parentId) {
        itemIdList.add(parentId);
    }

    @Override
    public void bindInsert(String parentId, DensityAreaType item) {
        PGpolygon area = BasicUtils.toPolygon(item.getArea().getPolyPoint());

        insertBatch.bind(
                parentId,
                area,
                BasicUtils.toSeconds(item.getDuration()),
                BasicUtils.toSeconds(item.getVariance())
        );
    }

    /**
     * @deprecated  Use the individual bind methods instead
     */
    @Override
    @Deprecated
    public void bind(String parentId, DensityAreaType item) {
        // no-op
    }

    @Override
    public void execute() {
        if (itemIdList.isEmpty()) {
            log.debug("Nothing to write in table {}", FLOATING_AREA_SUB_AREA.getName());
        } else {
            ctx.delete(FLOATING_AREA_SUB_AREA)
               .where(FLOATING_AREA_SUB_AREA.FLOATING_AREA_ID.in(itemIdList))
               .execute();

            insertBatch.execute();
        }
    }
}
