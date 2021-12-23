package util.tree.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Provides an immutable wrapper around an iterable. Specifically, the iterator
 * provided by this class throws an UnsupportedOperationException on remove().
 * 
 * @author Joshua Mark Rutherford
 * @author Shawn Hatch
 * 
 * @param <T>
 *            The type contained within the iterable.
 */
public final class ImmutableIterable<T> implements Iterable<T> {
	
	private static class ImmutableIterator<K> implements Iterator<K> {
		
		private Iterator<K> iterator;
		
		public ImmutableIterator(Iterator<K> iterator) {
			this.iterator = iterator;
		}
		
		@Override
		public boolean hasNext() {
			if (iterator == null) {
				return false;
			}
			return iterator.hasNext();
		}
		
		@Override
		public K next() {
			if (iterator == null) {				
				throw new NoSuchElementException();				
			}
			return iterator.next();
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * Creates a new instance of the com.saic.afk.common.util.ImmutableIterable
	 * class. Null is an allowable iterable to be wrapped.
	 */
	
	public ImmutableIterable(Iterable<T> iterable) {
		this.iterable = iterable;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<T> iterator() {
		if (iterable == null) {
			return new ImmutableIterator<>(null);
		}
		return new ImmutableIterator<>(iterable.iterator());
	}
	
	private Iterable<T> iterable;
	
}
