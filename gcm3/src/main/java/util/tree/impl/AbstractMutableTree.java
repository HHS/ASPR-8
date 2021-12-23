package util.tree.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.tree.MutableTree;


/**
 * An implementation of {@link MutableTree}.
 * 
 * @author Shawn Hatch
 * @author Christopher R. Ludka
 * 
 * @param <N>
 *            the type of nodes maintained by this tree
 */
public abstract class AbstractMutableTree<N> extends AbstractTree<N> implements MutableTree<N> {

	// Maps parent -> children:
	// Note: All nodes are parent candidates so all nodes reside in this map
	private Map<N, Set<N>> parentToChildMap;

	// Maps child --> parent
	private Map<N, N> childToParentMap;

	// maps null virtual parent -> Set(child)
	private Set<N> rootNodes;

	protected final void init() {
		parentToChildMap = createParentToChildMap();
		childToParentMap = createChildToParentMap();
		rootNodes = createNodeSet();
	}

	@Override
	public void addLink(N parent, N child) {
		// Add nodes if they do not exist
		addNode(parent);
		addNode(child);

		// If either node is null return
		if (parent == null || child == null) {
			return;
		}

		// If the link already exists return
		if (parent.equals(child)) {
			return;
		}

		// Update current linkages
		if (isDescendant(child, parent)) {
			// Remove parent's link if currently a descendant of the child
			removeLink(parent);
		}
		removeLink(child);
		rootNodes.remove(child);

		// Dual reference parent -> child linkage
		parentToChildMap.get(parent).add(child);
		childToParentMap.put(child, parent);
	}

	/*
	 * Returns true if the descendant has a lineage to the ancestor. Begin with
	 * the given descendant node and walk lineage of the tree 'up' until the
	 * ancestor is found or the trunk (node == null) is reached.
	 */
	private boolean isDescendant(N ancestor, N descendant) {
		boolean result = false;
		N node = descendant;
		while (node != null) {
			if (node.equals(ancestor)) {
				result = true;
				break;
			}
			node = childToParentMap.get(node);
		}
		return result;
	}

	@Override
	public void addNode(N node) {
		if ((node == null) || containsNode(node)) {
			return;
		}

		// Created as a root node
		rootNodes.add(node);
		parentToChildMap.put(node, createNodeSet());
	}

	@Override
	public boolean containsNode(Object node) {
		if (node == null) {
			return false;
		} else {
			return parentToChildMap.containsKey(node);
		}
	}

	protected abstract Map<N, N> createChildToParentMap();

	protected abstract Set<N> createNodeSet();

	protected abstract Map<N, Set<N>> createParentToChildMap();

	@Override
	public int getAllCount() {
		return parentToChildMap.size();
	}

	@Override
	public Iterable<N> getAllNodes() {
		return new ImmutableIterable<N>(parentToChildMap.keySet());
	}

	@Override
	public List<N> getAllNodesList() {
		return new ArrayList<>(parentToChildMap.keySet());
	}

	@Override
	public int getChildCount(N parent) {
		Set<N> children = getChildrenSet(parent);
		if (children == null) {
			return 0;
		}
		return children.size();
	}

	@Override
	public Iterable<N> getChildNodes(N parent) {
		Set<N> children = getChildrenSet(parent);
		if (children == null) {
			return new ImmutableIterable<N>(null);
		}
		return new ImmutableIterable<N>(children);
	}

	@Override
	public List<N> getChildNodesList(N parent) {
		List<N> result = new ArrayList<>();
		Set<N> children = getChildrenSet(parent);
		if (children != null) {
			result.addAll(children);
		}
		return result;
	}

	private Set<N> getChildrenSet(N parent) {
		if (!containsNode(parent)) {
			return null;
		}
		Set<N> children = parentToChildMap.get(parent);
		if (children == null) {
			throw new RuntimeException("Parent(" + parent.toString() + ") is not tracking its children.");
		}
		return children;
	}

	@Override
	public int getDescendantCount(N parent) {
		return getDescendantSet(parent).size();
	}

	@Override
	public Iterable<N> getDescendantNodes(N parent) {
		return new ImmutableIterable<N>(getDescendantSet(parent));
	}
	
	@Override
	public List<N> getDescendantNodesList(N parent) {
		return new ArrayList<>(getDescendantSet(parent));
	}

	private Set<N> getDescendantSet(N parent) {
		// Check if parent is valid
		Set<N> result = createNodeSet();
		if (!containsNode(parent)) {
			return result;
		}

		// Initialize
		Set<N> tempParentSet = createNodeSet();
		Set<N> tempChildrenSet = createNodeSet();
		Set<N> tempSwap;
		tempParentSet.add(parent);

		while (true) {
			// Add all parent's children
			for (N tempParent : tempParentSet) {
				tempChildrenSet.addAll(parentToChildMap.get(tempParent));
			}

			// All parents are added to the result
			result.addAll(tempParentSet);

			// If all parents had no children then result is complete
			if (tempChildrenSet.size() == 0) {
				break;
			}

			// Previous parent's children become next round of parents
			tempParentSet.clear();
			tempSwap = tempParentSet;
			tempParentSet = tempChildrenSet;
			tempChildrenSet = tempSwap;
		}

		// Remove artifact of the given parent being added
		result.remove(parent);
		return result;
	}

	@Override
	public N getParentNode(N node) {
		if (node == null) {
			return null;
		}
		return childToParentMap.get(node);
	}

	@Override
	public int getRootCount() {
		return rootNodes.size();
	}

	@Override
	public Iterable<N> getRootNodes() {
		return new ImmutableIterable<N>(rootNodes);
	}
	
	@Override
	public List<N> getRootNodesList() {
		return new ArrayList<>(rootNodes);
	}

	@Override
	public void removeLink(N child) {
		if (!containsNode(child)) {
			return;
		}

		// Remove Child -> Parent reference
		N parent = childToParentMap.remove(child);
		if (parent == null) {
			// Valid only occur if child is a root node
			if (!rootNodes.contains(child)) {
				throw new RuntimeException("Child(" + child.toString() + ") has a (parent == null) but is not recognized as a root node.");
			}
			return;
		}

		// Remove Parent -> Child reference
		Set<N> children = parentToChildMap.get(parent);
		if (children == null) {
			throw new RuntimeException("Parent(" + parent.toString() + ") did not contain any children when trying to remove the expected child(" + child.toString() + ").");
		}
		if (!children.remove(child)) {
			throw new RuntimeException("Parent(" + parent.toString() + ") with children(" + children.toString() + ") does not contain the expected child(" + child.toString() + ")");
		}

		// Add the child as a root node
		rootNodes.add(child);
	}

	@Override
	public void removeNode(N node) {
		if (!containsNode(node)) {
			return;
		}

		// Make the node and all it's children root nodes
		removeLink(node);
		List<N> list = new ArrayList<N>();
		for (N child : getChildNodes(node)) {
			list.add(child);
		}
		for (N child : list) {
			removeLink(child);
		}

		// Complete removals with validation
		if (getChildCount(node) != 0) {
			throw new RuntimeException("Node(" + node.toString() + ") has children(" + getChildNodes(node).toString() + ") when about to be removed.");
		}
		if (!rootNodes.remove(node)) {
			throw new RuntimeException("Node(" + node.toString() + ") is not a recognized root node when about to be removed.");
		}
		if (parentToChildMap.remove(node) == null) {
			throw new RuntimeException("Node(" + node.toString() + ") is not a recognized node when about to be removed.");
		}
	}

	@Override
	public void removeNodeAndAdoptDescendants(N node) {
		if (!containsNode(node)) {
			return;
		}

		// Obtain a list of orphans
		Set<N> orphans = parentToChildMap.get(node);
		List<N> list = new ArrayList<N>();
		for (N child : orphans) {
			list.add(child);
		}

		// Grand parent adopts the children before they become orphaned. If the
		// grand parent is null, then the children will become root nodes.
		N grandParent = getParentNode(node);
		for (N child : list) {
			addLink(grandParent, child);
		}

		// Remove the node
		removeNode(node);
	}

	@Override
	public void removeNodeAndDescendants(N node) {
		if (!containsNode(node)) {
			return;
		}

		// Remove the node and descendant nodes
		Set<N> nodesToRemove = getDescendantSet(node);
		nodesToRemove.add(node);
		for (N n : nodesToRemove) {
			removeNode(n);
		}
	}

}
