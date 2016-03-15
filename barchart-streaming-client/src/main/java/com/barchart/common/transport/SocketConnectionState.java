package com.barchart.common.transport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum SocketConnectionState {
	Uninitialized("Uninitialized", false, false),
	Initializing("Initializing", false, false),
	Connecting("Connecting", false, false),
	Connected("Connected", true, true),
	Disconnecting("Disconnecting", false, false),
	Disconnected("Disconnected", false, false),
	Invalid("Invalid", false, false);
	
	private String description;
	
	private final boolean canTransmit;
	private final boolean canReceive;

	private final static Map<SocketConnectionState, Set<SocketConnectionState>> transitions;
	
	static {
		transitions = new HashMap<SocketConnectionState, Set<SocketConnectionState>>();
		
		for (SocketConnectionState c : SocketConnectionState.values()) {
			transitions.put(c, new HashSet<SocketConnectionState>());
		}
		
		transitions.get(SocketConnectionState.Uninitialized).add(SocketConnectionState.Initializing);
		transitions.get(SocketConnectionState.Initializing).add(SocketConnectionState.Connecting);
		transitions.get(SocketConnectionState.Connecting).add(SocketConnectionState.Connected);
		transitions.get(SocketConnectionState.Connected).add(SocketConnectionState.Disconnecting);
		transitions.get(SocketConnectionState.Connected).add(SocketConnectionState.Disconnected);
		transitions.get(SocketConnectionState.Connected).add(SocketConnectionState.Connecting);
		transitions.get(SocketConnectionState.Disconnecting).add(SocketConnectionState.Disconnected);
		transitions.get(SocketConnectionState.Disconnected).add(SocketConnectionState.Connecting);
	}
	
	SocketConnectionState(final String description, final boolean canTransmit, final boolean canReceive) {
		this.description = description;
		
		this.canTransmit = canTransmit;
		this.canReceive = canTransmit;
	}
	
	public final String getDescription() {
		return description;
	}
	
	public final boolean getCanTransmit() {
		return canTransmit;
	}
	
	public final boolean getCanReceive() {
		return canReceive;
	}
	
	public final boolean getCanConnect() {
		return canTransitionTo(SocketConnectionState.Connecting);
	}
	
	public final boolean getCanDisconnect() {
		return canTransitionTo(SocketConnectionState.Disconnecting);
	}
	
	public final boolean canTransitionTo(final SocketConnectionState connectionState) {
		return transitions.get(this).contains(connectionState);
	}
	
	@Override
	public String toString() {
		return "[ConnectionState (description: " + description + ")]";
	}
}
