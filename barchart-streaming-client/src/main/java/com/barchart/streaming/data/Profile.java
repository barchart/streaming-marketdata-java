package com.barchart.streaming.data;

public interface Profile {
	String getSymbol();
	
	String getName();
	String getExchange();
	String getUnitCode();
	String getPointValue();
	String getTickIncrement();
	
	String getRoot();
	String getMonth();
	String getYear();
}
