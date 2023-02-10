package tools.metaunit.reports;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tools.metaunit.CircularInfoContainer;
import tools.metaunit.CircularInfoGenerator;
import tools.metaunit.warnings.ConstructorWarning;
import tools.metaunit.warnings.FieldWarning;
import tools.metaunit.warnings.MethodWarning;
import tools.metaunit.warnings.WarningType;
import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestField;
import util.annotations.UnitTestMethod;

/**
 * A script covering the details of the GCM Test Plan. It produces a console
 * report that measures the completeness/status of the test classes. It does not
 * measure the correctness of any test, but rather shows which tests exist and
 * their status.
 *
 *
 */
public class CircularInfoReport {

	public static void main(final String[] args) {

		// Should point to src/main/java
		final Path sourcePath = Paths.get(args[0]);

		// Should point to src/test/java
		final Path testPath = Paths.get(args[1]);
		CircularInfoContainer circularInfoContainer = CircularInfoGenerator	.builder()//
																.setSourcePath(sourcePath)//
																.setTestPath(testPath)//
																.build()//
																.execute();//

		reportWarnings(circularInfoContainer);
		reportTags(circularInfoContainer);

	}

	private CircularInfoReport() {
	}

	private static String getFieldString(UnitTestField unitTestField) {
		return "Field: " + unitTestField.target().getCanonicalName() + "." + unitTestField.name();
	}

	private static String getMethodString(UnitTestMethod unitTestMethod) {

		return "Method: " + unitTestMethod.target().getCanonicalName() + "." + unitTestMethod.name() + Arrays.toString(unitTestMethod.args());
	}

	private static String getConstructorString(UnitTestConstructor unitTestConstructor) {
		return "Constructor: " + unitTestConstructor.target().getCanonicalName() + "." + Arrays.toString(unitTestConstructor.args());
	}

	private static void reportTags(CircularInfoContainer circularInfoContainer) {
		Map<UnitTag, Map<Class<?>, List<String>>> taggedAnnotations = new LinkedHashMap<>();
		for (UnitTag unitTag : UnitTag.values()) {
			taggedAnnotations.put(unitTag, new LinkedHashMap<>());
		}
		for (UnitTestField unitTestField : circularInfoContainer.getUnitTestFields()) {
			Class<?> target = unitTestField.target();
			String fieldString = getFieldString(unitTestField);
			for (UnitTag unitTag : unitTestField.tags()) {
				Map<Class<?>, List<String>> map = taggedAnnotations.get(unitTag);
				List<String> list = map.get(target);
				if (list == null) {
					list = new ArrayList<>();
					map.put(target, list);
				}
				list.add(fieldString);
			}
		}
		for (UnitTestMethod unitTestMethod : circularInfoContainer.getUnitTestMethods()) {
			Class<?> target = unitTestMethod.target();
			String fieldString = getMethodString(unitTestMethod);
			for (UnitTag unitTag : unitTestMethod.tags()) {
				Map<Class<?>, List<String>> map = taggedAnnotations.get(unitTag);
				List<String> list = map.get(target);
				if (list == null) {
					list = new ArrayList<>();
					map.put(target, list);
				}
				list.add(fieldString);
			}
		}
		for (UnitTestConstructor unitTestConstructor : circularInfoContainer.getUnitTestConstructors()) {
			Class<?> target = unitTestConstructor.target();
			String fieldString = getConstructorString(unitTestConstructor);
			for (UnitTag unitTag : unitTestConstructor.tags()) {
				Map<Class<?>, List<String>> map = taggedAnnotations.get(unitTag);
				List<String> list = map.get(target);
				if (list == null) {
					list = new ArrayList<>();
					map.put(target, list);
				}
				list.add(fieldString);
			}
		}

		for (UnitTag unitTag : taggedAnnotations.keySet()) {
			Map<Class<?>, List<String>> map = taggedAnnotations.get(unitTag);
			if (!map.isEmpty()) {
				System.out.println("Tag = " + unitTag);
				for (Class<?> target : map.keySet()) {
					List<String> list = map.get(target);
					for (String annotationString : list) {
						System.out.println("\t"+target.getSimpleName()+"\t" + annotationString);
					}
				}
			}
		}

	}

	private static void reportWarnings(CircularInfoContainer circularInfoContainer) {

		Map<WarningType, List<String>> warningMap = new LinkedHashMap<>();
		for (WarningType warningType : WarningType.values()) {
			warningMap.put(warningType, new ArrayList<>());
		}

		for (FieldWarning fieldWarning : circularInfoContainer.getFieldWarnings()) {
			List<String> list = warningMap.get(fieldWarning.getWarningType());
			list.add(fieldWarning.getField().getDeclaringClass().getSimpleName() + "\t" + fieldWarning.getField().toString() + " " + fieldWarning.getDetails());
		}

		for (MethodWarning methodWarning : circularInfoContainer.getMethodWarnings()) {
			List<String> list = warningMap.get(methodWarning.getWarningType());
			list.add(methodWarning.getMethod().getDeclaringClass().getSimpleName() + "\t" + methodWarning.getMethod().toString() + " " + methodWarning.getDetails());
		}

		for (ConstructorWarning constructorWarning : circularInfoContainer.getConstructorWarnings()) {
			List<String> list = warningMap.get(constructorWarning.getWarningType());
			list.add(constructorWarning.getConstructor().getDeclaringClass().getSimpleName() + "\t" + constructorWarning.getConstructor().toString() + " " + constructorWarning.getDetails());
		}

		int warningCount = 0;
		for (WarningType warningType : WarningType.values()) {
			warningCount += warningMap.get(warningType).size();
		}

		System.out.println("(" + warningCount + ")");
		for (WarningType warningType : WarningType.values()) {
			List<String> warnings = warningMap.get(warningType);
			if (!warnings.isEmpty()) {

				System.out.println("(" + warnings.size() + ")" + warningType.getDescription());
				int n = warnings.size();
				for (int i = 0; i < n; i++) {
					String warning = warnings.get(i);
					System.out.println("\t" + warning);
				}
				System.out.println();
			}
		}

		List<String> generalWarnings = circularInfoContainer.getGeneralWarnings();
		if (!generalWarnings.isEmpty()) {
			System.out.println("(" + generalWarnings.size() + ")" + "General warnings");
		}
		for (String generalWarning : generalWarnings) {
			warningCount++;
			System.out.println("\t" + generalWarning);
		}

		if (warningCount == 0) {
			System.out.println("Test code is consistent with source code");
		}
	}

}
