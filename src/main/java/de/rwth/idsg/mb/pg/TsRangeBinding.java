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

import de.rwth.idsg.mb.pg.range.EmptyTsRange;
import de.rwth.idsg.mb.pg.range.ExcExcTsRange;
import de.rwth.idsg.mb.pg.range.ExcIncTsRange;
import de.rwth.idsg.mb.pg.range.IncExcTsRange;
import de.rwth.idsg.mb.pg.range.IncIncTsRange;
import de.rwth.idsg.mb.pg.range.RangeType;
import lombok.extern.slf4j.Slf4j;
import org.jooq.BindingSQLContext;
import org.jooq.Converter;
import org.jooq.RenderContext;
import org.jooq.impl.DSL;
import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;

import java.sql.SQLException;
import java.sql.Timestamp;

import static de.rwth.idsg.mb.pg.range.RangeAlphabet.CLOSE_EXC;
import static de.rwth.idsg.mb.pg.range.RangeAlphabet.CLOSE_INC;
import static de.rwth.idsg.mb.pg.range.RangeAlphabet.COMMA;
import static de.rwth.idsg.mb.pg.range.RangeAlphabet.EMPTY_PATTERN;
import static de.rwth.idsg.mb.pg.range.RangeAlphabet.OPEN_EXC;
import static de.rwth.idsg.mb.pg.range.RangeAlphabet.OPEN_INC;
import static de.rwth.idsg.mb.pg.range.RangeAlphabet.QUOTE;
import static de.rwth.idsg.mb.pg.range.RangeType.EMPTY;
import static de.rwth.idsg.mb.pg.range.RangeType.EXC_EXC;
import static de.rwth.idsg.mb.pg.range.RangeType.EXC_INC;
import static de.rwth.idsg.mb.pg.range.RangeType.INC_EXC;
import static de.rwth.idsg.mb.pg.range.RangeType.INC_INC;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.07.2015
 */
@Slf4j
public class TsRangeBinding extends AbstractBinding<TsRange> {
    private static final long serialVersionUID = -5303549912885769275L;

    @Override
    public Converter<Object, TsRange> converter() {
        return new TsRangeConverter();
    }

    @Override
    public void sql(BindingSQLContext<TsRange> ctx) throws SQLException {
        ctx.render()
           .castMode(RenderContext.CastMode.NEVER)
           .visit(DSL.val(ctx.convert(converter()).value()))
           .sql("::tsrange"); // tell postgresql to cast
    }

    // -------------------------------------------------------------------------
    // Helper class
    // -------------------------------------------------------------------------

    private static class TsRangeConverter implements Converter<Object, TsRange> {
        private static final long serialVersionUID = -8497224591866398609L;

        @Override
        public TsRange from(Object databaseObject) {
            if (databaseObject == null) {
                return null;
            }

            String value = ((PGobject) databaseObject).getValue();

            if (EMPTY_PATTERN.equals(value)) {
                return new EmptyTsRange();
            }

            if (value.startsWith(OPEN_EXC)) {
                if (value.endsWith(CLOSE_EXC)) {
                    return buildObject(EXC_EXC, new PGtokenizer(trimFirstLast(value), COMMA));

                } else if (value.endsWith(CLOSE_INC)) {
                    return buildObject(EXC_INC, new PGtokenizer(trimFirstLast(value), COMMA));
                }
            }

            if (value.startsWith(OPEN_INC)) {
                if (value.endsWith(CLOSE_EXC)) {
                    return buildObject(INC_EXC, new PGtokenizer(trimFirstLast(value), COMMA));

                } else if (value.endsWith(CLOSE_INC)) {
                    return buildObject(INC_INC, new PGtokenizer(trimFirstLast(value), COMMA));
                }
            }

            return null;
        }

        @Override
        public Object to(TsRange userObject) {
            if (userObject == null) {
                return null;

            } else if (EMPTY == userObject.getType()) {
                return "empty";

            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(userObject.getType().getStart());

                if (userObject.isBeginSet()) {
                    sb.append(QUOTE)
                      .append(userObject.getBegin().toString())
                      .append(QUOTE);
                }

                sb.append(COMMA);

                if (userObject.isEndSet()) {
                    sb.append(QUOTE)
                      .append(userObject.getEnd().toString())
                      .append(QUOTE);
                }

                sb.append(userObject.getType().getEnd());

                return sb.toString();
            }
        }

        @Override
        public Class<Object> fromType() {
            return Object.class;
        }

        @Override
        public Class<TsRange> toType() {
            return TsRange.class;
        }
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private static TsRange buildObject(RangeType rangeType, PGtokenizer t) {

        String beginString = t.getToken(0);
        String endString = t.getToken(1);

        // Remove quotation marks surrounding the timestamps first
        //
        Timestamp begin = (beginString == null) ? null : Timestamp.valueOf(trimFirstLast(beginString, QUOTE));
        Timestamp end   = (endString   == null) ? null : Timestamp.valueOf(trimFirstLast(endString, QUOTE));

        switch (rangeType) {
            case EXC_EXC : return new ExcExcTsRange(begin, end);
            case EXC_INC : return new ExcIncTsRange(begin, end);
            case INC_EXC : return new IncExcTsRange(begin, end);
            case INC_INC : return new IncIncTsRange(begin, end);
            default      : return null;
        }
    }

    /**
     * This removes the FIRST and LAST characters from a string
     */
    private static String trimFirstLast(String s) {
        if (s == null) {
            return null;
        } else {
            return s.substring(1, s.length() - 1);
        }
    }

    private static String trimFirstLast(String s, String toRemove) {
        return PGtokenizer.remove(s, toRemove, toRemove);
    }
}
