package tools.metaunit;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import util.graph.Graph;
import util.graph.GraphDepthEvaluator;
import util.graph.Graphs;
import util.graph.MutableGraph;

/**
 * A utility class for generating various warnings on the coverage deficiencies
 * of the the unit test suite.
 *
 *
 */
public class CircularInfoGenerator {

	private static boolean isJavaFile(Path file) {
		return Files.isRegularFile(file) && file.toString().endsWith(".java");
	}

	private static String getClassName(Path sourcePath, Path file) {
		return file.toString().substring(sourcePath.toString().length() + 1, file.toString().length() - 5).replace(File.separator, ".");
	}

	/**
	 * Assumes that the source path and file are consistent
	 */
	private static Class<?> getClassFromFile(Path sourcePath, Path file) {
		try {
			String className = getClassName(sourcePath, file);
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static class Edge{
		Set<Class<?>> classes = new LinkedHashSet<>();
	}
	
	private MutableGraph<Node, Edge> m = new MutableGraph<>();
	
	private CircularInfoContainer.Builder warningContainerBuilder = CircularInfoContainer.builder();

	private Node getImportNode(String line) {

		for (Node node : m.getNodes()) {
			if (line.startsWith(node.name)) {
				return node;
			}
		}
		return null;
	}

	private void probeFile(Path file) throws IOException {

		Class<?> c = getClassFromFile(data.sourcePath, file);
		List<String> lines = Files.readAllLines(file);

		Node originNode = null;
		Set<Node> destinationNodes = new LinkedHashSet<>();

		boolean packageRead = false;
		for (String line : lines) {
			if (!packageRead) {
				String trim = line.trim();
				if (trim.contains("package")) {
					trim = line.substring(7, trim.length()).trim();
					Node node = getImportNode(trim);
					if (node != null) {
						originNode = node;
						packageRead = true;
					}
				}
			} else {
				String trim = line.trim();
				if (trim.startsWith("import ")) {
					trim = line.substring(6, trim.length()).trim();
					Node node = getImportNode(trim);
					if (node != null) {
						destinationNodes.add(node);
					}
				}
			}
		}	
		
		if(originNode == null) {
			throw new RuntimeException("cannot find origin node for "+c.getCanonicalName());
		}
		
		destinationNodes.remove(originNode);
		
		for(Node node : destinationNodes) {
			List<Edge> edges = m.getEdges(originNode, node);
			Edge edge;
			if(edges.isEmpty()) {
				edge = new Edge();
				m.addEdge(edge, originNode, node);
			}else {
				edge = edges.get(0);
			}
			edge.classes.add(c);
		}
		

	}

	private static class Node {
		private String name;

		public Node(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	

	private final class SourceFileVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) throws IOException {
			if (isJavaFile(file)) {
				probeFile(file);
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private final Data data;

	private static class Data {
		private Path sourcePath;

		private Path testPath;
	}

	public final static Builder builder() {
		return new Builder();
	}

	public final static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		public CircularInfoGenerator build() {
			try {
				validate();
				return new CircularInfoGenerator(data);
			} finally {
				data = new Data();
			}
		}

		private void validate() {

		}

		public Builder setSourcePath(Path sourcePath) {
			data.sourcePath = sourcePath;
			return this;
		}

		public Builder setTestPath(Path testPath) {
			data.testPath = testPath;
			return this;
		}

	}

	private CircularInfoGenerator(Data data) {
		this.data = data;
		m.addNode(new Node("util"));
		m.addNode(new Node("nucleus"));
		m.addNode(new Node("plugins.globalproperties"));
		m.addNode(new Node("plugins.groups"));
		m.addNode(new Node("plugins.materials"));
		m.addNode(new Node("plugins.partitions"));
		m.addNode(new Node("plugins.people"));
		m.addNode(new Node("plugins.personproperties"));
		m.addNode(new Node("plugins.regions"));
		m.addNode(new Node("plugins.reports"));
		m.addNode(new Node("plugins.resources"));
		m.addNode(new Node("plugins.stochastics"));
		m.addNode(new Node("plugins.util"));
		m.addNode(new Node("tools"));
	}

	private void loadSourceClasses() {

		final SourceFileVisitor sourceFileVisitor = new SourceFileVisitor();
		try {
			Files.walkFileTree(data.sourcePath, sourceFileVisitor);
			Files.walkFileTree(data.testPath, sourceFileVisitor);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

	}
	
	public CircularInfoContainer execute() {
		
		loadSourceClasses();
		
		for(Edge edge : m.getEdges()) {
			Node originNode = m.getOriginNode(edge);
			Node destinationNode = m.getDestinationNode(edge);
			System.out.println(originNode.name+"->"+destinationNode.name);
			for(Class<?> c : edge.classes) {
				System.out.println("\t"+c.getSimpleName());
			}
		}
		
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		
		
		Optional<GraphDepthEvaluator<Node>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(m.toGraph());

		if (!optional.isPresent()) {
			/*
			 * Explain in detail why there is a circular dependency
			 */

			Graph<Node, Edge> g = m.toGraph();

			g = Graphs.getSourceSinkReducedGraph(g);
			g = Graphs.getEdgeReducedGraph(g);
			g = Graphs.getSourceSinkReducedGraph(g);

			List<Graph<Node, Edge>> cutGraphs = Graphs.cutGraph(g);
			StringBuilder sb = new StringBuilder();
			String lineSeparator = System.getProperty("line.separator");
			sb.append(lineSeparator);
			boolean firstCutGraph = true;

			for (Graph<Node, Edge> cutGraph : cutGraphs) {
				//cutGraph = Graphs.getSourceSinkReducedGraph(cutGraph);
				if (firstCutGraph) {
					firstCutGraph = false;
				} else {
					sb.append(lineSeparator);
				}
				sb.append("Dependency group: ");
				sb.append(lineSeparator);
				Set<Node> nodes = cutGraph.getNodes().stream().collect(Collectors.toCollection(LinkedHashSet::new));

				for (Node node : nodes) {
					sb.append("\t");
					sb.append(node);
					sb.append(" requires:");
					sb.append(lineSeparator);
					for (Edge edge : cutGraph.getOutboundEdges(node)) {
						Node dependencyNode = cutGraph.getDestinationNode(edge);
						if (nodes.contains(dependencyNode)) {
							sb.append("\t");
							sb.append("\t");
							sb.append(dependencyNode);
							sb.append(lineSeparator);
						}
					}
				}
			}	
			
			System.out.println(sb);
		}

		return warningContainerBuilder.build();

	}

}
