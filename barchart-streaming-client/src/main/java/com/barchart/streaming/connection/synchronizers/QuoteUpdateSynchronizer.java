package com.barchart.streaming.connection.synchronizers;

import org.json.JSONArray;
import org.json.JSONObject;

import com.barchart.common.data.Synchronizer;
import com.barchart.streaming.data.MutableQuote;

public class QuoteUpdateSynchronizer implements Synchronizer<MutableQuote> {
	public final String symbol;
	public final JSONObject data;
	
	public QuoteUpdateSynchronizer(final String symbol, final JSONObject data) {
		if (symbol == null) {
			throw new IllegalArgumentException("The \"symbol\" argument is required.");
		}
		
		if (data == null) {
			throw new IllegalArgumentException("The \"data\" argument is required.");
		}
		
		this.symbol = symbol;
		this.data = data;
	}

	@Override
	public void synchronize(final MutableQuote target) {
		if (target == null) {
			throw new IllegalArgumentException("The \"target\" argument is required.");
		}
		
		if (!symbol.equals(target.getSymbol())) {
			throw new IllegalArgumentException(String.format("The synchronizer does not apply to the \"target\" (target symbol: %s).", target.getSymbol()));
		}
		
		final JSONArray names = data.names();
			
		for (int i = 0; i < names.length(); i++) {
			final String name = names.optString(i);
			
			if (name != null) {
				synchronizeProperty(target, name); 
			}
		}
		
		if (data.has("lastPrice") || data.has("previousPrice")) {
			final Double lastPrice = target.getLastPrice();
			final Double previousPrice = target.getPreviousPrice();
			
			final Double priceChange;
			final Double priceChangePercent;
			
			if (lastPrice != null && previousPrice != null) {
				priceChange = Double.valueOf(lastPrice.doubleValue() - previousPrice.doubleValue());
				
				if (previousPrice.doubleValue() != 0) {
					priceChangePercent = Double.valueOf(priceChange.doubleValue() / previousPrice.doubleValue());
				} else {
					priceChangePercent = null;
				}
			} else {
				priceChange = null;
				priceChangePercent = null;
			}
			
			target.setPriceChange(priceChange);
			target.setPriceChangePercent(priceChangePercent);
		}
	}
	
	private void synchronizeProperty(final MutableQuote target, final String name) {
		switch (name) {
			case "sequence": {
				target.setSequence(Integer.valueOf(data.optInt(name)));
				break;
			}
			case "flag": {
				target.setFlag(data.optString(name));
				break;
			}
			case "online": {
				target.setOnline(data.optBoolean(name));
				break;
			}
			case "mode": {
				target.setMode(data.optString(name));
				break;
			}
			case "session": {
				target.setSession(data.optString(name));
				break;
			}
			case "day": {
				target.setDay(data.optString(name));
				break;
			}
			case "dayNum": {
				target.setDayNum(Integer.valueOf(data.optInt(name)));
				break;
			}
			case "lastPrice": {
				target.setLastPrice(Double.valueOf(data.optDouble(name)));
				break;
			}
			case "previousPrice": {
				target.setPreviousPrice(Double.valueOf(data.optDouble(name)));
				break;
			}
			case "tradePrice": {
				target.setTradePrice(Double.valueOf(data.optDouble(name)));
				break;
			}
			case "tradeSize": {
				target.setTradeSize(Integer.valueOf(data.optInt(name)));
				break;
			}
			case "bidPrice": {
				target.setBidPrice(Double.valueOf(data.optDouble(name)));
				break;
			}
			case "bidSize": {
				target.setBidSize(Integer.valueOf(data.optInt(name)));
				break;
			}
			case "askPrice": {
				target.setAskPrice(Double.valueOf(data.optDouble(name)));
				break;
			}
			case "askSize": {
				target.setAskSize(Integer.valueOf(data.optInt(name)));
				break;
			}
			case "openPrice": {
				target.setOpenPrice(Double.valueOf(data.optDouble(name)));
				break;
			}
			case "highPrice": {
				target.setHighPrice(Double.valueOf(data.optDouble(name)));
				break;
			}
			case "lowPrice": {
				target.setLowPrice(Double.valueOf(data.optDouble(name)));
				break;
			}
			case "settlementPrice": {
				target.setSettlementPrice(Double.valueOf(data.optDouble(name)));
				break;
			}
			case "volume": {
				target.setVolume(Integer.valueOf(data.optInt(name)));
				break;
			}
			case "openInterest": {
				target.setOpenInterest(Integer.valueOf(data.optInt(name)));
				break;
			}
			case "time": {
				target.setTime(String.valueOf(data.optString(name)));
				break;
			}
			case "timeDisplay": {
				target.setTimeDisplay(String.valueOf(data.optString(name)));
				break;
			}			
			default: {
				break;
			}
		}
	}
	
	@Override
	public String toString() {
		return String.format("[QuoteUpdateSynchronizer (symbol: %s)]", symbol);
	}
}
