/*
 * Copyright 2017, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.agent.protocol.serial;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import org.openremote.agent.protocol.ProtocolExecutorService;
import org.openremote.agent.protocol.io.AbstractNettyIoClient;
import org.openremote.agent.protocol.io.IoClient;
import org.openremote.model.util.TextUtil;

/**
 * This is a {@link IoClient} implementation for serial ports.
 */
public abstract class AbstractSerialClient<T> extends AbstractNettyIoClient<T, NrJavaSerialAddress> {

    protected String port;
    protected int baudRate;
    public static int DEFAULT_BAUD_RATE = 38400;

    public AbstractSerialClient(String port, Integer baudRate, ProtocolExecutorService executorService) {
        super(executorService);
        TextUtil.requireNonNullAndNonEmpty(port);
        this.port = port;
        this.baudRate = baudRate == null ? DEFAULT_BAUD_RATE : baudRate;
    }

    @Override
    protected Class<? extends Channel> getChannelClass() {
        return NrJavaSerialChannel.class;
    }

    @Override
    protected ChannelFuture startChannel() {
        return bootstrap.connect(new NrJavaSerialAddress(port, baudRate));
    }

    @Override
    protected String getSocketAddressString() {
        return "serial://" + port;
    }

    @Override
    protected io.netty.channel.EventLoopGroup getWorkerGroup() {
        return new NioEventLoopGroup(1);
    }
}