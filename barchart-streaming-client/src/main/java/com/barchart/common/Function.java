package com.barchart.common;

public interface Function<TData, TResult> {
	TResult execute(TData data);
}