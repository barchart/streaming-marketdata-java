package com.barchart.common.messaging;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.barchart.common.Action;
import com.barchart.common.Disposable;

public class Event<T> {
	private final String name;
	
	private Set<Action<T>> observers;

	public Event(final String name) {
		this.name = name;
		
		this.observers = new CopyOnWriteArraySet<Action<T>>();
	}
	
	public Disposable register(final Action<T> observer) {
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument cannot be null.");
		}
		
		observers.add(observer);
		
		return new Disposable() {
			@Override
			public void dispose() {
				unregister(observer);
			}
		};
	}
	
	public void unregister(final Action<T> observer) {
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument cannot be null.");
		}
		
		observers.remove(observer);
	}
	
	public void fire(T data) {
		for (final Action<T> observer : observers) {
			observer.execute(data);
		}
	}
	
	public boolean getIsEmpty() {
		return observers.isEmpty();
	}
	
	@Override
	public String toString() {
		return String.format("[Event (name: %)]", name);
	}
}
