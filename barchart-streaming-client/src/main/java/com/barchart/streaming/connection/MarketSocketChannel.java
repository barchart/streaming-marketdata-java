package com.barchart.streaming.connection;

import com.barchart.common.transport.ISocketChannel;

enum MarketSocketChannel implements ISocketChannel {
	SubscribeTimestamp("subscribe/timestamp"),
	UnsubscribeTimestamp("unsubscribe/timestamp"),
	Timestamp("timestamp"),
	
	ChangeSymbolSubscription("subscribe/symbols"),
	QuoteSnapshot("quote/snapshot"),
	QuoteDelta("quote/delta");
	
	private final String _channelName;
	
	MarketSocketChannel(final String channelName) {
		_channelName = channelName;
	}
	
	@Override
	public String getChannelName() {
		return _channelName;
	}
	
	@Override
	public String toString() {
		return String.format("[MarketSocketChannel (channelName: %s)]", _channelName);
	}
}
