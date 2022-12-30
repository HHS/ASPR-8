package tools.meta;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import tools.meta.warnings.ConstructorWarning;
import tools.meta.warnings.MethodWarning;
import tools.meta.warnings.WarningContainer;
import tools.meta.warnings.WarningGenerator;
import tools.meta.warnings.WarningType;

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

		reportWarnings(warningContainer, null);

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

		if (filter != null) {
			Predicate<String> predicate = new Predicate<String>() {

				@Override
				public boolean test(String t) {
					return !t.contains(filter);
				}

			};
			warningMap.keySet().removeIf(predicate);
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
