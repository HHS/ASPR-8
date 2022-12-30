package tools.metaunit.reports;

import java.nio.file.Path;
import java.nio.file.Paths;

import tools.metaunit.warnings.ConstructorWarning;
import tools.metaunit.warnings.FieldWarning;
import tools.metaunit.warnings.MethodWarning;
import tools.metaunit.warnings.WarningContainer;
import tools.metaunit.warnings.WarningGenerator;

public final class StatusReport {
	private StatusReport() {
		
	}
	public static void main(final String[] args) {

		// Should point to src/main/java
		final Path sourcePath = Paths.get(args[0]);

		// Should point to src/test/java
		final Path testPath = Paths.get(args[1]);

		WarningContainer warningContainer = WarningGenerator.builder().setSourcePath(sourcePath).setTestPath(testPath).build().execute();
		
		displayWarningContainer(warningContainer);

	}
	
	private static void displayWarningContainer(WarningContainer warningContainer) {
		for(FieldWarning fieldWarning : warningContainer.getFieldWarnings()) {
			System.out.println(fieldWarning.getWarningType().getDescription()+"\t"+fieldWarning.getField()+"\t"+fieldWarning.getDetails());
		}
		for(MethodWarning methodWarning : warningContainer.getMethodWarnings()) {
			System.out.println(methodWarning.getWarningType().getDescription()+"\t"+methodWarning.getMethod()+"\t"+methodWarning.getDetails());
		}
		for(ConstructorWarning constructorWarning : warningContainer.getConstructorWarnings()) {
			System.out.println(constructorWarning.getWarningType().getDescription()+"\t"+constructorWarning.getConstructor()+"\t"+constructorWarning.getDetails());
		}

	}
}
