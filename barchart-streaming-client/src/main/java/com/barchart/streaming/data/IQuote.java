package com.barchart.streaming.data;

public interface IQuote {
	String getSymbol();
	
	Integer getSequence();
	boolean getOnline();
	
	String getFlag();
	String getMode();
	String getSession();
	
	String getDay();
	Integer getDayNum();
	
	Double getLastPrice();
	Double getPreviousPrice();
	
	Double getPriceChange();
	Double getPriceChangePercent();
	
	Double getTradePrice();
	Integer getTradeSize();
	
	Double getBidPrice();
	Integer getBidSize();
	Double getAskPrice();
	Integer getAskSize();
	
	Double getOpenPrice();
	Double getHighPrice();
	Double getLowPrice();
	Double getSettlementPrice();
	
	Integer getVolume();
	Integer getOpenInterest();
	
	String getTime();
	String getTimeDisplay();
}
