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

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 04.08.2015
 */
public final class RangeAlphabet {
    private RangeAlphabet() { }

    public static final String EMPTY_PATTERN = "empty";
    public static final String QUOTE = "\"";
    public static final char COMMA = ',';

    public static final String OPEN_INC     = "[";
    public static final String OPEN_EXC     = "(";
    public static final String CLOSE_INC    = "]";
    public static final String CLOSE_EXC    = ")";
}
