package tools.meta;



public class PluginDependencyTest {
//	private PluginDependencyTest() {
//
//	}
//
//	private static class Stub implements Comparable<Stub> {
//		private final int loadedOrder;
//		private int dependencyOrder;
//		private final Plugin plugin;
//
//		public Stub(Plugin plugin, int loadedOrder) {
//			this.loadedOrder = loadedOrder;
//			this.plugin = plugin;
//		}
//
//		@Override
//		public int compareTo(Stub stub) {
//			int result = Integer.compare(dependencyOrder, stub.dependencyOrder);
//			if (result == 0) {
//				result = Integer.compare(loadedOrder, stub.loadedOrder);
//			}
//			return result;
//		}
//
//	}
//
//	private void execute() {
//
//		List<Plugin> plugins = new ArrayList<>();
//		plugins.add(new StochasticsPlugin(StochasticsInitialData.builder().build(), 234234L));
//		plugins.add(new ReportPlugin(ReportsInitialData.builder().build()));
//		plugins.add(new GlobalPlugin(GlobalInitialData.builder().build()));
//		plugins.add(new PeoplePlugin(PeopleInitialData.builder().build()));
//		plugins.add(new PartitionsPlugin());
//		plugins.add(new RegionPlugin(RegionInitialData.builder().build()));
//		plugins.add(new PersonPropertiesPlugin(PersonPropertyInitialData.builder().build()));
//		plugins.add(new GroupPlugin(GroupInitialData.builder().build()));
//		plugins.add(new ResourcesPlugin(ResourceInitialData.builder().build()));
//		plugins.add(new MaterialsPlugin(MaterialsInitialization.builder().build()));
//		plugins.add(new GCMPlugin());
//		plugins.add(new PropertiesPlugin());
//		plugins.add(new ComponentPlugin());
//
//		Collections.shuffle(plugins);
//
//		///////////////////////////////////////
//		// check for null inputs
//		for (Plugin plugin : plugins) {
//			if (plugin == null) {
//				throw new RuntimeException("null plugin");
//			}
//		}
//		// load structures and check for duplicate plugins
//		List<Stub> stubs = new ArrayList<>();
//		Map<Class<? extends Plugin>, Stub> map = new LinkedHashMap<>();
//		int loadedOrder = 0;
//		for (Plugin plugin : plugins) {
//			Stub stub = new Stub(plugin, loadedOrder++);
//			stubs.add(stub);
//			if (map.containsKey(plugin.getClass())) {
//				throw new RuntimeException("duplicate plugin " + plugin.getClass());
//			}
//			map.put(plugin.getClass(), stub);
//		}
//
//		// check for missing plugins
//		for (Stub stub : stubs) {
//			for (Class<? extends Plugin> dependencyClass : stub.plugin.getPluginDependencies()) {
//				Stub dependencyStub = map.get(dependencyClass);
//				if (dependencyStub == null) {
//					throw new RuntimeException("cannot locate instance of " + dependencyClass + " needed for " + stub.plugin.getClass().getSimpleName());
//				}
//			}
//		}
//
//		// build the dependency graph
//		MutableGraph<Stub, Object> m = new MutableGraph<>();
//		for (Stub stub : stubs) {
//			m.addNode(stub);
//			for (Class<? extends Plugin> dependencyClass : stub.plugin.getPluginDependencies()) {
//				Stub dependencyStub = map.get(dependencyClass);
//				m.addEdge(new Object(), stub, dependencyStub);
//			}
//		}
//
//		Optional<GraphDepthEvaluator<Stub>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(m.toGraph());
//
//		if (!optional.isPresent()) {
//			Graph<Stub, Object> g = m.toGraph();
//			g = Graphs.getSourceSinkReducedGraph(g);
//			List<Graph<Stub, Object>> cutGraphs = Graphs.cutGraph(g);
//			StringBuilder sb = new StringBuilder();
//			String lineSeparator = System.getProperty("line.separator");
//			sb.append(lineSeparator);
//			boolean firstCutGraph = true;
//			
//			for (Graph<Stub, Object> cutGraph : cutGraphs) {				
//				if(firstCutGraph) {
//					firstCutGraph = false;
//				}else {
//					sb.append(lineSeparator);
//				}
//				sb.append("Circular plugin dependency group: ");
//				sb.append(lineSeparator);
//				Set<Stub> nodes = cutGraph.getNodes().stream().collect(Collectors.toCollection(LinkedHashSet::new));
//				
//				for (Stub node : nodes) {
//					sb.append("\t");
//					sb.append(node.plugin.getClass().getSimpleName());
//					sb.append(" requires:");
//					sb.append(lineSeparator);
//					for(Class<? extends Plugin> pluginClass : node.plugin.getPluginDependencies()) {
//						Stub dependencyNode = map.get(pluginClass);
//						if(nodes.contains(dependencyNode)) {				
//							sb.append("\t");
//							sb.append("\t");
//							sb.append(dependencyNode.plugin.getClass().getSimpleName());
//							sb.append(lineSeparator);
//						}
//					}
//				}				
//			}			
//			
//			throw new RuntimeException(sb.toString());
//		}
//
//		GraphDepthEvaluator<Stub> graphDepthEvaluator = optional.get();
//
//		for (Stub stub : stubs) {
//			stub.dependencyOrder = graphDepthEvaluator.getDepth(stub);
//		}
//
//		Collections.sort(stubs);
//
//		for (Stub stub : stubs) {
//			System.out.println(stub.dependencyOrder + "\t" + stub.loadedOrder + "\t" + stub.plugin.getClass().getSimpleName());
//		}
//	}
//
//	public static void main(String[] args) {
//		new PluginDependencyTest().execute();
//
//	}
}
