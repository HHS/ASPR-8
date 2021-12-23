package util.delaunay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import util.dimensiontree.VolumetricDimensionTree;
import util.spherical.Chirality;
import util.vector.MutableVector2D;
import util.vector.Vector2D;

public class PlanarDelaunaySolver<T> {

	private static class Rec<T> implements Comparable<Rec<T>> {
		T t;
		MutableVector2D v;
		double distanceToCentroid;
		double angle;
		int step;

		@Override
		public int compareTo(Rec<T> other) {
			int result = Integer.compare(step, other.step);
			if (result == 0) {
				result = Double.compare(angle, other.angle);
			}
			return result;
		}
	}

	private List<T> spiralize(Map<T, Vector2D> itemLocationMap) {

		MutableVector2D centroid = new MutableVector2D();

		List<Rec<T>> list = new ArrayList<>();
		for (T t : itemLocationMap.keySet()) {
			Rec<T> rec = new Rec<>();
			rec.t = t;
			rec.v = new MutableVector2D(itemLocationMap.get(t));
			list.add(rec);
			centroid.add(rec.v);
		}

		centroid.scale(1.0 / list.size());

		MutableVector2D xAxis = new MutableVector2D(1, 0);
		double maxDistance = Double.NEGATIVE_INFINITY;

		MutableVector2D v = new MutableVector2D();
		for (Rec<T> rec : list) {
			rec.distanceToCentroid = centroid.distanceTo(rec.v);
			v.assign(rec.v);
			v.sub(centroid);
			rec.angle = v.angle(xAxis) * v.cross(xAxis);
			maxDistance = FastMath.max(maxDistance, rec.distanceToCentroid);
		}

		double area = 2 * FastMath.PI * maxDistance * maxDistance;
		double stepDistance = FastMath.sqrt(area / list.size());

		for (Rec<T> rec : list) {
			rec.step = (int) (rec.distanceToCentroid / stepDistance);
		}

		Collections.sort(list);

		List<T> result = new ArrayList<>();
		for (Rec<T> rec : list) {
			result.add(rec.t);
		}
		return result;
	}

	private static class Edge {
		int[] vertexIds;
		boolean markedForRemoval;

		public Edge(final int[] vertexIds) {
			this.vertexIds = vertexIds;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Edge)) {
				return false;
			}
			final Edge other = (Edge) obj;
			if (!Arrays.equals(vertexIds, other.vertexIds)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + Arrays.hashCode(vertexIds);
			return result;
		}

	}

	private static class Triangle {
		private double radius;

		public Triangle(double radius) {
			this.radius = radius;
		}

		private boolean markedForRemoval;
	}

	private static class Vertex<T> {
		int id;
		MutableVector2D position;
		T t;

		public Vertex(final int id, final MutableVector2D position, T t) {
			super();
			this.id = id;
			this.position = position;
			this.t = t;
		}
	}

	public static <T> List<Pair<T, T>> solve(Map<T, Vector2D> itemLocationMap) {
		return new PlanarDelaunaySolver<>(itemLocationMap).solve();
	}

	private final int scaffoldCount = 4;

	private Map<T, Vector2D> itemLocationMap;

	private final Map<Triangle, List<Edge>> triangleToEdgeMap = new LinkedHashMap<>();

	private final Map<Edge, List<Triangle>> edgeToTriangleMap = new LinkedHashMap<>();

	private final List<Vertex<T>> vertexes = new ArrayList<>();

	private VolumetricDimensionTree<Triangle> searchTree;

	private PlanarDelaunaySolver(Map<T, Vector2D> itemLocationMap) {
		this.itemLocationMap = itemLocationMap;
	}

	private void addTriangle(final int id1, final int id2, final int id3) {
		int[] ids = new int[3];

		ids[0] = id1;
		ids[1] = id2;
		ids[2] = id3;

		Arrays.sort(ids);

		MutableVector2D a = new MutableVector2D(vertexes.get(ids[0]).position);
		MutableVector2D b = new MutableVector2D(vertexes.get(ids[1]).position);
		MutableVector2D c = new MutableVector2D(vertexes.get(ids[2]).position);

		MutableVector2D m1 = new MutableVector2D(a);
		m1.add(b);
		m1.scale(0.5);

		MutableVector2D p1 = new MutableVector2D(a);
		p1.sub(b);
		p1.perpendicularRotation(Chirality.LEFT_HANDED);

		MutableVector2D m2 = new MutableVector2D(b);
		m2.add(c);
		m2.scale(0.5);

		MutableVector2D p2 = new MutableVector2D(b);
		p2.sub(c);
		p2.perpendicularRotation(Chirality.LEFT_HANDED);

		MutableVector2D q = new MutableVector2D(c);
		q.sub(b);

		m2.sub(m1);
		double j = m2.dot(q) / p1.dot(q);

		MutableVector2D center = new MutableVector2D(p1);
		center.scale(j);
		center.add(m1);

		double radius = center.distanceTo(a);

		Triangle triangle = new Triangle(radius);

		int[] edgeIds = new int[2];
		edgeIds[0] = ids[0];
		edgeIds[1] = ids[1];
		Edge edge1 = new Edge(edgeIds);
		List<Triangle> list = edgeToTriangleMap.get(edge1);
		if (list == null) {
			list = new ArrayList<>();
			edgeToTriangleMap.put(edge1, list);
		}
		list.add(triangle);

		edgeIds = new int[2];
		edgeIds[0] = ids[1];
		edgeIds[1] = ids[2];
		Edge edge2 = new Edge(edgeIds);
		list = edgeToTriangleMap.get(edge2);
		if (list == null) {
			list = new ArrayList<>();
			edgeToTriangleMap.put(edge2, list);
		}
		list.add(triangle);

		edgeIds = new int[2];
		edgeIds[0] = ids[0];
		edgeIds[1] = ids[2];
		Edge edge3 = new Edge(edgeIds);
		list = edgeToTriangleMap.get(edge3);
		if (list == null) {
			list = new ArrayList<>();
			edgeToTriangleMap.put(edge3, list);
		}
		list.add(triangle);

		List<Edge> edgeList = new ArrayList<>();
		edgeList.add(edge1);
		edgeList.add(edge2);
		edgeList.add(edge3);
		triangleToEdgeMap.put(triangle, edgeList);

		searchTree.add(center.toArray(), triangle.radius, triangle);
	}

	private void addTriangles(final Vertex<T> vertex, final List<Edge> hullEdges) {
		for (final Edge edge : hullEdges) {
			addTriangle(edge.vertexIds[0], edge.vertexIds[1], vertex.id);
		}
	}

	private List<Edge> getEdgesToRemove(final List<Triangle> trianglesToRemove) {
		final List<Edge> result = new ArrayList<>();
		for (final Triangle triangleToRemove : trianglesToRemove) {
			final List<Edge> edges = triangleToEdgeMap.get(triangleToRemove);
			for (final Edge edge : edges) {
				final List<Triangle> triangles = edgeToTriangleMap.get(edge);
				int triangleRemovalCount = 0;
				for (final Triangle triangle : triangles) {
					if (triangle.markedForRemoval) {
						triangleRemovalCount++;
					}
				}
				if (triangleRemovalCount == 2) {
					if (!edge.markedForRemoval) {
						edge.markedForRemoval = true;
						result.add(edge);
					}
				}
			}
		}
		return result;
	}

	private List<Edge> getHullEdges(final List<Triangle> trianglesToRemove) {
		final List<Edge> result = new ArrayList<>();
		for (final Triangle badTriangle : trianglesToRemove) {
			final List<Edge> edges = triangleToEdgeMap.get(badTriangle);
			for (final Edge edge : edges) {
				if (!edge.markedForRemoval) {
					result.add(edge);
				}
			}
		}
		return result;
	}

	private List<Triangle> getTrianglesStruckByVertex(final Vertex<T> vertex) {
		final List<Triangle> result = new ArrayList<>();
		searchTree.getMembersInSphere(0, vertex.position.toArray()).forEach(triangle -> {
			triangle.markedForRemoval = true;
			result.add(triangle);
		});
		return result;
	}

	private void initialize() {
		List<T> points = spiralize(itemLocationMap);

		double maxX = Double.NEGATIVE_INFINITY;
		double minX = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;

		for (T t : itemLocationMap.keySet()) {
			Vector2D v = itemLocationMap.get(t);
			maxX = FastMath.max(maxX, v.getX());
			minX = FastMath.min(minX, v.getX());
			maxY = FastMath.max(maxY, v.getY());
			minY = FastMath.min(minY, v.getY());
		}
		double[] lowerBounds = { minX, minY };
		double[] upperBounds = { maxX, maxY };

		searchTree = VolumetricDimensionTree.builder()//
											.setFastRemovals(true)//
											.setLowerBounds(lowerBounds)//
											.setUpperBounds(upperBounds)//
											.build();//

		double pad = 0.01;

		double padX = (maxX - minX) * pad;
		double padY = (maxY - minY) * pad;

		minX -= padX;
		maxX += padX;

		minY -= padY;
		maxY += padY;

		Vertex<T> vertex0 = new Vertex<>(0, new MutableVector2D(minX, minY), null);
		vertexes.add(vertex0);

		Vertex<T> vertex1 = new Vertex<>(1, new MutableVector2D(minX, maxY), null);
		vertexes.add(vertex1);

		Vertex<T> vertex2 = new Vertex<>(2, new MutableVector2D(maxX, minY), null);
		vertexes.add(vertex2);

		Vertex<T> vertex3 = new Vertex<>(3, new MutableVector2D(maxX, maxY), null);
		vertexes.add(vertex3);

		int n = points.size();
		for (int i = 0; i < n; i++) {
			T t = points.get(i);
			Vector2D v = itemLocationMap.get(t);
			MutableVector2D m = new MutableVector2D(v);
			vertexes.add(new Vertex<>(i + scaffoldCount, m, t));
		}

		addTriangle(vertex0.id, vertex1.id, vertex2.id);

		addTriangle(vertex1.id, vertex2.id, vertex3.id);

	}

	private void removeEdges(final List<Edge> edgesToRemove) {
		for (final Edge edge : edgesToRemove) {
			edgeToTriangleMap.remove(edge);
		}
	}

	private void removeTriangles(final List<Triangle> trianglesToRemove) {
		trianglesToRemove.forEach(triangle -> {
			triangleToEdgeMap.remove(triangle).forEach(edge -> {
				edgeToTriangleMap.get(edge).remove(triangle);
			});
			searchTree.remove(triangle.radius, triangle);
		});

	}

	private List<Pair<T, T>> solve() {
		initialize();

		for (int i = scaffoldCount; i < vertexes.size(); i++) {
			final Vertex<T> vertex = vertexes.get(i);
			final List<Triangle> trianglesToRemove = getTrianglesStruckByVertex(vertex);
			final List<Edge> edgesToRemove = getEdgesToRemove(trianglesToRemove);
			final List<Edge> hullEdges = getHullEdges(trianglesToRemove);
			removeTriangles(trianglesToRemove);
			removeEdges(edgesToRemove);
			addTriangles(vertex, hullEdges);
		}

		List<Pair<T, T>> result = new ArrayList<>();
		edgeToTriangleMap.keySet().forEach(edge -> {
			Vertex<T> vertex0 = vertexes.get(edge.vertexIds[0]);
			if (vertex0.t != null) {
				Vertex<T> vertex1 = vertexes.get(edge.vertexIds[1]);
				Pair<T, T> pair = new Pair<>(vertex0.t, vertex1.t);
				result.add(pair);
			}
		});

		return result;
	}

}
