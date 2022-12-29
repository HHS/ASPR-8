package tools.meta.warnings;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

/**
 * A utility class for generating various warnings on the coverage deficiencies
 * of the the unit test suite.
 *
 * @author Shawn Hatch
 *
 */
public class WarningGenerator {

	private static boolean isJavaFile(Path file) {
		return Files.isRegularFile(file) && file.toString().endsWith(".java");
	}

	private static String getClassName(Path sourcePath, Path file) {
		return file.toString().substring(sourcePath.toString().length() + 1, file.toString().length() - 5).replace(File.separator, ".");
	}

	/**
	 * Assumes that the source path and file are consistent
	 */
	private static Class<?> getClassFromFile(Path sourcePath, Path file) {
		try {
			String className = getClassName(sourcePath, file);
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private WarningContainer.Builder warningContainerBuilder = WarningContainer.builder();

	private void probeClass(Class<?> c) {

		final Method[] methods = c.getMethods();
		boolean isEnum = c.isEnum();
		boolean isRecord = c.isRecord();

		for (final Method method : methods) {

			boolean addRec = method.getDeclaringClass().equals(c);
			addRec &= !isRecord;
			addRec &= !method.isBridge();
			addRec &= !method.isSynthetic();			
			addRec &= !(Modifier.isAbstract(method.getModifiers())&&!isEnum);
			addRec &= !c.isInterface();
			
			if (isEnum) {
				if (method.getName().equals("values")) {
					if (method.getParameters().length == 0) {
						addRec = false;
					}
				} else if (method.getName().equals("valueOf")) {
					Parameter[] parameters = method.getParameters();
					if (parameters.length == 1) {
						Parameter parameter = parameters[0];
						if (parameter.getType() == String.class) {
							addRec = false;
						}
					}
				}
			}

			if (addRec) {
				sourceMethods.add(method);
			}
		}

		Constructor<?>[] constructors = c.getConstructors();
		for (final Constructor<?> constructor : constructors) {
			boolean addRec = constructor.getDeclaringClass().equals(c);
			addRec &= !constructor.isSynthetic();
			if (addRec) {
				sourceConstructors.add(constructor);
			}
		}

	}

	private Set<Method> sourceMethods = new LinkedHashSet<>();
	private Set<Method> coveredSourceMethods = new LinkedHashSet<>();
	private Set<Constructor<?>> sourceConstructors = new LinkedHashSet<>();
	private Set<Constructor<?>> coveredSourceConstructors = new LinkedHashSet<>();

	private Set<Class<?>> getClasses(Class<?> c) {
		Set<Class<?>> result = new LinkedHashSet<>();
		getClasses(c, result);
		return result;
	}

	private void getClasses(Class<?> c, Set<Class<?>> set) {
		if (c.isAnnotation()) {
			return;
		}
		set.add(c);
		Class<?>[] declaredClasses = c.getDeclaredClasses();
		for (Class<?> subClass : declaredClasses) {
			if (Modifier.isPublic(subClass.getModifiers())) {
				if (Modifier.isStatic(subClass.getModifiers())) {
					getClasses(subClass, set);
				} else {
					warningContainerBuilder.addGeneralWarning(WarningType.NONSTATIC_SUBCLASS.getDescription() + " " + subClass);
				}
			}
		}
	}

	private final class SourceFileVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) {
			if (isJavaFile(file)) {
				final Class<?> c = getClassFromFile(data.sourcePath, file);
				for (Class<?> c2 : getClasses(c)) {
					probeClass(c2);
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private void probeConstructorTest(Method testMethod, UnitTest unitTest, UnitTestConstructor unitTestConstructor) {
		Constructor<?> sourceConstructor = null;
		try {
			if (unitTestConstructor.target() != Object.class) {
				sourceConstructor = unitTestConstructor.target().getConstructor(unitTestConstructor.args());
			} else {
				if (unitTest != null) {
					sourceConstructor = unitTest.target().getConstructor(unitTestConstructor.args());
				}
			}
		} catch (NoSuchMethodException | SecurityException e) {
			sourceConstructor = null;
		}
		if (sourceConstructor != null) {
			if (!sourceConstructors.contains(sourceConstructor)) {
				warningContainerBuilder.addMethodWarning(new MethodWarning(testMethod, WarningType.TEST_METHOD_NOT_MAPPED_TO_PROPER_SOURCE_CONSTRUCTOR));
			}
			coveredSourceConstructors.add(sourceConstructor);

		} else {
			warningContainerBuilder.addMethodWarning(new MethodWarning(testMethod, WarningType.SOURCE_CONSTRUCTOR_CANNOT_BE_RESOLVED));
		}
	}

	private void probeMethodTest(Method testMethod, UnitTest unitTest, UnitTestMethod unitTestMethod) {
		// if(testMethod.toString().equals("public void
		// plugins.groups.testsupport.AT_GroupsActionSupport.testTestConsumer()"))
		// {
		// System.out.println("arrived");
		// }
		Method sourceMethod = null;
		String methodExceptionMessage = "";
		try {
			if (unitTestMethod.target() != Object.class) {
				sourceMethod = unitTestMethod.target().getMethod(unitTestMethod.name(), unitTestMethod.args());
			} else {
				if (unitTest != null) {
					sourceMethod = unitTest.target().getMethod(unitTestMethod.name(), unitTestMethod.args());
				}
			}
		} catch (NoSuchMethodException | SecurityException e) {
			methodExceptionMessage = e.getMessage();
			sourceMethod = null;
		}
		if (sourceMethod != null) {
			if (!sourceMethods.contains(sourceMethod)) {
				warningContainerBuilder.addMethodWarning(new MethodWarning(testMethod, WarningType.TEST_METHOD_NOT_MAPPED_TO_PROPER_SOURCE_METHOD, sourceMethod.toString()));
			} else {
				coveredSourceMethods.add(sourceMethod);
			}
		} else {
			warningContainerBuilder.addMethodWarning(new MethodWarning(testMethod, WarningType.SOURCE_METHOD_CANNOT_BE_RESOLVED, methodExceptionMessage));
		}
	}

	private void probeTestClass(Class<?> c) {
		final UnitTest unitTest = c.getAnnotation(UnitTest.class);
		final Method[] methods = c.getMethods();
		for (final Method testMethod : methods) {
			final Test test = testMethod.getAnnotation(Test.class);
			final UnitTestMethod unitTestMethod = testMethod.getAnnotation(UnitTestMethod.class);
			final UnitTestConstructor unitTestConstructor = testMethod.getAnnotation(UnitTestConstructor.class);
			int caseIndex = 0;
			if (test != null) {
				caseIndex += 4;
			}
			if (unitTestMethod != null) {
				caseIndex += 2;
			}
			if (unitTestConstructor != null) {
				caseIndex++;
			}

			switch (caseIndex) {
			case 0:
				// ignore the method
				break;
			case 1:
				warningContainerBuilder.addMethodWarning(new MethodWarning(testMethod, WarningType.UNIT_CONSTRUCTOR_ANNOTATION_WITHOUT_TEST_ANNOTATION));
				break;
			case 2:
				warningContainerBuilder.addMethodWarning(new MethodWarning(testMethod, WarningType.UNIT_METHOD_ANNOTATION_WITHOUT_TEST_ANNOTATION));
				break;
			case 3:
				warningContainerBuilder.addMethodWarning(new MethodWarning(testMethod, WarningType.UNIT_CONSTRUCTOR_AND_METHOD_ANNOTATIONS_PRESENT));
				break;
			case 4:
				warningContainerBuilder.addMethodWarning(new MethodWarning(testMethod, WarningType.TEST_ANNOTATION_WITHOUT_UNIT_ANNOTATION));
				break;
			case 5:
				probeConstructorTest(testMethod, unitTest, unitTestConstructor);
				break;
			case 6:
				probeMethodTest(testMethod, unitTest, unitTestMethod);
				break;
			case 7:
				warningContainerBuilder.addMethodWarning(new MethodWarning(testMethod, WarningType.UNIT_CONSTRUCTOR_AND_METHOD_ANNOTATIONS_PRESENT));
				break;
			default:
				throw new RuntimeException("unhandled case index " + caseIndex);
			}
		}
	}

	private final class TestFileVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) {
			if (isJavaFile(file)) {
				final Class<?> c = getClassFromFile(data.testPath, file);
				probeTestClass(c);
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private final Data data;

	private static class Data {
		private Path sourcePath;

		private Path testPath;
	}

	public final static Builder builder() {
		return new Builder();
	}

	public final static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		public WarningGenerator build() {
			try {
				validate();
				return new WarningGenerator(data);
			} finally {
				data = new Data();
			}
		}

		private void validate() {

		}

		public Builder setSourcePath(Path sourcePath) {
			data.sourcePath = sourcePath;
			return this;
		}

		public Builder setTestPath(Path testPath) {
			data.testPath = testPath;
			return this;
		}

	}

	private WarningGenerator(Data data) {
		this.data = data;
	}

	// private void reportWarnings() {
	//
	// int warningCount = 0;
	// for (WarningType warningType : WarningType.values()) {
	// warningCount += warningMap.get(warningType).size();
	// }
	// System.out.println("(" + warningCount + ")");
	// for (WarningType warningType : WarningType.values()) {
	// List<String> warnings = warningMap.get(warningType);
	// if (!warnings.isEmpty()) {
	//
	// System.out.println("(" + warnings.size() + ")" +
	// warningType.getDescription());
	// int n = warnings.size();
	// for (int i = 0; i < n; i++) {
	// String warning = warnings.get(i);
	// System.out.println("\t" + warning);
	// }
	// System.out.println();
	// }
	// }
	// if (warningCount == 0) {
	// System.out.println("Test code is consistent with source code");
	// }
	// }

	private void checkSourceMethodCoverage() {
		for (Method method : sourceMethods) {
			if (!coveredSourceMethods.contains(method)) {
				warningContainerBuilder.addMethodWarning(new MethodWarning(method, WarningType.SOURCE_METHOD_REQUIRES_TEST));
			}
		}
	}

	private void checkSourceConstructorCoverage() {
		for (Constructor<?> constructor : sourceConstructors) {
			if (!coveredSourceConstructors.contains(constructor)) {
				this.warningContainerBuilder.addConstructorWarning(new ConstructorWarning(constructor, WarningType.SOURCE_CONSTRUCTOR_REQUIRES_TEST));
			}
		}
	}

	private void loadSourceClasses() {

		final SourceFileVisitor sourceFileVisitor = new SourceFileVisitor();
		try {
			Files.walkFileTree(data.sourcePath, sourceFileVisitor);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

	}

	private void loadTestClasses() {
		final TestFileVisitor testFileVisitor = new TestFileVisitor();
		try {
			Files.walkFileTree(data.testPath, testFileVisitor);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public WarningContainer execute() {

		loadSourceClasses();

		loadTestClasses();

		checkSourceMethodCoverage();

		checkSourceConstructorCoverage();

		return warningContainerBuilder.build();

	}

}
