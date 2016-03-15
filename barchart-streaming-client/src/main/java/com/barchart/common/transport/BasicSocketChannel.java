package com.barchart.common.transport;

import com.barchart.common.transport.SocketChannel;

enum BasicSocketChannel implements SocketChannel {
	Response("response");
	
	private final String channelName;
	
	BasicSocketChannel(final String channelName) {
		this.channelName = channelName;
	}
	
	@Override
	public String getChannelName() {
		return channelName;
	}
	
	@Override
	public String toString() {
		return String.format("[BasicSocketChannel (channelName: %s)]", channelName);
	}
}
