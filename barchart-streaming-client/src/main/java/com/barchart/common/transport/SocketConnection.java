package com.barchart.common.transport;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.common.Action;
import com.barchart.common.Disposable;
import com.barchart.common.messaging.Event;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public abstract class SocketConnection implements Disposable {
	private static final Logger logger;

	private final String host;
	private final int port;
	private final boolean secure;

	private SocketConnector socketConnector;
	private SocketConnectionState connectionState;
	private final Object connectionLock;
	
	private final Event<SocketConnectionState> connectionStateChanged;
	
	private final Map<SocketSubscription, Disposable> subscriptions;
	
	static {
		logger = LoggerFactory.getLogger(SocketConnection.class);
	}
	
	public SocketConnection(final String host, final int port, final boolean secure) {
		if (host == null) {
			throw new IllegalArgumentException("The \"host\" argument is required.");
		}
		
		if (port < 0 || port > 65536) {
			throw new IllegalArgumentException("The \"port\" is not a valid TCP port number.");
		}
		
		this.host = host;
		this.port = port;
		this.secure = secure;
		
		this.socketConnector = null;
		this.connectionState = SocketConnectionState.Disconnected;
		this.connectionLock = new Object();
		
		this.connectionStateChanged = new Event<SocketConnectionState>("connectionStateChanged");
		
		this.subscriptions = new LinkedHashMap<SocketSubscription, Disposable>();
		
		createSocketConnector();
	}
	
	public final void connect() {
		synchronized (connectionLock) {
			if (changeConnectionState(SocketConnectionState.Connecting)) {				
				socketConnector.connect();
			}
		}
	}
	
	public final void disconnect() {
		synchronized (connectionLock) {
			if (changeConnectionState(SocketConnectionState.Disconnecting)) {
				socketConnector.disconnect();
			}
		}
	}
	
	protected void onConnectionStateChanged(final SocketConnectionState connectionState) {
		return;
	}

	public final Disposable registerConnectionStateChangeObserver(Action<SocketConnectionState> observer) {
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument is required.");
		}
		
		return connectionStateChanged.register(observer);
	}
	
	protected final Disposable registerSocketEventListener(final SocketChannel socketChannel, final Emitter.Listener listener) {
		final Disposable returnRef;
		
		synchronized (connectionLock) {
			final SocketSubscription subscription = new SocketSubscription(socketChannel, listener);
			
			if (!subscriptions.containsKey(subscription)) {
				final Disposable connectorBinding = socketConnector.registerSocketEventListener(socketChannel, listener);
				
				final Disposable compositeBinding = new Disposable() {
					@Override
					public void dispose() {
						synchronized(connectionLock) {
							connectorBinding.dispose();
						
							subscriptions.remove(subscription);
						}
					}
				};
				
				subscriptions.put(subscription, compositeBinding);
			}
			
			returnRef = subscriptions.get(subscription);
		}
		
		return returnRef;
	}
	
	protected final void sendToServer(final SocketChannel socketChannel, final JSONObject data) {
		synchronized (connectionLock) {
			socketConnector.sendToServer(socketChannel, data);
		}
	}
	
	protected final void requestFromServer(final SocketChannel socketChannel, final JSONObject data, final Action<JSONObject> callback) {
		synchronized (connectionLock) {
			socketConnector.requestFromServer(socketChannel, data, callback);
		}
	}
    
	private boolean changeConnectionState(final SocketConnectionState targetState) {
		boolean returnVal = connectionState != targetState && connectionState.canTransitionTo(targetState);
		
		if (returnVal) {
			logger.debug("Changing socket connection state to {}.", targetState);
			
			onConnectionStateChanged(connectionState = targetState);
			
			connectionStateChanged.fire(targetState);
			
			logger.debug("Changed socket connection state to {}", targetState);
		}
		
		return returnVal;
	}

	private void createSocketConnector() {
		synchronized (connectionLock) {
			if (socketConnector != null) {
				socketConnector.dispose();
			}
			
			socketConnector = new SocketConnector(host, port, secure);
			
			final Action<String> socketStateChangedObserver = new Action<String>() {
				@Override
				public void execute(String socketState) {
					synchronized (connectionLock) {
						switch (socketState) {
							case Socket.EVENT_CONNECT: {
								changeConnectionState(SocketConnectionState.Connected);
								break;
							}
							case Socket.EVENT_RECONNECTING: {
								changeConnectionState(SocketConnectionState.Connecting);
								break;
							}
							case Socket.EVENT_RECONNECT: {
								changeConnectionState(SocketConnectionState.Connected);
								break;
							}
							case Socket.EVENT_DISCONNECT: {
								changeConnectionState(SocketConnectionState.Disconnected);
								break;
							}
							case Socket.EVENT_RECONNECT_FAILED: {
								logger.warn("{} repeatedly failed to reconnect. Recreating socket connector.", socketConnector);
								
								createSocketConnector();
								
								break;
							}
							default: {
								logger.warn("Socket connection ignored underlying socket state change {}.", socketState);
								break;
							}
						}
					}
				}
			};
			
			socketConnector.registerSocketStateChangedObserver(socketStateChangedObserver);
			
			final SocketSubscription[] copies = subscriptions.keySet().toArray(new SocketSubscription[0]);
			
			for (int i = 0; i < copies.length; i++) {
				SocketSubscription subscription = copies[i];
				
				subscriptions.get(subscription).dispose();
				
				registerSocketEventListener(subscription.getSocketChannel(), subscription.getListener());
			}
			
			if (connectionState == SocketConnectionState.Connecting || connectionState == SocketConnectionState.Connected) {
				changeConnectionState(SocketConnectionState.Connecting);
				
				socketConnector.connect();
			} else {
				changeConnectionState(SocketConnectionState.Disconnected);
			}
		}
	}
	
	@Override
	public void dispose() {
		return;
	}
	
	@Override
	public String toString() {
		return String.format("[SocketConnection (host: %s, port: %s, secure: %s)]", host, port, secure);
	}
	
	private final class SocketSubscription {
		private final SocketChannel socketChannel;
		private final Emitter.Listener listener;
		
		public SocketSubscription(final SocketChannel socketChannel, final Emitter.Listener listener) {
			if (socketChannel == null) {
				throw new IllegalArgumentException("The \"socketChannel\" argument is required.");
			}
			
			if (listener == null) {
				throw new IllegalArgumentException("The \"listener\" argument is required.");
			}
			
			this.socketChannel = socketChannel;
			this.listener = listener;
		}
		
		public SocketChannel getSocketChannel() {
			return socketChannel;
		}
		
		public Emitter.Listener getListener() {
			return listener;
		}

		@Override
		public int hashCode() {
			return socketChannel.hashCode() * listener.hashCode() * 31;
		}

		@Override
		public boolean equals(Object candidate) {
			final boolean returnVal;
			
			if (candidate == this) {
				returnVal = true;
			} else if (candidate instanceof SocketSubscription) {
				final SocketSubscription typedCandidate = (SocketSubscription)candidate;
				
				returnVal = socketChannel.equals(typedCandidate.getSocketChannel()) && listener.equals(typedCandidate.getListener());
			} else {
				returnVal = false;
			}
			
			return returnVal;
		}
	}
}
