package com.barchart.streaming.data;

import com.barchart.common.data.ISynchronizer;

public class MutableQuote implements IMutableQuote {
	public final String _symbol;
	
	public Integer _bidSize;
	public Integer _askSize;
	
	public Integer _sequence;
	
	public MutableQuote(final String symbol) {
		this(symbol, null);
	}
	
	public MutableQuote(final String symbol, final ISynchronizer<IMutableQuote> synchronizer) {
		_symbol = symbol;
		
		_bidSize = null;
		_askSize = null;
		
		_sequence = null;
		
		if (synchronizer != null) {
			synchronizer.synchronize(this);
		}
	}

	@Override
	public String getSymbol() {
		return _symbol;
	}

	@Override
	public Integer getBidSize() {
		return _bidSize;
	}
	
	@Override
	public void setBidSize(Integer value) {
		_bidSize = value;
	}
	
	@Override
	public Integer getAskSize() {
		return _askSize;
	}

	@Override
	public void setAskSize(Integer value) {
		_askSize = value;
	}
	
	@Override
	public Integer getSequence() {
		return _sequence;
	}

	@Override
	public void setSequence(Integer value) {
		_sequence = value;
	}
	
	@Override
	public String toString() {
		return String.format("[MutableQuote (symbol: %s, sequence: %s)]", _symbol, _sequence == null ? "--" : _sequence.intValue());
	}
}
