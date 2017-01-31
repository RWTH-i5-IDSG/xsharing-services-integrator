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
import xjc.schema.ixsi.IncExcGeoAreaType;

import java.util.ArrayList;
import java.util.List;

import static jooq.db.ixsi.tables.FloatingAreaArea.FLOATING_AREA_AREA;

/**
 * Created by max on 18/12/14.
 */
@Slf4j
@RequiredArgsConstructor
public class FloatingAreaAreaWorker implements ChildWorker<String, IncExcGeoAreaType> {
    private final DSLContext ctx;
    private BatchBindStep insertBatch;
    private List<String> itemIdList = new ArrayList<>();

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(FLOATING_AREA_AREA,
                        FLOATING_AREA_AREA.FLOATING_AREA_ID,
                        FLOATING_AREA_AREA.GPS_AREA,
                        FLOATING_AREA_AREA.IS_EXCLUDED)
                        .values("", null, null)
        );
    }

    @Override
    public void bindDelete(String parentId) {
        itemIdList.add(parentId);
    }

    @Override
    public void bindInsert(String parentId, IncExcGeoAreaType item) {
        PGpolygon area = BasicUtils.toPolygon(item.getPolyPoint());

        insertBatch.bind(
                parentId,
                area,
                item.isExclude()
        );
    }

    /**
     * @deprecated  Use the individual bind methods instead
     */
    @Override
    @Deprecated
    public void bind(String parentId, IncExcGeoAreaType item) {
        // no-op
    }

    @Override
    public void execute() {
        if (itemIdList.isEmpty()) {
            log.debug("Nothing to write in table {}", FLOATING_AREA_AREA.getName());
        } else {
            ctx.delete(FLOATING_AREA_AREA)
               .where(FLOATING_AREA_AREA.FLOATING_AREA_ID.in(itemIdList))
               .execute();

            insertBatch.execute();
        }
    }
}
