package util.tree.impl;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import util.tree.Tree;

/**
 * A mutable tree using where iteration of nodes occurs in the order in which
 * nodes are added to this tree.
 * 
 * @author Shawn Hatch
 * @author Christopher R. Ludka
 * 
 * @param <N>
 */
public final class LinkedHashTree<N> extends AbstractMutableTree<N> {
	
	/**
	 * Constructs an empty mutable <tt>LinkedHashTree</tt> instance.
	 */
	public LinkedHashTree() {
		this(null);
	}
	
	/**
	 * Constructs an mutable <tt>LinkedHashTree</tt> instance with the same
	 * mappings as the specified tree.
	 * 
	 * @param <T>
	 *            node type
	 * @param tree
	 */
	public <T extends N> LinkedHashTree(Tree<T> tree) {
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
		return new LinkedHashMap<N, N>();
	}
	
	@Override
	protected Set<N> createNodeSet() {
		return new LinkedHashSet<N>();
	}
	
	@Override
	protected Map<N, Set<N>> createParentToChildMap() {
		return new LinkedHashMap<N, Set<N>>();
	}
	
}
