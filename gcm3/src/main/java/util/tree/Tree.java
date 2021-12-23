package util.tree;

import java.util.List;

/**
 * An multi-rooted tree that presents as a forest (the trunk at depth 0 is not
 * modeled). The tree contains nodes (or vertices) that are generically typed
 * objects held in a collection. By definition, all nodes have exactly one
 * parent (or are a root node on the trunk) and zero to many children. Root
 * nodes will return <tt>null</tt> as their parent.
 * <p>
 * Implementors of this interface will follow the equals contract for
 * comparisons and containment. As such, any node object can be held in a
 * <tt>Tree</tt> instance at most once. Item inspection is {@link Iterable}
 * based. All <tt>Tree</tt> iterators will throw
 * {@link UnsupportedOperationException} on remove.
 * <p>
 * <b>Example:</b> For the following example see the tree below.
 * <ul>
 * <li>Depicted is a tree with 3 root nodes {1, 2, 3} at depth 1, where rooted
 * node '3' has additional descendant children with a depth 2 and 3.
 * </ul>
 * 
 * <pre>
 * + <i>(depth 0, trunk)</i>
 * |
 * +-+ 1 <i>(depth 1, rooted node)</i>
 * |
 * +-+ 2
 * |
 * +-+ 3
 * | |
 * | +-+ 3-1 <i>(depth 2)</i>
 * | |
 * | +-+ 3-2
 * | | |
 * | | +-+ 3-2-1 <i>(depth 3)</i>
 * | | |
 * | | +-+ 3-2-2
 * | | 
 * | +-+ 3-3
 * | | |
 * | | +-+ 3-3-1
 * | | |
 * | | +-+ 3-3-2
 * |
 * <i>(part of larger tree)</i>
 * </pre>
 * 
 * @see <a
 *      href="http://en.wikipedia.org/wiki/Tree_(graph_theory)#rooted_tree">http://en.wikipedia.org/wiki/Tree_(graph_theory)#rooted_tree</a>
 * 
 * @see <a
 *      href="http://mathworld.wolfram.com/RootedTree.html">http://mathworld.wolfram.com/RootedTree.html</a>
 * 
 * @author Shawn Hatch
 * @author Christopher R. Ludka
 * 
 * @param <N>
 *            the type of nodes maintained by this tree
 */
public interface Tree<N> {
	
	/**
	 * Returns <tt>true</tt> if given object exists in the <tt>Tree</tt>.
	 * <p>
	 * If the given object is <tt>null</tt> will return <tt>false</tt> since
	 * <tt>null</tt> does not explicitly exist in the <tt>Tree</tt>.
	 * <p>
	 * <b>Examples:</b> For the following (node) value examples see the tree
	 * below.
	 * <ul>
	 * <li><tt>containsNode(3)</tt>: <tt>true</tt>
	 * <li><tt>containsNode(3-3)</tt>: <tt>true</tt>
	 * <li><tt>containsNode(3-3-1)</tt>: <tt>true</tt>
	 * <li><tt>containsNode(4-2)</tt>: <tt>false</tt>, assuming '4-2' is
	 * <b>not</b> part of the larger tree.
	 * <li><tt>containsNode(null)</tt>: <tt>false</tt>
	 * </ul>
	 * 
	 * <pre>
	 * <i>(part of larger tree)</i>
	 * |
	 * +-+ 3
	 * | |
	 * | +-+ 3-1
	 * | |
	 * | +-+ 3-2
	 * | | |
	 * | | +-+ 3-2-1
	 * | | |
	 * | | +-+ 3-2-2
	 * | | 
	 * | +-+ 3-3
	 * | | |
	 * | | +-+ 3-3-1
	 * | | |
	 * | | +-+ 3-3-2
	 * |
	 * <i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * @param node
	 * @return <tt>true</tt> if given object exists in the <tt>Tree</tt>
	 */
	public boolean containsNode(Object node);
	
	/**
	 * Returns the number of nodes in this <tt>Tree</tt>. This count includes
	 * all rooted nodes and their child descendants.
	 * <p>
	 * <b>Example:</b> For the following example see the tree below.
	 * <ul>
	 * <li><tt>getAllCount()</tt>: 5
	 * </ul>
	 * 
	 * <pre>
	 * + <i>(trunk does <b>not</b> contribute to the count)</i>
	 * |
	 * +-+ 1 <i>(1)</i>
	 * |
	 * +-+ 2 <i>(2)</i>
	 * |
	 * +-+ 3 <i>(3)</i>
	 * | |
	 * | +-+ 3-1 <i>(4)</i>
	 * | |
	 * --+-+ 3-2 <i>(5)</i>
	 * </pre>
	 * 
	 * <p>
	 * <b>Example:</b> For the following example see the unpopulated (empty)
	 * tree below.
	 * <ul>
	 * <li><tt>getAllCount()</tt>: 0
	 * </ul>
	 * 
	 * <pre>
	 * + <i>(trunk does <b>not</b> contribute to the count)</i>
	 * |
	 * <i>unpopulated</i>
	 * </pre>
	 * 
	 * @return the number of nodes in this <tt>Tree</tt>
	 */
	public int getAllCount();
	
	/**
	 * Returns all nodes in the <tt>Tree</tt>.
	 * 
	 * <p>
	 * <b>Example:</b> For the following example see the tree below.
	 * <ul>
	 * <li><tt>getAllNodes()</tt>: {@link Iterable} set of nodes {1, 2, 3, 3-1,
	 * 3-2}.
	 * </ul>
	 * 
	 * <pre>
	 * + <i>(trunk <b>not</b> in the result)</i>
	 * |
	 * +-+ 1
	 * |
	 * +-+ 2
	 * |
	 * +-+ 3
	 * | |
	 * | +-+ 3-1
	 * | |
	 * --+-+ 3-2
	 * </pre>
	 * 
	 * <p>
	 * <b>Example:</b> For the following example see the unpopulated (empty)
	 * tree below.
	 * <ul>
	 * <li><tt>getAllNodes()</tt>: {@link Iterable} empty set.
	 * </ul>
	 * 
	 * <pre>
	 * + <i>(trunk <b>not</b> in the result)</i>
	 * |
	 * <i>unpopulated</i>
	 * </pre>
	 * 
	 * @return all nodes in the <tt>Tree</tt>
	 */
	public Iterable<N> getAllNodes();
	public List<N> getAllNodesList();
	
	/**
	 * Returns the number of first order children for the given parent.
	 * <p>
	 * Providing a <tt>null</tt> object for the parent node will return a value
	 * of zero since <tt>null</tt> does not explicitly exist in the
	 * <tt>Tree</tt>.
	 * <p>
	 * <b>Examples:</b> For the following (parent) value examples see the tree
	 * below.
	 * <ul>
	 * <li><tt>getChildCount(3)</tt>: 3, since '3' has the first order children
	 * of {3-1, 3-2, 3-3}.
	 * <li><tt>getChildCount(3-3)</tt>: 2, since '3-3' has the first order
	 * children of {3-2-1, 3-2-2}.
	 * <li><tt>getChildCount(3-3-1)</tt>: 0
	 * <li><tt>getChildCount(4-2)</tt>: 0, assuming '4-2' is <b>not</b> part of
	 * the larger tree.
	 * <li><tt>getChildCount(null)</tt>: 0. <b>Note:</b> To obtain the number of
	 * rooted nodes use <tt>getRootCount</tt>.
	 * </ul>
	 * 
	 * <pre>
	 * <i>(part of larger tree)</i>
	 * |
	 * +-+ 3
	 * | |
	 * | +-+ 3-1
	 * | |
	 * | +-+ 3-2
	 * | | |
	 * | | +-+ 3-2-1
	 * | | |
	 * | | +-+ 3-2-2
	 * | | 
	 * | +-+ 3-3
	 * | | |
	 * | | +-+ 3-3-1
	 * | | |
	 * | | +-+ 3-3-2
	 * |
	 * <i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * @param parent
	 * @return the number of first order children for the given parent
	 */
	public int getChildCount(N parent);
	
	/**
	 * Returns the first order children for the given parent.
	 * <p>
	 * If the given parent is <tt>null</tt> or cannot be found in the
	 * <tt>Tree</tt> an {@link Iterable} empty set will be returned.
	 * <p>
	 * <b>Example:</b> For the following <tt>getChildNodes(parent)</tt> example
	 * see the tree below.
	 * <ul>
	 * <li><tt>getChildNodes(3)</tt>: {@link Iterable} set of nodes {3-1, 3-2,
	 * 3-3}. Note that {3-2-1, 3-2-2, 3-3-1, 3-3-2} are <b>not</b> in the result
	 * because those nodes are second order children nodes from '3', not first
	 * order.
	 * </ul>
	 * 
	 * <pre>
	 * <i>(part of larger tree)</i>
	 * |
	 * +-+ 3 <i>(given parent)</i>
	 * | |
	 * | +-+ 3-1 <i>(in result)</i>
	 * | |
	 * | +-+ 3-2 <i>(in result)</i>
	 * | | |
	 * | | +-+ 3-2-1 <i>(<b>not</b> in result)</i>
	 * | | |
	 * | | +-+ 3-2-2 <i>(<b>not</b> in result)</i>
	 * | | 
	 * | +-+ 3-3 <i>(in result)</i>
	 * | | |
	 * | | +-+ 3-3-1 <i>(<b>not</b> in result)</i>
	 * | | |
	 * | | +-+ 3-3-2 <i>(<b>not</b> in result)</i>
	 * |
	 * <i>(part of larger tree)</i>
	 * </pre>
	 * <p>
	 * <b>Examples:</b> For the following (parent) value examples see the tree
	 * below.
	 * <ul>
	 * <li><tt>getChildNodes(3-1)</tt>: {@link Iterable} empty set.
	 * <li><tt>getChildNodes(3-2)</tt>: {@link Iterable} set of nodes {3-2-1,
	 * 3-2-2}.
	 * <li><tt>getChildNodes(4-2)</tt>: {@link Iterable} empty set, assuming
	 * '4-2' is <b>not</b> part of the larger tree.
	 * <li><tt>getChildNodes(null)</tt>: {@link Iterable} empty set.
	 * <b>Note:</b> To get all the rooted nodes and their descendants use
	 * <tt>getRootNodes</tt>.
	 * </ul>
	 * 
	 * <pre>
	 * + <i>(trunk)</i>
	 * |
	 * +-+ 1
	 * |
	 * +-+ 2
	 * |
	 * +-+ 3
	 * | |
	 * | +-+ 3-1
	 * | |
	 * | +-+ 3-2
	 * | | |
	 * | | +-+ 3-2-1
	 * | | |
	 * | | +-+ 3-2-2
	 * | | 
	 * | +-+ 3-3
	 * | | |
	 * | | +-+ 3-3-1
	 * | | |
	 * | | +-+ 3-3-2
	 * |
	 * <i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * @param parent
	 * @return the first order children for the given parent
	 */
	public Iterable<N> getChildNodes(N parent);
	public List<N> getChildNodesList(N parent);
	
	/**
	 * Returns the number of descendant children for the given parent.
	 * <p>
	 * Providing a <tt>null</tt> object for the parent node will return a value
	 * of zero since <tt>null</tt> does not explicitly exist in the
	 * <tt>Tree</tt>.
	 * <p>
	 * 
	 * <b>Examples:</b> For the following (parent) value examples see the tree
	 * below.
	 * <ul>
	 * <li><tt>getDescendantCount(3)</tt>: 7, since '3' has descendants {3-1,
	 * 3-2, 3-2-1, 3-2-2, 3-3, 3-3-1, 3-3-2}.
	 * <li>
	 * <tt>getDescendantCount(3-3)</tt>: 2, since '3-3' has the descendants
	 * {3-2-1, 3-2-2}.
	 * <li><tt>getDescendantCount(3-3-1)</tt>: 0
	 * <li><tt>getDescendantCount(4-2)</tt>: 0, assuming '4-2' is <b>not</b>
	 * part of the larger tree.
	 * <li><tt>getDescendantCount(null)</tt>: 0. <b>Note:</b> To obtain the
	 * number of rooted nodes use <tt>getRootCount</tt>.
	 * 
	 * <pre>
	 * <i>(part of larger tree)</i>
	 * |
	 * +-+ 3
	 * | |
	 * | +-+ 3-1
	 * | |
	 * | +-+ 3-2
	 * | | |
	 * | | +-+ 3-2-1
	 * | | |
	 * | | +-+ 3-2-2
	 * | | 
	 * | +-+ 3-3
	 * | | |
	 * | | +-+ 3-3-1
	 * | | |
	 * | | +-+ 3-3-2
	 * |
	 * <i>(part of larger tree)</i>
	 * </pre>
	 * </ul>
	 * 
	 * @param parent
	 * @return the number of descendant children for the given parent
	 */
	public int getDescendantCount(N parent);
	
	/**
	 * Returns the given parent's descendants.
	 * <p>
	 * If the given parent is <tt>null</tt> or cannot be found in the
	 * <tt>Tree</tt> an {@link Iterable} empty set will be returned.
	 * <p>
	 * <b>Example:</b> For the following <tt>getChildNodes(parent)</tt> examples
	 * see the tree below.
	 * <ul>
	 * <li><tt>getDescendantNodes(3)</tt>: {@link Iterable} set of nodes {3-1,
	 * 3-2, 3-2-1, 3-2-2, 3-3, 3-3-1, 3-3-2}.
	 * <li><tt>getDescendantNodes(3-2)</tt>: {@link Iterable} set of nodes
	 * {3-2-1, 3-2-2}.
	 * <li><tt>getDescendantNodes(3-1)</tt>: {@link Iterable} empty set.
	 * <li><tt>getDescendantNodes(4-2)</tt>: {@link Iterable} empty set,
	 * assuming '4-2' is <b>not</b> part of the larger tree.
	 * <li><tt>getDescendantNodes(null)</tt>: {@link Iterable} empty set.
	 * <b>Note:</b> To get all the rooted nodes and their descendants use
	 * <tt>getAllNodes</tt>.
	 * 
	 * <pre>
	 * <i>(part of larger tree)</i>
	 * |
	 * +-+ 3 <i>(given parent)</i>
	 * | |
	 * | +-+ 3-1 <i>(in result)</i>
	 * | |
	 * | +-+ 3-2 <i>(in result)</i>
	 * | | |
	 * | | +-+ 3-2-1 <i>(in result)</i>
	 * | | |
	 * | | +-+ 3-2-2 <i>(in result)</i>
	 * | | 
	 * | +-+ 3-3 <i>(in result)</i>
	 * | | |
	 * | | +-+ 3-3-1 <i>(in result)</i>
	 * | | |
	 * | | +-+ 3-3-2 <i>(in result)</i>
	 * |
	 * <i>(part of larger tree)</i>
	 * </pre>
	 * </ul>
	 * 
	 * @param parent
	 * @return the given parent's descendants
	 */
	public Iterable<N> getDescendantNodes(N parent);
	public List<N> getDescendantNodesList(N parent);
	
	/**
	 * Returns the parent of the given child if that child is contained in this
	 * tree and has a parent contained in this tree. Returns null otherwise.
	 * 
	 * Recall, by definition all nodes have exactly one parent (or are a root
	 * node on the trunk) and zero to many children.
	 * <p>
	 * If the given child is a root node or cannot be found in the <tt>Tree</tt>
	 * then a value of <tt>null</tt> is returned.
	 * <p>
	 * <b>Example:</b> For the following <tt>getParentNode(child)</tt> examples
	 * see the tree below.
	 * <ul>
	 * <li><tt>getParentNode(3-2)</tt>: '3'
	 * <li><tt>getParentNode(3)</tt>: <tt>null</tt>
	 * <li><tt>getParentNode(4-2)</tt>: <tt>null</tt>, assuming '4-2' is
	 * <b>not</b> part of the larger tree.
	 * <li><tt>getParentNode(null)</tt>: <tt>null</tt>
	 * 
	 * <pre>
	 * + <i>(trunk)</i>
	 * |
	 * +-+ 1
	 * |
	 * +-+ 2
	 * |
	 * +-+ 3
	 * | |
	 * | +-+ 3-1
	 * | |
	 * --+-+ 3-2
	 * </pre>
	 * </ul>
	 * 
	 * @param child
	 * @return the parent of the given child
	 */
	public N getParentNode(N child);
	
	/**
	 * Returns the number of root nodes in this <tt>Tree</tt>. Rooted nodes are
	 * depth 1 in the <tt>Tree</tt> off the trunk, which is depth 0.
	 * <p>
	 * <b>Example:</b> For the following example see the tree below.
	 * <ul>
	 * <li><tt>getRootCount()</tt>: 3. Nodes {3-1, 3-2} are <b>not</b> part of
	 * the count because their parent is '3' rather than the trunk.
	 * </ul>
	 * 
	 * <pre>
	 * + <i>(depth 0, trunk <b>not</b> in the result)</i>
	 * |
	 * +-+ 1 <i>(depth 1, in the result)</i>
	 * |
	 * +-+ 2 <i>(depth 1, in the result)</i>
	 * |
	 * +-+ 3 <i>(depth 1, in the result)</i>
	 * | |
	 * | +-+ 3-1 <i>(depth 2, <b>not</b> in the result)</i>
	 * | |
	 * | +-+ 3-2 <i>(depth 2, <b>not</b> in the result)</i>
	 * |
	 * <i>(part of larger tree)</i>
	 * </pre>
	 * <p>
	 * <b>Example:</b> For the following example see the unpopulated (empty)
	 * tree below.
	 * <ul>
	 * <li><tt>getRootCount()</tt>: 0
	 * </ul>
	 * 
	 * <pre>
	 * + <i>(trunk does <b>not</b> contribute to the count)</i>
	 * |
	 * <i>unpopulated</i>
	 * </pre>
	 * </ul>
	 * 
	 * @return the number of root nodes in this <tt>Tree</tt>
	 */
	public int getRootCount();
	
	/**
	 * Returns the root nodes in this <tt>Tree</tt>. Rooted nodes are depth 1 in
	 * the <tt>Tree</tt> off the trunk, which is depth 0.
	 * <p>
	 * <b>Example:</b> For the following example see the tree below.
	 * <ul>
	 * <li><tt>getRootNodes()</tt>: {@link Iterable} set of root nodes {1, 2,
	 * 3}. Nodes {3-1, 3-2} are <b>not</b> part of the of the returned set
	 * because their parent is '3' rather than the trunk.
	 * </ul>
	 * 
	 * <pre>
	 * + <i>(depth 0, trunk <b>not</b> in the result)</i>
	 * |
	 * +-+ 1 <i>(depth 1, in the result)</i>
	 * |
	 * +-+ 2 <i>(depth 1, in the result)</i>
	 * |
	 * +-+ 3 <i>(depth 1, in the result)</i>
	 * | |
	 * | +-+ 3-1 <i>(depth 2, <b>not</b> in the result)</i>
	 * | |
	 * | +-+ 3-2 <i>(depth 2, <b>not</b> in the result)</i>
	 * |
	 * <i>(part of larger tree)</i>
	 * </pre>
	 * <p>
	 * <b>Example:</b> For the following example see the unpopulated (empty)
	 * tree below.
	 * <ul>
	 * <li><tt>getRootNodes()</tt>: {@link Iterable} empty set.
	 * </ul>
	 * 
	 * <pre>
	 * + <i>(trunk <b>not</b> in the result)</i>
	 * |
	 * <i>unpopulated</i>
	 * </pre>
	 * 
	 * @return the root nodes in this <tt>Tree</tt>
	 */
	public Iterable<N> getRootNodes();
	public List<N> getRootNodesList();
	
}
