/*
 * Copyright 2020, OpenRemote Inc.
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
package org.openremote.agent.protocol.knx;

import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.ValueDescriptor;
import org.openremote.model.value.ValueType;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Optional;

@Entity
public class KNXAgent extends Agent<KNXAgent, KNXProtocol, KNXAgent.KNXAgentLink> {

    public static class KNXAgentLink extends AgentLink<KNXAgentLink> {

        @NotNull
        @Pattern(regexp = "^\\d{1,3}\\.\\d{1,3}$")
        protected String dpt;

        @Pattern(regexp = "^\\d{1,3}/\\d{1,3}/\\d{1,3}$")
        protected String actionGroupAddress;

        @Pattern(regexp = "^\\d{1,3}/\\d{1,3}/\\d{1,3}$")
        protected String statusGroupAddress;

        // For Hydrators
        protected KNXAgentLink() {}

        public KNXAgentLink(String id, String dpt, String actionGroupAddress, String statusGroupAddress) {
            super(id);
            this.dpt = dpt;
            this.actionGroupAddress = actionGroupAddress;
            this.statusGroupAddress = statusGroupAddress;
        }

        public Optional<String> getDpt() {
            return Optional.ofNullable(dpt);
        }

        public KNXAgentLink setDpt(String dpt) {
            this.dpt = dpt;
            return this;
        }

        public Optional<String> getActionGroupAddress() {
            return Optional.ofNullable(actionGroupAddress);
        }

        public KNXAgentLink setActionGroupAddress(String actionGroupAddress) {
            this.actionGroupAddress = actionGroupAddress;
            return this;
        }

        public Optional<String> getStatusGroupAddress() {
            return Optional.ofNullable(statusGroupAddress);
        }

        public KNXAgentLink setStatusGroupAddress(String statusGroupAddress) {
            this.statusGroupAddress = statusGroupAddress;
            return this;
        }
    }

    @Pattern(regexp = "^\\d\\.\\d\\.\\d$")
    public static final ValueDescriptor<String> SOURCE_ADDRESS_VALUE = new ValueDescriptor<>("kNXMessageSourceAddress", String.class);

    public static final AttributeDescriptor<String> GATEWAY = HOST.withOptional(false);
    public static final AttributeDescriptor<Boolean> NAT_MODE = new AttributeDescriptor<>("NATMode", ValueType.BOOLEAN);
    public static final AttributeDescriptor<Boolean> ROUTING_MODE = new AttributeDescriptor<>("routingMode", ValueType.BOOLEAN);
    public static final AttributeDescriptor<String> MESSAGE_SOURCE_ADDRESS = new AttributeDescriptor<>("messageSourceAddress", SOURCE_ADDRESS_VALUE);

    public static final AgentDescriptor<KNXAgent, KNXProtocol, KNXAgentLink> DESCRIPTOR = new AgentDescriptor<>(
        KNXAgent.class, KNXProtocol.class, KNXAgentLink.class
    );

    /**
     * For use by hydrators (i.e. JPA/Jackson)
     */
    protected KNXAgent() {
    }

    public KNXAgent(String name) {
        super(name);
    }

    public Optional<String> getMessageSourceAddress() {
        return getAttributes().getValue(MESSAGE_SOURCE_ADDRESS);
    }

    public KNXAgent setMessageSourceAddress(String value) {
        getAttributes().getOrCreate(MESSAGE_SOURCE_ADDRESS).setValue(value);
        return this;
    }

    public Optional<Boolean> isNATMode() {
        return getAttributes().getValue(NAT_MODE);
    }

    public KNXAgent setNATMode(Boolean value) {
        getAttributes().getOrCreate(NAT_MODE).setValue(value);
        return this;
    }

    public Optional<Boolean> isRoutingMode() {
        return getAttributes().getValue(ROUTING_MODE);
    }

    public KNXAgent setRoutingMode(Boolean value) {
        getAttributes().getOrCreate(ROUTING_MODE).setValue(value);
        return this;
    }

    @Override
    public KNXProtocol getProtocolInstance() {
        return new KNXProtocol(this);
    }
}
