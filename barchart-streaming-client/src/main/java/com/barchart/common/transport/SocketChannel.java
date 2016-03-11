package com.barchart.common.transport;

import com.barchart.common.transport.ISocketChannel;

enum SocketChannel implements ISocketChannel {
	Response("response");
	
	private final String _channelName;
	
	SocketChannel(final String channelName) {
		_channelName = channelName;
	}
	
	@Override
	public String getChannelName() {
		return _channelName;
	}
	
	@Override
	public String toString() {
		return String.format("[SocketChannel (channelName: %s)]", _channelName);
	}
}
