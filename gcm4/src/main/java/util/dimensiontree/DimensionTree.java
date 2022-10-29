package util.dimensiontree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.util.FastMath;

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
			
			throw new RuntimeException("null position");
		}
		if (position.length != commonState.dimension) {
			throw new RuntimeException("dimensional mismatch");
		}
		if (member == null) {
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
			throw new RuntimeException("null position");
		}
		if (commonState.dimension != position.length) {
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
			throw new RuntimeException("null lower bounds");
		}
		if (upperBounds == null) {
			throw new RuntimeException("null lower bounds");
		}
		if (lowerBounds.length != this.commonState.dimension) {
			throw new RuntimeException("lower bounds do not match dimension of tree");
		}
		if (upperBounds.length != this.commonState.dimension) {
			throw new RuntimeException("upper bounds do not match dimension of tree");
		}
		for (int i = 0; i < upperBounds.length; i++) {
			if (lowerBounds[i] > upperBounds[i]) {
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
			throw new RuntimeException("null position");
		}
		if (this.commonState.dimension != position.length) {
			throw new RuntimeException("dimensional mismatch");
		}
		if (radius < 0) {
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

	
	/*
	 * Returns true if and only if sqrt(a) + sqrt(b) < sqrt(c). Used in distance
	 * comparisons where square distances are known and calculating square roots
	 * should be avoided.
	 */
	private static boolean squareRootInequality(double aSquare, double bSquare, double cSquare) {
		/*
		 * We want to know when a+b<c. However, calculating these values would
		 * require using square roots and we can achieve better performance by
		 * only using square values.
		 * 
		 * a+b < c becomes...
		 * 
		 * a^2 + 2ab + b^2 < c^2 becomes...
		 * 
		 * 2ab < c^2 - a^2 - b^2 becomes...
		 * 
		 * 4 a^2 b^2 < (c^2 - a^2 - b^2)^2 with the caveat that c^2 - a^2 - b^2
		 * > 0
		 */
		double d = cSquare - aSquare - bSquare;
		return d >= 0 && 4 * aSquare * bSquare < d * d;
	}
	
	/*
	 * The common parameters shared by all nodes that takes up less memory than
	 * storing these values on each node
	 * 
	 * @author Shawn Hatch
	 *
	 */
	private static class CommonState {

		public final int leafSize;

		public final int dimension;

		public final int childCount;

		public CommonState(int leafSize, int dimension) {
			this.leafSize = leafSize;
			this.dimension = dimension;
			this.childCount = 1 << dimension;
		}

	}
	
	/*
	 * Represents a single node the in dimension tree.
	 * 
	 * @author Shawn Hatch
	 *
	 */
	private static class Node<T> {

		public Node<T> parent = null;

		public Node<T>[] children = null;

		public boolean canFormChildren;

		public double[] lowerBounds;

		public double[] upperBounds;

		private final CommonState commonState;

		public List<Group<T>> groups = new ArrayList<>();

		public final double squareRadius;

		public int groupCount;

		public int indexInParent = -1;

		// public int childWalkIndex;

		public Node(CommonState commonState, double[] lowerBounds, double[] upperBounds) {

			this.commonState = commonState;
			this.lowerBounds = lowerBounds;
			this.upperBounds = upperBounds;
			canFormChildren = true;
			for (int i = 0; i < commonState.dimension; i++) {
				double bound = (upperBounds[i] + lowerBounds[i]) * 0.5;
				if ((bound >= upperBounds[i]) || (bound <= lowerBounds[i])) {
					canFormChildren = false;
					break;
				}
			}

			double sum = 0;
			for (int i = 0; i < commonState.dimension; i++) {
				double delta = (upperBounds[i] - lowerBounds[i]) / 2;
				delta *= delta;
				sum += delta;
			}
			squareRadius = sum;
		}

		public boolean containsMember(T t) {
			if (children == null) {
				for (Group<T> memberGroup : groups) {
					if (memberGroup.members.contains(t)) {
						return true;
					}
				}
				return false;
			}

			for (Node<T> child : children) {
				if (child != null) {
					if (child.containsMember(t)) {
						return true;
					}
				}
			}
			return false;
		}

		public void getNearestMember(NearestMemberQuery<T> nearestMemberData) {
			findInitialNearestMemberSolution(nearestMemberData);
			findBetterNearestMemberSolution(nearestMemberData);
		}

		/*
		 * This method drill down to the node that that either contains the position
		 * or comes fairly close. We use this to quickly reduce the volume around
		 * the position where a solution might be found. If a solution is found this
		 * way, there is no guarantee that it will be the best solution, but it very
		 * often will be very close.
		 */
		private void findInitialNearestMemberSolution(NearestMemberQuery<T> nearestMemberData) {
			if (children == null) {
				for (Group<T> memberGroup : groups) {
					double squareDistance = memberGroup.squareDistanceTo(nearestMemberData.position);
					if ((squareDistance <= nearestMemberData.bestSquareDistance) || (nearestMemberData.bestSquareDistance < 0)) {
						nearestMemberData.bestSquareDistance = squareDistance;
						nearestMemberData.closestObject = memberGroup.members.get(0);
					}
				}
				return;
			}
			int childIndex = getChildIndex(nearestMemberData.position);
			Node<T> child = children[childIndex];
			if (child == null) {
				// calculate the distance to the farthest corner from the position
				double greatestSquaredDistance = 0;
				double value;
				for (int i = 0; i < commonState.dimension; i++) {
					double deltaToUpperBound = FastMath.abs(nearestMemberData.position[i] - upperBounds[i]);
					double deltaToLowerBound = FastMath.abs(nearestMemberData.position[i] - lowerBounds[i]);
					value = FastMath.max(deltaToUpperBound, deltaToLowerBound);
					greatestSquaredDistance += value * value;
				}
				nearestMemberData.bestSquareDistance = greatestSquaredDistance;
				return;
			}
			child.findInitialNearestMemberSolution(nearestMemberData);
		}

		/*
		 * This method searches the entire tree for a solution and tries to
		 * terminate branching quickly. It depends on first having found a
		 * reasonable near-solution, otherwise it will walk the entire tree.
		 */
		private void findBetterNearestMemberSolution(NearestMemberQuery<T> nearestMemberData) {
			/*
			 * We try to exclude any calculations if this node does not overlap the
			 * sphere given by the current solution. We do this by comparing the
			 * radius of this node, the radius of the current solution and the
			 * distance to the query position. We will sometimes fail to reject
			 * further work, but this will reject most of the potential wasted
			 * comparisons.
			 *
			 */

			double squareDistanceToPositionFromNodeCenter = 0;
			for (int i = 0; i < commonState.dimension; i++) {
				double delta = nearestMemberData.position[i] - ((upperBounds[i] + lowerBounds[i]) / 2);
				delta *= delta;
				squareDistanceToPositionFromNodeCenter += delta;
			}

			if (squareRootInequality(squareRadius, nearestMemberData.bestSquareDistance, squareDistanceToPositionFromNodeCenter)) {
				return;
			}

			if (children == null) {
				for (Group<T> memberGroup : groups) {
					double squareDistance = memberGroup.squareDistanceTo(nearestMemberData.position);
					if ((squareDistance <= nearestMemberData.bestSquareDistance) || (nearestMemberData.bestSquareDistance < 0)) {
						nearestMemberData.bestSquareDistance = squareDistance;
						nearestMemberData.closestObject = memberGroup.members.get(0);
					}
				}
				return;
			}

			for (Node<T> child : children) {
				if (child != null) {
					child.findBetterNearestMemberSolution(nearestMemberData);
				}
			}
		}

		public void getAllMembers(List<T> list) {

			if (children == null) {

				for (Group<T> memberGroup : groups) {
					list.addAll(memberGroup.members);
				}
				return;
			}

			for (Node<T> child : children) {
				if (child == null) {
					continue;
				}
				child.getAllMembers(list);
			}
		}

		public void getObjectsInDimensionalShape(Shape dimensionalShape, List<T> list) {
			ShapeIntersectionType shapeIntersectionType = dimensionalShape.intersectsBox(this);
			switch (shapeIntersectionType) {
			case NONE:
				return;
			case COMPLETE:
				getAllMembers(list);
				return;
			case PARTIAL:
				if (children == null) {
					for (Group<T> memberGroup : groups) {
						if (dimensionalShape.containsPosition(memberGroup.position)) {
							list.addAll(memberGroup.members);
						}
					}
				} else {
					for (Node<T> child : children) {
						if (child != null) {
							child.getObjectsInDimensionalShape(dimensionalShape, list);
						}
					}
				}
				return;
			default:			
				throw new RuntimeException("unhandled shape intersection type " + shapeIntersectionType);
			}
		}

		public int getChildIndex(double[] position) {
			/*
			 * Rather than store an array of booleans for our analysis of child
			 * bounds, we will use an index value that will be composed and then
			 * decomposed to eliminate the cost of array construction and garbage
			 * collection which has been shown in testing to be fairly expensive.
			 */
			int result = 0;
			for (int i = 0; i < commonState.dimension; i++) {
				result *= 2;
				if (upperBounds[i] + lowerBounds[i] < 2 * position[i]) {
					result++;
				}
			}
			return result;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (String s : toStrings()) {
				sb.append(s);
				sb.append("\n");
			}
			return sb.toString();
		}

		public void retrieveGroupsForMember(List<Group<T>> groups, T member) {
			if (children == null) {
				for (Group<T> group : this.groups) {
					if (group.members.contains(member)) {
						groups.add(group);
					}
				}
			} else {
				for (Node<T> child : children) {
					if (child != null) {
						child.retrieveGroupsForMember(groups, member);
					}
				}
			}
		}

		public void retrieveGroups(List<Group<T>> groups) {
			if (children == null) {
				groups.addAll(this.groups);
			} else {
				for (Node<T> child : children) {
					if (child != null) {
						child.retrieveGroups(groups);
					}
				}
			}
		}

		private List<String> toStrings() {
			List<String> result = new ArrayList<>();
			result.add(groupCount + " : " + groups.size());
			if (children != null) {
				for (Node<T> child : children) {
					if (child != null) {
						for (String s : child.toStrings()) {
							result.add("\t" + s);
						}
					}
				}
			}
			return result;
		}

	}
	
	/*
	 * Represent the members that are associated with a single position. Rather than
	 * storing members in the leaf nodes, we store member groups. Each member group
	 * can contain an unlimited number of members, but all such members have exactly
	 * the same position. This is done to prevent infinite branching when there are
	 * too many members at the same position.
	 */
	private static class Group<T> {

		public Node<T> node;

		public double position[];

		public List<T> members = new ArrayList<>();

		public boolean add(T t, double[] position) {
			if (members.size() == 0) {
				this.position = position.clone();
			}
			if (!members.contains(t)) {
				members.add(t);
				return true;
			}
			return false;
		}

		public boolean canContain(double[] p) {
			if (members.size() == 0) {
				return true;
			}
			for (int i = 0; i < position.length; i++) {
				if (p[i] != position[i]) {
					return false;
				}
			}
			return true;
		}

		public double squareDistanceTo(double[] p) {
			double result = 0;
			for (int i = 0; i < position.length; i++) {
				double value = position[i] - p[i];
				result += value * value;
			}
			return result;
		}

	}
	
	/*
	 * Represents a rectangular box in the dimension of the tree.
	 * 
	 * @author Shawn Hatch
	 *
	 */
	private static class Rectanguloid implements Shape {

		private double[] position = new double[0];

		private double[] bounds = new double[0];

		public Rectanguloid(double[] lowerBounds, double[] upperBounds) {
			position = new double[lowerBounds.length];
			bounds = new double[lowerBounds.length];
			for (int i = 0; i < position.length; i++) {
				position[i] = (upperBounds[i] + lowerBounds[i]) * 0.5;
				bounds[i] = (upperBounds[i] - lowerBounds[i]) * 0.5;
			}
		}

		@SuppressWarnings("unused")
		public double[] bounds() {
			return bounds.clone();
		}

		@Override
		public boolean containsPosition(double[] position) {
			for (int i = 0; i < position.length; i++) {
				if (FastMath.abs(this.position[i] - position[i]) > bounds[i]) {
					return false;
				}
			}
			return true;
		}

		@Override
		public <T> ShapeIntersectionType intersectsBox(Node<T> node) {
			// double[] lowerBounds, double[] upperBounds
			int containmentCount = 0;
			for (int i = 0; i < position.length; i++) {
				if (position[i] + bounds[i] < node.lowerBounds[i]) {
					return ShapeIntersectionType.NONE;
				}
				if (position[i] - bounds[i] > node.upperBounds[i]) {
					return ShapeIntersectionType.NONE;
				}

				if ((position[i] + bounds[i] > node.upperBounds[i]) && (position[i] - bounds[i] < node.lowerBounds[i])) {
					containmentCount++;
				}
			}
			if (containmentCount == position.length) {
				return ShapeIntersectionType.COMPLETE;
			}
			return ShapeIntersectionType.PARTIAL;

		}

		@SuppressWarnings("unused")
		public double[] position() {
			return position.clone();
		}
	}

	/*
	 * General interface for shapes used for gathering member from the tree.
	 * 
	 * @author Shawn Hatch
	 *
	 */
	private interface Shape {

		/**
		 * Returns true if the position is located inside this shape
		 */
		public boolean containsPosition(double[] position);

		/**
		 * Returns a ShapeIntersectionType that is the shapes determination of the
		 * overlap of the shape and the given node. Used to streamline the process
		 * of gathering members from nodes during intersection tests.
		 */
		public <T> ShapeIntersectionType intersectsBox(Node<T> node);
	}

	/*
	 * Represents a sphere in the dimension of the tree.
	 * 
	 * @author Shawn Hatch
	 *
	 */
	private static class Sphere implements Shape {

		private final double radius;
		private final double sqRadius;

		private double[] position = new double[0];

		public Sphere(double radius, double[] position) {
			this.radius = radius;
			this.sqRadius = radius * radius;
			this.position = position.clone();
		}

		@Override
		public boolean containsPosition(double[] position) {

			double distance = 0;
			for (int i = 0; i < position.length; i++) {
				double d = this.position[i] - position[i];
				distance += d * d;
			}
			return distance < sqRadius;
		}

		@Override
		public <T> ShapeIntersectionType intersectsBox(Node<T> node) {

			double squareDistanceToBoxCenter = 0;
			for (int i = 0; i < position.length; i++) {
				double value = position[i] - (node.upperBounds[i] + node.lowerBounds[i]) / 2;
				squareDistanceToBoxCenter += value * value;
			}

			if (squareRootInequality(node.squareRadius, sqRadius, squareDistanceToBoxCenter)) {
				return ShapeIntersectionType.NONE;
			}

			if (squareRootInequality(squareDistanceToBoxCenter, node.squareRadius, sqRadius)) {
				return ShapeIntersectionType.COMPLETE;
			}

			return ShapeIntersectionType.PARTIAL;
		}

		@SuppressWarnings("unused")
		public double[] position() {
			return position.clone();
		}

		@SuppressWarnings("unused")
		public double radius() {
			return radius;
		}

	}

	/*
	 * Represents the evolving answer to finding the nearest member to a given
	 * position.
	 * 
	 * @author Shawn Hatch
	 */
	private static class NearestMemberQuery<T> {

		public double bestSquareDistance = Double.POSITIVE_INFINITY;

		public T closestObject = null;

		public double[] position;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("NearestMemberQuery [bestSquareDistance=");
			builder.append(bestSquareDistance);
			builder.append(", closestObject=");
			builder.append(closestObject);
			builder.append(", position=");
			builder.append(Arrays.toString(position));
			builder.append("]");
			return builder.toString();
		}

	}

	/*
	 * Represents the degree of intersection of the shape and a node.
	 * 
	 * @author Shawn Hatch
	 *
	 */
	private static enum ShapeIntersectionType {
		/**
		 * The shape and node have no intersection
		 */
		NONE,

		/**
		 * The shape may intersect the node. Members of the node will require
		 * further comparison to the shape.
		 */
		PARTIAL,

		/**
		 * The shape fully contains the node and all members of the node can be
		 * gathered without further comparison to the shape.
		 */
		COMPLETE;
	}


}