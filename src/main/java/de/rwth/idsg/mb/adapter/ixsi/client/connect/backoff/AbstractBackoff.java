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
package de.rwth.idsg.mb.adapter.ixsi.client.connect.backoff;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 15.10.2015
 */
public abstract class AbstractBackoff implements Backoff {

    private static final int RETRY_COUNT = 50;

    private int counter;
    private long retryDelay;

    public AbstractBackoff(long initialRetryDelay) {
        retryDelay = initialRetryDelay;
        counter = 0;
    }

    protected void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    @Override
    public boolean willRetry() {
        return counter < RETRY_COUNT;
    }

    @Override
    public void incrementCounter() {
        counter++;
    }

    @Override
    public abstract void calculateDelay();

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public int getCounter() {
        return this.counter;
    }

    public long getRetryDelay() {
        return this.retryDelay;
    }

    public int getRetryCount() {
        return RETRY_COUNT;
    }
}
