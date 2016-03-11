package com.barchart.streaming.connection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.common.IAction;
import com.barchart.common.IDisposable;
import com.barchart.common.data.ISynchronizer;
import com.barchart.common.messaging.Event;
import com.barchart.common.transport.SocketConnection;
import com.barchart.common.transport.SocketConnectionState;
import com.barchart.streaming.connection.synchronizers.QuoteSynchronizer;
import com.barchart.streaming.data.IMutableQuote;
import com.barchart.streaming.data.IProfile;
import com.barchart.streaming.data.IQuote;
import com.barchart.streaming.data.MutableQuote;
import com.barchart.streaming.data.Profile;

import io.socket.emitter.Emitter;

public final class MarketSocketConnection extends SocketConnection {
	private static final Logger logger = LoggerFactory.getLogger(MarketSocketConnection.class);
	
	private final ConcurrentMap<String, IProfile> _profiles;
	private final ConcurrentMap<String, IQuote> _quotes;
	
	private final ConcurrentMap<String, Event<ISynchronizer<IMutableQuote>>> _quoteEvents;
	private final ConcurrentMap<String, Event<ISynchronizer<IMutableQuote>>> _priceChangeEvents;
	
	private final Event<String> _timestampEvent;
	
	public MarketSocketConnection() {
		this("jerq-aggregator-prod.aws.barchart.com", 80, true);
	}
	
	public MarketSocketConnection(final String host) {
		this(host, 80, true);
	}
	
	public MarketSocketConnection(final String host, final int port, final boolean secure) {
		super(host, port, secure);
		
		_profiles = new ConcurrentHashMap<String, IProfile>(64, 0.75f, 2);
		_quotes = new ConcurrentHashMap<String, IQuote>(64, 0.75f, 2);
		
		_quoteEvents = new ConcurrentHashMap<String, Event<ISynchronizer<IMutableQuote>>>(64, 0.75f, 2);
		_priceChangeEvents = new ConcurrentHashMap<String, Event<ISynchronizer<IMutableQuote>>>(64, 0.75f, 2);
		
		_timestampEvent = new Event<String>("timestampUpdate");
		
		registerSocketEventListener(MarketSocketChannel.Timestamp, new Emitter.Listener() {
			public void call(Object... args) {
				final JSONObject data = (JSONObject)args[0];
				
				logMessageReceipt(MarketSocketChannel.Timestamp, data);
				
				final String timestamp = data.optString("timestamp");
				
				if (timestamp != null) {
					_timestampEvent.fire(timestamp);
				} else {
					logger.warn("Unable to extract \"{}\" property from {}", "timestamp", MarketSocketChannel.Timestamp);
				}
			}
		});
		
		registerSocketEventListener(MarketSocketChannel.ProfileSnapshot, new Emitter.Listener() {
			public void call(Object... args) {
				final JSONObject data = (JSONObject)args[0];
				final String symbol = data.optString("symbol");
				
				logMessageReceipt(MarketSocketChannel.ProfileSnapshot, data);

				if (symbol != null) {
					updateProfile(symbol, data);
				}
			}
		});
		
		registerSocketEventListener(MarketSocketChannel.QuoteSnapshot, new Emitter.Listener() {
			public void call(Object... args) {
				final JSONObject data = (JSONObject)args[0];
				final String symbol = data.optString("symbol");
				
				logMessageReceipt(MarketSocketChannel.QuoteSnapshot, data);

				if (symbol != null) {
					final ISynchronizer<IMutableQuote> synchronizer = new QuoteSynchronizer(symbol, data);
					final IQuote quote = new MutableQuote(symbol, synchronizer);
					
					_quotes.put(symbol, quote);
					
					final Event<ISynchronizer<IMutableQuote>> event = _quoteEvents.get(symbol);
					
					if (event != null) {
						event.fire(synchronizer);
					}
				}
			}
		});
		
		registerSocketEventListener(MarketSocketChannel.QuoteDelta, new Emitter.Listener() {
			public void call(Object... args) {
				final JSONObject data = (JSONObject)args[0];
				final String symbol = data.optString("symbol");
				
				logMessageReceipt(MarketSocketChannel.QuoteDelta, data);
				
				if (symbol != null) {
					Event<ISynchronizer<IMutableQuote>> event = _quoteEvents.get(symbol);
					
					if (event != null) {
						
					}
				}
			}
		});
	}
	
	@Override
	protected void onConnectionStateChanged(SocketConnectionState connectionState) {
		if (connectionState == SocketConnectionState.Connected) {
			if (!_timestampEvent.getIsEmpty()) {
				sendToServer(MarketSocketChannel.SubscribeTimestamp, new JSONObject());
			} else {
				sendToServer(MarketSocketChannel.UnsubscribeTimestamp, new JSONObject());
			}
			
			synchronized (_quoteEvents) {
				if (!_quoteEvents.isEmpty()) {
					sendToServer(MarketSocketChannel.ChangeSymbolSubscription, getSymbolSubscriptionPayload(_quoteEvents.keySet().toArray(new String[0]), Boolean.TRUE, null));
				}
			}
			
			synchronized (_priceChangeEvents) {
				if (!_priceChangeEvents.isEmpty()) {
					sendToServer(MarketSocketChannel.ChangeSymbolSubscription, getSymbolSubscriptionPayload(_priceChangeEvents.keySet().toArray(new String[0]), null, Boolean.TRUE));
				}
			}
		}
	}
	
	public IDisposable subscribeToTimestamp(final IAction<String> timestampHandler) {
		if (timestampHandler == null) {
			throw new IllegalArgumentException("The \"timestampHandler\" argument is required.");
		}
		
		synchronized (_timestampEvent) {
			final boolean empty = _timestampEvent.getIsEmpty();
			
			_timestampEvent.register(timestampHandler);
			
			if (empty) {
				sendToServer(MarketSocketChannel.SubscribeTimestamp, new JSONObject());
			}
		}
		
		return new IDisposable() {
			@Override
			public void dispose() {
				unsubscribeFromTimestamp(timestampHandler);
			}
		};
	}
	
	public void unsubscribeFromTimestamp(final IAction<String> timestampHandler) {
		if (timestampHandler == null) {
			throw new IllegalArgumentException("The \"timestampHandler\" argument is required.");
		}
		
		synchronized (_timestampEvent) {
			boolean empty = _timestampEvent.getIsEmpty();
			
			_timestampEvent.unregister(timestampHandler);
			
			if (!empty && _timestampEvent.getIsEmpty()) {
				sendToServer(MarketSocketChannel.UnsubscribeTimestamp, new JSONObject());
			}
		}
	}
	
	public IDisposable subscribeToQuotes(final String symbol, final IAction<ISynchronizer<IMutableQuote>> observer) {
		if (symbol == null) {
			throw new IllegalArgumentException("The \"symbol\" argument is required.");
		}
		
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument is required.");
		}
		
		synchronized (_quoteEvents) {
			if (!_quoteEvents.containsKey(symbol)) {
				_quoteEvents.putIfAbsent(symbol, new Event<ISynchronizer<IMutableQuote>>(String.format("%s quoteUpdated", symbol)));
			}
			
			final Event<ISynchronizer<IMutableQuote>> quoteUpdated = _quoteEvents.get(symbol);
			final boolean empty = quoteUpdated.getIsEmpty();
			
			quoteUpdated.register(observer);
			
			if (empty) {
				sendToServer(MarketSocketChannel.ChangeSymbolSubscription, getSymbolSubscriptionPayload(new String[] { symbol }, Boolean.TRUE, null));
			}
		}
		
		return new IDisposable() {
			@Override
			public void dispose() {
				unsubscribeFromQuotes(symbol, observer);
			}
		};
	}
	
	public void unsubscribeFromQuotes(final String symbol, final IAction<ISynchronizer<IMutableQuote>> observer) {
		if (symbol == null) {
			throw new IllegalArgumentException("The \"symbol\" argument is required.");
		}
		
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument is required.");
		}
		
		synchronized (_quoteEvents) {
			if (_quoteEvents.containsKey(symbol)) {
				final Event<ISynchronizer<IMutableQuote>> quoteUpdated = _quoteEvents.get(symbol);
				final boolean empty = quoteUpdated.getIsEmpty();
				
				quoteUpdated.unregister(observer);
				
				if (!empty && quoteUpdated.getIsEmpty()) {
					sendToServer(MarketSocketChannel.ChangeSymbolSubscription, getSymbolSubscriptionPayload(new String[] { symbol }, Boolean.FALSE, null));
					
					_quoteEvents.remove(symbol);
				}
			}
		}
	}
	
	public IDisposable subscribeToPriceChanges(final String symbol, final IAction<ISynchronizer<IMutableQuote>> observer) {
		if (symbol == null) {
			throw new IllegalArgumentException("The \"symbol\" argument is required.");
		}
		
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument is required.");
		}
		
		synchronized (_priceChangeEvents) {
			if (!_priceChangeEvents.containsKey(symbol)) {
				_priceChangeEvents.put(symbol, new Event<ISynchronizer<IMutableQuote>>(String.format("%s priceUpdated", symbol)));
			}
			
			final Event<ISynchronizer<IMutableQuote>> priceChanged = _priceChangeEvents.get(symbol);
			final boolean empty = priceChanged.getIsEmpty();
			
			priceChanged.register(observer);
			
			if (empty) {
				sendToServer(MarketSocketChannel.ChangeSymbolSubscription, getSymbolSubscriptionPayload(new String[] { symbol }, null, Boolean.TRUE));
			}
		}
		
		return new IDisposable() {
			@Override
			public void dispose() {
				unsubscribeFromPriceChanges(symbol, observer);
			}
		};
	}
	
	public void unsubscribeFromPriceChanges(String symbol, IAction<ISynchronizer<IMutableQuote>> observer) {
		if (symbol == null) {
			throw new IllegalArgumentException("The \"symbol\" argument is required.");
		}
		
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument is required.");
		}
		
		synchronized (_priceChangeEvents) {
			if (_priceChangeEvents.containsKey(symbol)) {
				final Event<ISynchronizer<IMutableQuote>> priceChanged = _priceChangeEvents.get(symbol);
				final boolean empty = priceChanged.getIsEmpty();
				
				priceChanged.unregister(observer);
				
				if (!empty && priceChanged.getIsEmpty()) {
					sendToServer(MarketSocketChannel.ChangeSymbolSubscription, getSymbolSubscriptionPayload(new String[] { symbol }, null, Boolean.FALSE));
				
					_priceChangeEvents.remove(symbol);
				}
			}
		}
	}
	
	public void requestProfile(final String symbol, final IAction<IProfile> callback) {
		if (symbol == null) {
			throw new IllegalArgumentException("The \"symbol\" argument is required.");
		}
		
		final IProfile profile = _profiles.get(symbol);
		
		if (profile != null) {
			callback.execute(profile);
		} else {
			JSONObject payload = new JSONObject();
			
			try {
				payload.put("symbol", symbol);
			} catch (JSONException e) {
				logger.error("Unable to construct JSON payload for profile request.", e);
				
				payload = null;
			}
			
			final IAction<JSONObject> requestHandler = new IAction<JSONObject>() {
				@Override
				public void execute(JSONObject data) {
					callback.execute(updateProfile(symbol, data));
				}
			};
			
			requestFromServer(MarketSocketChannel.RequestProfile, payload, requestHandler);
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
	private static JSONObject getSymbolSubscriptionPayload(final String[] symbols, final Boolean subscribeToQuotes, final Boolean subscribeToPrices) {
		JSONObject returnRef = new JSONObject();
	
		final JSONArray symbolArray = new JSONArray();
		
		for (int i = 0; i < symbols.length; i++) {
			symbolArray.put(symbols[i]);
		}
		
		try {
			returnRef.put("symbols", symbolArray);

			if (subscribeToQuotes != null) {
				returnRef.put("subscribeToQuotes", subscribeToQuotes.booleanValue());
			}
			
			if (subscribeToPrices != null) {
				returnRef.put("subscribeToPrices", subscribeToPrices.booleanValue());
			}
		} catch (JSONException e) {
			logger.error("Unable to construct JSON payload for symbol subscription.", e);
			
			returnRef = null;
		}
		
		return returnRef;
	}
	
	private IProfile updateProfile(final String symbol, final JSONObject data) {
		IProfile profile = new Profile(
				symbol, 
				data.optString("name"),
				data.optString("exchange"),
				data.optString("unitCode"),
				data.optString("pointValue"),
				data.optString("tickIncrement"),
				data.optString("root"),
				data.optString("month"),
				data.optString("year")
			);

		_profiles.put(symbol, profile);
		
		return profile;
	}
}
