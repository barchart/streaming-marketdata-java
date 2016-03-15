package com.barchart.streaming.data;

public class BasicProfile implements Profile {
	private final String symbol;
	
	private final String name;
	private final String exchange;
	private final String unitCode;
	private final String pointValue;
	private final String tickIncrement;
	
	private final String root;
	private final String month;
	private final String year;
	
	public BasicProfile(final String symbol, final String name, final String exchange, final String unitCode, final String pointValue, final String tickIncrement, final String root, final String month, final String year) {
		this.symbol = symbol;
		
		this.name = name;
		this.exchange = exchange;
		this.unitCode = unitCode;
		this.pointValue = pointValue;
		this.tickIncrement = tickIncrement;
		
		this.root = root;
		this.month = month;
		this.year = year;
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getExchange() {
		return exchange;
	}

	@Override
	public String getUnitCode() {
		return unitCode;
	}

	@Override
	public String getPointValue() {
		return pointValue;
	}

	@Override
	public String getTickIncrement() {
		return tickIncrement;
	}

	@Override
	public String getRoot() {
		return root;
	}

	@Override
	public String getMonth() {
		return month;
	}

	@Override
	public String getYear() {
		return year;
	}
	
	@Override
	public String toString() {
		return String.format("[BasicProfile (symbol: %s)]", symbol);
	}
}
