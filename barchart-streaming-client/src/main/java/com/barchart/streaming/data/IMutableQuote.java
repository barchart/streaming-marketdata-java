package com.barchart.streaming.data;

public interface IMutableQuote extends IQuote {
	void setSequence(Integer value);
	void setOnline(boolean value);
	
	void setFlag(String value);
	void setMode(String value);
	void setSession(String value);
	
	void setDay(String value);
	void setDayNum(Integer value);
	
	void setLastPrice(Double value);
	void setPreviousPrice(Double value);
	
	void setPriceChange(Double value);
	void setPriceChangePercent(Double value);
	
	void setTradePrice(Double value);
	void setTradeSize(Integer value);
	
	void setBidPrice(Double value);
	void setBidSize(Integer value);
	void setAskPrice(Double value);
	void setAskSize(Integer value);
	
	void setOpenPrice(Double value);
	void setHighPrice(Double value);
	void setLowPrice(Double value);
	void setSettlementPrice(Double value);
	
	void setVolume(Integer value);
	void setOpenInterest(Integer value);
	
	void setTime(String value);
	void setTimeDisplay(String value);
}
