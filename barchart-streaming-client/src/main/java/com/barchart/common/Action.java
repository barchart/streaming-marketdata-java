package com.barchart.common;

public interface Action<TData> {
	void execute(TData data);
}
