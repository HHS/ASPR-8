package tools.dependencyanalysis.dependency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import util.graph.Graph;
import util.graph.GraphDepthEvaluator;

public class DependencyGraph {
	private final Path file;

	private DependencyGraph(Path file) {
		this.file = file;
	}

	private void execute() throws IOException {
		List<String> lines = Files.readAllLines(file);

		Graph.Builder<String, Object> builder = Graph.builder();

		for (String line : lines) {
			String[] splits = line.split("\t");
			builder.addEdge(new Object(), splits[0].trim(), splits[1].trim());
		}
		Graph<String, Object> graph = builder.build();

		Optional<GraphDepthEvaluator<String>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(graph);
		if (optional.isPresent()) {
			GraphDepthEvaluator<String> graphDepthEvaluator = optional.get();
			int maxDepth = graphDepthEvaluator.getMaxDepth();
			for (int depth = 0; depth <= maxDepth; depth++) {
				List<String> nodesForDepth = graphDepthEvaluator.getNodesForDepth(depth);
				System.out.println(depth + ": " + nodesForDepth);

			}
		} else {
			System.out.println("cannot perform depth evaluation");
		}

	}

	public static void main(String[] args) throws IOException {
		new DependencyGraph(Paths.get("c:\\temp\\dependencies.txt")).execute();
	}
}
