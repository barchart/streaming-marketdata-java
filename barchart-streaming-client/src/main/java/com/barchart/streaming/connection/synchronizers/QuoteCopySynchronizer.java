package com.barchart.streaming.connection.synchronizers;

import com.barchart.common.data.Synchronizer;
import com.barchart.streaming.data.MutableQuote;
import com.barchart.streaming.data.Quote;

public class QuoteCopySynchronizer implements Synchronizer<MutableQuote> {
	private final String symbol;
	
	private final Integer sequence;
	private final boolean online;
		
	private final String flag;
	private final String mode;
	private final String session;
		
	private final String day;
	private final Integer dayNum;
		
	private final Double lastPrice;
	private final Double previousPrice;
		
	private final Double priceChange;
	private final Double priceChangePercent;
		
	private final Double tradePrice;
	private final Integer tradeSize;
		
	private final Double bidPrice;
	private final Integer bidSize;
	private final Double askPrice;
	private final Integer askSize;
		
	private final Double openPrice;
	private final Double highPrice;
	private final Double lowPrice;
	private final Double settlementPrice;
		
	private final Integer volume;
	private final Integer openInterest;
		
	private final String time;
	private final String timeDisplay;
	
	public QuoteCopySynchronizer(Quote quote) {
		if (quote == null) {
			throw new IllegalArgumentException("The \"quote\" argument is required.");
		}
		
		this.symbol = quote.getSymbol();
		
		this.sequence = quote.getSequence();
		this.online = quote.getOnline();
			
		this.flag = quote.getFlag();
		this.mode = quote.getMode();
		this.session = quote.getSession();
			
		this.day = quote.getDay();
		this.dayNum = quote.getDayNum();
			
		this.lastPrice = quote.getLastPrice();
		this.previousPrice = quote.getPreviousPrice();
			
		this.priceChange = quote.getPriceChange();
		this.priceChangePercent = quote.getPriceChangePercent();
			
		this.tradePrice = quote.getTradePrice();
		this.tradeSize = quote.getTradeSize();
			
		this.bidPrice = quote.getBidPrice();
		this.bidSize = quote.getBidSize();
		this.askPrice = quote.getAskPrice();
		this.askSize = quote.getAskSize();
			
		this.openPrice = quote.getOpenPrice();
		this.highPrice = quote.getHighPrice();
		this.lowPrice = quote.getLowPrice();
		this.settlementPrice = quote.getSettlementPrice();
			
		this.volume = quote.getVolume();
		this.openInterest = quote.getOpenInterest();
			
		this.time = quote.getTime();
		this.timeDisplay = quote.getTimeDisplay();
	}

	@Override
	public void synchronize(final MutableQuote target) {
		if (target == null) {
			throw new IllegalArgumentException("The \"target\" argument is required.");
		}
		
		if (!symbol.equals(target.getSymbol())) {
			throw new IllegalArgumentException(String.format("The synchronizer does not apply to the \"target\" (target symbol: %s).", target.getSymbol()));
		}
		
		target.setSequence(sequence);
		target.setOnline(online);
		
		target.setFlag(flag);
		target.setMode(mode);
		target.setSession(session);
		
		target.setDay(day);
		target.setDayNum(dayNum);

		target.setLastPrice(lastPrice);
		target.setPreviousPrice(previousPrice);
		
		target.setPriceChange(priceChange);
		target.setPriceChangePercent(priceChangePercent);
		
		target.setTradePrice(tradePrice);
		target.setTradeSize(tradeSize);
		
		target.setBidPrice(bidPrice);
		target.setBidSize(bidSize);
		target.setAskPrice(askPrice);
		target.setAskSize(askSize);
		
		target.setOpenPrice(openPrice);
		target.setHighPrice(highPrice);
		target.setLowPrice(lowPrice);
		target.setSettlementPrice(settlementPrice);

		target.setVolume(volume);
		target.setOpenInterest(openInterest);
		
		target.setTime(time);
		target.setTimeDisplay(timeDisplay);
	}
	
	@Override
	public String toString() {
		return String.format("[QuoteCopySynchronizer (symbol: %s)]", symbol);
	}
}
