package tools.meta;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

/**
 * A script covering the details of the GCM Test Plan. It produces a console
 * report that measures the completeness/status of the test classes. It does not
 * measure the correctness of any test, but rather shows which tests exist and
 * their status.
 *
 * @author Shawn Hatch
 *
 */
public class TestPlan {
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

	private enum WarningType {

		SOURCE_METHOD_CANNOT_BE_RESOLVED("The source method for a test method cannot be resolved"),

		SOURCE_CONSTRUCTOR_CANNOT_BE_RESOLVED("The source constructor for a Test method cannot be resolved"),

		TEST_METHOD_NOT_MAPPED_TO_PROPER_SOURCE_METHOD("Test method linked to unknown source method"),

		TEST_METHOD_NOT_MAPPED_TO_PROPER_SOURCE_CONSTRUCTOR("Test method linked to unknown source contructor"),

		SOURCE_METHOD_REQUIRES_TEST("Source method requires a test method but does not have one"),

		SOURCE_CONSTRUCTOR_REQUIRES_TEST("Source constructor requires a test method but does not have one"),

		UNIT_CONSTRUCTOR_ANNOTATION_WITHOUT_TEST_ANNOTATION("Test method is marked with @UnitTestConstructor but does not have a corresponding @Test annotation"),

		UNIT_METHOD_ANNOTATION_WITHOUT_TEST_ANNOTATION("Test method is marked with @UnitTestMethod but does not have a corresponding @Test annotation"),

		UNIT_CONSTRUCTOR_AND_METHOD_ANNOTATIONS_PRESENT("Test method is marked with borth @UnitTestMethod and @UnitTestConstructor annotations"),

		TEST_ANNOTATION_WITHOUT_UNIT_ANNOTATION("Test method is marked with @Test but does not have a corresponding @UnitTestMethod or @UnitTestConstructor"),

		NONSTATIC_SUBCLASS("Non-static public subclasses are not testable"),

		;

		private final String description;

		private WarningType(String description) {
			this.description = description;
		}
	}

	private Map<WarningType, List<String>> warningMap = new LinkedHashMap<>();

	private void addWarning(WarningType warningType, Object details) {
		warningMap.get(warningType).add(details.toString());
	}

	private void probeClass(Class<?> c) {


		final Method[] methods = c.getMethods();
		for (final Method method : methods) {

			boolean addRec = method.getDeclaringClass().equals(c);
			addRec &= !method.isBridge();
			addRec &= !method.isSynthetic();
			addRec &= !(Modifier.isAbstract(method.getModifiers()) && c.isInterface());

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
					addWarning(WarningType.NONSTATIC_SUBCLASS, subClass);
				}
			}
		}
	}

	private final class SourceFileVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) {
			if (isJavaFile(file)) {
				final Class<?> c = getClassFromFile(sourcePath, file);
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
				addWarning(WarningType.TEST_METHOD_NOT_MAPPED_TO_PROPER_SOURCE_CONSTRUCTOR, testMethod);
			}
			coveredSourceConstructors.add(sourceConstructor);

		} else {
			addWarning(WarningType.SOURCE_CONSTRUCTOR_CANNOT_BE_RESOLVED, testMethod);
		}
	}

	private void probeMethodTest(Method testMethod, UnitTest unitTest, UnitTestMethod unitTestMethod) {
//		if(testMethod.toString().equals("public void plugins.groups.testsupport.AT_GroupsActionSupport.testTestConsumer()")) {
//			System.out.println("arrived");
//		}
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
				addWarning(WarningType.TEST_METHOD_NOT_MAPPED_TO_PROPER_SOURCE_METHOD, testMethod);
			} else {
				coveredSourceMethods.add(sourceMethod);
			}
		} else {			
			addWarning(WarningType.SOURCE_METHOD_CANNOT_BE_RESOLVED, testMethod.toString()+" "+methodExceptionMessage);
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
				addWarning(WarningType.UNIT_CONSTRUCTOR_ANNOTATION_WITHOUT_TEST_ANNOTATION, testMethod);
				break;
			case 2:
				addWarning(WarningType.UNIT_METHOD_ANNOTATION_WITHOUT_TEST_ANNOTATION, testMethod);
				break;
			case 3:
				addWarning(WarningType.UNIT_CONSTRUCTOR_AND_METHOD_ANNOTATIONS_PRESENT, testMethod);
				break;
			case 4:
				addWarning(WarningType.TEST_ANNOTATION_WITHOUT_UNIT_ANNOTATION, testMethod);
				break;
			case 5:
				probeConstructorTest(testMethod, unitTest, unitTestConstructor);
				break;
			case 6:
				probeMethodTest(testMethod, unitTest, unitTestMethod);
				break;
			case 7:
				addWarning(WarningType.UNIT_CONSTRUCTOR_AND_METHOD_ANNOTATIONS_PRESENT, testMethod);
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
				final Class<?> c = getClassFromFile(testPath, file);
				probeTestClass(c);
			}
			return FileVisitResult.CONTINUE;
		}
	}

	public static void main(final String[] args) {

		// Should point to src/main/java
		final Path sourcePath = Paths.get(args[0]);

		// Should point to src/test/java
		final Path testPath = Paths.get(args[1]);

		final TestPlan testPlan = new TestPlan(sourcePath, testPath);

		testPlan.execute();
	}

	private final Path sourcePath;

	private final Path testPath;


	private TestPlan(final Path sourcePath, final Path testPath) {
		for (WarningType warningType : WarningType.values()) {
			warningMap.put(warningType, new ArrayList<String>());
		}
		this.sourcePath = sourcePath;
		this.testPath = testPath;
	}

	private void reportWarnings() {
		
		int warningCount = 0;
		for (WarningType warningType : WarningType.values()) {
			warningCount += warningMap.get(warningType).size();
		}
		System.out.println("(" + warningCount + ")");
		for (WarningType warningType : WarningType.values()) {
			List<String> warnings = warningMap.get(warningType);
			if (!warnings.isEmpty()) {
				
				System.out.println("(" + warnings.size() + ")" + warningType.description);
				int n = warnings.size();
				for (int i = 0; i < n; i++) {
					String warning = warnings.get(i);
					System.out.println("\t" + warning);
				}
				System.out.println();
			}
		}
		if (warningCount==0) {
			System.out.println("Test code is consistent with source code");
		}
	}

	private void checkSourceMethodCoverage() {
		for (Method method : sourceMethods) {
			if (!coveredSourceMethods.contains(method)) {
				addWarning(WarningType.SOURCE_METHOD_REQUIRES_TEST, method);
			}
		}
	}

	private void checkSourceConstructorCoverage() {
		for (Constructor<?> constructor : sourceConstructors) {
			if (!coveredSourceConstructors.contains(constructor)) {
				addWarning(WarningType.SOURCE_CONSTRUCTOR_REQUIRES_TEST, constructor);
			}
		}
	}

	private void loadSourceClasses() {
		final SourceFileVisitor sourceFileVisitor = new SourceFileVisitor();
		try {
			Files.walkFileTree(sourcePath, sourceFileVisitor);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadTestClasses() {
		final TestFileVisitor testFileVisitor = new TestFileVisitor();
		try {
			Files.walkFileTree(testPath, testFileVisitor);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void execute() {

		loadSourceClasses();

		loadTestClasses();

		checkSourceMethodCoverage();

		checkSourceConstructorCoverage();

		reportWarnings();

	}

}
