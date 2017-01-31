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
package de.rwth.idsg.mb.adapter.ixsi.repository.worker;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.12.2014
 */
public interface ChildWorker<T1, T2> {
    void prepare();
    void bindDelete(T1 parentId);
    void bindInsert(T1 parentId, T2 item);
    void bind(T1 parentId, T2 item);
    void execute();
}
