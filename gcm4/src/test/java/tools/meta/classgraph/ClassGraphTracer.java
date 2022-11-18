package tools.meta.classgraph;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import util.graph.Graph;
import util.graph.GraphDepthEvaluator;
import util.graph.Graphs;
import util.graph.MutableGraph;

public class ClassGraphTracer {

	private static boolean isJavaFile(Path file) {
		return Files.isRegularFile(file) && file.toString().endsWith(".java");
	}

	private final class SourceFileVisitor extends SimpleFileVisitor<Path> {
		private final Path baseDirectory;

		private SourceFileVisitor(Path baseDirectory) {
			this.baseDirectory = baseDirectory;
		}

		private Set<String> packages = new LinkedHashSet<>();
		private Set<Pair<String, String>> importPairs = new LinkedHashSet<>();

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			Objects.requireNonNull(dir);
			Objects.requireNonNull(attrs);
			int baseLength = baseDirectory.toString().length();
			String dirString = dir.toString();
			String reducedString = dirString.substring(baseLength, dirString.length());
			if (reducedString.startsWith("\\")) {
				reducedString = reducedString.substring(1);
			}
			reducedString = reducedString.replaceAll("\\\\", ".");
			packages.add(reducedString);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) {
			String packageName = file.getParent().toString();
			int baseLength = baseDirectory.toString().length();
			packageName = packageName.substring(baseLength);
			if (packageName.startsWith("\\")) {
				packageName = packageName.substring(1);
			}
			packageName = packageName.replaceAll("\\\\", ".");

			if (isJavaFile(file)) {
				try {

					List<String> lines = Files.readAllLines(file);
					for (String line : lines) {
						if (line.startsWith("import ")) {
							// imports.add(file.getFileName() + "\t" + line);
							String linePackage = line.substring(7);
							int index = linePackage.length();
							for (int i = 0; i < linePackage.length(); i++) {
								if (linePackage.charAt(i) == '.') {
									index = i;
								}
							}
							linePackage = linePackage.substring(0, index);
							importPairs.add(new Pair<>(packageName, linePackage));
							System.out.println(packageName + "\t" + linePackage + "\t" + file.toString());
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private final Path dir;

	private ClassGraphTracer(Path dir) {
		this.dir = dir;
	}

	private void execute() throws IOException {
		final SourceFileVisitor sourceFileVisitor = new SourceFileVisitor(dir);
		Files.walkFileTree(dir, sourceFileVisitor);
		System.out.println();
		for (Pair<String, String> importPair : sourceFileVisitor.importPairs) {
			String sourcePackageName = importPair.getFirst();
			String importPackageName = importPair.getSecond();
			if (sourceFileVisitor.packages.contains(importPair.getSecond())) {
				System.out.println(sourcePackageName + "\t" + importPackageName);
			}
		}
		System.out.println();

		MutableGraph<String, Object> m = new MutableGraph<>();
		for (Pair<String, String> importPair : sourceFileVisitor.importPairs) {
			String sourcePackageName = importPair.getFirst();
			String importPackageName = importPair.getSecond();
			if (sourceFileVisitor.packages.contains(importPackageName)) {
				m.addEdge(new Object(), sourcePackageName, importPackageName);
			}
		}

		Optional<GraphDepthEvaluator<String>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(m.toGraph());
		if (optional.isPresent()) {
			System.out.println("acyclic");
			GraphDepthEvaluator<String> graphDepthEvaluator = optional.get();
			int maxDepth = graphDepthEvaluator.getMaxDepth();
			for (int i = 0; i <= maxDepth; i++) {
				List<String> nodesForDepth = graphDepthEvaluator.getNodesForDepth(i);
				for (String node : nodesForDepth) {
					System.out.println(i + "\t" + node);
				}
			}
		} else {
			System.out.println("cyclic");
			Graph<String, Object> g = m.toGraph();
			
			g = Graphs.getSourceSinkReducedGraph(g);
			g = Graphs.getEdgeReducedGraph(g);
			g = Graphs.getSourceSinkReducedGraph(g);
			
			List<Graph<String, Object>> cutGraphs = Graphs.cutGraph(g);
			StringBuilder sb = new StringBuilder();
			String lineSeparator = System.getProperty("line.separator");
			boolean firstCutGraph = true;
			for (Graph<String, Object> cutGraph : cutGraphs) {
				if (firstCutGraph) {
					firstCutGraph = false;
				} else {
					sb.append(lineSeparator);
				}
				sb.append(lineSeparator);
				sb.append("Dependency group: ");
				sb.append(lineSeparator);
				Set<String> nodes = cutGraph.getNodes().stream().collect(Collectors.toCollection(LinkedHashSet::new));

				for (String node : nodes) {
					sb.append("\t");
					sb.append(node);
					sb.append(" requires:");
					sb.append(lineSeparator);
					for (Object edge : cutGraph.getOutboundEdges(node)) {
						String dependencyNode = cutGraph.getDestinationNode(edge);

						sb.append("\t");
						sb.append("\t");
						sb.append(dependencyNode);
						sb.append(lineSeparator);

					}
				}
			}
			System.out.println(sb);
		}
	}

	public static void execute(Path dir) throws IOException {
		new ClassGraphTracer(dir).execute();
	}

}
