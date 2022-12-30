package tools.meta;

import java.nio.file.Path;
import java.nio.file.Paths;

import tools.meta.warnings.ConstructorWarning;
import tools.meta.warnings.MethodWarning;
import tools.meta.warnings.WarningContainer;
import tools.meta.warnings.WarningGenerator;

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
		for(MethodWarning methodWarning : warningContainer.getMethodWarnings()) {
			System.out.println(methodWarning.getWarningType().getDescription()+"\t"+methodWarning.getMethod()+"\t"+methodWarning.getDetails());
		}
		for(ConstructorWarning constructorWarning : warningContainer.getConstructorWarnings()) {
			System.out.println(constructorWarning.getWarningType().getDescription()+"\t"+constructorWarning.getConstructor()+"\t"+constructorWarning.getDetails());
		}

	}
}
