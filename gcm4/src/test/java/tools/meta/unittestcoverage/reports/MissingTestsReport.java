package tools.meta.unittestcoverage.reports;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import tools.meta.unittestcoverage.MetaInfoContainer;
import tools.meta.unittestcoverage.MetaInfoGenerator;
import tools.meta.unittestcoverage.warnings.ConstructorWarning;
import tools.meta.unittestcoverage.warnings.FieldWarning;
import tools.meta.unittestcoverage.warnings.MethodWarning;
import tools.meta.unittestcoverage.warnings.WarningType;

/**
 * A script that produces a console report shows missing unit tests.
 *
 *
 */
public class MissingTestsReport {

	/**
	 * Runs the test and print the result to console. The first argument is the
	 * path reference to the source folder for the production code base. The
	 * second argument is the path reference to the source folder for the unit
	 * test code. The third argument is optional and is a filter string that
	 * will exclude all source classes that do not contain(case insensitive) the
	 * filter string.
	 */
	public static void main(final String[] args) {

		// Should point to src/main/java
		final Path sourcePath = Paths.get(args[0]);

		// Should point to src/test/java
		final Path testPath = Paths.get(args[1]);
		MetaInfoContainer metaInfoContainer = MetaInfoGenerator.builder()//
															.setSourcePath(sourcePath)//
															.setTestPath(testPath)//
															.build()//
															.execute();//

		String classNameFilter = null;
		if (args.length > 2) {
			classNameFilter = args[2];			
		}

		reportWarnings(metaInfoContainer, classNameFilter);

	}

	private MissingTestsReport() {
	}

	private static void reportWarnings(MetaInfoContainer metaInfoContainer, String filter) {
		Map<String, List<String>> warningMap = new TreeMap<>();

		for (ConstructorWarning constructorWarning : metaInfoContainer.getConstructorWarnings()) {
			if (constructorWarning.getWarningType().equals(WarningType.SOURCE_CONSTRUCTOR_REQUIRES_TEST)) {
				String classRef = constructorWarning.getConstructor().getDeclaringClass().getName();
				List<String> list = warningMap.get(classRef);
				if (list == null) {
					list = new ArrayList<>();
					warningMap.put(classRef, list);
				}
				list.add(constructorWarning.getConstructor().toString());
			}
		}

		for (MethodWarning methodWarning : metaInfoContainer.getMethodWarnings()) {
			if (methodWarning.getWarningType().equals(WarningType.SOURCE_METHOD_REQUIRES_TEST)) {
				String classRef = methodWarning.getMethod().getDeclaringClass().getName();
				List<String> list = warningMap.get(classRef);
				if (list == null) {
					list = new ArrayList<>();
					warningMap.put(classRef, list);
				}
				list.add(methodWarning.getMethod().toString());
			}
		}

		for (FieldWarning fieldWarning : metaInfoContainer.getFieldWarnings()) {
			if (fieldWarning.getWarningType().equals(WarningType.SOURCE_FIELD_REQUIRES_TEST)) {
				String classRef = fieldWarning.getField().getDeclaringClass().getName();
				List<String> list = warningMap.get(classRef);
				if (list == null) {
					list = new ArrayList<>();
					warningMap.put(classRef, list);
				}
				list.add(fieldWarning.getField().toString());
			}
		}

		if (filter != null && !filter.isEmpty()) {
			System.out.println("Results are filtered on the class references containing the string '" + filter + "'");
			warningMap.keySet().removeIf((t) -> !t.toLowerCase().contains(filter.toLowerCase()));
		}

		for (String classRef : warningMap.keySet()) {
			List<String> warnings = warningMap.get(classRef);
			System.out.println(classRef);
			for (String warning : warnings) {
				System.out.println("\t" + warning);
			}
			System.out.println();
		}
	}

}
