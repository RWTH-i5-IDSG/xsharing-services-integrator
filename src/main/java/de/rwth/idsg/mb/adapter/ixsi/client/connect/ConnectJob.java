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

import de.rwth.idsg.mb.adapter.ixsi.client.WebSocketClientEndpoint;
import lombok.RequiredArgsConstructor;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;

/**
 * This is a one-time, throw away connect trial. When it succeeds, every thing is fine.
 * When it fails, the parent ConnectContext is informed to decide what to do.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 14.10.2015
 */
@RequiredArgsConstructor
public class ConnectJob implements Runnable {
    private final ConnectContext cc;

    @Override
    public void run() {
        try {
            WebSocketClientEndpoint e = new WebSocketClientEndpoint(cc.getPartnerContext());
            String serverPath = cc.getPartnerContext().getConfig().getBasePath();

            cc.getManager()
              .getWebSocketContainer()
              .connectToServer(e, URI.create(serverPath));

            cc.connected();

        } catch (DeploymentException | IOException e) {
            cc.handleFail(e);
        }
    }
}
