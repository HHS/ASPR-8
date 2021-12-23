package util.tree.impl;

import util.tree.Tree;

/**
 * This class implements the <tt>hashCode</tt> and <tt>equals</tt> portions of
 * the {@link Tree} interface. All iterators returned by this implementation
 * will fail to mutate the tree and will throw
 * {@link UnsupportedOperationException} if <tt>remove</tt> is called on the
 * iterator.
 * 
 * @author Shawn Hatch
 * @author Christopher R. Ludka
 * 
 * @param <N>
 *            the type of nodes maintained by this tree
 */

public abstract class AbstractTree<N> implements Tree<N> {
	
	/**
	 * Returns <tt>true</tt> if the object meets the equality criteria for
	 * {@link tree}. Equality is <tt>true</tt> if the given object is also a
	 * <tt>Tree</tt>, the two trees are the same size, and contain <i>equal</i>
	 * corresponding pairs of elements occurring in the same order.
	 * <p>
	 * Recall by definition, all nodes have exactly one parent (possibly the
	 * null root) and zero to many children. Therefore, order can be determined
	 * by examining if each node shares the same parent. The same parent is
	 * determined by examining if the parents <tt>areRelated</tt> which
	 * implements the equals contract.
	 * 
	 * @param obj
	 *            object to be compared for equality with this <tt>Tree</tt>
	 * @return <tt>true</tt> if the specified object is equal to this
	 *         <tt>Tree</tt>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		// If the object is itself return true
		if (this == obj) {
			return true;
		}
		
		// Null check
		if (obj == null) {
			return false;
		}
		
		// If the object is not a tree return false
		if (!(obj instanceof Tree)) {
			return false;
		}
		
		@SuppressWarnings("rawtypes")
		Tree other = (Tree) obj;
		
		// If the two trees are not the same size return false
		if (other.getAllCount() != getAllCount()) {
			return false;
		}
		
		// Check that both trees contain 'equal' corresponding pairs of elements
		
		for (N node : getAllNodes()) {
			// Containment check
			if (!other.containsNode(node)) {
				return false;
			}
			// Establish that each node shares the same parent by examining if
			// those parents are 'equal'
			N parent = getParentNode(node);
			if (parent == null) {
				// Parent is a root node, so the other node must also be a root
				// node
				if (other.getParentNode(node) != null) {
					return false;
				}
			} else {
				if (!parent.equals(other.getParentNode(node))) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Returns the hash code value for {@link Tree}. The hash code of
	 * <tt>Tree</tt> is defined to be the sum of the hash codes of each node in
	 * the <tt>Tree</tt>.
	 * 
	 * @return the hash code value for this map
	 */
	@Override
	public int hashCode() {
		int result = 0;
		for (N node : getAllNodes()) {
			result += node.hashCode();
		}
		return result;
	}
	
	/**
	 * Returns a string representation of this tree. The string representation
	 * consists of a list of parent-child pairs where the child parts are in the
	 * order returned by the tree's <tt>getAllNodes</tt> iterable, enclosed in
	 * braces ( <tt>"{}"</tt>). Adjacent pairs are separated by the characters
	 * <tt>", "</tt> (comma and space). Each pair is rendered as the parent
	 * followed by an arrow sign (<tt>"->"</tt>) followed by the associated
	 * child. Nodes are converted to strings as by
	 * {@link String#valueOf(Object)}.
	 * 
	 * @return a string representation of this tree
	 */
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (N child : getAllNodes()) {
			if (sb.length() > 1) {
				sb.append(", ");
			}
			N parent = getParentNode(child);
			if (parent == this) {
				sb.append("(this Tree)");
			} else {
				sb.append(parent);
			}
			sb.append("->");
			if (child == this) {
				sb.append("(this Tree)");
			} else {
				sb.append(child);
			}
		}
		sb.append('}');
		return sb.toString();
	}
}
