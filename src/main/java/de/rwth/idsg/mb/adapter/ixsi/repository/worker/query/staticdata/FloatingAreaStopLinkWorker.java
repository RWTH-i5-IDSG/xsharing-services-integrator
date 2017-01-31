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
import xjc.schema.ixsi.StopLinkType;

import java.util.ArrayList;
import java.util.List;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.getDistanceOrNull;
import static jooq.db.ixsi.tables.FloatingAreaStopLink.FLOATING_AREA_STOP_LINK;

/**
 * Created by max on 18/12/14.
 */
@Slf4j
@RequiredArgsConstructor
public class FloatingAreaStopLinkWorker implements ChildWorker<String, StopLinkType> {
    private final DSLContext ctx;
    private BatchBindStep insertBatch;
    private List<String> itemIdList = new ArrayList<>();

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(FLOATING_AREA_STOP_LINK,
                        FLOATING_AREA_STOP_LINK.STOP_ID,
                        FLOATING_AREA_STOP_LINK.DURATION_IN_SECONDS,
                        FLOATING_AREA_STOP_LINK.DURATION_VARIANCE_IN_SECONDS,
                        FLOATING_AREA_STOP_LINK.DISTANCE_IN_METERS,
                        FLOATING_AREA_STOP_LINK.FLOATING_AREA_ID)
                        .values("", null, null, null, null)
        );
    }

    @Override
    public void bindDelete(String parentId) {
        itemIdList.add(parentId);
    }

    @Override
    public void bindInsert(String parentId, StopLinkType item) {
        Short distance = getDistanceOrNull(item);

        insertBatch.bind(
                item.getStopID(),
                BasicUtils.toSeconds(item.getDuration()),
                BasicUtils.toSeconds(item.getVariance()),
                distance,
                parentId
        );
    }

    /**
     * @deprecated  Use the individual bind methods instead
     */
    @Override
    @Deprecated
    public void bind(String parentId, StopLinkType item) {
        // no-op
    }

    @Override
    public void execute() {
        if (itemIdList.isEmpty()) {
            log.debug("Nothing to write in table {}", FLOATING_AREA_STOP_LINK.getName());
        } else {
            ctx.delete(FLOATING_AREA_STOP_LINK)
               .where(FLOATING_AREA_STOP_LINK.FLOATING_AREA_ID.in(itemIdList))
               .execute();

            insertBatch.execute();
        }
    }

}
