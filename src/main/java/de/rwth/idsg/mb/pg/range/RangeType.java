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

import lombok.Getter;

import static de.rwth.idsg.mb.pg.range.RangeAlphabet.CLOSE_EXC;
import static de.rwth.idsg.mb.pg.range.RangeAlphabet.CLOSE_INC;
import static de.rwth.idsg.mb.pg.range.RangeAlphabet.OPEN_EXC;
import static de.rwth.idsg.mb.pg.range.RangeAlphabet.OPEN_INC;

/**
 * http://www.postgresql.org/docs/9.4/static/rangetypes.html
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 03.08.2015
 */
public enum RangeType {
    INC_INC(OPEN_INC, CLOSE_INC),
    INC_EXC(OPEN_INC, CLOSE_EXC),
    EXC_INC(OPEN_EXC, CLOSE_INC),
    EXC_EXC(OPEN_EXC, CLOSE_EXC),
    EMPTY(null, null);

    @Getter private final String start;
    @Getter private final String end;

    RangeType(String start, String end) {
        this.start = start;
        this.end = end;
    }

}
