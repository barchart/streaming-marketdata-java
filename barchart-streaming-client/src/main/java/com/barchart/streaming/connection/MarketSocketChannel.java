package com.barchart.streaming.connection;

import com.barchart.common.transport.SocketChannel;

enum MarketSocketChannel implements SocketChannel {
	RequestProfile("request/profile"),
	Response("response"),
	
	SubscribeTimestamp("subscribe/timestamp"),
	UnsubscribeTimestamp("unsubscribe/timestamp"),
	Timestamp("timestamp"),
	
	ChangeSymbolSubscription("subscribe/symbols"),
	QuoteSnapshot("quote/snapshot"),
	QuoteDelta("quote/delta"),
	
	ProfileSnapshot("profile/snapshot");
	
	private final String channelName;
	
	MarketSocketChannel(final String channelName) {
		this.channelName = channelName;
	}
	
	@Override
	public String getChannelName() {
		return channelName;
	}
	
	@Override
	public String toString() {
		return String.format("[MarketSocketChannel (channelName: %s)]", channelName);
	}
}
