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
package de.rwth.idsg.mb.controller.parse;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 01.07.2015
 */
public class ValueTransformer implements Maps.EntryTransformer<String, List<String>, List<String>> {

    private static final InvalidStopTypePredicate VAL = new InvalidStopTypePredicate();
    private static final TrimReturnValuesPredicate TRIM = new TrimReturnValuesPredicate();

    @Override
    public List<String> transformEntry(String key, List<String> value) {
        switch (key) {
            case "ReturnList":
                return FluentIterable.from(value).filter(TRIM).toList();

            case "StopPointType":
                return FluentIterable.from(value).filter(VAL).toList();

            default:
                return value;
        }
    }
}
