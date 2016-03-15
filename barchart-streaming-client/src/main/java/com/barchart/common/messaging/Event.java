package com.barchart.common.messaging;

import java.util.LinkedHashSet;
import java.util.Set;

import com.barchart.common.Action;
import com.barchart.common.Disposable;

public class Event<T> {
	private final String name;
	
	private Set<Action<T>> observers;
	private final Object observersLock;

	public Event(final String name) {
		this.name = name;
		
		this.observers = new LinkedHashSet<Action<T>>();
		this.observersLock = new Object();
	}
	
	public Disposable register(final Action<T> observer) {
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument cannot be null.");
		}
		
		final Disposable returnRef;
		
		synchronized (observersLock) {
			final Set<Action<T>> copy = new LinkedHashSet<Action<T>>();
			
			for (final Action<T> existing : observers) {
				copy.add(existing);
			}
			
			copy.add(observer);
			
			observers = copy;
			
			returnRef = new Disposable() {
				@Override
				public void dispose() {
					unregister(observer);
				}
			};
		}
		
		return returnRef;
	}
	
	public void unregister(final Action<T> observer) {
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument cannot be null.");
		}
		
		synchronized (observersLock) {
			if (observers.contains(observer)) {
				final Set<Action<T>> copy = new LinkedHashSet<Action<T>>();
				
				for (final Action<T> existing : observers) {
					if (existing != observer) {
						copy.add(existing);
					}
				}
				
				observers = copy;	
			}
		}
	}
	
	public void fire(T data) {
		final Iterable<Action<T>> observerReference = observers;
		
		for (final Action<T> observer : observerReference) {
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
