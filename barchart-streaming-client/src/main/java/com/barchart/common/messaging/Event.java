package com.barchart.common.messaging;

import java.util.LinkedHashSet;
import java.util.Set;

import com.barchart.common.Action;
import com.barchart.common.Disposable;

public class Event<T> {
	private final String _name;
	
	private Set<Action<T>> _observers;
	private final Object _observersLock;

	public Event(final String name) {
		_name = name;
		
		_observers = new LinkedHashSet<Action<T>>();
		_observersLock = new Object();
	}
	
	public Disposable register(final Action<T> observer) {
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument cannot be null.");
		}
		
		final Disposable returnRef;
		
		synchronized (_observersLock) {
			final Set<Action<T>> copy = new LinkedHashSet<Action<T>>();
			
			for (final Action<T> existing : _observers) {
				copy.add(existing);
			}
			
			copy.add(observer);
			
			_observers = copy;
			
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
		
		synchronized (_observersLock) {
			if (_observers.contains(observer)) {
				final Set<Action<T>> copy = new LinkedHashSet<Action<T>>();
				
				for (final Action<T> existing : _observers) {
					if (existing != observer) {
						copy.add(existing);
					}
				}
				
				_observers = copy;	
			}
		}
	}
	
	public void fire(T data) {
		final Iterable<Action<T>> observers = _observers;
		
		for (final Action<T> observer : observers) {
			observer.execute(data);
		}
	}
	
	public boolean getIsEmpty() {
		return _observers.isEmpty();
	}
	
	@Override
	public String toString() {
		return String.format("[Event (name: %)]", _name);
	}
}
