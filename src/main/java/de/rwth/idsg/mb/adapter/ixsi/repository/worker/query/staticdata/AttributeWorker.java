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
import xjc.schema.ixsi.AttributeType;

import java.util.List;

import static de.rwth.idsg.mb.utils.IxsiConverterUtils.getClazzOrNull;
import static de.rwth.idsg.mb.utils.IxsiConverterUtils.getImportanceOrNull;
import static jooq.db.ixsi.tables.Attribute.ATTRIBUTE;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 17.12.2014
 */
@RequiredArgsConstructor
public class AttributeWorker implements ParentWorker<AttributeType> {
    private final DSLContext ctx;
    private final List<String> dbList;
    private final int partnerId;

    private BatchBindStep insertBatch;
    private BatchBindStep updateBatch;

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(ATTRIBUTE,
                        ATTRIBUTE.STATUS,
                        ATTRIBUTE.PARTNER_ID,
                        ATTRIBUTE.ATTRIBUTE_ID,
                        ATTRIBUTE.WITH_TEXT,
                        ATTRIBUTE.CLASS,
                        ATTRIBUTE.SEPARATE,
                        ATTRIBUTE.MANDATORY,
                        ATTRIBUTE.IMPORTANCE,
                        ATTRIBUTE.URL)
                   .values(null, 0, "", null, null, null, null, null, null)
        );

        updateBatch = ctx.batch(
                ctx.update(ATTRIBUTE)
                   .set(ATTRIBUTE.STATUS, (Status) null)
                   .set(ATTRIBUTE.WITH_TEXT, false)
                   .set(ATTRIBUTE.CLASS, "")
                   .set(ATTRIBUTE.SEPARATE, false)
                   .set(ATTRIBUTE.MANDATORY, false)
                   .set(ATTRIBUTE.IMPORTANCE, (Integer) null)
                   .set(ATTRIBUTE.URL, "")
                   .where(ATTRIBUTE.PARTNER_ID.equal(0))
                   .and(ATTRIBUTE.ATTRIBUTE_ID.equal(""))
        );
    }

    @Override
    public void bind(AttributeType item) {
        String attributeId = item.getID();

        Short importance = getImportanceOrNull(item);
        String clazz = getClazzOrNull(item);

        if (dbList.contains(attributeId)) {
            updateBatch.bind(
                    Status.ACTIVE,
                    item.isWithText(),
                    clazz,
                    item.isSeparate(),
                    item.isMandatory(),
                    importance,
                    item.getURL(),
                    partnerId,
                    item.getID()
            );
        } else {
            insertBatch.bind(
                    Status.ACTIVE,
                    partnerId,
                    item.getID(),
                    item.isWithText(),
                    clazz,
                    item.isSeparate(),
                    item.isMandatory(),
                    importance,
                    item.getURL()
            );
        }
    }

    @Override
    public void execute() {
        insertBatch.execute();
        updateBatch.execute();
    }

}
