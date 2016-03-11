package com.barchart.streaming.data;

public interface IQuote {
	String getSymbol();
	
	Integer getBidSize();
	Integer getAskSize();
	
	Integer getSequence();
}
