package com.barchart.realtime.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.common.IAction;
import com.barchart.common.data.ISynchronizer;
import com.barchart.common.transport.SocketConnectionState;
import com.barchart.streaming.connection.MarketSocketConnection;
import com.barchart.streaming.data.IQuote;

public class RealtimeTestClient {
	private static final Logger logger;
	
	static {
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");
		
		logger = LoggerFactory.getLogger(RealtimeTestClient.class);
	}
	
	public static void main(String[] args) {
		final BlockingQueue<String> workQueue = new LinkedBlockingQueue<String>();
		
		//final MarketSocketConnection c = new MarketSocketConnection("jerq-aggregator-stage.elasticbeanstalk.com", 80, false);
		final MarketSocketConnection c = new MarketSocketConnection("localhost", 8082, false);
		
		c.registerConnectionStateChangeObserver(new IAction<SocketConnectionState>() {
			@Override
			public void execute(SocketConnectionState data) {
				workQueue.add(String.format("Connection state changed to %s", data));
			}
		});
		
		c.subscribeToTimestamp(new IAction<String>() {
			@Override
			public void execute(String timestamp) {
				workQueue.add(String.format("Timestamp: %s", timestamp));
			}
		});
		
		c.subscribeToQuotes("TSLA", new IAction<ISynchronizer<IQuote>>() {
			@Override
			public void execute(ISynchronizer<IQuote> synchronizer) {
				workQueue.add(String.format("Quote synchronizer received: %s", synchronizer));
			}
		});

		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				c.connect();
				
				while (true) {
					try {
						logger.info(workQueue.take());
					} catch (InterruptedException e) {
						logger.warn("Thread interrupted.");
					}
					
				   if (Thread.interrupted()) {
					   logger.warn("Shutting down.");
					   return;
				   }
				}
			}
		}, "Test Client Thread");
		
		t.setDaemon(false);
		t.start();
	}
}
