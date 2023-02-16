package tools.meta.classdependency.classgraph.reports;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import tools.meta.classdependency.classgraph.support.ClassDependencyScan;
import tools.meta.classdependency.classgraph.support.JavaDependency;
import util.graph.Graph;
import util.graph.GraphDepthEvaluator;
import util.graph.Graphs;
import util.graph.MutableGraph;

public final class CircularClassDependencyReport {
	private CircularClassDependencyReport() {
	}

	public static void report(ClassDependencyScan classDependencyScan) {
		System.out.println();
		System.out.println("circular class dependency report");
		

		Set<JavaDependency> javaDependencies = classDependencyScan.getJavaDependencies();
		Set<String> localPackageNames = classDependencyScan.getLocalPackageNames();
		
		
		MutableGraph<String, Object> m = new MutableGraph<>();
		for (JavaDependency javaDependency : javaDependencies) {
			String sourceFileName = javaDependency.getDependentRef().getPackageName()+"."+javaDependency.getDependentRef().getClassName();
			String importFileName = javaDependency.getSupportRef().getPackageName()+"."+javaDependency.getSupportRef().getClassName();
			if (localPackageNames.contains(javaDependency.getSupportRef().getPackageName())) {
				m.addEdge(new Object(), sourceFileName, importFileName);
			}
		}
		

		Optional<GraphDepthEvaluator<String>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(m.toGraph());
		if (optional.isPresent()) {
			System.out.println();
			System.out.println("acyclic layers");
			
			GraphDepthEvaluator<String> graphDepthEvaluator = optional.get();
			int maxDepth = graphDepthEvaluator.getMaxDepth();
			for (int i = 0; i <= maxDepth; i++) {
				List<String> nodesForDepth = graphDepthEvaluator.getNodesForDepth(i);
				for (String node : nodesForDepth) {
					System.out.println(i + "\t" + node);
				}
			}
		} else {
			System.out.println();
			System.out.println("cyclic groups");
			
			Graph<String, Object> g = m.toGraph();
			
			g = Graphs.getSourceSinkReducedGraph(g);
			
			g = Graphs.getEdgeReducedGraph(g);
			
			g = Graphs.getSourceSinkReducedGraph(g);
			
			List<Graph<String, Object>> cutGraphs = Graphs.cutGraph(g);
			StringBuilder sb = new StringBuilder();
			String lineSeparator = System.getProperty("line.separator");
			
			for (Graph<String, Object> cutGraph : cutGraphs) {				
				sb.append(lineSeparator);
				Set<String> nodes = cutGraph.getNodes().stream().collect(Collectors.toCollection(LinkedHashSet::new));

				for (String node : nodes) {
					for (Object edge : cutGraph.getOutboundEdges(node)) {
						String dependencyNode = cutGraph.getDestinationNode(edge);
						sb.append(node);
						sb.append(",");
						sb.append(dependencyNode);
						sb.append(lineSeparator);
					}
				}
			}
			System.out.println(sb);
		}
		
	}
}
