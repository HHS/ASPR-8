package tools.meta.plugindependency;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import nucleus.Plugin;
import nucleus.PluginId;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginId;
import plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import plugins.groups.GroupsPlugin;
import plugins.groups.GroupsPluginId;
import plugins.groups.datamanagers.GroupsPluginData;
import plugins.materials.MaterialsPlugin;
import plugins.materials.MaterialsPluginData;
import plugins.materials.MaterialsPluginId;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.PartitionsPluginId;
import plugins.partitions.datamanagers.PartitionsPluginData;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.PeoplePluginId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.PersonPropertiesPluginId;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginId;
import plugins.regions.datamanagers.RegionsPluginData;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginId;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.ResourcesPluginId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.support.WellState;
import tools.meta.packagedependency.PackageDependencyData;
import util.graph.Graph;
import util.graph.GraphDepthEvaluator;
import util.graph.Graphs;
import util.graph.MutableGraph;
import util.path.MapPathSolver;

/**
 * 
 *
 *
 */
public class PluginDependencyInfoGenerator {

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

	private static class Edge {
		Set<Class<?>> classes = new LinkedHashSet<>();
	}

	private MutableGraph<Node, Edge> packageDependencyGraph = new MutableGraph<>();

	private PackageDependencyData.Builder warningContainerBuilder = PackageDependencyData.builder();

	private Node getImportNode(String line) {

		for (Node node : packageDependencyGraph.getNodes()) {
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

		if (originNode == null) {
			throw new RuntimeException("cannot find origin node for " + c.getCanonicalName());
		}

		destinationNodes.remove(originNode);

		for (Node node : destinationNodes) {
			List<Edge> edges = packageDependencyGraph.getEdges(originNode, node);
			Edge edge;
			if (edges.isEmpty()) {
				edge = new Edge();
				packageDependencyGraph.addEdge(edge, originNode, node);
			} else {
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Node)) {
				return false;
			}
			Node other = (Node) obj;
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			return true;
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
		
		public Data() {}
		public Data(Data data) {
			sourcePath = data.sourcePath;
			testPath = data.testPath;
		}
	}

	public final static Builder builder() {
		return new Builder();
	}

	public final static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		public PluginDependencyInfoGenerator build() {
			validate();
			return new PluginDependencyInfoGenerator(new Data(data));
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

	private PluginDependencyInfoGenerator(Data data) {
		this.data = data;
		packageDependencyGraph.addNode(new Node("util"));
		packageDependencyGraph.addNode(new Node("nucleus"));
		packageDependencyGraph.addNode(new Node("plugins.globalproperties"));
		packageDependencyGraph.addNode(new Node("plugins.groups"));
		packageDependencyGraph.addNode(new Node("plugins.materials"));
		packageDependencyGraph.addNode(new Node("plugins.partitions"));
		packageDependencyGraph.addNode(new Node("plugins.people"));
		packageDependencyGraph.addNode(new Node("plugins.personproperties"));
		packageDependencyGraph.addNode(new Node("plugins.regions"));
		packageDependencyGraph.addNode(new Node("plugins.reports"));
		packageDependencyGraph.addNode(new Node("plugins.resources"));
		packageDependencyGraph.addNode(new Node("plugins.stochastics"));
		packageDependencyGraph.addNode(new Node("plugins.util"));
		packageDependencyGraph.addNode(new Node("tools"));
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

	private void addToGraph(MutableGraph<PluginId, Object> mutableGraph, Plugin plugin) {

		PluginId origin = plugin.getPluginId();
		mutableGraph.addNode(origin);
		for (PluginId destination : plugin.getPluginDependencies()) {
			mutableGraph.addEdge(new Object(), origin, destination);
		}
	}

	private Graph<Node, Edge> getPluginDependencyGraph() {
		// create a graph of the plugin dependencies
		MutableGraph<PluginId, Object> pluginDependencyGraph = new MutableGraph<>();

		
		
		addToGraph(pluginDependencyGraph, GlobalPropertiesPlugin.builder().setGlobalPropertiesPluginData(GlobalPropertiesPluginData.builder().build()).getGlobalPropertiesPlugin());
		addToGraph(pluginDependencyGraph, GroupsPlugin.builder().setGroupsPluginData(GroupsPluginData.builder().build()).getGroupsPlugin());
		addToGraph(pluginDependencyGraph, MaterialsPlugin.builder().setMaterialsPluginData(MaterialsPluginData.builder().build()).getMaterialsPlugin());
		addToGraph(pluginDependencyGraph, PartitionsPlugin.builder().setPartitionsPluginData(PartitionsPluginData.builder().build()).getPartitionsPlugin());
		addToGraph(pluginDependencyGraph, PeoplePlugin.getPeoplePlugin(PeoplePluginData.builder().build()));
		addToGraph(pluginDependencyGraph, PersonPropertiesPlugin.builder().setPersonPropertiesPluginData(PersonPropertiesPluginData.builder().build()).getPersonPropertyPlugin());

		addToGraph(pluginDependencyGraph, RegionsPlugin.builder().setRegionsPluginData(RegionsPluginData.builder().build()).getRegionsPlugin());
		addToGraph(pluginDependencyGraph, ReportsPlugin.getReportsPlugin());
		addToGraph(pluginDependencyGraph, ResourcesPlugin.builder().setResourcesPluginData(ResourcesPluginData.builder().build()).getResourcesPlugin());
		WellState wellState = WellState.builder().setSeed(0L).build();
		addToGraph(pluginDependencyGraph, StochasticsPlugin.getStochasticsPlugin(StochasticsPluginData.builder().setMainRNGState(wellState).build()));

		// build a map to help convert the map above into the type of graph we
		// need
		Map<PluginId, Node> map = new LinkedHashMap<>();
		map.put(GlobalPropertiesPluginId.PLUGIN_ID, new Node("plugins.globalproperties"));
		map.put(GroupsPluginId.PLUGIN_ID, new Node("plugins.groups"));
		map.put(MaterialsPluginId.PLUGIN_ID, new Node("plugins.materials"));
		map.put(PartitionsPluginId.PLUGIN_ID, new Node("plugins.partitions"));
		map.put(PeoplePluginId.PLUGIN_ID, new Node("plugins.people"));
		map.put(PersonPropertiesPluginId.PLUGIN_ID, new Node("plugins.personproperties"));
		map.put(RegionsPluginId.PLUGIN_ID, new Node("plugins.regions"));
		map.put(ReportsPluginId.PLUGIN_ID, new Node("plugins.reports"));
		map.put(ResourcesPluginId.PLUGIN_ID, new Node("plugins.resources"));
		map.put(StochasticsPluginId.PLUGIN_ID, new Node("plugins.stochastics"));

		/*
		 * Make sure that the map above maps each plugin to a node that is
		 * contained in the package dependency map. Note that this should be a
		 * subset of those nodes.
		 */
		for (PluginId pluginId : map.keySet()) {
			Node node = map.get(pluginId);
			if (!packageDependencyGraph.containsNode(node)) {
				throw new RuntimeException("map contains unknown node for " + pluginId);
			}
		}

		MutableGraph<Node, Edge> mutableResult = new MutableGraph<>();

		for (Object o : pluginDependencyGraph.getEdges()) {
			PluginId originPluginId = pluginDependencyGraph.getOriginNode(o);
			PluginId destinationPluginId = pluginDependencyGraph.getDestinationNode(o);

			Node originNode = map.get(originPluginId);
			Node destinationNode = map.get(destinationPluginId);

			mutableResult.addEdge(new Edge(), originNode, destinationNode);
		}

		Graph<Node, Edge> result = mutableResult.toGraph();
		Optional<GraphDepthEvaluator<Node>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(result);
		if (!optional.isPresent()) {
			throw new RuntimeException("the plugin dependencies are circular");
		}
		return result;
	}

	public PackageDependencyData execute() {

		loadSourceClasses();

		for (Edge edge : packageDependencyGraph.getEdges()) {
			Node originNode = packageDependencyGraph.getOriginNode(edge);
			Node destinationNode = packageDependencyGraph.getDestinationNode(edge);
			System.out.println(originNode.name + "->" + destinationNode.name);
			for (Class<?> c : edge.classes) {
				System.out.println("\t" + c.getSimpleName());
			}
		}

		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

		Optional<GraphDepthEvaluator<Node>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(packageDependencyGraph.toGraph());

		if (!optional.isPresent()) {
			/*
			 * Explain in detail why there is a circular dependency
			 */

			Graph<Node, Edge> g = packageDependencyGraph.toGraph();

			g = Graphs.getSourceSinkReducedGraph(g);
			g = Graphs.getEdgeReducedGraph(g);
			g = Graphs.getSourceSinkReducedGraph(g);

			List<Graph<Node, Edge>> cutGraphs = Graphs.cutGraph(g);
			StringBuilder sb = new StringBuilder();
			String lineSeparator = System.getProperty("line.separator");
			sb.append(lineSeparator);
			boolean firstCutGraph = true;

			for (Graph<Node, Edge> cutGraph : cutGraphs) {
				// cutGraph = Graphs.getSourceSinkReducedGraph(cutGraph);
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

		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

		Graph<Node, Edge> pluginDependencyGraph = getPluginDependencyGraph();

		for (Edge edge : pluginDependencyGraph.getEdges()) {
			Node originNode = pluginDependencyGraph.getOriginNode(edge);
			Node destinationNode = pluginDependencyGraph.getDestinationNode(edge);
			System.out.println(originNode + "---->" + destinationNode);
		}

		MapPathSolver<Node, Edge> mapPathSolver = new MapPathSolver<>(pluginDependencyGraph, (e) -> 1, (a, b) -> 0);

		for (Edge edge : packageDependencyGraph.getEdges()) {
			Node originNode = packageDependencyGraph.getOriginNode(edge);
			Node destinationNode = packageDependencyGraph.getDestinationNode(edge);

			// if (originNode.name.equals("plugins.partitions")) {
			// if (destinationNode.name.equals("plugins.materials")) {
			// System.out.println("weeeeeeeeeeeeeeee");
			//
			// for (Node node : pluginDependencyGraph.getNodes()) {
			// if (node.name.equals(originNode.name)) {
			// System.out.println("found the origin node " +
			// pluginDependencyGraph.containsNode(originNode));
			//
			// }
			// if (node.name.equals(destinationNode.name)) {
			// System.out.println("found the destination node " +
			// pluginDependencyGraph.containsNode(destinationNode));
			//
			// }
			// }
			// System.out.println(pluginDependencyGraph.containsNode(originNode));
			// System.out.println(pluginDependencyGraph.containsNode(originNode));
			// System.out.println(pluginDependencyGraph.containsNode(originNode)
			// && pluginDependencyGraph.containsEdge(destinationNode));
			//
			// }
			// }

			if (pluginDependencyGraph.containsNode(originNode) && pluginDependencyGraph.containsNode(destinationNode)) {
				Optional<util.path.Path<Edge>> optionalPath = mapPathSolver.getPath(originNode, destinationNode);
				if (optionalPath.isEmpty()) {
					System.out.println("The package dependency of " + originNode + "->" + destinationNode + " violates the plugin dependency graph");
				}
			}
		}

		return warningContainerBuilder.build();

	}

}
