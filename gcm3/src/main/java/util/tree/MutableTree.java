package util.tree;

/**
 * An extension to the {@link Tree} interface that specifies the mutable
 * behavior of {@link Tree}.
 * 
 * 
 * @author Shawn Hatch
 * @author Christopher R. Ludka
 * 
 * @param <N>
 */
public interface MutableTree<N> extends Tree<N> {
	
	/**
	 * Links a parent and child together in the tree.
	 * <p>
	 * There are several major functionalities that exist in <tt>addLink</tt>.
	 * Each functionality is described below with corresponding (parent, child)
	 * value examples and trees that depict the 'Before' and 'After' state of
	 * the mutation. These major functionalities include:
	 * <ul>
	 * <li>Linking a parent and a child together in the tree
	 * <li>Swapping existing linkages
	 * <li>Adding new nodes with linkages in a single method call
	 * </ul>
	 * <p>
	 * The following (parent, child) value examples for the trees below depict
	 * the mutation for each of these major functionalities.
	 * <p>
	 * <b>Linking a parent and a child together in the tree:</b> To add a
	 * linkage to an existing pair of nodes, specify the intended parent and
	 * child.
	 * <p>
	 * <b>Example:</b> For the following <tt>addLink(parent, child)</tt>
	 * examples the trees below depict the mutation.
	 * <ul>
	 * <li><tt>addLink(1, 2)</tt>:
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				| |
	 * +-+ 2				| +-+ 2
	 * |				|
	 * <i>(part of larger tree)</i>		<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * <p>
	 * 
	 * <li><tt>addLink(2, 1)</tt>:
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 2
	 * |				| |
	 * +-+ 2				| +-+ 1
	 * |				|
	 * <i>(part of larger tree)</i>		<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * </ul>
	 * 
	 * <b>Swapping existing linkages:</b> To swap an existing linkages, specify
	 * the new intended parent and child. All descendants will be carried with
	 * their parent in the swap. An <b>'ancestry conflict'</b> can occur when
	 * the <tt>addLink</tt> parent is currently a descendant of the
	 * <tt>addLink</tt> child. When an ancestry conflict occurs the
	 * <tt>addLink</tt> parent undergoes a <tt>removeLink</tt>, with full
	 * enforcement of the <tt>removeLink</tt> contract, making the
	 * <tt>addLink</tt> parent a root node. Then the <tt>addLink</tt> child is
	 * placed under the <tt>addLink</tt> parent, maintaining all of the
	 * remaining <tt>addLink</tt> child descendant linkages.
	 * <p>
	 * <b>Example:</b> For the following <tt>addLink(parent, child)</tt>
	 * examples the trees below depict the mutation.
	 * <ul>
	 * <li><tt>addLink(2, 1)</tt> swaps the parent with the child.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 2
	 * | |				| |
	 * | +-+ 2				| +-+ 1
	 * |				|
	 * <i>(part of larger tree)</i>		<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * <p>
	 * <li><tt>addLink(2, 3)</tt> swaps '3' from a root node to a child of '2'.
	 * All descendants of '3' are moved as part of the swap as well.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * |				| |
	 * +-+ 3				| +-+ 3
	 * | |				| | |
	 * | +-+ 3-1 			| | +-+ 3-1
	 * | |				| | |
	 * --+-+ 3-2			----+-+ 3-2
	 * </pre>
	 * 
	 * <li><tt>addLink(3-1, 2)</tt>: Since '3-1' is currently a descendant of
	 * '2', the <tt>removeLink('3-1')</tt> contract is first enforced making
	 * '3-1' a root node. Then '2' is added as a child of '3-1' where '2'
	 * maintains it's remaining descendants.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 3-1
	 * | | |				| |
	 * | | +-+ 3			| +-+ 2
	 * | | | |				| | |
	 * | | | +-+ 3-1			| | +-+ 3
	 * | | | |				| | |
	 * ------+-+ 3-2			----+-+ 3-2
	 * </pre>
	 * 
	 * <li><tt>addLink(3-1, 3)</tt>: Since, '3-1' is currently a descendant of
	 * '3', the <tt>removeLink('3-1')</tt> contract is first enforced making
	 * '3-1' a root node. Then '3' is added as a child of '3-1' where '3'
	 * maintains it's remaining descendants.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * | | |				| |
	 * | | +-+ 3			| +-+ 3-1
	 * | | | |				| | |
	 * | | | +-+ 3-1			| | +-+ 3
	 * | | | |				| | | |
	 * ------+-+ 3-2			------+-+ 3-2
	 * </pre>
	 * 
	 * </ul>
	 * <p>
	 * 
	 * <b>Adding new nodes with linkages in a single method call:</b> The parent
	 * and child are <b>not</b> required to contained in the tree before calling
	 * <tt>addLink</tt>. The given parent and child will be added to the tree if
	 * not already contained within the tree. This allows <tt>addLink</tt> to be
	 * used for quickly populating a tree in any arbitrary order (rather than
	 * having to first add nodes, then recursively adding links in a specific
	 * order). The behavior by which nodes are added follows the
	 * <tt>addNode</tt> contract.
	 * <p>
	 * <b>Example:</b> For the following <tt>addLink(parent, child)</tt>
	 * examples the trees below depict the mutation.
	 * <ul>
	 * <li><tt>addLink(2, 3)</tt>: '3' is added to the tree with '2' as it's
	 * parent.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * |				| |
	 * <i>(part of larger tree)</i>		|-+-+ 3
	 * 				|
	 * 				<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * <li><tt>addLink(3, 2)</tt>: '3' is added to the tree with '2' as it's
	 * child.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 3
	 * |				| |
	 * <i>(part of larger tree)</i>		|-+-+ 2
	 * 				|
	 * 				<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * </ul>
	 * Furthermore, <tt>addLink</tt> is <tt>null</tt> tolerant and adds new
	 * nodes even if the linkage overall cannot be created (in other words, due
	 * to a <tt>null</tt> or equal parent and child node being provided to
	 * <tt>addLink</tt>).
	 * <p>
	 * <b>Example:</b> For the following <tt>addLink(parent, child)</tt>
	 * examples the trees below depict the mutation.
	 * <ul>
	 * <li><tt>addLink(3, null)</tt>: '3' is added without a linkage.
	 * <li><tt>addLink(null, 3)</tt>: '3' is added without a linkage.
	 * <li><tt>addLink(3, 3)</tt>: '3' is added without a linkage.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * |				|
	 * <i>(part of larger tree)</i>		+-+ 3
	 * 				|
	 * 				<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * </ul>
	 * 
	 * @param parent
	 * @param child
	 */
	public void addLink(N parent, N child);
	
	/**
	 * Adds the given node to the tree as a root node.
	 * <p>
	 * No mutation occurs if the node is <tt>null</tt> or if the node already
	 * exists in the tree. To add a node as a child to an existing parent (as
	 * opposed to the truck as a root node) use <tt>addLink</tt> for convenient
	 * one step 'add-and-link' functionality.
	 * <p>
	 * <b>Example:</b> For the following <tt>addNode(node)</tt> examples the
	 * trees below depicts the mutation.
	 * <ul>
	 * <li><tt>addNode(3)</tt>: Adds node '3' to the tree.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * |				|
	 * <i>(part of larger tree)</i>		+-+ 3
	 * 				|
	 * 				<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * <li><tt>addNode(2)<tt>: Imparts <b>no mutation</b> since the tree
	 * already contains the node.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * |				|
	 * <i>(part of larger tree)</i>		<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * <li><tt>addNode(null)<tt>: Imparts <b>no mutation</b>.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * |				|
	 * <i>(part of larger tree)</i>		<i>(part of larger tree)</i>
	 * </pre>
	 * </ul>
	 * <p>
	 * 
	 * @param node
	 */
	public void addNode(N node);
	
	/**
	 * Removes the link between the child node and its parent in this tree. If
	 * the child is <tt>null</tt>, is not contained in the tree or is a root
	 * node then no mutation will occur.
	 * <p>
	 * <b>Example:</b> For the following <tt>removeLink(child)</tt> examples the
	 * trees below depict the mutation.
	 * <ul>
	 * <li><tt>removeLink(2)</tt>: '2' becomes a root node.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * | |				| 
	 * | +-+ 2				+-+ 2
	 * |				|
	 * <i>(part of larger tree)</i>		<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * <li><tt>removeLink(3)</tt>: '3' becomes a root node and maintains its
	 * descendants.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * | |				|	
	 * | +-+ 3				+-+ 3
	 * | | |				| |
	 * | | +-+ 3-1			| +-+ 3-1
	 * | | |				| |
	 * ----+-+ 3-2			--+-+ 3-2
	 * </pre>
	 * 
	 * <li><tt>removeLink(3-1)</tt>: '3-1' becomes a root node, '3' remains a
	 * child of '2', and '3-2' remains a child of '3'.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * | |				| |	
	 * | +-+ 3				| +-+ 3
	 * | | |				| |
	 * | | +-+ 3-1			| +-+ 3-2
	 * | | |				| 
	 * ----+-+ 3-2			+-+ 3-1
	 * </pre>
	 * 
	 * <li><tt>removeLink(1)</tt>: Imparts <b>no mutation</b>, since '1' is
	 * already a root node.
	 * <li><tt>removeLink(3)</tt>: Imparts <b>no mutation</b>, since '3' is not
	 * contained in the tree.
	 * <li><tt>removeLink(null)</tt>: Imparts <b>no mutation</b>.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * | |				| |
	 * | +-+ 2				| +-+ 2
	 * |				|
	 * <i>(part of larger tree)</i>		<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * </ul>
	 * 
	 * @param child
	 */
	public void removeLink(N child);
	
	/**
	 * Removes the node from the <tt>Tree</tt> severing all links to other
	 * nodes, but leaving all of the node's children in the tree at root level.
	 * <p>
	 * <b>Example:</b> For the following <tt>removeNode(node)</tt> examples the
	 * trees below depict the mutation.
	 * <ul>
	 * <li><tt>removeNode(1)</tt>:
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 2
	 * |				|
	 * +-+ 2				<i>(part of larger tree)</i>
	 * |
	 * <i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * <li><tt>removeNode(3-1)</tt>:
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * | |				| |
	 * | +-+ 3				| +-+ 3
	 * | | |				| | |
	 * | | +-+ 3-1			----+-+ 3-2
	 * | | |
	 * ----+-+ 3-2
	 * </pre>
	 * 
	 * <li><tt>removeNode(2)</tt>:
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 3
	 * | |				| |
	 * | +-+ 3				| +-+ 3-1
	 * | | |				| |
	 * | | +-+ 3-1			--+-+ 3-2
	 * | | |
	 * ----+-+ 3-2
	 * </pre>
	 * 
	 * <li><tt>removeNode(3)</tt>:
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * | |				|
	 * | +-+ 3				+-+ 3-1
	 * | | |				|
	 * | | +-+ 3-1			+-+ 3-2
	 * | | |
	 * ----+-+ 3-2
	 * </pre>
	 * 
	 * </ul>
	 * <p>
	 * <b>Example:</b> For the following <tt>removeNode(node)</tt> examples the
	 * trees below depict <b>no mutation</b> cases.
	 * <ul>
	 * <li><tt>removeNode(3)</tt>: Imparts <b>no mutation</b>.
	 * <li><tt>removeNode(null)</tt>: Imparts <b>no mutation</b>.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * |				|
	 * <i>(part of larger tree)</i>		<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * </ul>
	 * 
	 * @param node
	 */
	public void removeNode(N node);
	
	/**
	 * Removes the node from the <tt>Tree</tt> and all children of the node's
	 * descendants are then linked as children of the node's parent, if it
	 * exists.
	 * <p>
	 * <b>Example:</b> For the following
	 * <tt>removeNodeAndAdoptDescendants(node)</tt> examples the trees below
	 * depict the mutation.
	 * <ul>
	 * 
	 * <li><tt>removeNodeAndAdoptDescendants(3)</tt>: Node '3' is removed and
	 * '2' adopts '3-1' and '3-2'.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * | |				| |
	 * | +-+ 3				| +-+ 3-1
	 * | | |				| |
	 * | | +-+ 3-1			--+-+ 3-2
	 * | | |
	 * ----+-+ 3-2
	 * </pre>
	 * 
	 * <li><tt>removeNodeAndAdoptDescendants(1)</tt>: No descendants, acts the
	 * same as <tt>removeNode</tt>.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 2
	 * |				|
	 * +-+ 2				<i>(part of larger tree)</i>
	 * |
	 * <i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * <li><tt>removeNodeAndAdoptDescendants(3-1)</tt>: No descendants, acts the
	 * same as <tt>removeNode</tt>.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * | |				| |
	 * | +-+ 3				| +-+ 3
	 * | | |				| | |
	 * | | +-+ 3-1			----+-+ 3-2
	 * | | |
	 * ----+-+ 3-2
	 * </pre>
	 * 
	 * <li><tt>removeNodeAndAdoptDescendants(2)</tt>: '3' becomes a root node
	 * and maintains its descendants.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 3
	 * | |				| |
	 * | +-+ 3				| +-+ 3-1
	 * | | |				| |
	 * | | +-+ 3-1			--+-+ 3-2
	 * | | |
	 * ----+-+ 3-2
	 * </pre>
	 * 
	 * </ul>
	 * <p>
	 * <b>Example:</b> For the following <tt>removeNode(node)</tt> examples the
	 * trees below depict <b>no mutation</b> cases.
	 * <ul>
	 * <li><tt>removeNodeAndAdoptDescendants(3)</tt>: Imparts <b>no
	 * mutation</b>.
	 * <li><tt>removeNodeAndAdoptDescendants(null)</tt>: Imparts <b>no
	 * mutation</b>.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * |				|
	 * <i>(part of larger tree)</i>		<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * </ul>
	 * 
	 * @param node
	 */
	public void removeNodeAndAdoptDescendants(N node);
	
	/**
	 * Removes the node from the <tt>Tree</tt> and all of its children
	 * recursively.
	 * <p>
	 * <b>Example:</b> For the following <tt>removeNodeAndDescendants(node)</tt>
	 * examples the trees below depict the mutation.
	 * <ul>
	 * 
	 * <li><tt>removeNodeAndDescendants(3)</tt>: Node '3' and its descendants
	 * are removed.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				--+ 2
	 * | |
	 * | +-+ 3
	 * | | |
	 * | | +-+ 3-1
	 * | | |
	 * ----+-+ 3-2
	 * </pre>
	 * 
	 * <li><tt>removeNodeAndDescendants(2)</tt>: Node '2' and its descendants
	 * are removed.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |
	 * +-+ 2
	 * | |
	 * | +-+ 3
	 * | | |
	 * | | +-+ 3-1
	 * | | |
	 * ----+-+ 3-2
	 * </pre>
	 * 
	 * <li><tt>removeNodeAndDescendants(1)</tt>: No descendants, acts the same
	 * as <tt>removeNode</tt>.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 2
	 * |				|
	 * +-+ 2				<i>(part of larger tree)</i>
	 * |
	 * <i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * <li><tt>removeNodeAndDescendants(3-1)</tt>: No descendants, acts the same
	 * as <tt>removeNode</tt>.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(trunk)</i>			+ <i>(trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * | |				| |
	 * | +-+ 3				| +-+ 3
	 * | | |				| | |
	 * | | +-+ 3-1			----+-+ 3-2
	 * | | |
	 * ----+-+ 3-2
	 * </pre>
	 * 
	 * </ul>
	 * <p>
	 * <b>Example:</b> For the following <tt>removeNode(node)</tt> examples the
	 * trees below depict <b>no mutation</b> cases.
	 * <ul>
	 * <li><tt>removeNodeAndDescendants(3)</tt>: Imparts <b>no mutation</b>.
	 * <li><tt>removeNodeAndDescendants(null)</tt>: Imparts <b>no mutation</b>.
	 * 
	 * <pre>
	 * <b>Before</b>				<b>After</b>
	 * + <i>(depth 0, trunk)</i>		+ <i>(depth 0, trunk)</i>
	 * |				|
	 * +-+ 1				+-+ 1
	 * |				|
	 * +-+ 2				+-+ 2
	 * |				|
	 * <i>(part of larger tree)</i>		<i>(part of larger tree)</i>
	 * </pre>
	 * 
	 * </ul>
	 * 
	 * @param node
	 */
	public void removeNodeAndDescendants(N node);
	
}
