package com.barchart.streaming.data;

import com.barchart.common.data.ISynchronizer;

public class MutableQuote implements IMutableQuote {
	private final String _symbol;
	
	private Integer _sequence;
	private boolean _online;
		
	private String _flag;
	private String _mode;
	private String _session;
		
	private String _day;
	private Integer _dayNum;
		
	private Double _lastPrice;
	private Double _previousPrice;
		
	private Double _priceChange;
	private Double _priceChangePercent;
		
	private Double _tradePrice;
	private Integer _tradeSize;
		
	private Double _bidPrice;
	private Integer _bidSize;
	private Double _askPrice;
	private Integer _askSize;
		
	private Double _openPrice;
	private Double _highPrice;
	private Double _lowPrice;
	private Double _settlementPrice;
		
	private Integer _volume;
	private Integer _openInterest;
		
	private String _time;
	private String _timeDisplay;
	
	public MutableQuote(final String symbol) {
		this(symbol, null);
	}
	
	public MutableQuote(final String symbol, final ISynchronizer<IMutableQuote> synchronizer) {
		_symbol = symbol;
		
		_sequence = null;
		_online = false;
			
		_flag = null;
		_mode = null;
		_session = null;
			
		_day = null;
		_dayNum = null;
			
		_lastPrice = null;
		_previousPrice = null;
			
		_priceChange = null;
		_priceChangePercent = null;
			
		_tradePrice = null;
		_tradeSize = null;
			
		_bidPrice = null;
		_bidSize = null;
		_askPrice = null;
		_askSize = null;
			
		_openPrice = null;
		_highPrice = null;
		_lowPrice = null;
		_settlementPrice = null;
			
		_volume = null;
		_openInterest = null;
			
		_time = null;
		_timeDisplay = null;
		
		if (synchronizer != null) {
			synchronizer.synchronize(this);
		}
	}

	@Override
	public String getSymbol() {
		return _symbol;
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
	public boolean getOnline() {
		return _online;
	}
	
	@Override
	public void setOnline(boolean value) {
		_online = value;
	}

	@Override
	public String getFlag() {
		return _flag;
	}
	
	@Override
	public void setFlag(String value) {
		_flag = value;
	}

	@Override
	public String getMode() {
		return _mode;
	}
	
	@Override
	public void setMode(String value) {
		_mode = value;
	}

	@Override
	public String getSession() {
		return _session;
	}
	
	@Override
	public void setSession(String value) {
		_session = value;
	}

	@Override
	public String getDay() {
		return _day;
	}
	
	@Override
	public void setDay(String value) {
		_day = value;
	}

	@Override
	public Integer getDayNum() {
		return _dayNum;
	}
	
	@Override
	public void setDayNum(Integer value) {
		_dayNum = value;
	}

	@Override
	public Double getLastPrice() {
		return _lastPrice;
	}
	
	@Override
	public void setLastPrice(Double value) {
		_lastPrice = value;
	}

	@Override
	public Double getPreviousPrice() {
		return _previousPrice;
	}
	
	@Override
	public void setPreviousPrice(Double value) {
		_previousPrice = value;
	}

	@Override
	public Double getPriceChange() {
		return _priceChange;
	}
	
	@Override
	public void setPriceChange(Double value) {
		_priceChange = value;
	}

	@Override
	public Double getPriceChangePercent() {
		return _priceChangePercent;
	}
	
	@Override
	public void setPriceChangePercent(Double value) {
		_priceChangePercent = value;
	}

	@Override
	public Double getTradePrice() {
		return _tradePrice;
	}
	
	@Override
	public void setTradePrice(Double value) {
		_tradePrice = value;
	}

	@Override
	public Integer getTradeSize() {
		return _tradeSize;
	}
	
	@Override
	public void setTradeSize(Integer value) {
		_tradeSize = value;
	}

	@Override
	public Double getBidPrice() {
		return _bidPrice;
	}
	
	@Override
	public void setBidPrice(Double value) {
		_bidPrice = value;
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
	public Double getAskPrice() {
		return _askPrice;
	}
	
	@Override
	public void setAskPrice(Double value) {
		_askPrice = value;
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
	public Double getOpenPrice() {
		return _openPrice;
	}
	
	@Override
	public void setOpenPrice(Double value) {
		_openPrice = value;
	}

	@Override
	public Double getHighPrice() {
		return _highPrice;
	}
	
	@Override
	public void setHighPrice(Double value) {
		_highPrice = value;
	}

	@Override
	public Double getLowPrice() {
		return _lowPrice;
	}
	
	@Override
	public void setLowPrice(Double value) {
		_lowPrice = value;
	}

	@Override
	public Double getSettlementPrice() {
		return _settlementPrice;
	}

	@Override
	public void setSettlementPrice(Double value) {
		_settlementPrice = value;
	}
	
	@Override
	public Integer getVolume() {
		return _volume;
	}
	
	@Override
	public void setVolume(Integer value) {
		_volume = value;
	}

	@Override
	public Integer getOpenInterest() {
		return _openInterest;
	}
	
	@Override
	public void setOpenInterest(Integer value) {
		_openInterest = value;
	}

	@Override
	public String getTime() {
		return _time;
	}
	
	@Override
	public void setTime(String value) {
		_time = value;
	}

	@Override
	public String getTimeDisplay() {
		return _timeDisplay;
	}
	
	@Override
	public void setTimeDisplay(String value) {
		_timeDisplay = value;
	}


	@Override
	public String toString() {
		return String.format("[MutableQuote (symbol: %s, sequence: %s)]", _symbol, _sequence == null ? "--" : _sequence.intValue());
	}
}