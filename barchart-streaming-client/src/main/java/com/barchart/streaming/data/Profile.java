package com.barchart.streaming.data;

public class Profile implements IProfile {
	private final String _symbol;
	private final String _name;
	private final String _exchange;
	private final String _unitCode;
	private final String _pointValue;
	private final String _tickIncrement;
	private final String _root;
	private final String _month;
	private final String _year;
	
	public Profile(final String symbol, final String name, final String exchange, final String unitCode, final String pointValue, final String tickIncrement, final String root, final String month, final String year) {
		_symbol = symbol;
		_name = name;
		_exchange = exchange;
		_unitCode = unitCode;
		_pointValue = pointValue;
		_tickIncrement = tickIncrement;
		_root = root;
		_month = month;
		_year = year;
	}

	@Override
	public String getSymbol() {
		return _symbol;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getExchange() {
		return _exchange;
	}

	@Override
	public String getUnitCode() {
		return _unitCode;
	}

	@Override
	public String getPointValue() {
		return _pointValue;
	}

	@Override
	public String getTickIncrement() {
		return _tickIncrement;
	}

	@Override
	public String getRoot() {
		return _root;
	}

	@Override
	public String getMonth() {
		return _month;
	}

	@Override
	public String getYear() {
		return _year;
	}
	
	@Override
	public String toString() {
		return String.format("[Profile (symbol: %s)]", _symbol);
	}
}
