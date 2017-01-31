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

import static jooq.db.ixsi.tables.PlaceStopLink.PLACE_STOP_LINK;

/**
 * Created by max on 17/12/14.
 */
@Slf4j
@RequiredArgsConstructor
public class PlaceStopLinkWorker implements ChildWorker<String, StopLinkType> {
    private final DSLContext ctx;
    private BatchBindStep insertBatch;
    private List<String> itemIdList = new ArrayList<>();

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(PLACE_STOP_LINK,
                        PLACE_STOP_LINK.PLACE_ID,
                        PLACE_STOP_LINK.STOP_ID,
                        PLACE_STOP_LINK.DURATION_IN_SECONDS,
                        PLACE_STOP_LINK.DURATION_VARIANCE_IN_SECONDS,
                        PLACE_STOP_LINK.DISTANCE_IN_METERS)
                        .values("", null, null, null, null)
        );
    }

    @Override
    public void bindDelete(String parentId) {
        itemIdList.add(parentId);
    }

    @Override
    public void bindInsert(String parentId, StopLinkType item) {
        Integer distance = item.getDistance();

        insertBatch.bind(
                parentId,
                item.getStopID(),
                BasicUtils.toSeconds(item.getDuration()),
                BasicUtils.toSeconds(item.getVariance()),
                distance
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
            log.debug("Nothing to write in table {}", PLACE_STOP_LINK.getName());
        } else {
            ctx.delete(PLACE_STOP_LINK)
               .where(PLACE_STOP_LINK.PLACE_ID.in(itemIdList))
               .execute();

            insertBatch.execute();
        }
    }

}
