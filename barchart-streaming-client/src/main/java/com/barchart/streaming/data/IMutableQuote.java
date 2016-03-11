package com.barchart.streaming.data;

public interface IMutableQuote extends IQuote {
	void setBidSize(Integer value);
	void setAskSize(Integer value);
	
	void setSequence(Integer value);
}
