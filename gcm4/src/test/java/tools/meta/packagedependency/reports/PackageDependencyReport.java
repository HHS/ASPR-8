package tools.meta.packagedependency.reports;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import tools.meta.packagedependency.PackageDependencyData;
import tools.meta.packagedependency.PackageDependencyData.PackageDependencyDetails;
import tools.meta.packagedependency.PackageDependencyData.PackageRef;
import tools.meta.packagedependency.PackageDependencyDataGenerator;
import util.graph.Graph;
import util.graph.GraphDepthEvaluator;
import util.graph.Graphs;

/**
 * A console report detailing java package level dependencies across multiple
 * directories. Inputs consist of 1) directories and 2) package names. The
 * package names do not need to be complete. For example, if alpha.beta is a
 * full package name, any of the following will work: alph, alpha, alpha.,
 * alpha.bet, etc.
 * 
 * Every java class in the covered directories is scanned and the import
 * statements of the class are analyzed. Wildcard characters in the import
 * statements are tolerated, but will result in a warning that the analysis may
 * be invalid. Each import statement is assumed to occupy a single line and each
 * java file should have a package statement.
 * 
 * Classes are mapped to the first package name that matches their actual
 * package name. Thus class to class dependencies are mapped to package to
 * package dependencies. Each package to package dependency retains the list of
 * classes that formed that dependency.
 * 
 * The ouput consists of:
 * <li>a summary section that lists the inputs
 * <li>a listing of classes containing wildcard characters in their import
 * statement
 * <li>a listing of classes that have no package statements
 * <li>a listing of classes that have no corresponding package name in the
 * inputs statements
 * <li>either a listing of any circular package dependencies found or a listinge
 * of the packages in DAG order
 * <li>a listing of the package dependencies
 *
 * 
 */
public class PackageDependencyReport {
	
	/*
	 *  Cover the issue of package names masking each other
	 * 
	 *  Add help when no arguments are used
	 * 
	 */

	
	private static enum Command{
		PACKAGE_NAME("-p"),
		HELP("-help"),
		DIRECTORY_NAME("-d");
		private final String commandString;
		private Command(String commandString){
			this.commandString = commandString;
		}
		public static Command getCommand(String value) {
			for(Command command : Command.values()) {
				if(command.commandString.equals(value)) {
					return command;
				}
			}
			return null;
		}
	}
	private static void printInstructions() {
		System.out.println("Usage: ");
		System.out.println("\t"+ "Any number or order of the following commands are legal:");
		System.out.println("\t"+"\t"+"-p followed by any number of package names");
		System.out.println("\t"+"\t"+"-d followed by any number of directory names");
		System.out.println("\t"+"\t"+"\t"+"directory names containing spaces should");
		System.out.println("\t"+"\t"+"\t"+"be surrounded in double quotes");
		System.out.println("\t"+"\t"+"-help for instructions");
		System.out.println();
		System.out.println("Example: ");
		System.out.println("\t"+"-d c:\\temp\\src\\main\\java c:\\temp\\src\\test\\java" );
		System.out.println("\t"+"-p util engine.start engine.main plugins.plugin1 ");
		System.out.println("\t"+"-d c:\\temp\\other" );
		System.out.println("\t"+"-p coreutils math");
		
	}

	public static void main(final String[] args) {
		
		Map<Command,List<String>> commandMap = new LinkedHashMap<>();
		for(Command command : Command.values()) {
			commandMap.put(command, new ArrayList<>());
		}
		
		
		if(args.length == 0) {
			printInstructions();
			return;
		}

		Command currentCommand = null;
		for(String arg : args) {
			Command command = Command.getCommand(arg);
			if(command != null) {
				currentCommand = command;
				if(currentCommand == Command.HELP) {
					printInstructions();
					return;
				}
			}else {
				if(currentCommand != null) {
					commandMap.get(currentCommand).add(arg);
				}else {
					throw new IllegalArgumentException(arg);
				}
			}			
		}

		PackageDependencyDataGenerator.Builder builder = PackageDependencyDataGenerator.builder();

		for (String directoryName : commandMap.get(Command.DIRECTORY_NAME)) {
			Path path = Paths.get(directoryName);
			builder.addDirectory(path);
		}
		
		for(String packageName : commandMap.get(Command.PACKAGE_NAME)) {
			builder.addPackageName(packageName);//
		}
		

		

		PackageDependencyData packageDependencyData = builder.build().execute();//

		reportInputSummary(packageDependencyData);
		reportWildCardClasses(packageDependencyData);
		reportPackagelessClasses(packageDependencyData);
		reportUncoveredClasses(packageDependencyData);
		reportCircularity(packageDependencyData);
		reportGraph(packageDependencyData);

	}

	private PackageDependencyReport() {
	}

	private static void reportInputSummary(PackageDependencyData packageDependencyData) {
		System.out.println("Package dependency analysis was performed over the following directories:");
		for (Path directory : packageDependencyData.getCoveredDirectories()) {
			System.out.println(directory);
		}
		System.out.println();
		System.out.println("Packages are grouped by input package names.");
		System.out.println("Each package name may be truncated or not correspond to any actual package.");
		System.out.println("Classes associated with a given package name are free to have circular references.");
		System.out.println("Only cross package circularity is considered.");
		System.out.println("The package names are:");
		System.out.println();
		for (String packageName : packageDependencyData.getCoveredPackageNames()) {
			System.out.println(packageName);
		}
	}

	private static void reportGraph(PackageDependencyData packageDependencyData) {
		System.out.println();
		System.out.println("The package dependencies are:");
		System.out.println();

		System.out.println("package" + "\t" + "dependency" + "\t" + "class");

		Graph<PackageRef, PackageDependencyDetails> packageDependencyGraph = packageDependencyData.getPackageDependencyGraph();
		for (PackageDependencyDetails edge : packageDependencyGraph.getEdges()) {
			PackageRef originNode = packageDependencyGraph.getOriginNode(edge);
			PackageRef destinationNode = packageDependencyGraph.getDestinationNode(edge);
			for (Class<?> c : edge.getClasses()) {
				System.out.println(originNode.getName() + "\t" + destinationNode.getName() + "\t" + c.getCanonicalName());
			}
		}
	}

	private static void reportCircularity(PackageDependencyData packageDependencyData) {
		System.out.println();

		Graph<PackageRef, PackageDependencyDetails> packageDependencyGraph = packageDependencyData.getPackageDependencyGraph();
		Optional<GraphDepthEvaluator<PackageRef>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(packageDependencyGraph);

		if (!optional.isPresent()) {
			System.out.println("Circular dependencies were found:");

			/*
			 * Explain in detail why there is a circular dependency
			 */

			Graph<PackageRef, PackageDependencyDetails> g = packageDependencyGraph;

			g = Graphs.getSourceSinkReducedGraph(g);
			g = Graphs.getEdgeReducedGraph(g);
			g = Graphs.getSourceSinkReducedGraph(g);

			List<Graph<PackageRef, PackageDependencyDetails>> cutGraphs = Graphs.cutGraph(g);			
			
			StringBuilder sb = new StringBuilder();
			String lineSeparator = System.getProperty("line.separator");
			sb.append(lineSeparator);
			boolean firstCutGraph = true;

			for (Graph<PackageRef, PackageDependencyDetails> cutGraph : cutGraphs) {
				// cutGraph = Graphs.getSourceSinkReducedGraph(cutGraph);
				if (firstCutGraph) {
					firstCutGraph = false;
				} else {
					sb.append(lineSeparator);
				}
				sb.append("Dependency group: ");
				sb.append(lineSeparator);
				for (PackageRef node : cutGraph.getNodes()) {
					for (PackageDependencyDetails edge : cutGraph.getOutboundEdges(node)) {
						PackageRef dependencyNode = cutGraph.getDestinationNode(edge);
						sb.append(node.getName());
						sb.append("-->");
						sb.append(dependencyNode.getName());
						sb.append(lineSeparator);
						for(Class<?> classRef : edge.getClasses()) {
							sb.append("\t");
							sb.append(classRef.getCanonicalName());
							sb.append(lineSeparator);
						}
					}
				}
			}

			System.out.println(sb);
		} else {
			System.out.println("No circular package dependencies were found.  The packages form an acyclic directed graph with the following levels.");
			System.out.println();
			GraphDepthEvaluator<PackageRef> graphDepthEvaluator = optional.get();
			int maxDepth = graphDepthEvaluator.getMaxDepth();
			for (int depth = 0; depth <= maxDepth; depth++) {
				List<PackageRef> nodes = graphDepthEvaluator.getNodesForDepth(depth);
				for (PackageRef node : nodes) {
					System.out.println(depth + "\t" + node.getName());
				}
			}
		}
	}

	private static void reportWildCardClasses(PackageDependencyData packageDependencyData) {
		Set<Class<?>> wildcardClasses = packageDependencyData.getWildcardClasses();
		if (!wildcardClasses.isEmpty()) {

			System.out.println("The following classes contain wildcard characters in their import statements.  All other findings are potentially invalid.");

			for (Class<?> wildcardClass : wildcardClasses) {
				System.out.println(wildcardClass);
			}
		}
	}

	private static void reportPackagelessClasses(PackageDependencyData packageDependencyData) {
		Set<Class<?>> packagelessClasses = packageDependencyData.getPackagelessClasses();
		if (!packagelessClasses.isEmpty()) {
			System.out.println("The following classes do not contain package statements.  All other findings are potentially invalid.");

			for (Class<?> wildcardClass : packagelessClasses) {
				System.out.println(wildcardClass);
			}
		}
	}

	private static void reportUncoveredClasses(PackageDependencyData packageDependencyData) {
		Set<Class<?>> uncoveredClasses = packageDependencyData.getUncoveredClasses();
		if (!uncoveredClasses.isEmpty()) {
			System.out.println("The following classes are not covered in the analysis. They were encounted in the contributed directories, but do not match any of the contributed package names.");

			for (Class<?> wildcardClass : uncoveredClasses) {
				System.out.println(wildcardClass);
			}
		}
	}

}
