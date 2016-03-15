package com.barchart.streaming.connection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.common.Action;
import com.barchart.common.Disposable;
import com.barchart.common.data.Synchronizer;
import com.barchart.common.messaging.Event;
import com.barchart.common.transport.SocketConnection;
import com.barchart.common.transport.SocketConnectionState;
import com.barchart.streaming.connection.synchronizers.QuoteCopySynchronizer;
import com.barchart.streaming.connection.synchronizers.QuoteUpdateSynchronizer;
import com.barchart.streaming.data.MutableQuote;
import com.barchart.streaming.data.Profile;
import com.barchart.streaming.data.Quote;
import com.barchart.streaming.data.BasicMutableQuote;
import com.barchart.streaming.data.BasicProfile;

import io.socket.emitter.Emitter;

public final class MarketSocketConnection extends SocketConnection {
	private static final Logger logger;
	
	private final ConcurrentMap<String, Profile> profiles;
	private final ConcurrentMap<String, MutableQuote> quotes;
	
	private final ConcurrentMap<String, Event<Synchronizer<MutableQuote>>> quoteEvents;
	private final ConcurrentMap<String, Event<Synchronizer<MutableQuote>>> priceChangeEvents;
	
	private final Event<String> timestampEvent;
	
	static {
		logger = LoggerFactory.getLogger(MarketSocketConnection.class);
	}
	
	public MarketSocketConnection() {
		this("jerq-aggregator-stage.aws.barchart.com", 80, false);
	}
	
	public MarketSocketConnection(final String host) {
		this(host, 80, false);
	}
	
	public MarketSocketConnection(final String host, final int port, final boolean secure) {
		super(host, port, secure);
		
		profiles = new ConcurrentHashMap<String, Profile>(64, 0.75f, 2);
		quotes = new ConcurrentHashMap<String, MutableQuote>(64, 0.75f, 2);
		
		quoteEvents = new ConcurrentHashMap<String, Event<Synchronizer<MutableQuote>>>(64, 0.75f, 2);
		priceChangeEvents = new ConcurrentHashMap<String, Event<Synchronizer<MutableQuote>>>(64, 0.75f, 2);
		
		timestampEvent = new Event<String>("timestampUpdate");
		
		registerSocketEventListener(MarketSocketChannel.Timestamp, new Emitter.Listener() {
			public void call(Object... args) {
				final JSONObject data = (JSONObject)args[0];
				final String timestamp = data.optString("timestamp");
				
				if (timestamp != null) {
					timestampEvent.fire(timestamp);
				} else {
					logger.warn("Unable to extract \"{}\" property from {}", "timestamp", MarketSocketChannel.Timestamp);
				}
			}
		});
		
		registerSocketEventListener(MarketSocketChannel.ProfileSnapshot, new Emitter.Listener() {
			public void call(Object... args) {
				final JSONObject data = (JSONObject)args[0];
				final String symbol = data.optString("symbol");

				if (symbol != null) {
					updateProfile(symbol, data);
				}
			}
		});
		
		registerSocketEventListener(MarketSocketChannel.QuoteSnapshot, new Emitter.Listener() {
			public void call(Object... args) {
				final JSONObject data = (JSONObject)args[0];
				final String symbol = data.optString("symbol");

				if (symbol != null) {
					final Synchronizer<MutableQuote> synchronizer = new QuoteUpdateSynchronizer(symbol, data);
					final MutableQuote quote = new BasicMutableQuote(symbol, synchronizer);
					
					quotes.put(symbol, quote);
					
					final Event<Synchronizer<MutableQuote>> quoteEvent = quoteEvents.get(symbol);
					
					if (quoteEvent != null) {
						quoteEvent.fire(synchronizer);
					}
					
					final Event<Synchronizer<MutableQuote>> priceUpdateEvent = priceChangeEvents.get(symbol);
					
					if (priceUpdateEvent != null) {
						priceUpdateEvent.fire(synchronizer);
					}
				} else {
					logger.warn("Dropping {} due to missing symbol.", MarketSocketChannel.QuoteSnapshot);
				}
			}
		});
		
		registerSocketEventListener(MarketSocketChannel.QuoteDelta, new Emitter.Listener() {
			public void call(Object... args) {
				final JSONObject data = (JSONObject)args[0];
				final String symbol = data.optString("symbol");
				
				if (symbol != null) {
					final Synchronizer<MutableQuote> synchronizer = new QuoteUpdateSynchronizer(symbol, data);
					Event<Synchronizer<MutableQuote>> event = quoteEvents.get(symbol);
					
					MutableQuote quote = quotes.get(symbol);
					
					if (quote != null) {
						synchronizer.synchronize(quote);
					}
					
					if (event != null) {
						event.fire(synchronizer);
					}
				} else {
					logger.warn("Dropping {} due to missing symbol.", MarketSocketChannel.QuoteDelta);
				}
			}
		});
	}
	
	@Override
	protected void onConnectionStateChanged(SocketConnectionState connectionState) {
		if (connectionState == SocketConnectionState.Connected) {
			if (!timestampEvent.getIsEmpty()) {
				sendToServer(MarketSocketChannel.SubscribeTimestamp, new JSONObject());
			} else {
				sendToServer(MarketSocketChannel.UnsubscribeTimestamp, new JSONObject());
			}
			
			synchronized (quoteEvents) {
				if (!quoteEvents.isEmpty()) {
					sendToServer(MarketSocketChannel.ChangeSymbolSubscription, getSymbolSubscriptionPayload(quoteEvents.keySet().toArray(new String[quoteEvents.size()]), Boolean.TRUE, null));
				}
			}
			
			synchronized (priceChangeEvents) {
				if (!priceChangeEvents.isEmpty()) {
					sendToServer(MarketSocketChannel.ChangeSymbolSubscription, getSymbolSubscriptionPayload(priceChangeEvents.keySet().toArray(new String[priceChangeEvents.size()]), null, Boolean.TRUE));
				}
			}
		}
	}
	
	public Disposable subscribeToTimestamp(final Action<String> timestampHandler) {
		if (timestampHandler == null) {
			throw new IllegalArgumentException("The \"timestampHandler\" argument is required.");
		}
		
		synchronized (timestampEvent) {
			final boolean empty = timestampEvent.getIsEmpty();
			
			timestampEvent.register(timestampHandler);
			
			if (empty) {
				sendToServer(MarketSocketChannel.SubscribeTimestamp, new JSONObject());
			}
		}
		
		return new Disposable() {
			@Override
			public void dispose() {
				unsubscribeFromTimestamp(timestampHandler);
			}
		};
	}
	
	public void unsubscribeFromTimestamp(final Action<String> timestampHandler) {
		if (timestampHandler == null) {
			throw new IllegalArgumentException("The \"timestampHandler\" argument is required.");
		}
		
		synchronized (timestampEvent) {
			boolean empty = timestampEvent.getIsEmpty();
			
			timestampEvent.unregister(timestampHandler);
			
			if (!empty && timestampEvent.getIsEmpty()) {
				sendToServer(MarketSocketChannel.UnsubscribeTimestamp, new JSONObject());
			}
		}
	}
	
	public Disposable subscribeToQuotes(final String symbol, final Action<Synchronizer<MutableQuote>> observer) {
		if (symbol == null) {
			throw new IllegalArgumentException("The \"symbol\" argument is required.");
		}
		
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument is required.");
		}
		
		final Quote quote = quotes.get(symbol);
		
		if (quote != null) {
			observer.execute(new QuoteCopySynchronizer(quote));
		}
		
		synchronized (quoteEvents) {
			if (!quoteEvents.containsKey(symbol)) {
				quoteEvents.putIfAbsent(symbol, new Event<Synchronizer<MutableQuote>>(String.format("%s quoteUpdated", symbol)));
			}
			
			final Event<Synchronizer<MutableQuote>> quoteUpdated = quoteEvents.get(symbol);
			final boolean empty = quoteUpdated.getIsEmpty();
			
			quoteUpdated.register(observer);
			
			if (empty) {
				sendToServer(MarketSocketChannel.ChangeSymbolSubscription, getSymbolSubscriptionPayload(new String[] { symbol }, Boolean.TRUE, null));
			}
		}
		
		return new Disposable() {
			@Override
			public void dispose() {
				unsubscribeFromQuotes(symbol, observer);
			}
		};
	}
	
	public void unsubscribeFromQuotes(final String symbol, final Action<Synchronizer<MutableQuote>> observer) {
		if (symbol == null) {
			throw new IllegalArgumentException("The \"symbol\" argument is required.");
		}
		
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument is required.");
		}
		
		synchronized (quoteEvents) {
			if (quoteEvents.containsKey(symbol)) {
				final Event<Synchronizer<MutableQuote>> quoteUpdated = quoteEvents.get(symbol);
				final boolean empty = quoteUpdated.getIsEmpty();
				
				quoteUpdated.unregister(observer);
				
				if (!empty && quoteUpdated.getIsEmpty()) {
					sendToServer(MarketSocketChannel.ChangeSymbolSubscription, getSymbolSubscriptionPayload(new String[] { symbol }, Boolean.FALSE, null));
					
					quoteEvents.remove(symbol);
				}
			}
		}
	}
	
	public Disposable subscribeToPriceChanges(final String symbol, final Action<Synchronizer<MutableQuote>> observer) {
		if (symbol == null) {
			throw new IllegalArgumentException("The \"symbol\" argument is required.");
		}
		
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument is required.");
		}
		
		final Quote quote = quotes.get(symbol);
		
		if (quote != null) {
			observer.execute(new QuoteCopySynchronizer(quote));
		}
		
		synchronized (priceChangeEvents) {
			if (!priceChangeEvents.containsKey(symbol)) {
				priceChangeEvents.put(symbol, new Event<Synchronizer<MutableQuote>>(String.format("%s priceUpdated", symbol)));
			}
			
			final Event<Synchronizer<MutableQuote>> priceChanged = priceChangeEvents.get(symbol);
			final boolean empty = priceChanged.getIsEmpty();
			
			priceChanged.register(observer);
			
			if (empty) {
				sendToServer(MarketSocketChannel.ChangeSymbolSubscription, getSymbolSubscriptionPayload(new String[] { symbol }, null, Boolean.TRUE));
			}
		}
		
		return new Disposable() {
			@Override
			public void dispose() {
				unsubscribeFromPriceChanges(symbol, observer);
			}
		};
	}
	
	public void unsubscribeFromPriceChanges(String symbol, Action<Synchronizer<MutableQuote>> observer) {
		if (symbol == null) {
			throw new IllegalArgumentException("The \"symbol\" argument is required.");
		}
		
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument is required.");
		}
		
		synchronized (priceChangeEvents) {
			if (priceChangeEvents.containsKey(symbol)) {
				final Event<Synchronizer<MutableQuote>> priceChanged = priceChangeEvents.get(symbol);
				final boolean empty = priceChanged.getIsEmpty();
				
				priceChanged.unregister(observer);
				
				if (!empty && priceChanged.getIsEmpty()) {
					sendToServer(MarketSocketChannel.ChangeSymbolSubscription, getSymbolSubscriptionPayload(new String[] { symbol }, null, Boolean.FALSE));
				
					priceChangeEvents.remove(symbol);
				}
			}
		}
	}
	
	public void requestProfile(final String symbol, final Action<Profile> callback) {
		if (symbol == null) {
			throw new IllegalArgumentException("The \"symbol\" argument is required.");
		}
		
		final Profile profile = profiles.get(symbol);
		
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
			
			final Action<JSONObject> requestHandler = new Action<JSONObject>() {
				@Override
				public void execute(JSONObject data) {
					callback.execute(updateProfile(symbol, data));
				}
			};
			
			requestFromServer(MarketSocketChannel.RequestProfile, payload, requestHandler);
		}
	}
	
	private Profile updateProfile(final String symbol, final JSONObject data) {
		final Profile profile = new BasicProfile(
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

		profiles.put(symbol, profile);
		
		return profile;
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
}
