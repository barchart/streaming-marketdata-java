package com.barchart.common.messaging;

import java.util.LinkedHashSet;
import java.util.Set;

import com.barchart.common.IAction;
import com.barchart.common.IDisposable;

public class Event<T> {
	private final String _name;
	
	private Set<IAction<T>> _observers;
	private final Object _observersLock;

	public Event(final String name) {
		_name = name;
		
		_observers = new LinkedHashSet<IAction<T>>();
		_observersLock = new Object();
	}
	
	public IDisposable register(final IAction<T> observer) {
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument cannot be null.");
		}
		
		final IDisposable returnRef;
		
		synchronized (_observersLock) {
			final Set<IAction<T>> copy = new LinkedHashSet<IAction<T>>();
			
			for (final IAction<T> existing : _observers) {
				copy.add(existing);
			}
			
			copy.add(observer);
			
			_observers = copy;
			
			returnRef = new IDisposable() {
				@Override
				public void dispose() {
					unregister(observer);
				}
			};
		}
		
		return returnRef;
	}
	
	public void unregister(final IAction<T> observer) {
		if (observer == null) {
			throw new IllegalArgumentException("The \"observer\" argument cannot be null.");
		}
		
		synchronized (_observersLock) {
			if (_observers.contains(observer)) {
				final Set<IAction<T>> copy = new LinkedHashSet<IAction<T>>();
				
				for (final IAction<T> existing : _observers) {
					if (existing != observer) {
						copy.add(existing);
					}
				}
				
				_observers = copy;	
			}
		}
	}
	
	public void fire(T data) {
		final Iterable<IAction<T>> observers = _observers;
		
		for (final IAction<T> observer : observers) {
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
