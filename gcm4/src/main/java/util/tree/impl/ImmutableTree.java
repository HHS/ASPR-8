package util.tree.impl;

import java.util.List;

import util.tree.MutableTree;
import util.tree.Tree;

/**
 * A simple implementation of the {@link Tree} interface that uses a
 * {@link MutableTree} instance to internally contain the tree. All iterators
 * returned by this implementation will fail to mutate the tree and will throw
 * {@link UnsupportedOperationException} if <tt>remove</tt> is called on the
 * iterator.
 * 
 * @author Shawn Hatch
 * @author Christopher R. Ludka
 * 
 * @param <N>
 */

// UnmodifiableTree
public final class ImmutableTree<N> extends AbstractTree<N> {
	
	private Tree<N> tree;
	
	@SuppressWarnings("unused")
    private ImmutableTree() {
		//hidden constructor
	}
	
	public ImmutableTree(Tree<N> tree) {
		super();
		this.tree = tree;
	}

	
	@Override
	public boolean containsNode(Object obj) {
		return tree.containsNode(obj);
	}
	
	@Override
	public int getAllCount() {
		return tree.getAllCount();
	}
	
	@Override
	public Iterable<N> getAllNodes() {
		return tree.getAllNodes();
	}
	
	@Override
	public int getChildCount(N parent) {
		return tree.getChildCount(parent);
	}
	
	@Override
	public Iterable<N> getChildNodes(N node) {
		return tree.getChildNodes(node);
	}
	
	@Override
	public int getDescendantCount(N parent) {
		return tree.getDescendantCount(parent);
	}
	
	@Override
	public Iterable<N> getDescendantNodes(N node) {
		return tree.getDescendantNodes(node);
	}
	
	@Override
	public N getParentNode(N node) {
		return tree.getParentNode(node);
	}
	
	@Override
	public int getRootCount() {
		return tree.getRootCount();
	}
	
	@Override
	public Iterable<N> getRootNodes() {
		return tree.getRootNodes();
	}
	
	@Override
	public String toString() {
		return tree.toString();
	}

	@Override
	public List<N> getAllNodesList() {		
		return tree.getAllNodesList();
	}

	@Override
	public List<N> getChildNodesList(N parent) {		
		return tree.getChildNodesList(parent);
	}

	@Override
	public List<N> getDescendantNodesList(N parent) {
		return tree.getDescendantNodesList(parent);
	}

	@Override
	public List<N> getRootNodesList() {
		return tree.getRootNodesList();
	}
}
