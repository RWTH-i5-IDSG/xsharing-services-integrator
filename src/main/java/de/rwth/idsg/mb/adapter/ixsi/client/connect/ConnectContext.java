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
package de.rwth.idsg.mb.adapter.ixsi.client.connect;

import de.rwth.idsg.mb.adapter.ixsi.client.connect.backoff.AbstractBackoff;
import de.rwth.idsg.mb.adapter.ixsi.client.connect.backoff.SteppingBackoff;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.DeploymentException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * For a partner with N number of connections defined, we will have N ConnectContext instances. Each instance
 * spawns ConnectJobs that do the actual connecting. ConnectContext is more of a coordinator between the
 * ConnectJobs.
 *
 * When a ConnectJob fails, ConnectContext can retry if the criteria is met (decided by Backoff) by scheduling
 * another ConnectJob. This also means, that it will spawn multiple, disposable ConnectJobs sequentially
 * until the connection is established, or we give up after a while.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 14.10.2015
 */
@Slf4j
@Getter
public class ConnectContext {
    private final long contextId;
    private final PartnerContext partnerContext;
    private final AbstractBackoff backoff;
    private final ConnectionManager manager;

    private static final String LOG_PREFIX = "[partnerName={}, contextId={}] Connection attempt {} of {} failed. ";

    private ScheduledFuture scheduledReconnectJob;

    public ConnectContext(ConnectionManager manager, PartnerContext partnerContext) {
        this.contextId = manager.getNextContextId();
        this.manager = manager;
        this.partnerContext = partnerContext;
        this.backoff = new SteppingBackoff();
    }

    public void start() {
        new ConnectJob(this).run();
    }

    public void connected() {
        manager.deleteContext(this);
        scheduledReconnectJob = null;
    }

    public void cancelReconnectJob() {
        if (scheduledReconnectJob != null) {
            log.info("Cancelling reconnect job for ConnectContext id={}", contextId);
            scheduledReconnectJob.cancel(false);
        }
    }

    public void handleDeploymentFail(DeploymentException e) {
        log.error("Exception occurred", e);
        manager.deleteContext(this);
        sendMail(e.getMessage());
    }

    public void handleFail(Exception e) {
        backoff.incrementCounter();

        if (backoff.willRetry()) {
            retry(e);
        } else {
            giveUp();
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void retry(Exception e) {
        backoff.calculateDelay();

        log.warn(LOG_PREFIX + "Exception: {}({}). Will try to reconnect in {} seconds",
                partnerContext.getConfig().getPartnerName(),
                contextId,
                backoff.getCounter(),
                backoff.getRetryCount(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                backoff.getRetryDelay() / 1000);

        // Schedule the next try
        scheduledReconnectJob = manager.getScheduler()
                                       .schedule(new ConnectJob(this), backoff.getRetryDelay(), TimeUnit.MILLISECONDS);
    }

    private void giveUp() {
        String msg = "Gave up, because reached the limit of retries";

        log.error(LOG_PREFIX + msg,
                partnerContext.getConfig().getPartnerName(), contextId,
                backoff.getCounter(), backoff.getRetryCount());

        manager.deleteContext(this);
        sendMail(msg);
    }

    private void sendMail(String msg) {
        manager.getMailer().cannotConnect(partnerContext, msg);
    }
}
