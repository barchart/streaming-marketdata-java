package com.barchart.common.transport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum SocketConnectionState {
	Connecting("Connecting"),
	Connected("Connected"),
	Disconnecting("Disconnecting"),
	Disconnected("Disconnected");
	
	private String description;

	private final static Map<SocketConnectionState, Set<SocketConnectionState>> transitions;
	
	static {
		transitions = new HashMap<SocketConnectionState, Set<SocketConnectionState>>();
		
		for (SocketConnectionState c : SocketConnectionState.values()) {
			transitions.put(c, new HashSet<SocketConnectionState>());
		}
		
		transitions.get(SocketConnectionState.Connecting).add(SocketConnectionState.Connected);
		transitions.get(SocketConnectionState.Connecting).add(SocketConnectionState.Disconnecting);
		transitions.get(SocketConnectionState.Connecting).add(SocketConnectionState.Disconnected);
		transitions.get(SocketConnectionState.Connected).add(SocketConnectionState.Disconnecting);
		transitions.get(SocketConnectionState.Connected).add(SocketConnectionState.Disconnected);
		transitions.get(SocketConnectionState.Connected).add(SocketConnectionState.Connecting);
		transitions.get(SocketConnectionState.Disconnecting).add(SocketConnectionState.Disconnected);
		transitions.get(SocketConnectionState.Disconnected).add(SocketConnectionState.Connecting);
	}
	
	SocketConnectionState(final String description) {
		this.description = description;
	}
	
	public final String getDescription() {
		return description;
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
		return String.format("[ConnectionState (description: %s)]", description);
	}
}
