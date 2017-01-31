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
package de.rwth.idsg.mb.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.12.2014
 */
@Slf4j
@Getter
@Setter
public class ItemIdComparator<T> {

    private List<T> newList;
    private List<T> databaseList;

    // -------------------------------------------------------------------------

    public List<T> getForUpdate() {
        Set<T> intersection = new HashSet<>(newList);
        intersection.retainAll(databaseList);
        log.debug("forUpdate: {}", intersection.size());
        return new ArrayList<>(intersection);
    }

    public List<T> getForInsert() {
        Set<T> difference = new HashSet<>(newList);
        difference.removeAll(databaseList);
        log.debug("forInsert: {}", difference.size());
        return new ArrayList<>(difference);
    }

    public List<T> getForDelete() {
        Set<T> difference = new HashSet<>(databaseList);
        difference.removeAll(newList);
        log.debug("forDelete: {}", difference.size());
        return new ArrayList<>(difference);
    }
}
