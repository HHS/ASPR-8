package util.meta.unittestcoverage.reports;

import java.nio.file.Path;
import java.nio.file.Paths;

import util.meta.unittestcoverage.MetaInfoContainer;
import util.meta.unittestcoverage.MetaInfoGenerator;
import util.meta.unittestcoverage.warnings.ConstructorWarning;
import util.meta.unittestcoverage.warnings.FieldWarning;
import util.meta.unittestcoverage.warnings.MethodWarning;

public final class StatusReport {

	public static void run(final String[] args) {

		// Should point to src/main/java
		final Path sourcePath = Paths.get(args[0]);

		// Should point to src/test/java
		final Path testPath = Paths.get(args[1]);

		MetaInfoContainer metaInfoContainer = MetaInfoGenerator.builder().setSourcePath(sourcePath).setTestPath(testPath).build().execute();
		
		displayWarningContainer(metaInfoContainer);

	}
	
	private static void displayWarningContainer(MetaInfoContainer metaInfoContainer) {
		for(FieldWarning fieldWarning : metaInfoContainer.getFieldWarnings()) {
			System.out.println(fieldWarning.getWarningType().getDescription()+"\t"+fieldWarning.getField()+"\t"+fieldWarning.getDetails());
		}
		for(MethodWarning methodWarning : metaInfoContainer.getMethodWarnings()) {
			System.out.println(methodWarning.getWarningType().getDescription()+"\t"+methodWarning.getMethod()+"\t"+methodWarning.getDetails());
		}
		for(ConstructorWarning constructorWarning : metaInfoContainer.getConstructorWarnings()) {
			System.out.println(constructorWarning.getWarningType().getDescription()+"\t"+constructorWarning.getConstructor()+"\t"+constructorWarning.getDetails());
		}

	}
}
