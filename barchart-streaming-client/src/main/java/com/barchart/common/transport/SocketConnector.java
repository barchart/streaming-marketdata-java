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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.common.Action;
import com.barchart.common.Disposable;
import com.barchart.common.messaging.Event;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketConnector implements Disposable {
	private static final Logger logger;
	private static final AtomicInteger socketCounter;
	
	private final int id;
	
	private final String host;
	private final int port;
	private final boolean secure;
	
	private final Socket socket;
	private final Event<String> socketStateChanged;
	private final AtomicBoolean socketDisposed;

	private final AtomicInteger messageSequencer;
	
	private final ConcurrentMap<String, Action<JSONObject>> requestMap;
	
	static {
		logger = LoggerFactory.getLogger(SocketConnector.class);
		
		socketCounter = new AtomicInteger(0);
	}
	
	public SocketConnector(final String host, final int port, final boolean secure) {
		if (host == null) {
			throw new IllegalArgumentException("The \"host\" argument is required.");
		}
		
		if (port < 0 || port > 65536) {
			throw new IllegalArgumentException("The \"port\" is not a valid TCP port number.");
		}
		
		this.id = socketCounter.incrementAndGet();
		
		this.host = host;
		this.port = port;
		this.secure = secure;
		
		Socket socket;
		
		logger.info("Socket connector created using (host: {}, port: {}, secure: {}).", host, port, secure);
		
		final String serverUri = getServerUri(host, port, secure);
		
		logger.info("Attempting to open socket.io connection to {}.", serverUri);
		
		final IO.Options options = new IO.Options();
		options.reconnectionAttempts = 5;
		
		try {	
			socket = IO.socket(serverUri, options);
		} catch (URISyntaxException e) {
			logger.error("Socket URI is invalid", e);
			
			socket = null;
		}
		
		this.socket = socket;
		this.messageSequencer = new AtomicInteger(0);
		
		this.socketStateChanged = new Event<String>("socketStateChanged");
		this.socketDisposed = new AtomicBoolean(false);
		
		this.requestMap = new ConcurrentHashMap<String, Action<JSONObject>>(16, 0.75f, 2);
		
		registerSocketEventListener(Socket.EVENT_CONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				onSocketStateChanged(Socket.EVENT_CONNECT);
			}
		});
		
		registerSocketEventListener(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				onSocketStateChanged(Socket.EVENT_RECONNECTING);
			}
		});
		
		registerSocketEventListener(Socket.EVENT_RECONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				onSocketStateChanged(Socket.EVENT_RECONNECT);
			}
		});
		
		registerSocketEventListener(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				onSocketStateChanged(Socket.EVENT_DISCONNECT);
			}
		});
		
		registerSocketEventListener(Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				onSocketStateChanged(Socket.EVENT_RECONNECT_FAILED);
			}
		});
		
		registerSocketEventListener(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				logger.warn("Error on socket connect connect. Error: {}.", Socket.EVENT_CONNECT_ERROR, args[0]);
			}
		});
		
		registerSocketEventListener(BasicSocketChannel.Response, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				final JSONObject data = (JSONObject)args[0];
				
				final String requestId = data.optString("requestId");

				Action<JSONObject> responseHandler = requestMap.remove(requestId);
				
				if (responseHandler != null) {
					responseHandler.execute(data.optJSONObject("response"));
				} else {
					logger.warn("Received response without corresponding request {}.", requestId);
				}
			}
		});
	}
	
	public boolean getIsValid() {
		return socket != null;
	}
	
	public final void connect() {
		if (socketDisposed.get()) {
			throw new IllegalStateException("Unable to connect, the socket has been disposed.");
		}
		
		logger.debug("Staring manual connection attempt.");
		
		socket.connect();
	}
	
	public final void disconnect() {
		if (socketDisposed.get()) {
			throw new IllegalStateException("Unable to disconnect, the socket has been disposed.");
		}
		
		logger.debug("Staring manual disconnect attempt.");
		
		socket.disconnect();
	}
	
	public final Disposable registerSocketStateChangedObserver(Action<String> observer) {
		if (socketDisposed.get()) {
			throw new IllegalStateException("Unable to register state change observer, the socket has been disposed.");
		}
		
		return socketStateChanged.register(observer);
	}
	
	public final Disposable registerSocketEventListener(final SocketChannel socketChannel, final Emitter.Listener listener) {
		if (socketChannel == null) {
			throw new IllegalArgumentException("The \"socketChannel\" argument is required.");
		}
		
		logger.debug("Registering listener for {}.", socketChannel);
		
		return registerSocketEventListener(socketChannel.getChannelName(), listener);
	}
	
	public Disposable registerSocketEventListener(final String socketChannelName, final Emitter.Listener listener) {
		if (socketChannelName == null) {
			throw new IllegalArgumentException("The \"socketChannelName\" argument is required.");
		}
		
		if (listener == null) {
			throw new IllegalArgumentException("The \"listener\" argument is required.");
		}
		
		if (socketDisposed.get()) {
			throw new IllegalStateException("Unable to register state change observer, the socket has been disposed.");
		}
		
		final Emitter.Listener wrappedListener = new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				int messageSequence = messageSequencer.incrementAndGet();
				
		    	logger.debug("Received message {} on {}", messageSequence, socketChannelName);
		    	
		    	if (logger.isTraceEnabled() && args.length > 0 && args[0] instanceof JSONObject) {
		    		final JSONObject data = (JSONObject)args[0];
		    		
		    		logger.trace("Payload for message {}:\n{}", messageSequence, stringify(data));
		    	}
				
				listener.call(args);
			}
		};
		
		socket.on(socketChannelName, wrappedListener);
		
		return new Disposable() {
			@Override
			public void dispose() {
				unregisterSocketEventListener(socketChannelName, wrappedListener);
			}
		};
	}

	private void unregisterSocketEventListener(final String socketChannelName, final Emitter.Listener listener) {
		if (socketChannelName == null) {
			throw new IllegalArgumentException("The \"socketChannelName\" argument is required.");
		}
		
		if (listener == null) {
			throw new IllegalArgumentException("The \"listener\" argument is required.");
		}
		
		socket.off(socketChannelName, listener);
	}
	
	public final void sendToServer(final SocketChannel socketChannel, final JSONObject data) {
		if (socketChannel == null) {
			throw new IllegalArgumentException("The \"socketChannel\" argument is required.");
		}
		
		if (data == null) {
			throw new IllegalArgumentException("The \"data\" argument is required.");
		}
		
		if (socketDisposed.get()) {
			throw new IllegalStateException("Unable to register send message, the socket has been disposed.");
		}
		
    	int messageSequence = messageSequencer.incrementAndGet();
    	
		if (socket.connected()) {	    	
	    	logger.debug("Sending message {} to {}.", messageSequence, socketChannel);
	    	
	    	if (logger.isTraceEnabled()) {        		
	    		logger.trace("Payload for message {}:\n{}", messageSequence, stringify(data));
	    	}
	    	
	        socket.emit(socketChannel.getChannelName(), data);
	        
	        logger.debug("Sent message {} to {}.", messageSequence, socketChannel);
		} else {
			logger.debug("Dropped message {} to {} because socket was not connected.", messageSequence, socketChannel);
		}
	}
	
	public final void requestFromServer(final SocketChannel socketChannel, final JSONObject data, final Action<JSONObject> callback) {
		if (socketChannel == null) {
			throw new IllegalArgumentException("The \"socketChannel\" argument is required.");
		}
		
		if (data == null) {
			throw new IllegalArgumentException("The \"data\" argument is required.");
		}
		
		final UUID requestUuid = UUID.randomUUID();
		final String requestId = requestUuid.toString();
		
		requestMap.put(requestId, callback);
		
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
	
	private void onSocketStateChanged(final String socketState) {
		if (socketDisposed.get()) {
			return;
		}
		
		logger.debug("A socket.io {} event occurred.", socketState);
		
		socketStateChanged.fire(socketState);
	}

	@Override
	public void dispose() {
		if (!socketDisposed.getAndSet(true)) {
			try {
				socket.close();
			} catch (Exception e) {
				logger.error("Unable to close socket.", e);
			}
		}
	}
	
	@Override
	public String toString() {
		return String.format("[SocketConnector (id: %s, host: %s, port: %s, secure: %s)]", id, host, port, secure);
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
					logger.error("Unable query server for IP address.", e);
					
					hostToUse = "invalid-host";
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
