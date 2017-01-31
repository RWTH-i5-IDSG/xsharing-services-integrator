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

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.Timestamp;

import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.val;

/**
 * Current Postgres support of Jooq: {@link org.jooq.util.postgres.PostgresDSL}
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 22.07.2015
 */
public class CustomDSL {

    /**
     * https://www.postgresql.org/docs/current/static/functions-matching.html
     */
    public static Condition includes(Field<String> field, String input) {
        return field.likeIgnoreCase("%" + input.trim() + "%");
    }

    // -------------------------------------------------------------------------

    public static Condition elementContainedBy(Timestamp f1, TsRange f2) {
        return elementContainedBy(DSL.timestamp(f1), internalVal(f2));
    }

    public static Condition elementContainedBy(Timestamp f1, Field<TsRange> f2) {
        return elementContainedBy(DSL.timestamp(f1), f2);
    }

    public static Condition elementContainedBy(Field<Timestamp> f1, TsRange f2) {
        return elementContainedBy(f1, internalVal(f2));
    }

    public static Condition elementContainedBy(Field<Timestamp> f1, Field<TsRange> f2) {
        return condition("{0} :: TIMESTAMP <@ {1}", nullSafe(f1), nullSafe(f2));
    }

    // -------------------------------------------------------------------------

    public static Condition containedBy(TsRange f1, TsRange f2) {
        return containedBy(internalVal(f1), internalVal(f2));
    }

    public static Condition containedBy(TsRange f1, Field<TsRange> f2) {
        return containedBy(internalVal(f1), f2);
    }

    public static Condition containedBy(Field<TsRange> f1, TsRange f2) {
        return containedBy(f1, internalVal(f2));
    }

    public static <T> Condition containedBy(Field<T> f1, Field<T> f2) {
        return condition("{0} <@ {1}", nullSafe(f1), nullSafe(f2));
    }

    // -------------------------------------------------------------------------

    public static Condition overlaps(TsRange f1, TsRange f2) {
        return overlaps(internalVal(f1), internalVal(f2));
    }

    public static Condition overlaps(TsRange f1, Field<TsRange> f2) {
        return overlaps(internalVal(f1), f2);
    }

    public static Condition overlaps(Field<TsRange> f1, TsRange f2) {
        return overlaps(f1, internalVal(f2));
    }

    public static <T> Condition overlaps(Field<T> f1, Field<T> f2) {
        return condition("{0} && {1}", nullSafe(f1), nullSafe(f2));
    }

    // -------------------------------------------------------------------------

    public static Field<TsRange> difference(TsRange f1, TsRange f2) {
        return difference(internalVal(f1), internalVal(f2));
    }

    public static Field<TsRange> difference(TsRange f1, Field<TsRange> f2) {
        return difference(internalVal(f1), f2);
    }

    public static Field<TsRange> difference(Field<TsRange> f1, TsRange f2) {
        return difference(f1, internalVal(f2));
    }

    public static <T> Field<T> difference(Field<T> f1, Field<T> f2) {
        return field("{0} - {1}", nullSafe(f1).getDataType(), nullSafe(f1), nullSafe(f2));
    }

    // -------------------------------------------------------------------------

    public static Field<Timestamp> lower(TsRange f1) {
        return lower(internalVal(f1));
    }

    public static <T> Field<Timestamp> lower(Field<T> f1) {
        return field("lower({0})", org.jooq.impl.SQLDataType.TIMESTAMP, nullSafe(f1));
    }

    // -------------------------------------------------------------------------

    public static Field<Timestamp> upper(TsRange f1) {
        return upper(internalVal(f1));
    }

    public static <T> Field<Timestamp> upper(Field<T> f1) {
        return field("upper({0})", org.jooq.impl.SQLDataType.TIMESTAMP, nullSafe(f1));
    }

    // -------------------------------------------------------------------------

    public static Field<Boolean> isEmpty(TsRange f1) {
        return isEmpty(internalVal(f1));
    }

    public static <T> Field<Boolean> isEmpty(Field<T> f1) {
        return field("isEmpty({0})", org.jooq.impl.SQLDataType.BOOLEAN, nullSafe(f1));
    }

    // -------------------------------------------------------------------------

    public static Field<Timestamp> utcTimestamp() {
        return field("{now() at time zone 'UTC'}", SQLDataType.TIMESTAMP);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Field<TsRange> internalVal(TsRange value) {
        return DSL.field("tsrange({0}, {1})", TsRange.class, value.getBegin(), value.getEnd());
    }

    /**
     * Exact copy of {@link org.jooq.impl.DSL#nullSafe(org.jooq.Field)}
     */
    private static <T> Field<T> nullSafe(Field<T> field) {
        return field == null ? val((T) null) : field;
    }
}
