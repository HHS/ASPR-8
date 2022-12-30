package tools.meta;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
public class WarningTypeReport {

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

		reportWarnings(warningContainer);

	}

	private WarningTypeReport() {
	}

	private static void reportWarnings(WarningContainer warningContainer) {

		Map<WarningType, List<String>> warningMap = new LinkedHashMap<>();
		for (WarningType warningType : WarningType.values()) {
			warningMap.put(warningType, new ArrayList<>());
		}

		for (MethodWarning methodWarning : warningContainer.getMethodWarnings()) {
			List<String> list = warningMap.get(methodWarning.getWarningType());
			list.add(methodWarning.getMethod().getDeclaringClass().getSimpleName()+"\t"+methodWarning.getMethod().toString() + " " + methodWarning.getDetails());
		}		

		for (ConstructorWarning constructorWarning : warningContainer.getConstructorWarnings()) {
			List<String> list = warningMap.get(constructorWarning.getWarningType());
			list.add(constructorWarning.getConstructor().getDeclaringClass().getSimpleName()+"\t"+constructorWarning.getConstructor().toString() + " " + constructorWarning.getDetails());
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
		
		
		List<String> generalWarnings = warningContainer.getGeneralWarnings();
		if(!generalWarnings.isEmpty()) {
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
