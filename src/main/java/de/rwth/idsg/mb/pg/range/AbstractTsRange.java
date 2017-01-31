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
package de.rwth.idsg.mb.pg.range;

import de.rwth.idsg.mb.pg.TsRange;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.sql.Timestamp;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 03.08.2015
 */
@Getter
@ToString
public abstract class AbstractTsRange implements TsRange {

    private final RangeType type;
    private final Timestamp begin;
    private final Timestamp end;

    public AbstractTsRange(RangeType type) {
        this(null, null, type);
    }

    public AbstractTsRange(Timestamp begin, Timestamp end, RangeType type) {
        this.begin = begin;
        this.end = end;
        this.type = type;
    }

    /**
     * Strict check without short-circuit! Only true iff both are null.
     */
    @Override
    public boolean isEmpty() {
        return begin == null & end == null;
    }

    @Override
    public boolean isBeginSet() {
        return begin != null;
    }

    @Override
    public boolean isEndSet() {
        return end != null;
    }

    @Nullable
    @Override
    public Timestamp getBegin() {
        return begin;
    }

    @Nullable
    @Override
    public Timestamp getEnd() {
        return end;
    }
}
