package com.barchart.common;

public interface IAction<TData> {
	void execute(TData data);
}
