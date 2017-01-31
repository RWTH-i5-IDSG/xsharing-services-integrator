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

import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;

/**
 * http://www.jooq.org/doc/3.5/manual/code-generation/custom-data-type-bindings/
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.07.2015
 */
public abstract class AbstractBinding<U> implements Binding<Object, U> {

    @Override
    public Converter<Object, U> converter() {
        // Override!
        return null;
    }

    @Override
    public void sql(BindingSQLContext<U> ctx) throws SQLException {
        // Override!
    }

    @Override
    public void register(BindingRegisterContext<U> ctx) throws SQLException {
        ctx.statement()
           .registerOutParameter(ctx.index(), Types.OTHER);
    }

    @Override
    public void set(BindingSetStatementContext<U> ctx) throws SQLException {
        ctx.statement()
           .setObject(ctx.index(), ctx.convert(converter()).value());
    }

    @Override
    public void get(BindingGetResultSetContext<U> ctx) throws SQLException {
        ctx.convert(converter())
           .value(ctx.resultSet().getObject(ctx.index()));
    }

    @Override
    public void get(BindingGetStatementContext<U> ctx) throws SQLException {
        ctx.convert(converter())
           .value(ctx.statement().getObject(ctx.index()));
    }

    // -------------------------------------------------------------------------
    // No-impl
    // -------------------------------------------------------------------------

    // Getting a value from a JDBC SQLInput (useful for Oracle OBJECT types)
    @Override
    public void get(BindingGetSQLInputContext<U> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    // Setting a value on a JDBC SQLOutput (useful for Oracle OBJECT types)
    @Override
    public void set(BindingSetSQLOutputContext<U> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
