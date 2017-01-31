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
import jooq.db.ixsi.enums.Language;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import xjc.schema.ixsi.TextType;

import java.util.ArrayList;
import java.util.List;

import static jooq.db.ixsi.tables.FloatingAreaDescription.FLOATING_AREA_DESCRIPTION;

/**
 * Created by max on 18/12/14.
 */
@Slf4j
@RequiredArgsConstructor
public class FloatingAreaDescriptionWorker implements ChildWorker<String, TextType> {
    private final DSLContext ctx;
    private BatchBindStep insertBatch;
    private List<String> itemIdList = new ArrayList<>();

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(FLOATING_AREA_DESCRIPTION,
                        FLOATING_AREA_DESCRIPTION.FLOATING_AREA_ID,
                        FLOATING_AREA_DESCRIPTION.LANGUAGE,
                        FLOATING_AREA_DESCRIPTION.VALUE)
                   .values("", null, null)
        );
    }

    @Override
    public void bindDelete(String parentId) {
        itemIdList.add(parentId);
    }

    @Override
    public void bindInsert(String parentId, TextType item) {
        Language lng = BasicUtils.getDatabaseLangOrDefault(item.getLanguage());

        insertBatch.bind(
                parentId,
                lng,
                item.getText()
        );
    }

    /**
     * @deprecated Use the individual bind methods instead
     */
    @Override
    @Deprecated
    public void bind(String parentId, TextType item) {
        // no-op
    }

    @Override
    public void execute() {
        if (itemIdList.isEmpty()) {
            log.debug("Nothing to write in table {}", FLOATING_AREA_DESCRIPTION.getName());
        } else {
            ctx.delete(FLOATING_AREA_DESCRIPTION)
               .where(FLOATING_AREA_DESCRIPTION.FLOATING_AREA_ID.in(itemIdList))
               .execute();

            insertBatch.execute();
        }
    }
}
