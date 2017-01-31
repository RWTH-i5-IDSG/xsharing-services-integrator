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
package de.rwth.idsg.mb.pg;

import lombok.extern.slf4j.Slf4j;
import org.jooq.BindingSQLContext;
import org.jooq.Converter;
import org.jooq.RenderContext;
import org.jooq.impl.DSL;
import org.postgresql.geometric.PGpoint;

import java.sql.SQLException;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 19.12.2014
 */
@Slf4j
public class PGpointBinding extends AbstractBinding<PGpoint> {
    private static final long serialVersionUID = 5914874423891557556L;

    @Override
    public Converter<Object, PGpoint> converter() {
        return new PGpointConverter();
    }

    @Override
    public void sql(BindingSQLContext<PGpoint> ctx) throws SQLException {
        ctx.render()
           .castMode(RenderContext.CastMode.NEVER)
           .visit(DSL.val(ctx.convert(converter()).value()))
           .sql("::point"); // tell postgresql to cast to point type
    }

    // -------------------------------------------------------------------------
    // Helper class
    // -------------------------------------------------------------------------

    private static class PGpointConverter implements Converter<Object, PGpoint> {
        private static final long serialVersionUID = -3311362924467571994L;

        @Override
        public PGpoint from(Object databaseObject) {
            if (databaseObject == null) {
                return null;
            } else {
                return (PGpoint) databaseObject;
            }
        }

        @Override
        public Object to(PGpoint userObject) {
            if (userObject == null) {
                return null;
            } else {
                return userObject.getValue();
            }
        }

        @Override
        public Class<Object> fromType() {
            return Object.class;
        }

        @Override
        public Class<PGpoint> toType() {
            return PGpoint.class;
        }
    }
}
