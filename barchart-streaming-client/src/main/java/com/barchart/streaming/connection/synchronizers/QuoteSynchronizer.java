package com.barchart.streaming.connection.synchronizers;

import org.json.JSONArray;
import org.json.JSONObject;

import com.barchart.common.data.ISynchronizer;
import com.barchart.streaming.data.IMutableQuote;

public class QuoteSynchronizer implements ISynchronizer<IMutableQuote> {
	public final String _symbol;
	public final JSONObject _data;
	
	public QuoteSynchronizer(final String symbol, final JSONObject data) {
		if (symbol == null) {
			throw new IllegalArgumentException("The \"symbol\" argument is required.");
		}
		
		if (data == null) {
			throw new IllegalArgumentException("The \"data\" argument is required.");
		}
		
		_symbol = symbol;
		_data = data;
	}

	@Override
	public void synchronize(IMutableQuote target) {
		if (target == null) {
			throw new IllegalArgumentException("The \"target\" argument is required.");
		}
		
		if (!_symbol.equals(target.getSymbol())) {
			throw new IllegalArgumentException(String.format("The synchronizer does not apply to the \"target\" (target symbol: %s).", target.getSymbol()));
		}
		
		JSONArray names = _data.names();
			
		for (int i = 0; i < names.length(); i++) {
			final String name = names.optString(i);
			
			if (name != null) {
				synchronizeProperty(target, name); 
			}
		}
	}
	
	private void synchronizeProperty(final IMutableQuote target, final String name) {
		switch (name) {
			case "sequence": {
				target.setSequence(Integer.valueOf(_data.optInt(name)));
				break;
			}
			case "flag": {
				target.setFlag(_data.optString(name));
				break;
			}
			case "online": {
				target.setOnline(_data.optBoolean(name));
				break;
			}
			case "mode": {
				target.setMode(_data.optString(name));
				break;
			}
			case "session": {
				target.setSession(_data.optString(name));
				break;
			}
			case "day": {
				target.setDay(_data.optString(name));
				break;
			}
			case "dayNum": {
				target.setDayNum(Integer.valueOf(_data.optInt(name)));
				break;
			}
			case "lastPrice": {
				target.setLastPrice(Double.valueOf(_data.optDouble(name)));
				break;
			}
			case "previousPrice": {
				target.setPreviousPrice(Double.valueOf(_data.optDouble(name)));
				break;
			}
			case "tradePrice": {
				target.setTradePrice(Double.valueOf(_data.optDouble(name)));
				break;
			}
			case "tradeSize": {
				target.setTradeSize(Integer.valueOf(_data.optInt(name)));
				break;
			}
			case "bidPrice": {
				target.setBidPrice(Double.valueOf(_data.optDouble(name)));
				break;
			}
			case "bidSize": {
				target.setBidSize(Integer.valueOf(_data.optInt(name)));
				break;
			}
			case "askPrice": {
				target.setAskPrice(Double.valueOf(_data.optDouble(name)));
				break;
			}
			case "askSize": {
				target.setAskSize(Integer.valueOf(_data.optInt(name)));
				break;
			}
			case "openPrice": {
				target.setOpenPrice(Double.valueOf(_data.optDouble(name)));
				break;
			}
			case "highPrice": {
				target.setHighPrice(Double.valueOf(_data.optDouble(name)));
				break;
			}
			case "lowPrice": {
				target.setLowPrice(Double.valueOf(_data.optDouble(name)));
				break;
			}
			case "settlementPrice": {
				target.setSettlementPrice(Double.valueOf(_data.optDouble(name)));
				break;
			}
			case "volume": {
				target.setVolume(Integer.valueOf(_data.optInt(name)));
				break;
			}
			case "openInterest": {
				target.setOpenInterest(Integer.valueOf(_data.optInt(name)));
				break;
			}
			case "time": {
				target.setTime(String.valueOf(_data.optString(name)));
				break;
			}
			case "timeDisplay": {
				target.setTimeDisplay(String.valueOf(_data.optString(name)));
				break;
			}			
			default: {
				break;
			}
		}
	}
	
	@Override
	public String toString() {
		return String.format("[QuoteSynchronizer (symbol: %s)]", _symbol);
	}
}
