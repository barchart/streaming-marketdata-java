# streaming-marketdata-java

## A Java library for accessing Barchart's streaming market data feed

A remote server vends Barchart's market data feed. This client library 
uses [socket.io](http://socket.io) to access the feed and allows clients to:

* Request basic **Profile** data for individual symbols
* Subscribe to **Quote** updates for individual symbols

##Setup

The can be referenced and built with maven. Please refer to the
pom.xml file in the barchart-streaming-client directory.

	cd [project-directory]/barchart-streaming-client
	mvn clean
	mvn install
	
	
#Example

A simple example project can be found in the barchart-streaming-client-test
directory. There is no need to include this project with your application.

Please refer to the main method of:

	com.barchart.streaming.test.StreamingTestClient


##Usage

The Barchart market data server uses socket.io[http://socket.io] endpoints. This
library is a convenience-wrapper. So, instead of making the socket.io connection
directly, consumers can make single-line method calls to perform asynchronous
operations (e.g. subscribe quotes, subscribe prices, get profile).


### Simple Java Example

	MarketSocketConnection c = new MarketSocketConnection();
	c.connect();
	
	final MutableQuote tsla = new BasicMutableQuote("TSLA");
				
	c.subscribeToPriceChanges(tsla.getSymbol(), new Action<Synchronizer<MutableQuote>>() {
		@Override
		public void execute(Synchronizer<MutableQuote> synchronizer) {
			synchronizer.synchronize(tsla);
		}
	});
	

## Object Model


### com.barchart.streaming.connection.MarketSocketConnection

Manages the underlying connection to the JERQ market data server.

Three primary operations exist:

* subscribeToQuotes - Creates a subscription for a symbol. Changes are published when any property of the Quote changes. This method updates very frequently.
* subscribeToPriceChanges - Creates a subscription for a symbol. Changes are published only when a trade occurs at a different price level. This method updates far less frequently than the "subscribeToQuotes" method. 
* requestProfile - Asynchronous lookup symbol metadata.

All operations are thread-safe.


### com.barchart.streaming.data.MutableQuote

The contract for an instrument quote that can be updated. 

Each time the quote changes, a Synchronizer<MutableQuote> will 
be generated. This synchronizer will update any properties of the
MutableQuote which have changed.

The implementation of this interface is the responsibility of the
client. In a user interface, setter methods could be written to
perform data-binding.


### com.barchart.common.data.Synchronizer

A thread-safe mechanism for updating a data structure.

	
## Environments

### Staging

The staging environment can be used for testing.

	jerq-aggregator-stage.us-east-1.elasticbeanstalk.com
	

### Production

The production environment has not been defined.


## Questions

Please email bryan.ingle@barchart.com.