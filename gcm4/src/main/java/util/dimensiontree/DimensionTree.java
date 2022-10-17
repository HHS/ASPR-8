package util.dimensiontree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import util.dimensiontree.internal.CommonState;
import util.dimensiontree.internal.Group;
import util.dimensiontree.internal.NearestMemberQuery;
import util.dimensiontree.internal.Node;
import util.dimensiontree.internal.Rectanguloid;
import util.dimensiontree.internal.Shape;
import util.dimensiontree.internal.Sphere;
import util.errors.ContractException;

/**
 * <P>
 * Represents a multi-dimensional, generics-based, searchable tree giving
 * generally log2(N) retrieval times for stored members.
 * </P>
 * 
 * <P>
 * Positions in the tree are represented as arrays of double and the number of
 * dimensions in the tree are fixed by its build parameters. The tree's span in
 * the multi-dimensional space will contract and expand as needed and does so
 * with generally efficient performance. The tree specifically allows for a
 * single object to be stored at multiple locations and allows for multiple
 * objects to be stored at a single location.
 * </P>
 * 
 * <p>
 * The tree supports object retrieval by:
 * <LI>all objects in the tree</LI>
 * <LI>the closest object to a given point</LI>
 * <LI>spherical and rectanguloid intersection with the tree</LI>
 * </p>
 * 
 * <p>
 * The tree supports object removal in O(log2(N)) time whenever it is
 * constructed with the fast removals option and O(N) without. Fast removals
 * requires significantly more memory.
 * </p>
 * 
 * @author Shawn Hatch
 **/

public final class DimensionTree<T> {

	private final static int DEFAULTLEAFSIZE = 15;

	public static Builder builder() {
		return new Builder();
	}

	private static class Scaffold {
		private double[] lowerBounds;
		private double[] upperBounds;
		private int leafSize = DEFAULTLEAFSIZE;
		private boolean fastRemovals = false;
	}

	public static class Builder {

		private Scaffold scaffold = new Scaffold();

		private Builder() {
		}

		/**
		 * Sets the fast removals policy. When fast removals is chosen, the tree
		 * will remove objects in near constant time. This requires significant
		 * memory resources and adds time to the storage and retrieval process.
		 * Without fast removals, the tree must remove objects in order O(N)
		 * time through exhaustive searching.
		 */
		public Builder setFastRemovals(boolean fastRemovals) {
			scaffold.fastRemovals = fastRemovals;
			return this;
		}

		/**
		 * Sets the leaf size used to determine how many objects positions can
		 * be stored in any particular node of the tree. Setting the value to a
		 * high number such as 100 will slow down retrieval performance, but
		 * will reduce memory overhead. Low values, such as 1 will maximize
		 * memory use since this will cause more nodes to come into existence.
		 * Both high and low values can slow down the tree's performance when
		 * adding objects. Common practice is to set the value to
		 * DEFAULTLEAFSIZE (=15), which works well in most applications.
		 * Defaulted to 15.
		 */

		public Builder setLeafSize(int leafSize) {
			scaffold.leafSize = leafSize;
			return this;
		}

		/**
		 * Sets the initial lower bounds of the tree. While the tree will expand
		 * and contract as needed, it is often important to set the bounds to
		 * roughly the proper magnitude to avoid some initial performance slow
		 * downs. Lower bounds should not exceed upper bounds.
		 */
		public Builder setLowerBounds(double[] lowerBounds) {
			scaffold.lowerBounds = Arrays.copyOf(lowerBounds, lowerBounds.length);
			return this;
		}

		/**
		 * Sets the initial upper bounds of the tree. While the tree will expand
		 * and contract as needed, it is often important to set the bounds to
		 * roughly the proper magnitude to avoid some initial performance slow
		 * downs. Lower bounds should not exceed upper bounds.
		 */
		public Builder setUpperBounds(double[] upperBounds) {
			scaffold.upperBounds = Arrays.copyOf(upperBounds, upperBounds.length);
			return this;
		}

		/**
		 * Builds a {@link DimensionTree} from the contributed parameters.
		 * 
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain DimensionTreeError#NON_POSITIVE_LEAF_SIZE}
		 *             if the selected leaf size is not positive</li>
		 *             <li>{@linkplain DimensionTreeError#LOWER_BOUNDS_ARE_NULL}
		 *             if the lower bounds were not contributed or were
		 *             null</li>
		 *             <li>{@linkplain DimensionTreeError#UPPER_BOUNDS_ARE_NULL}
		 *             if the upper bounds were not contributed or were
		 *             null</li>
		 *             <li>{@linkplain DimensionTreeError#BOUNDS_MISMATCH} if
		 *             the lower and upper bounds do not match in length</li>
		 *             <li>{@linkplain DimensionTreeError#LOWER_BOUNDS_EXCEED_UPPER_BOUNDS}
		 *             if any of the lower bounds exceed the corresponding upper
		 *             bounds</li>
		 * 
		 * 
		 */
		public <T> DimensionTree<T> build() {

			try {
				return new DimensionTree<>(scaffold);
			} finally {
				scaffold = new Scaffold();
			}

		}
	}

	private CommonState commonState;

	private Node<T> root = null;

	/*
	 * Hidden constructor
	 */
	private DimensionTree(Scaffold scaffold) {

		if (scaffold.leafSize < 1) {
			throw new ContractException(DimensionTreeError.NON_POSITIVE_LEAF_SIZE);
		}

		if (scaffold.lowerBounds == null) {
			throw new ContractException(DimensionTreeError.LOWER_BOUNDS_ARE_NULL);
		}

		if (scaffold.upperBounds == null) {
			throw new ContractException(DimensionTreeError.UPPER_BOUNDS_ARE_NULL);
		}

		int dimension = scaffold.lowerBounds.length;

		if (scaffold.upperBounds.length != dimension) {
			throw new ContractException(DimensionTreeError.BOUNDS_MISMATCH);
		}

		for (int i = 0; i < dimension; i++) {
			if (scaffold.lowerBounds[i] > scaffold.upperBounds[i]) {
				throw new ContractException(DimensionTreeError.LOWER_BOUNDS_EXCEED_UPPER_BOUNDS);
			}
		}

		commonState = new CommonState(scaffold.leafSize, dimension);

		root = new Node<>(commonState, scaffold.lowerBounds, scaffold.upperBounds);

		if (scaffold.fastRemovals) {
			groupMap = new LinkedHashMap<>();
		}
	}

	@SuppressWarnings("unchecked")
	private void expandRootToFitPosition(double[] position) {

		while (true) {
			boolean rootContainsPosition = true;
			for (int i = 0; i < commonState.dimension; i++) {
				if (root.upperBounds[i] < position[i]) {
					rootContainsPosition = false;
					break;
				}

				if (root.lowerBounds[i] > position[i]) {
					rootContainsPosition = false;
					break;
				}
			}

			if (rootContainsPosition) {
				break;
			}

			int childIndex = 0;
			double[] newRootLowerBounds = new double[commonState.dimension];
			double[] newRootUpperBounds = new double[commonState.dimension];
			for (int i = 0; i < commonState.dimension; i++) {
				childIndex *= 2;
				if (root.lowerBounds[i] > position[i]) {
					newRootLowerBounds[i] = 2 * root.lowerBounds[i] - root.upperBounds[i];
					newRootUpperBounds[i] = root.upperBounds[i];
					childIndex += 1;
				} else {
					newRootUpperBounds[i] = 2 * root.upperBounds[i] - root.lowerBounds[i];
					newRootLowerBounds[i] = root.lowerBounds[i];
				}
			}
			root.parent = new Node<>(commonState, newRootLowerBounds, newRootUpperBounds);
			root.parent.groupCount = root.groupCount;
			root.parent.children = new Node[commonState.childCount];
			root.parent.children[childIndex] = root;
			root.indexInParent = childIndex;
			root = root.parent;
		}
	}

	/*
	 * Returns the child that should contain the position. It is assumed that
	 * the position is contained in this node's bounds. If there is no such
	 * child, one is created and placed in the appropriate slot of the children.
	 */
	private Node<T> getChild(Node<T> node, double[] position) {
		int childIndex = node.getChildIndex(position);
		Node<T> child = node.children[childIndex];
		if (child != null) {
			return child;
		}

		int index = childIndex;

		double[] childLowerBounds = new double[commonState.dimension];
		double[] childUpperBounds = new double[commonState.dimension];
		for (int i = commonState.dimension - 1; i >= 0; i--) {
			if (index % 2 == 1) {
				childUpperBounds[i] = node.upperBounds[i];
				childLowerBounds[i] = (node.upperBounds[i] + node.lowerBounds[i]) * 0.5;
			} else {
				childUpperBounds[i] = (node.upperBounds[i] + node.lowerBounds[i]) * 0.5;
				childLowerBounds[i] = node.lowerBounds[i];
			}
			index /= 2;
		}

		child = new Node<>(commonState, childLowerBounds, childUpperBounds);
		node.children[childIndex] = child;
		child.parent = node;
		child.indexInParent = childIndex;

		return child;
	}

	/*
	 * Rather than have the root node recursively push the new member into the
	 * tree, we elect to avoid stack loading to save some execution time.
	 */
	@SuppressWarnings("unchecked")
	private Group<T> deep_add(double[] position, T t) {
		Group<T> result = null;
		Node<T> node = root;

		mainloop: while (true) {
			if (node.children == null) {
				for (Group<T> memberGroup : node.groups) {
					if (memberGroup.canContain(position)) {
						if (memberGroup.add(t, position)) {
							result = memberGroup;
						}
						break mainloop;
					}
				}

				if (node.groups.size() < commonState.leafSize || !node.canFormChildren) {
					Group<T> memberGroup = new Group<>();
					memberGroup.node = node;
					memberGroup.add(t, position);
					node.groups.add(memberGroup);
					node.groupCount++;
					result = memberGroup;
					break mainloop;
				}

				// distribute groups into children
				node.children = new Node[commonState.childCount];
				for (Group<T> memberGroup : node.groups) {
					Node<T> child = getChild(node, memberGroup.position);
					child.groupCount++;
					child.groups.add(memberGroup);
					memberGroup.node = child;
				}
				node.groups.clear();

				node = getChild(node, position);
			} else {
				node = getChild(node, position);
			}
		}

		if (result != null) {
			if (result.members.size() == 1) {
				node = result.node;
				while (node != null) {
					node.groupCount++;
					node = node.parent;
				}
			}
		}
		return result;
	}

	/**
	 * Adds a member at the given position. Returns true if the member is not
	 * already associated with the position. Returns false otherwise.
	 * 
	 * @throws RuntimeException
	 *             <li>if the position is null
	 *             <li>if the member is null
	 *             <li>if the position does not match the dimension of this
	 *             {@link DimensionTree}
	 */
	public boolean add(double[] position, T member) {

		if (position == null) {
			// deception
			throw new RuntimeException("null position");
		}
		if (position.length != commonState.dimension) {
			// deception
			throw new RuntimeException("dimensional mismatch");
		}
		if (member == null) {
			// deception
			throw new RuntimeException("null value being added");
		}
		expandRootToFitPosition(position);

		Group<T> group = deep_add(position, member);

		if (group != null && groupMap != null) {
			List<Group<T>> list = groupMap.get(member);
			if (list == null) {
				list = new ArrayList<>();
				groupMap.put(member, list);
			}
			list.add(group);
		}
		return group != null;
	}

	/**
	 * Return true if and only if the given member is contained in this
	 * {@link DimensionTree}
	 * 
	 */
	public boolean contains(T member) {
		if (groupMap != null) {
			return groupMap.containsKey(member);
		}
		return root.containsMember(member);
	}

	/**
	 * Returns the member nearest to the given position.
	 */
	public Optional<T> getNearestMember(double[] position) {
		if (position == null) {
			// deception
			throw new RuntimeException("null position");
		}
		if (commonState.dimension != position.length) {
			// deception
			throw new RuntimeException("dimensional mismatch");
		}

		NearestMemberQuery<T> nearestMemberData = new NearestMemberQuery<>();
		nearestMemberData.position = position;
		root.getNearestMember(nearestMemberData);
		return Optional.ofNullable(nearestMemberData.closestObject);
	}

	/**
	 * Retrieves all of the objects stored in this {@link DimensionTree}. This
	 * may include duplicates if any object is stored in multiple locations.
	 * 
	 */

	public List<T> getAll() {
		List<T> result = new ArrayList<>();
		if (groupMap != null) {
			result.addAll(groupMap.keySet());
		} else {
			root.getAllMembers(result);
		}
		return result;
	}

	/**
	 * Retrieves all of the objects stored in the tree within the
	 * IDimensionalShape. The shape itself need not lie fully inside the tree's
	 * volume, but must be well formed and agree with the tree's dimension.
	 * 
	 * @param dimensionalShape
	 *            a non-null IDimensionalShape implementation
	 * @return an ArrayList of Object containing all unique objects within
	 *         shape's intersection with the tree.
	 */
	private List<T> getObjectsInDimensionalShape(Shape dimensionalShape) {
		List<T> result = new ArrayList<>();
		root.getObjectsInDimensionalShape(dimensionalShape, result);
		return result;
	}

	/**
	 * Retrieves all of the objects within the rectanguloid formed by the lower
	 * and upper bounds. This may include duplicates if any object is stored in
	 * multiple locations.The rectanguloid itself need not lie fully inside this
	 * tree's volume. The lengths of the lower and upper bound arrays must agree
	 * with the dimension of the tree. For each dimension, lowerBounds[i] must
	 * not exceed upperBounds[i].
	 * 
	 *
	 * @throws RuntimeException
	 *             <li>if the lower bounds are null<\li>
	 *             <li>if the upper bounds are null<\li>
	 *             <li>if the length of the upper bounds does not match the
	 *             dimension of this tree<\li>
	 *             <li>if the length of the lower bounds does not match the
	 *             dimension of this tree<\li>
	 *             <li>if the values of the lower bounds exceed the
	 *             corresponding values of the upper bounds<\li>
	 */

	public List<T> getMembersInRectanguloid(double[] lowerBounds, double[] upperBounds) {
		if (lowerBounds == null) {
			// deception
			throw new RuntimeException("null lower bounds");
		}
		if (upperBounds == null) {
			// deception
			throw new RuntimeException("null lower bounds");
		}
		if (lowerBounds.length != this.commonState.dimension) {
			// deception
			throw new RuntimeException("lower bounds do not match dimension of tree");
		}
		if (upperBounds.length != this.commonState.dimension) {
			// deception
			throw new RuntimeException("upper bounds do not match dimension of tree");
		}
		for (int i = 0; i < upperBounds.length; i++) {
			if (lowerBounds[i] > upperBounds[i]) {
				// deception
				throw new RuntimeException("lower bounds exceed upper bounds");
			}
		}
		return getObjectsInDimensionalShape(new Rectanguloid(lowerBounds, upperBounds));
	}

	/**
	 * Retrieves all of the objects stored in the tree within the radius
	 * distance about the position. This may include duplicates if any object is
	 * stored in multiple locations. The position itself need not lie inside
	 * this tree's volume.
	 * 
	 *
	 *
	 * 
	 * @throw {@link RuntimeException}
	 *        <li>if the radius is negative<\li>
	 *        <li>if the position is null<\li>
	 *        <li>if the position's length does not match the dimension of this
	 *        tree
	 *
	 */
	public List<T> getMembersInSphere(double radius, double[] position) {
		if (position == null) {
			// deception
			throw new RuntimeException("null position");
		}
		if (this.commonState.dimension != position.length) {
			// deception
			throw new RuntimeException("dimensional mismatch");
		}
		if (radius < 0) {
			// deception
			throw new RuntimeException("negative radius");
		}
		return getObjectsInDimensionalShape(new Sphere(radius, position));

	}

	private Map<T, List<Group<T>>> groupMap;

	/**
	 * Removes the given member from this {@link DimensionTree} at all locations
	 * associated with the member. Returns true if the member was contained.
	 */
	public boolean remove(T member) {
		/*
		 * First, get the list of member groups that contain the member. This
		 * can come from the nodes via a brute force walk of the entire tree or
		 * from a map of T to List<MemberGroup>
		 */
		List<Group<T>> groups;
		if (groupMap == null) {
			groups = new ArrayList<>();
			root.retrieveGroupsForMember(groups, member);
		} else {
			groups = groupMap.remove(member);
		}

		/*
		 * For each member group we will remove the member. For those member
		 * groups where the member group is now empty, we remove the member
		 * group from its node and cascade member group counts upward. As we
		 * move upward toward the root, we record each node that will need to
		 * collapse, replacing this reference as we move up. Any node that
		 * reaches a member count of zero will be removed from its parent.
		 * 
		 * If we have a node that has been selected to collapse, we command the
		 * node to collapse.
		 * 
		 * We next walk downward from root looking to move the root downward
		 * into the tree?
		 */

		boolean result = groups != null && !groups.isEmpty();
		if (groups != null) {
			for (Group<T> group : groups) {
				group.members.remove(member);
				if (group.members.size() == 0) {
					Node<T> collapseNode = null;
					Node<T> node = group.node;
					node.groups.remove(group);
					while (node != null) {
						// reduce the group count
						node.groupCount--;
						/*
						 * If the node is now empty and has a parent, remove it
						 * from its parent
						 */
						if (node.groupCount == 0) {
							if (node.parent != null) {
								node.parent.children[node.indexInParent] = null;
							}
						}
						/*
						 * Don't select a node to collapse if it is being thrown
						 * out of the tree
						 */
						if (node.groupCount != 0 && node.groupCount <= commonState.leafSize) {
							collapseNode = node;
						}
						node = node.parent;
					}
					if (collapseNode != null) {
						List<Group<T>> groupsInCollapse = new ArrayList<>();
						collapseNode.retrieveGroups(groupsInCollapse);
						collapseNode.groups = groupsInCollapse;
						for (Group<T> g : groupsInCollapse) {
							g.node = collapseNode;
						}
						collapseNode.children = null;
					}
				}
			}
		}
		return result;
	}

	// @Override
	// public String toString() {
	// return root.toString();
	// }

}