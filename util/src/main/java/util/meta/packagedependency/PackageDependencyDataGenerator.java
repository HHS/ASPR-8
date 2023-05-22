package util.meta.packagedependency;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.meta.packagedependency.PackageDependencyData.PackageDependencyDetails;
import util.meta.packagedependency.PackageDependencyData.PackageRef;
import util.graph.MutableGraph;

/**
 * An utility class for generating a {@linkplain PackageDependencyData} that
 * contains an analysis of the package level java dependencies.
 * 
 */
public class PackageDependencyDataGenerator {

	private static boolean isJavaFile(Path file) {
		return Files.isRegularFile(file) && file.toString().endsWith(".java");
	}

	private static String getClassName(Path sourcePath, Path file) {
		return file.toString().substring(sourcePath.toString().length() + 1, file.toString().length() - 5).replace(File.separator, ".");
	}

	private static class Edge {
		Set<String> classes = new LinkedHashSet<>();
	}

	private void buildNodeIdentification() {
		for (Node node : data.packageDependencyGraph.getNodes()) {
			data.nodeMap.put(node.name, node);
			data.nodeNames.add(node.name);
		}
		Collections.sort(data.nodeNames);
	}

	private Node getImportNode(String line) {

		int index = Collections.binarySearch(data.nodeNames, line);

		if (index >= 0) {
			return data.nodeMap.get(line);
		}

		index = -(index + 1) - 1;

		if (index >= 0) {
			String nodeName = data.nodeNames.get(index);
			if (line.startsWith(nodeName)) {
				return data.nodeMap.get(nodeName);
			}
		}
		return null;
	}

	private void probeFile(Path dir, Path file) throws IOException {

		String className = getClassName(dir, file);

		List<String> lines = Files.readAllLines(file);

		Node originNode = null;
		Set<Node> destinationNodes = new LinkedHashSet<>();

		boolean packageRead = false;
		for (String line : lines) {
			if (!packageRead) {
				String trim = line.trim();
				if (trim.contains("package")) {
					packageRead = true;
					trim = line.substring(7, trim.length()).trim();
					if (trim.endsWith(";")) {
						trim = trim.substring(0, trim.length() - 1);
					}
					data.packageDependencyInfoBuilder.addFoundPackageName(trim);
					Node node = getImportNode(trim);
					if (node != null) {
						originNode = node;
					}
				}
			} else {
				String trim = line.trim();
				if (trim.startsWith("import ")) {
					if (trim.contains("*")) {
						data.packageDependencyInfoBuilder.addWildCardClass(className);
					}
					trim = line.substring(6, trim.length()).trim();
					Node node = getImportNode(trim);
					if (node != null) {
						destinationNodes.add(node);
					}
				}
			}
		}

		if (originNode == null) {
			if (packageRead) {
				data.packageDependencyInfoBuilder.addUncoveredClass(className);
			} else {
				data.packageDependencyInfoBuilder.addPackagelessClass(className);
			}
		} else {

			destinationNodes.remove(originNode);

			for (Node node : destinationNodes) {
				List<Edge> edges = data.packageDependencyGraph.getEdges(originNode, node);
				Edge edge;
				if (edges.isEmpty()) {
					edge = new Edge();
					data.packageDependencyGraph.addEdge(edge, originNode, node);
				} else {
					edge = edges.get(0);
				}
				edge.classes.add(className);
			}
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
		private Path dir;

		private SourceFileVisitor(Path dir) {
			this.dir = dir;
		}

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) throws IOException {
			if (isJavaFile(file)) {
				probeFile(dir, file);
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private Data data;

	private static class Data {
		private Set<Path> directories = new LinkedHashSet<>();
		private Set<String> packageNames = new LinkedHashSet<>();
		private MutableGraph<Node, Edge> packageDependencyGraph = new MutableGraph<>();
		private List<String> nodeNames = new ArrayList<>();
		private Map<String, Node> nodeMap = new LinkedHashMap<>();
		private PackageDependencyData.Builder packageDependencyInfoBuilder = PackageDependencyData.builder();
		
		public Data() {}
		public Data(Data data) {
			directories.addAll(data.directories);
			packageNames.addAll(data.packageNames);
			for(Node node : data.packageDependencyGraph.getNodes()) {
				packageDependencyGraph.addNode(node);
			}
			for(Edge edge : data.packageDependencyGraph.getEdges()) {
				Node originNode = data.packageDependencyGraph.getOriginNode(edge);
				Node destinationNode = data.packageDependencyGraph.getDestinationNode(edge);
				packageDependencyGraph.addEdge(edge, originNode, destinationNode);
			}
			nodeNames.addAll(data.nodeNames);
			nodeMap.putAll(data.nodeMap);
			
		}
	}

	public final static Builder builder() {
		return new Builder();
	}

	public final static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		public PackageDependencyDataGenerator build() {
			validate();
			return new PackageDependencyDataGenerator(new Data(data));
		}

		private void validate() {

		}

		/**
		 * Adds a source code directory to analyze
		 * 
		 * @throws NullPointerException
		 *             if the path is null
		 * @throws IllegalArgumentException
		 *             if the path is not a directory
		 * 
		 * 
		 */
		public Builder addDirectory(Path directory) {
			if (directory == null) {
				throw new NullPointerException();
			}
			if (!Files.isDirectory(directory)) {
				throw new IllegalArgumentException(directory + " is not a valid directory");
			}

			data.directories.add(directory);
			return this;
		}

		/**
		 * Adds a packageName prefix that will be used to group source files
		 * into a single node in the analysis
		 */
		public Builder addPackageName(String packageName) {
			validatePackageName(packageName);
			data.packageNames.add(packageName);
			return this;
		}

	}

	private static void validatePackageName(String packageName) {
		boolean valid = true;
		for (int i = 0; i < packageName.length(); i++) {
			char c = packageName.charAt(i);
			if (!isValidPackageNameCharacter(c)) {
				valid = false;
				break;
			}
		}
		if (packageName.startsWith(".")) {
			valid = false;
		}

		if (!valid) {
			throw new IllegalArgumentException("Illegal package name " + packageName);
		}

	}

	private static boolean isValidPackageNameCharacter(char c) {
		if (c == '.') {
			return true;
		}
		if ('a' <= c && 'z' >= c) {
			return true;
		}
		if ('A' <= c && 'Z' >= c) {
			return true;
		}

		return false;

	}

	private PackageDependencyDataGenerator(Data data) {
		this.data = data;

	}

	private void loadClasses() {

		try {
			for (Path path : data.directories) {
				final SourceFileVisitor sourceFileVisitor = new SourceFileVisitor(path);
				Files.walkFileTree(path, sourceFileVisitor);
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

	}

	private void loadNodes() {
		for (String packageName : data.packageNames) {
			data.packageDependencyGraph.addNode(new Node(packageName));
		}
	}

	public PackageDependencyData execute() {

		try {
			loadNodes();
			buildNodeIdentification();
			loadClasses();

			MutableGraph<PackageRef, PackageDependencyDetails> m = new MutableGraph<>();

			for (Node node : data.packageDependencyGraph.getNodes()) {
				m.addNode(new PackageRef(node.name));
			}
			for (Edge edge : data.packageDependencyGraph.getEdges()) {
				Node originNode = data.packageDependencyGraph.getOriginNode(edge);
				Node destinationNode = data.packageDependencyGraph.getDestinationNode(edge);
				m.addEdge(new PackageDependencyDetails(edge.classes), new PackageRef(originNode.name), new PackageRef(destinationNode.name));
			}

			data.packageDependencyInfoBuilder.setGraph(m.toGraph());

			for (Path path : data.directories) {
				data.packageDependencyInfoBuilder.addInputDirectory(path);
			}
			for (String packageName : data.packageNames) {
				data.packageDependencyInfoBuilder.addInputPackageName(packageName);
			}

			return data.packageDependencyInfoBuilder.build();
		} finally {
			data = new Data();
		}

	}

}
