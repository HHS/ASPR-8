package util.tree.impl;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import util.tree.Tree;

/**
 * A mutable tree using Comparable or Comparator for iteration of nodes.
 * 
 * @author Shawn Hatch
 * @author Christopher R. Ludka
 * 
 * @param <N>
 */
public final class SortedTree<N> extends AbstractMutableTree<N> {
	
	private Comparator<N> comparator;
	
	/**
	 * Constructs an empty mutable <tt>SortedTree</tt> instance.
	 */
	public SortedTree() {
		this(null,null);
	}
	
	/**
	 * Constructs an empty mutable <tt>SortedTree</tt> instance that will be
	 * sorted according to the given {@link Comparator}.
	 * 
	 * @param comparator
	 */
	public SortedTree(Comparator<N> comparator) {
		this(null,comparator);
	}
	
	/**
	 * Constructs an mutable <tt>SortedTree</tt> instance with the same mappings
	 * as the specified tree.
	 * 
	 * @param <T>
	 *            node type
	 * @param tree
	 */
	public SortedTree(Tree<N> tree) {
		this(tree, null);
	}
	
	/**
	 * Constructs an mutable <tt>SortedTree</tt> instance with the same mappings
	 * as the specified tree and sorted according to the given
	 * {@link Comparator}.
	 * 
	 * @param <T>
	 *            node type
	 * @param tree
	 * @param comparator
	 */
	public <T extends N> SortedTree(Tree<T> tree, Comparator<N> comparator) {
		this.comparator = comparator;
		init();
		if (tree != null) {
			for (T child : tree.getAllNodes()) {
				T parent = tree.getParentNode(child);
				addLink(parent, child);
			}
		}		
	}
	
	@Override
	protected Map<N, N> createChildToParentMap() {
		if (comparator == null) {
			return new TreeMap<N, N>();
		}
		return new TreeMap<N, N>(comparator);
	}
	
	@Override
	protected Set<N> createNodeSet() {
		if (comparator == null) {
			return new TreeSet<N>();
		}
		return new TreeSet<N>(comparator);
	}
	
	@Override
	protected Map<N, Set<N>> createParentToChildMap() {
		if (comparator == null) {
			return new TreeMap<N, Set<N>>();
		}
		return new TreeMap<N, Set<N>>(comparator);
	}
	
}
