package tools.metaunit.reports;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import tools.metaunit.warnings.ConstructorWarning;
import tools.metaunit.warnings.FieldWarning;
import tools.metaunit.warnings.MethodWarning;
import tools.metaunit.warnings.WarningContainer;
import tools.metaunit.warnings.WarningGenerator;
import tools.metaunit.warnings.WarningType;

/**
 * A script covering the details of the GCM Test Plan. It produces a console
 * report that measures the completeness/status of the test classes. It does not
 * measure the correctness of any test, but rather shows which tests exist and
 * their status.
 *
 * @author Shawn Hatch
 *
 */
public class MissingTestsReport {

	public static void main(final String[] args) {

		// Should point to src/main/java
		final Path sourcePath = Paths.get(args[0]);

		// Should point to src/test/java
		final Path testPath = Paths.get(args[1]);
		WarningContainer warningContainer = WarningGenerator.builder()//
				.setSourcePath(sourcePath)//
				.setTestPath(testPath)//
				.build()//
				.execute();//
		
		String classNameFilter = null;
		if(args.length>2) {
			classNameFilter = args[2];
		}


		reportWarnings(warningContainer, classNameFilter);

	}

	private MissingTestsReport() {
	}

	private static void reportWarnings(WarningContainer warningContainer, String filter) {
		Map<String, List<String>> warningMap = new TreeMap<>();

		for (ConstructorWarning constructorWarning : warningContainer.getConstructorWarnings()) {
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

		for (MethodWarning methodWarning : warningContainer.getMethodWarnings()) {
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
		
		for (FieldWarning fieldWarning : warningContainer.getFieldWarnings()) {
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

		if (filter != null) {
			System.out.println("Results are filtered on the class references containing the string '"+filter+"'");
			warningMap.keySet().removeIf((t)->!t.contains(filter));
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
