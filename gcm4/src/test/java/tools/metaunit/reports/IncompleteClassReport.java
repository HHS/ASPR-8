package tools.metaunit.reports;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import tools.metaunit.warnings.ConstructorWarning;
import tools.metaunit.warnings.FieldWarning;
import tools.metaunit.warnings.MethodWarning;
import tools.metaunit.warnings.WarningContainer;
import tools.metaunit.warnings.WarningGenerator;
import tools.metaunit.warnings.WarningType;

public final class IncompleteClassReport {
	private IncompleteClassReport() {
		
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
		Set<Class<?>> incompleteClasses = new LinkedHashSet<>();
		for(FieldWarning fieldWarning : warningContainer.getFieldWarnings()) {
			if(fieldWarning.getWarningType().equals(WarningType.SOURCE_FIELD_REQUIRES_TEST)) {
				incompleteClasses.add(fieldWarning.getField().getDeclaringClass());
			}			
		}
		for(MethodWarning methodWarning : warningContainer.getMethodWarnings()) {
			if(methodWarning.getWarningType().equals(WarningType.SOURCE_METHOD_REQUIRES_TEST)) {
				incompleteClasses.add(methodWarning.getMethod().getDeclaringClass());
			}			
		}
		for(ConstructorWarning constructorWarning : warningContainer.getConstructorWarnings()) {
			if(constructorWarning.getWarningType().equals(WarningType.SOURCE_CONSTRUCTOR_REQUIRES_TEST)) {
				incompleteClasses.add(constructorWarning.getConstructor().getDeclaringClass());
			}
		}
		
		System.out.println("Incomplete classes");
		for(Class<?> c : incompleteClasses) {
			System.out.println(c.getName());
		}

	}
}
