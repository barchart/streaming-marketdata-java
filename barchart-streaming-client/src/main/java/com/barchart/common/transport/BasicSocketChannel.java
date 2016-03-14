package com.barchart.common.transport;

import com.barchart.common.transport.SocketChannel;

enum BasicSocketChannel implements SocketChannel {
	Response("response");
	
	private final String _channelName;
	
	BasicSocketChannel(final String channelName) {
		_channelName = channelName;
	}
	
	@Override
	public String getChannelName() {
		return _channelName;
	}
	
	@Override
	public String toString() {
		return String.format("[BasicSocketChannel (channelName: %s)]", _channelName);
	}
}
