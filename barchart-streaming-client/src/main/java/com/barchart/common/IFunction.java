package com.barchart.common;

public interface IFunction<TData, TResult> {
	TResult execute(TData data);
}