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
	
	private String _description;
	
	private final boolean _canTransmit;
	private final boolean _canReceive;

	private final static Map<SocketConnectionState, Set<SocketConnectionState>> _transitions;
	
	static {
		_transitions = new HashMap<SocketConnectionState, Set<SocketConnectionState>>();
		
		for (SocketConnectionState c : SocketConnectionState.values()) {
			_transitions.put(c, new HashSet<SocketConnectionState>());
		}
		
		_transitions.get(SocketConnectionState.Uninitialized).add(SocketConnectionState.Initializing);
		_transitions.get(SocketConnectionState.Initializing).add(SocketConnectionState.Connecting);
		_transitions.get(SocketConnectionState.Connecting).add(SocketConnectionState.Connected);
		_transitions.get(SocketConnectionState.Connected).add(SocketConnectionState.Disconnecting);
		_transitions.get(SocketConnectionState.Connected).add(SocketConnectionState.Connecting);
		_transitions.get(SocketConnectionState.Disconnecting).add(SocketConnectionState.Disconnected);
		_transitions.get(SocketConnectionState.Disconnected).add(SocketConnectionState.Connecting);
	}
	
	SocketConnectionState(final String description, final boolean canTransmit, final boolean canReceive) {
		_description = description;
		
		_canTransmit = canTransmit;
		_canReceive = canTransmit;
	}
	
	public final String getDescription() {
		return _description;
	}
	
	public final boolean getCanTransmit() {
		return _canTransmit;
	}
	
	public final boolean getCanReceive() {
		return _canReceive;
	}
	
	public final boolean getCanConnect() {
		return canTransitionTo(SocketConnectionState.Connecting);
	}
	
	public final boolean getCanDisconnect() {
		return canTransitionTo(SocketConnectionState.Disconnecting);
	}
	
	public final boolean canTransitionTo(final SocketConnectionState connectionState) {
		return _transitions.get(this).contains(connectionState);
	}
	
	@Override
	public String toString() {
		return "[ConnectionState (description: " + _description + ")]";
	}
}
