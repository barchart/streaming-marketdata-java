package com.barchart.streaming.data;

import com.barchart.common.data.Synchronizer;

public class BasicMutableQuote implements MutableQuote {
	private final String symbol;
	
	private Integer sequence;
	private boolean online;
		
	private String flag;
	private String mode;
	private String session;
		
	private String day;
	private Integer dayNum;
		
	private Double lastPrice;
	private Double previousPrice;
		
	private Double priceChange;
	private Double priceChangePercent;
		
	private Double tradePrice;
	private Integer tradeSize;
		
	private Double bidPrice;
	private Integer bidSize;
	private Double askPrice;
	private Integer askSize;
		
	private Double openPrice;
	private Double highPrice;
	private Double lowPrice;
	private Double settlementPrice;
		
	private Integer volume;
	private Integer openInterest;
		
	private String time;
	private String timeDisplay;
	
	public BasicMutableQuote(final String symbol) {
		this(symbol, null);
	}
	
	public BasicMutableQuote(final String symbol, final Synchronizer<MutableQuote> synchronizer) {
		this.symbol = symbol;
		
		this.sequence = null;
		this.online = false;
			
		this.flag = null;
		this.mode = null;
		this.session = null;
			
		this.day = null;
		this.dayNum = null;
			
		this.lastPrice = null;
		this.previousPrice = null;
			
		this.priceChange = null;
		this.priceChangePercent = null;
			
		this.tradePrice = null;
		this.tradeSize = null;
			
		this.bidPrice = null;
		this.bidSize = null;
		this.askPrice = null;
		this.askSize = null;
			
		this.openPrice = null;
		this.highPrice = null;
		this.lowPrice = null;
		this.settlementPrice = null;
			
		this.volume = null;
		this.openInterest = null;
			
		this.time = null;
		this.timeDisplay = null;
		
		if (synchronizer != null) {
			synchronizer.synchronize(this);
		}
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

	@Override
	public Integer getSequence() {
		return sequence;
	}
	
	@Override
	public void setSequence(Integer value) {
		sequence = value;
	}
	
	@Override
	public boolean getOnline() {
		return online;
	}
	
	@Override
	public void setOnline(boolean value) {
		online = value;
	}

	@Override
	public String getFlag() {
		return flag;
	}
	
	@Override
	public void setFlag(String value) {
		flag = value;
	}

	@Override
	public String getMode() {
		return mode;
	}
	
	@Override
	public void setMode(String value) {
		mode = value;
	}

	@Override
	public String getSession() {
		return session;
	}
	
	@Override
	public void setSession(String value) {
		session = value;
	}

	@Override
	public String getDay() {
		return day;
	}
	
	@Override
	public void setDay(String value) {
		day = value;
	}

	@Override
	public Integer getDayNum() {
		return dayNum;
	}
	
	@Override
	public void setDayNum(Integer value) {
		dayNum = value;
	}

	@Override
	public Double getLastPrice() {
		return lastPrice;
	}
	
	@Override
	public void setLastPrice(Double value) {
		lastPrice = value;
	}

	@Override
	public Double getPreviousPrice() {
		return previousPrice;
	}
	
	@Override
	public void setPreviousPrice(Double value) {
		previousPrice = value;
	}

	@Override
	public Double getPriceChange() {
		return priceChange;
	}
	
	@Override
	public void setPriceChange(Double value) {
		priceChange = value;
	}

	@Override
	public Double getPriceChangePercent() {
		return priceChangePercent;
	}
	
	@Override
	public void setPriceChangePercent(Double value) {
		priceChangePercent = value;
	}

	@Override
	public Double getTradePrice() {
		return tradePrice;
	}
	
	@Override
	public void setTradePrice(Double value) {
		tradePrice = value;
	}

	@Override
	public Integer getTradeSize() {
		return tradeSize;
	}
	
	@Override
	public void setTradeSize(Integer value) {
		tradeSize = value;
	}

	@Override
	public Double getBidPrice() {
		return bidPrice;
	}
	
	@Override
	public void setBidPrice(Double value) {
		bidPrice = value;
	}

	@Override
	public Integer getBidSize() {
		return bidSize;
	}
	
	@Override
	public void setBidSize(Integer value) {
		bidSize = value;
	}

	@Override
	public Double getAskPrice() {
		return askPrice;
	}
	
	@Override
	public void setAskPrice(Double value) {
		askPrice = value;
	}

	@Override
	public Integer getAskSize() {
		return askSize;
	}
	
	@Override
	public void setAskSize(Integer value) {
		askSize = value;
	}

	@Override
	public Double getOpenPrice() {
		return openPrice;
	}
	
	@Override
	public void setOpenPrice(Double value) {
		openPrice = value;
	}

	@Override
	public Double getHighPrice() {
		return highPrice;
	}
	
	@Override
	public void setHighPrice(Double value) {
		highPrice = value;
	}

	@Override
	public Double getLowPrice() {
		return lowPrice;
	}
	
	@Override
	public void setLowPrice(Double value) {
		lowPrice = value;
	}

	@Override
	public Double getSettlementPrice() {
		return settlementPrice;
	}

	@Override
	public void setSettlementPrice(Double value) {
		settlementPrice = value;
	}
	
	@Override
	public Integer getVolume() {
		return volume;
	}
	
	@Override
	public void setVolume(Integer value) {
		volume = value;
	}

	@Override
	public Integer getOpenInterest() {
		return openInterest;
	}
	
	@Override
	public void setOpenInterest(Integer value) {
		openInterest = value;
	}

	@Override
	public String getTime() {
		return time;
	}
	
	@Override
	public void setTime(String value) {
		time = value;
	}

	@Override
	public String getTimeDisplay() {
		return timeDisplay;
	}
	
	@Override
	public void setTimeDisplay(String value) {
		timeDisplay = value;
	}


	@Override
	public String toString() {
		return String.format("[BasicMutableQuote (symbol: %s, sequence: %s)]", symbol, sequence == null ? "--" : sequence.intValue());
	}
}