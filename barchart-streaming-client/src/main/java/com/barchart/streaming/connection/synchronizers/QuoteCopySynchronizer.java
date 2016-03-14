package com.barchart.streaming.connection.synchronizers;

import com.barchart.common.data.ISynchronizer;
import com.barchart.streaming.data.IMutableQuote;
import com.barchart.streaming.data.IQuote;

public class QuoteCopySynchronizer implements ISynchronizer<IMutableQuote> {
	private final String _symbol;
	
	private final Integer _sequence;
	private final boolean _online;
		
	private final String _flag;
	private final String _mode;
	private final String _session;
		
	private final String _day;
	private final Integer _dayNum;
		
	private final Double _lastPrice;
	private final Double _previousPrice;
		
	private final Double _priceChange;
	private final Double _priceChangePercent;
		
	private final Double _tradePrice;
	private final Integer _tradeSize;
		
	private final Double _bidPrice;
	private final Integer _bidSize;
	private final Double _askPrice;
	private final Integer _askSize;
		
	private final Double _openPrice;
	private final Double _highPrice;
	private final Double _lowPrice;
	private final Double _settlementPrice;
		
	private final Integer _volume;
	private final Integer _openInterest;
		
	private final String _time;
	private final String _timeDisplay;
	
	public QuoteCopySynchronizer(IQuote quote) {
		if (quote == null) {
			throw new IllegalArgumentException("The \"quote\" argument is required.");
		}
		
		_symbol = quote.getSymbol();
		
		_sequence = quote.getSequence();
		_online = quote.getOnline();
			
		_flag = quote.getFlag();
		_mode = quote.getMode();
		_session = quote.getSession();
			
		_day = quote.getDay();
		_dayNum = quote.getDayNum();
			
		_lastPrice = quote.getLastPrice();
		_previousPrice = quote.getPreviousPrice();
			
		_priceChange = quote.getPriceChange();
		_priceChangePercent = quote.getPriceChangePercent();
			
		_tradePrice = quote.getTradePrice();
		_tradeSize = quote.getTradeSize();
			
		_bidPrice = quote.getBidPrice();
		_bidSize = quote.getBidSize();
		_askPrice = quote.getAskPrice();
		_askSize = quote.getAskSize();
			
		_openPrice = quote.getOpenPrice();
		_highPrice = quote.getHighPrice();
		_lowPrice = quote.getLowPrice();
		_settlementPrice = quote.getSettlementPrice();
			
		_volume = quote.getVolume();
		_openInterest = quote.getOpenInterest();
			
		_time = quote.getTime();
		_timeDisplay = quote.getTimeDisplay();
	}

	@Override
	public void synchronize(final IMutableQuote target) {
		if (target == null) {
			throw new IllegalArgumentException("The \"target\" argument is required.");
		}
		
		if (!_symbol.equals(target.getSymbol())) {
			throw new IllegalArgumentException(String.format("The synchronizer does not apply to the \"target\" (target symbol: %s).", target.getSymbol()));
		}
		
		target.setSequence(_sequence);
		target.setOnline(_online);
		
		target.setFlag(_flag);
		target.setMode(_mode);
		target.setSession(_session);
		
		target.setDay(_day);
		target.setDayNum(_dayNum);

		target.setLastPrice(_lastPrice);
		target.setPreviousPrice(_previousPrice);
		
		target.setPriceChange(_priceChange);
		target.setPriceChangePercent(_priceChangePercent);
		
		target.setTradePrice(_tradePrice);
		target.setTradeSize(_tradeSize);
		
		target.setBidPrice(_bidPrice);
		target.setBidSize(_bidSize);
		target.setAskPrice(_askPrice);
		target.setAskSize(_askSize);
		
		target.setOpenPrice(_openPrice);
		target.setHighPrice(_highPrice);
		target.setLowPrice(_lowPrice);
		target.setSettlementPrice(_settlementPrice);

		target.setVolume(_volume);
		target.setOpenInterest(_openInterest);
		
		target.setTime(_time);
		target.setTimeDisplay(_timeDisplay);
	}
	
	@Override
	public String toString() {
		return String.format("[QuoteCopySynchronizer (symbol: %s)]", _symbol);
	}
}
