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
package de.rwth.idsg.mb.notification;

import de.rwth.idsg.mb.AppConfiguration;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.MessagingException;

/**
 * http://crunchify.com/java-mailapi-example-send-an-email-via-gmail-smtp/
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 11.01.2016
 */
@Slf4j
@ApplicationScoped
public class Mailer {

    @Resource private ManagedScheduledExecutorService scheduler;
    @Inject private AppConfiguration appConfig;

    private NoticationStrategy noticationStrategy;

    @PostConstruct
    public void init() {
        if (appConfig.getMail().isEnabled()) {
            enable();
        } else {
            disable();
        }
    }

    public void disconnectedAll(PartnerContext ctx) {
        String subject = "Disconnected from partner " + ctx.getConfig().getPartnerName();

        noticationStrategy.notify(subject, null);
    }

    public void connectedAll(PartnerContext ctx) {
        String subject = "Connected to partner " + ctx.getConfig().getPartnerName();

        noticationStrategy.notify(subject, null);
    }

    public void cannotConnect(PartnerContext ctx, String errorMessage) {
        String subject = "Cannot connect to partner " + ctx.getConfig().getPartnerName();
        String body = "Reason: " + errorMessage;

        noticationStrategy.notify(subject, body);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void enable() {
        try {
            EnabledNotificationStrategy temp = new EnabledNotificationStrategy(scheduler, appConfig.getMail());
            temp.testConnection();
            noticationStrategy = temp;
        } catch (MessagingException e) {
            log.warn("Mail connection test failed, because '{}: {}'. Disabling mail notifications",
                    e.getClass().getSimpleName(), e.getMessage());
            disable();
        }
    }

    private void disable() {
        noticationStrategy = new DisabledNotificationStrategy();
    }
}
