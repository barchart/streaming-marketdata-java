package com.barchart.common.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.common.IAction;
import com.barchart.common.IDisposable;
import com.barchart.common.messaging.Event;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public abstract class SocketConnection implements IDisposable {
	private static final Logger logger;
	
	private static final AtomicInteger socketCounter;
	
	private final int _id;
	
	private final String _host;
	private final int _port;
	private final boolean _secure;
	
	private final Socket _socket;
	private final AtomicInteger _socketSequence;
	
	private SocketConnectionState _connectionState;
	private final Object _connectionLock;
	
	private final Event<SocketConnectionState> _connectionStateChanged;
	
	private final ConcurrentMap<String, IAction<JSONObject>> _requestMap;
	
	static {
		logger = LoggerFactory.getLogger(SocketConnection.class);
		
		socketCounter = new AtomicInteger(0);
	}
	
	public SocketConnection(final String host, final int port, final boolean secure) {
		if (host == null) {
			throw new IllegalArgumentException("The \"host\" argument is required.");
		}
		
		if (port < 0 || port > 65536) {
			throw new IllegalArgumentException("The \"port\" is not a valid TCP port number.");
		}
		
		_id = socketCounter.incrementAndGet();
		
		_host = host;
		_port = port;
		_secure = secure;
		
		Socket socket;
		
		logger.info("Creating socket.io connection to (host: %s, port: %s, secure: %s)", host, port, secure);
		
		final String serverUri = getServerUri(_host, _port, _secure);
		
		logger.info("Attempting to open socket.io connection to {}", serverUri);
		
		try {	
			socket = IO.socket(serverUri);
		} catch (URISyntaxException e) {
			logger.error("Socket URI is invalid", e);
			
			socket = null;
		}
		
		_socket = socket;
		_socketSequence = new AtomicInteger(0);
		
		_connectionState = _socket == null ? SocketConnectionState.Invalid : SocketConnectionState.Disconnected;
		_connectionLock = new Object();
		
		_connectionStateChanged = new Event<SocketConnectionState>("connectionStateChanged");
		
		_requestMap = new ConcurrentHashMap<String, IAction<JSONObject>>(16, 0.75f, 2);
		
		registerSocketEventListener(Socket.EVENT_CONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				logger.debug("A socket.io {} event occurred.", Socket.EVENT_CONNECT);
				
				changeConnectionState(SocketConnectionState.Connected);
			}
		});
		
		registerSocketEventListener(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				logger.debug("A socket.io {} event occurred.", Socket.EVENT_RECONNECTING);
				
				changeConnectionState(SocketConnectionState.Connecting);
			}
		});
		
		registerSocketEventListener(Socket.EVENT_RECONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				logger.debug("A socket.io {} event occurred.", Socket.EVENT_RECONNECT);
				
				changeConnectionState(SocketConnectionState.Connected);
			}
		});
		
		registerSocketEventListener(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				logger.debug("A socket.io {} event occurred.", Socket.EVENT_DISCONNECT);
				
				changeConnectionState(SocketConnectionState.Disconnected);
			}
		});
		
		registerSocketEventListener(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				logger.warn("A socket.io {} event occurred. Current state is {}. Error: {}.", Socket.EVENT_CONNECT_ERROR, _connectionState, args[0]);
			}
		});
		
		registerSocketEventListener(SocketChannel.Response, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				final JSONObject data = (JSONObject)args[0];
				
				final String requestId = data.optString("requestId");

				logMessageReceipt(SocketChannel.Response, data);
				
				IAction<JSONObject> responseHandler = _requestMap.remove(requestId);
				
				if (responseHandler != null) {
					responseHandler.execute(data.optJSONObject("response"));
				} else {
					logger.warn("Recieved response without corresponding request {}", requestId);
				}
			}
		});
	}
	
	public final void connect() {
		logger.debug("Staring manual connection attempt.");
		
		if (changeConnectionState(SocketConnectionState.Connecting, true)) {
			_socket.connect();
		}
	}
	
	public final void disconnect() {
		logger.debug("Staring manual disconnect attempt.");
		
		if (changeConnectionState(SocketConnectionState.Disconnecting, true)) {
			_socket.disconnect();
		}
	}
	
	protected void onConnectionStateChanged(final SocketConnectionState connectionState) {
		return;
	}

	public final IDisposable registerConnectionStateChangeObserver(IAction<SocketConnectionState> observer) {
		return _connectionStateChanged.register(observer);
	}
	
	protected final IDisposable registerSocketEventListener(final ISocketChannel socketChannel, final Emitter.Listener listener) {
		if (socketChannel == null) {
			throw new IllegalArgumentException("The \"socketChannel\" argument is required.");
		}
		
		if (listener == null) {
			throw new IllegalArgumentException("The \"listener\" argument is required.");
		}
		
		logger.debug("Registering listener for {}", socketChannel);
		
		return registerSocketEventListener(socketChannel.getChannelName(), listener);
	}
	
	protected final void unregisterSocketEventListener(final ISocketChannel socketChannel, final Emitter.Listener listener) {
		if (socketChannel == null) {
			throw new IllegalArgumentException("The \"socketChannel\" argument is required.");
		}
		
		if (listener == null) {
			throw new IllegalArgumentException("The \"listener\" argument is required.");
		}
		
		unregisterSocketEventListener(socketChannel.getChannelName(), listener);
	}
	
	private IDisposable registerSocketEventListener(final String socketChannelName, final Emitter.Listener listener) {
		_socket.on(socketChannelName, listener);
		
		return new IDisposable() {
			@Override
			public void dispose() {
				unregisterSocketEventListener(socketChannelName, listener);
			}
		};
	}
	
	private void unregisterSocketEventListener(final String socketChannelName, final Emitter.Listener listener) {
		_socket.off(socketChannelName, listener);
	}
	
	protected final void sendToServer(final ISocketChannel socketChannel, final JSONObject data) {
		if (socketChannel == null) {
			throw new IllegalArgumentException("The \"socketChannel\" argument is required.");
		}
		
		if (data == null) {
			throw new IllegalArgumentException("The \"data\" argument is required.");
		}
		
        if (this._connectionState.getCanTransmit()) {
        	int socketSequence = _socketSequence.incrementAndGet();
        	
        	logger.debug("Sending message {} to {}", socketSequence, socketChannel);
        	
        	if (logger.isTraceEnabled()) {        		
        		logger.trace("Payload for message {}:\n{}", socketSequence, stringify(data));
        	}
        	
            _socket.emit(socketChannel.getChannelName(), data);
            
            logger.debug("Sent message {} to {}", socketSequence, socketChannel);
        }
	}
	
	protected final void requestFromServer(final ISocketChannel socketChannel, final JSONObject data, final IAction<JSONObject> callback) {
		if (socketChannel == null) {
			throw new IllegalArgumentException("The \"socketChannel\" argument is required.");
		}
		
		if (data == null) {
			throw new IllegalArgumentException("The \"data\" argument is required.");
		}
		
		final UUID requestUuid = UUID.randomUUID();
		final String requestId = requestUuid.toString();
		
		_requestMap.put(requestId, callback);
		
		JSONObject envelope = new JSONObject();
		
		try {
			envelope.put("requestId", requestId);
			envelope.put("request", data);
		} catch (JSONException e) {
			logger.error("Unable to construct JSON payload for request message.", e);
			
			envelope = null;
		}
		
		sendToServer(socketChannel, envelope);
	}
    
	private boolean changeConnectionState(final SocketConnectionState connectionState, final boolean ignoreInvalidStateChange) {
		boolean returnVal = false;
		
		synchronized (_connectionLock) {
			if (connectionState != _connectionState) {
				if (!_connectionState.canTransitionTo(connectionState)) {
					logger.debug("Changing socket connection state to {}", connectionState);
					
					onConnectionStateChanged(_connectionState = connectionState);
					
					_connectionStateChanged.fire(_connectionState);
					
					logger.debug("Changed socket connection state to {}", _connectionState);
					
					returnVal = true;
				} else {
					if (!ignoreInvalidStateChange) {
						throw new IllegalStateException(String.format("Unable to change connection from %s to %s", _connectionState, connectionState));
					}
				}
			}
		}
		
		return returnVal;
	}
	
	private boolean changeConnectionState(final SocketConnectionState connectionState) {
		return changeConnectionState(connectionState, false);
	}

	@Override
	public void dispose() {
		return;
	}
	
	@Override
	public String toString() {
		return String.format("[SocketConnection (id: %s, host: %s, port: %s, secure: %s)]", _id, _host, _port, _secure);
	}
	
	private static final String getServerUri(final String host, final int port, final boolean secure) {
		final String protocol;
		
		String hostToUse = host;
		
		if (secure) {
			protocol = "https";
		} else {
			protocol = "http";
			
			if (!host.equals("localhost")) {
				BufferedReader bufferedReader = null;
				
				try {
					final URL url = new URL(String.format("http://%s:%s/ip", host, port));
					final URLConnection urlConnection = url.openConnection();
					final HttpURLConnection connection = (HttpURLConnection)urlConnection;
					
					connection.setRequestMethod("GET");
					connection.setReadTimeout(30000);

					connection.connect();
					
					final InputStreamReader inputStramReader = new InputStreamReader(connection.getInputStream());
					bufferedReader = new BufferedReader(inputStramReader);
					
					hostToUse = bufferedReader.readLine();
				} catch (Exception e) {
					logger.error("Unable to construct URL for server address query.", e);
					
					hostToUse = host;
				} finally {
					if (bufferedReader != null) {
						try {
							bufferedReader.close();
						} catch (IOException e) {
							logger.error("Unable to close BufferedReader", e);
						}
					}
				}
			}
		}
		
		return String.format("%s://%s:%s", protocol, hostToUse, port);
	}
	
	protected void logMessageReceipt(final ISocketChannel socketChannel, JSONObject data) {
		final int socketSequence = _socketSequence.incrementAndGet();
		
    	logger.debug("Received message {} on {}", socketSequence, socketChannel);
    	
    	if (logger.isTraceEnabled()) {        		
    		logger.trace("Payload for message {}:\n{}", socketSequence, stringify(data));
    	}
	}
	
	private static final String stringify(final JSONObject data) {
		String returnRef;
		
		try {
			returnRef = data.toString(4);
		} catch (JSONException e) {
			returnRef = "[failed to stringify JSON object]";
		}
		
		return returnRef;
	}
}
