package com.barchart.realtime.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.common.Action;
import com.barchart.common.data.Synchronizer;
import com.barchart.common.transport.SocketConnectionState;
import com.barchart.streaming.connection.MarketSocketConnection;
import com.barchart.streaming.data.MutableQuote;
import com.barchart.streaming.data.Profile;
import com.barchart.streaming.data.BasicMutableQuote;

public class RealtimeTestClient {
	private static final Logger logger;
	
	static {
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");
		
		logger = LoggerFactory.getLogger(RealtimeTestClient.class);
	}
	
	public static void main(String[] args) {
		final BlockingQueue<String> workQueue = new LinkedBlockingQueue<String>();
		
		final MarketSocketConnection c = new MarketSocketConnection("jerq-aggregator-test.us-east-1.elasticbeanstalk.com", 80, false);
		
		c.registerConnectionStateChangeObserver(new Action<SocketConnectionState>() {
			@Override
			public void execute(SocketConnectionState data) {
				workQueue.add(String.format("Connection state changed to %s", data));
				
				if (data == SocketConnectionState.Connected) {
					c.requestProfile("TSLA", new Action<Profile>() {
						@Override
						public void execute(Profile profile) {
							workQueue.add(String.format("Profile request fulfilled %s.", profile));
						}
					});
				}
			}
		});

		final MutableQuote eurusd = new BasicMutableQuote("^EURUSD");
		
		c.subscribeToQuotes(eurusd.getSymbol(), new Action<Synchronizer<MutableQuote>>() {
			@Override
			public void execute(Synchronizer<MutableQuote> synchronizer) {
				synchronizer.synchronize(eurusd);
				
				workQueue.add(String.format("Quote synchronizer received: %s", synchronizer));
				workQueue.add(String.format("Quote udpated: %s", eurusd));
			}
		});
		
		final MutableQuote tsla = new BasicMutableQuote("TSLA");
				
		c.subscribeToPriceChanges(tsla.getSymbol(), new Action<Synchronizer<MutableQuote>>() {
			@Override
			public void execute(Synchronizer<MutableQuote> synchronizer) {
				synchronizer.synchronize(tsla);
				
				workQueue.add(String.format("Quote synchronizer received: %s", synchronizer));
				workQueue.add(String.format("Quote udpated: %s", tsla));
			}
		});
		
		/*
		c.subscribeToTimestamp(new IAction<String>() {
			@Override
			public void execute(String timestamp) {
				workQueue.add(String.format("Timestamp: %s", timestamp));
			}
		});
		*/

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
