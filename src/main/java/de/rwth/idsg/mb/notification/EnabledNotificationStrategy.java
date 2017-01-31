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

import com.google.common.base.Strings;
import de.rwth.idsg.mb.AppConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 18.03.2016
 */
@Slf4j
public class EnabledNotificationStrategy implements NoticationStrategy {

    private final ManagedScheduledExecutorService scheduler;
    private final AppConfiguration.Mail mailConfig;
    private final Session session;

    public EnabledNotificationStrategy(ManagedScheduledExecutorService scheduler, AppConfiguration.Mail mailConfig) {
        this.scheduler = scheduler;
        this.mailConfig = mailConfig;
        this.session = createSession();
    }

    /**
     * @throws MessagingException   when the test fails
     */
    public void testConnection() throws MessagingException {
        Transport transport = session.getTransport();
        try {
            transport.connect();
        } finally {
            transport.close();
        }
    }

    @Override
    public void notify(String subject, @Nullable String body) {
        scheduler.execute(() -> sendMail(subject, addTimestamp(body)));
    }

    private static String addTimestamp(@Nullable String body) {
        String eventTs = "Timestamp of the event: " + DateTime.now().toString();
        String newLine = "\r\n\r\n";

        if (Strings.isNullOrEmpty(body)) {
            return eventTs;
        } else {
            return body + newLine + "--" + newLine + eventTs;
        }
    }

    private void sendMail(String subject, String body) {
        try {
            MimeMessage mail = new MimeMessage(session);
            mail.setSubject("[MB Adapter] " + subject);
            mail.setContent(body, "text/plain");
            mail.setFrom(new InternetAddress(mailConfig.getFrom()));

            for (String rep : mailConfig.getRecipients()) {
                mail.addRecipient(Message.RecipientType.TO, new InternetAddress(rep));
            }

            Transport transport = session.getTransport();
            try {
                transport.connect();
                transport.sendMessage(mail, mail.getAllRecipients());
            } finally {
                transport.close();
            }
        } catch (MessagingException e) {
            log.error("Failed to send mail", e);
        }
    }

    private Session createSession() {
        Properties props = new Properties();
        String protocol = mailConfig.getProtocol();

        props.setProperty("mail.host", "" + mailConfig.getHost());
        props.setProperty("mail.transport.protocol", "" + protocol);
        props.setProperty("mail." + protocol + ".port", "" + mailConfig.getPort());

        if (mailConfig.getPort() == 465) {
            props.setProperty("mail." + protocol + ".ssl.enable", "" + true);

        } else if (mailConfig.getPort() == 587) {
            props.setProperty("mail." + protocol + ".starttls.enable", "" + true);
        }

        boolean isUserSet = !Strings.isNullOrEmpty(mailConfig.getUsername());
        boolean isPassSet = !Strings.isNullOrEmpty(mailConfig.getPassword());

        if (isUserSet && isPassSet) {
            props.setProperty("mail." + protocol + ".auth", "" + true);
            return Session.getInstance(props, getAuth());

        } else {
            return Session.getInstance(props);
        }
    }

    private Authenticator getAuth() {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailConfig.getUsername(), mailConfig.getPassword());
            }
        };
    }
}

