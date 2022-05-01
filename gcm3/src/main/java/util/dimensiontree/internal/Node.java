package util.dimensiontree.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

/**
 * Represents a single node the in dimension tree.
 * 
 * @author Shawn Hatch
 *
 */
public class Node<T> {

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

		if (SquareRootInequality.evaluate(squareRadius, nearestMemberData.bestSquareDistance, squareDistanceToPositionFromNodeCenter)) {
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