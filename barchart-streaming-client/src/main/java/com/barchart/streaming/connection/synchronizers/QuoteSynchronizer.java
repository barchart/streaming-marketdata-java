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
			case "bidSize": {
				target.setBidSize(_data.optInt(name));
				break;
			}
			case "askSize": {
				target.setAskSize(_data.optInt(name));
				break;
			}
			case "sequence": {
				target.setSequence(_data.optInt(name));
				break;
			}
			default: {
				break;
			}
		}
	}
	
	@Override
	public String toString() {
		return "[MutableQuote]";
	}
}
