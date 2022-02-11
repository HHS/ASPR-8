package meta;

//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.Constructor;
//import java.lang.reflect.Method;
//import java.lang.reflect.Modifier;
//import java.nio.file.FileVisitResult;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.SimpleFileVisitor;
//import java.nio.file.attribute.BasicFileAttributes;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.junit.jupiter.api.Test;
//
//import util.TimeElapser;
//import util.annotations.UnitTest;
//import util.annotations.UnitTestConstructor;
//import util.annotations.UnitTestMethod;

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
//	private static boolean isJavaFile(Path file) {
//		return Files.isRegularFile(file) && file.toString().endsWith(".java");
//	}
//
//	private static String getClassName(Path sourcePath, Path file) {
//		return file.toString().substring(sourcePath.toString().length() + 1, file.toString().length() - 5).replace(File.separator, ".");
//	}
//
//	/**
//	 * Assumes that the source path and file are consistent
//	 */
//	private static Class<?> getClassFromFile(Path sourcePath, Path file) {
//		try {
//			String className = getClassName(sourcePath, file);
//			return Class.forName(className);
//		} catch (ClassNotFoundException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	private enum WarningType {
//
//		SOURCE_METHOD_CANNOT_BE_RESOLVED("The source method for a test method cannot be resolved"),
//
//		SOURCE_CONSTRUCTOR_CANNOT_BE_RESOLVED("The source constructor for a test method cannot be resolved"),
//
//		PROXY_LEADS_OUTSIDE_SOURCE_FOLDER("Source class marked with proxy coverage leads to a class not in the source folder"),
//
//		PROXY_HAS_LOWER_TEST_STATUS("Source class marked with proxy coverage leads to a class that has a lower test status"),
//
//		CIRCULAR_PROXIES("Source class marked with proxy coverage leads to a circular proxy relationship"),
//
//		TEST_CLASS_LINKED_OUTSIDE_SOURCE("Test class linked to source class that is not in the source folder"),
//
//		TEST_METHOD_NOT_MAPPED_TO_PROPER_SOURCE_METHOD("Test method linked to unknown source method"),
//
//		TEST_METHOD_NOT_MAPPED_TO_PROPER_SOURCE_CONSTRUCTOR("Test method linked to unknown source contructor"),
//
//		TEST_METHOD_LINKED_TO_PROXIED_SOURCE("Test method linked to source method that is proxied to another source class"),
//
//		TEST_METHOD_TESTS_SOURCE_METHOD_THAT_DOES_NOT_REQUIRE_A_TEST("Test method tests source method that does not require a test"),
//
//		TEST_METHOD_TESTS_SOURCE_CONSTRUCTOR_THAT_DOES_NOT_REQUIRE_A_TEST("Test method tests source constructor that does not require a test"),
//
//		SOURCE_METHOD_REQUIRES_TEST("Source method requires a test method but does not have one"),
//
//		SOURCE_CONSTRUCTOR_REQUIRES_TEST("Source constructor requires a test method but does not have one"),
//
//		SUITE_CLASS_MISSING_TEST_CLASS("Test class not listed in the Suite Test"),
//
//		SUITE_CLASS_CONTAINS_NON_TEST_CLASS("Suite Test contains non test class"),
//
//		UNIT_TEST_ANNOTATION_LACKS_SOURCE_CLASS("Unit test annotation lacks source class reference"),
//
//		UNIT_CONSTRUCTOR_ANNOTATION_WITHOUT_TEST_ANNOTATION("Test method is marked with @UnitTestConstructor but does not have a corresponding @Test annotation"),
//
//		UNIT_METHOD_ANNOTATION_WITHOUT_TEST_ANNOTATION("Test method is marked with @UnitTestMethod but does not have a corresponding @Test annotation"),
//
//		UNIT_CONSTRUCTOR_AND_METHOD_ANNOTATIONS_PRESENT("Test method is marked with borth @UnitTestMethod and @UnitTestConstructor annotations"),
//
//		TEST_ANNOTATION_WITHOUT_UNIT_ANNOTATION("Test method is marked with @Test but does not have a corresponding @UnitTestMethod or @UnitTestConstructor"),
//
//		NONSTATIC_SUBCLASS("Non-static public subclasses are not testable");
//
//		private final String description;
//
//		private WarningType(String description) {
//			this.description = description;
//		}
//	}
//
//	private Map<WarningType, List<String>> warningMap = new LinkedHashMap<>();
//
//	private void addWarning(WarningType warningType, Object details) {
//		warningMap.get(warningType).add(details.toString());
//	}
//
//	private final static class SourceClassRec {
//
//		private final Class<?> sourceClass;
//		private final TestStatus testStatus;
//		private final Class<?> proxyClass;
//
//		public SourceClassRec(final Class<?> sourceClass, TestStatus testStatus, Class<?> proxyClass) {
//			this.sourceClass = sourceClass;
//			this.testStatus = testStatus;
//			this.proxyClass = proxyClass;
//		}
//
//		public Class<?> getProxyClass() {
//			return proxyClass;
//		}
//
//		public Class<?> getSourceClass() {
//			return sourceClass;
//		}
//
//		public TestStatus getTestStatus() {
//			return testStatus;
//		}
//	}
//
//	private Source getSource(Class<?> c) {
//		Class<?> target = c;
//		while (target != null) {
//			Source source = target.getAnnotation(Source.class);
//			if (source != null) {
//				return source;
//			}
//			target = target.getDeclaringClass();
//		}
//		return null;
//	}
//
//	private void probeClass(Class<?> c) {
//
//		TestStatus testStatus;
//		Class<?> proxyClass;
//		// final Source source = c.getAnnotation(Source.class);
//		final Source source = getSource(c);
//		if (source != null) {
//			testStatus = source.status();
//			if (source.proxy() != Object.class) {
//				proxyClass = source.proxy();
//			} else {
//				proxyClass = null;
//			}
//		} else {
//			testStatus = TestStatus.REQUIRED;
//			proxyClass = null;
//		}
//
//		final SourceClassRec sourceClassRec = new SourceClassRec(c, testStatus, proxyClass);
//		sourceClassRecs.put(sourceClassRec.getSourceClass(), sourceClassRec);
//
//		final Method[] methods = c.getMethods();
//		for (final Method method : methods) {
//
//			boolean addRec = method.getDeclaringClass().equals(c);
//			addRec &= !method.isBridge();
//			addRec &= !method.isSynthetic();
//			addRec &= !(Modifier.isAbstract(method.getModifiers()) && c.isInterface());
//
//			if (addRec) {
//				TestStatus methodTestStatus = testStatus;				
//				final SourceMethodRec sourceMethodRec = new SourceMethodRec(method, methodTestStatus, proxyClass != null);
//				sourceMethodRecs.put(sourceMethodRec.getMethod(), sourceMethodRec);
//			}
//		}
//
//		Constructor<?>[] constructors = c.getConstructors();
//		for (final Constructor<?> constructor : constructors) {
//			boolean addRec = constructor.getDeclaringClass().equals(c);
//			addRec &= !constructor.isSynthetic();
//			if (addRec) {
//				TestStatus constructorTestStatus = testStatus;
//				final SourceConstructorRec sourceConstructorRec = new SourceConstructorRec(constructor, constructorTestStatus, proxyClass != null);
//				sourceConstructorRecs.put(sourceConstructorRec.getConstructor(), sourceConstructorRec);
//			}
//		}
//
//	}
//
//	private Set<Class<?>> getClasses(Class<?> c) {
//		Set<Class<?>> result = new LinkedHashSet<>();
//		getClasses(c, result);
//		return result;
//	}
//
//	private void getClasses(Class<?> c, Set<Class<?>> set) {
//		if (c.isAnnotation()) {
//			return;
//		}
//		set.add(c);
//		Class<?>[] declaredClasses = c.getDeclaredClasses();
//		for (Class<?> subClass : declaredClasses) {
//			if (Modifier.isPublic(subClass.getModifiers())) {
//				if (Modifier.isStatic(subClass.getModifiers())) {
//					getClasses(subClass, set);
//				} else {
//					addWarning(WarningType.NONSTATIC_SUBCLASS, subClass);
//				}
//			}
//		}
//	}
//
//	private final class SourceFileVisitor extends SimpleFileVisitor<Path> {
//		@Override
//		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) {
//			if (isJavaFile(file)) {
//				final Class<?> c = getClassFromFile(sourcePath, file);
//				for (Class<?> c2 : getClasses(c)) {
//					probeClass(c2);
//				}
//			}
//			return FileVisitResult.CONTINUE;
//		}
//	}
//
//	private final static class SourceConstructorRec {
//
//		private final Constructor<?> constructor;
//
//		private final boolean isProxied;
//
//		private final TestStatus testStatus;
//
//		public SourceConstructorRec(final Constructor<?> constructor, TestStatus testStatus, boolean isProxied) {
//			this.constructor = constructor;
//			this.testStatus = testStatus;
//			this.isProxied = isProxied;
//		}
//
//		public Constructor<?> getConstructor() {
//			return constructor;
//		}
//
//		public TestStatus getTestStatus() {
//			return testStatus;
//		}
//
//		public boolean isProxied() {
//			return isProxied;
//		}
//
//	}
//
//	private final static class SourceMethodRec {
//
//		private final Method method;
//
//		private final boolean isProxied;
//
//		private final TestStatus testStatus;
//
//		public SourceMethodRec(final Method method, TestStatus testStatus, boolean isProxied) {
//			this.method = method;
//			this.testStatus = testStatus;
//			this.isProxied = isProxied;
//		}
//
//		public Method getMethod() {
//			return method;
//		}
//
//		public TestStatus getTestStatus() {
//			return testStatus;
//		}
//
//		public boolean isProxied() {
//			return isProxied;
//		}
//
//		@Override
//		public String toString() {
//			StringBuilder builder = new StringBuilder();
//			builder.append("SourceMethodRec [method=");
//			builder.append(method);
//			builder.append(", isProxied=");
//			builder.append(isProxied);
//			builder.append(", testStatus=");
//			builder.append(testStatus);
//			builder.append("]");
//			return builder.toString();
//		}
//
//	}
//
//	private final static class TestClassRec {
//
//		private final Class<?> testClass;
//
//		private final Class<?> sourceClass;
//
//		public TestClassRec(final Class<?> testClass) {
//			final UnitTest unitTest = testClass.getAnnotation(UnitTest.class);
//			this.testClass = testClass;
//			sourceClass = unitTest.target();
//		}
//
//		public Class<?> getSourceClass() {
//			return sourceClass;
//		}
//
//		public Class<?> getTestClass() {
//			return testClass;
//		}
//
//	}
//
//	private final class TestFileVisitor extends SimpleFileVisitor<Path> {
//		@Override
//		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) {
//			/*
//			 * For a file to be a test file, it must be 1) a java file and 2) be
//			 * annotated with a UnitTest annotation.
//			 * 
//			 * The UnitTest annotation must have a non-null source class
//			 * reference. The validity of the source class reference is examined
//			 * in the downstream process after all TestClassRecs have been
//			 * loaded.
//			 * 
//			 * 
//			 */
//
//			if (isJavaFile(file)) {
//				final Class<?> c = getClassFromFile(testPath, file);
//
//				final UnitTest unitTest = c.getAnnotation(UnitTest.class);
//				if (unitTest != null) {
//					if (unitTest.target() == null) {
//						addWarning(WarningType.UNIT_TEST_ANNOTATION_LACKS_SOURCE_CLASS, c.getCanonicalName());
//					} else {
//						final TestClassRec testClassRec = new TestClassRec(c);
//						testClassRecs.put(testClassRec.getTestClass(), testClassRec);
//						final Method[] methods = c.getMethods();
//						for (final Method testMethod : methods) {
//							final Test test = testMethod.getAnnotation(Test.class);
//							final UnitTestMethod unitTestMethod = testMethod.getAnnotation(UnitTestMethod.class);
//							final UnitTestConstructor unitTestConstructor = testMethod.getAnnotation(UnitTestConstructor.class);
//
//							if (test == null) {
//								if (unitTestMethod == null) {
//									if (unitTestConstructor == null) {
//										// case 0
//										// ignore the method, it is benign
//									} else {
//										// case 1
//										addWarning(WarningType.UNIT_CONSTRUCTOR_ANNOTATION_WITHOUT_TEST_ANNOTATION, testMethod);
//									}
//								} else {
//									if (unitTestConstructor == null) {
//										// case 2
//										addWarning(WarningType.UNIT_METHOD_ANNOTATION_WITHOUT_TEST_ANNOTATION, testMethod);
//									} else {
//										// case 3
//										addWarning(WarningType.UNIT_CONSTRUCTOR_AND_METHOD_ANNOTATIONS_PRESENT, testMethod);
//									}
//								}
//							} else {
//								if (unitTestMethod == null) {
//									if (unitTestConstructor == null) {
//										// case 4
//										addWarning(WarningType.TEST_ANNOTATION_WITHOUT_UNIT_ANNOTATION, testMethod);
//									} else {
//										// case 5
//										// add the unit constructor rec
//										Constructor<?> sourceConstructor;
//										try {
//											if (unitTestConstructor.target() != Object.class) {
//												sourceConstructor = unitTestConstructor.target().getConstructor(unitTestConstructor.args());
//											} else {
//												sourceConstructor = unitTest.target().getConstructor(unitTestConstructor.args());
//											}
//										} catch (NoSuchMethodException | SecurityException e) {
//											sourceConstructor = null;
//										}
//										if (sourceConstructor != null) {
//											final TestConstructorRec testConstructorRec = new TestConstructorRec(testMethod, sourceConstructor);
//											testConstructorRecs.put(testConstructorRec.getSourceConstructor(), testConstructorRec);
//										} else {
//											addWarning(WarningType.SOURCE_CONSTRUCTOR_CANNOT_BE_RESOLVED, testMethod);
//										}
//
//									}
//								} else {
//									if (unitTestConstructor == null) {
//										// case 6
//										// add the unit method rec
//
//										Method sourceMethod;
//										try {
//											if (unitTestMethod.target() != Object.class) {
//												sourceMethod = unitTestMethod.target().getMethod(unitTestMethod.name(), unitTestMethod.args());
//											} else {
//												sourceMethod = unitTest.target().getMethod(unitTestMethod.name(), unitTestMethod.args());
//											}
//										} catch (NoSuchMethodException | SecurityException e) {
//											sourceMethod = null;
//										}
//										if (sourceMethod != null) {
//											final TestMethodRec testMethodRec = new TestMethodRec(testMethod, sourceMethod);
//											testMethodRecs.put(testMethodRec.getSourceMethod(), testMethodRec);
//										} else {
//											addWarning(WarningType.SOURCE_METHOD_CANNOT_BE_RESOLVED, testMethod);
//										}
//									} else {
//										// case 7
//										addWarning(WarningType.UNIT_CONSTRUCTOR_AND_METHOD_ANNOTATIONS_PRESENT, testMethod);
//									}
//								}
//							}
//
//							// if ((test != null) && (unitTestMethod != null)) {
//							// Method sourceMethod;
//							// try {
//							// sourceMethod =
//							// unitTest.target().getMethod(unitTestMethod.name(),
//							// unitTestMethod.args());
//							// } catch (NoSuchMethodException |
//							// SecurityException e) {
//							// sourceMethod = null;
//							// }
//							// if (sourceMethod != null) {
//							// final TestMethodRec testMethodRec = new
//							// TestMethodRec(testMethod, sourceMethod);
//							// testMethodRecs.put(testMethodRec.getSourceMethod(),
//							// testMethodRec);
//							// } else {
//							// addWarning(WarningType.SOURCE_METHOD_CANNOT_BE_RESOLVED,
//							// testMethod);
//							// }
//							// }
//
//						}
//					}
//				}
//			}
//			return FileVisitResult.CONTINUE;
//		}
//	}
//
//	private final static class TestMethodRec {
//
//		private final Method testMethod;
//
//		private final Method sourceMethod;
//
//		public TestMethodRec(final Method testMethod, Method sourceMethod) {
//			this.testMethod = testMethod;
//			this.sourceMethod = sourceMethod;
//		}
//
//		public Method getSourceMethod() {
//			return sourceMethod;
//		}
//
//		public Method getTestMethod() {
//			return testMethod;
//		}
//	}
//
//	private final static class TestConstructorRec {
//
//		private final Method testMethod;
//
//		private final Constructor<?> sourceConstructor;
//
//		public TestConstructorRec(final Method testMethod, Constructor<?> sourceConstructor) {
//			this.testMethod = testMethod;
//			this.sourceConstructor = sourceConstructor;
//		}
//
//		public Constructor<?> getSourceConstructor() {
//			return sourceConstructor;
//		}
//
//		public Method getTestMethod() {
//			return testMethod;
//		}
//	}
//
//	public static void main(final String[] args) {
//
//		// Should point to src/main/java
//		final Path sourcePath = Paths.get(args[0]);
//
//		// Should point to src/test/java
//		final Path testPath = Paths.get(args[1]);
//
//		// Should use true or false
//		final boolean produceSourceInfo = Boolean.parseBoolean(args[2]);
//
//		final TestPlan testPlan = new TestPlan(sourcePath, testPath, produceSourceInfo);
//
//		testPlan.execute();
//	}
//
//	private final Path sourcePath;
//
//	private final Path testPath;
//
//	private final boolean produceSourceInfo;
//
//	private Map<Class<?>, SourceClassRec> sourceClassRecs = new LinkedHashMap<>();
//
//	private Map<Method, SourceMethodRec> sourceMethodRecs = new LinkedHashMap<>();
//
//	private Map<Constructor<?>, SourceConstructorRec> sourceConstructorRecs = new LinkedHashMap<>();
//
//	private Map<Class<?>, TestClassRec> testClassRecs = new LinkedHashMap<>();
//
//	private Map<Method, TestMethodRec> testMethodRecs = new LinkedHashMap<>();
//
//	private Map<Constructor<?>, TestConstructorRec> testConstructorRecs = new LinkedHashMap<>();
//
//	private TestPlan(final Path sourcePath, final Path testPath, final boolean produceSourceInfo) {
//		for (WarningType warningType : WarningType.values()) {
//			warningMap.put(warningType, new ArrayList<String>());
//		}
//		this.sourcePath = sourcePath;
//		this.testPath = testPath;
//		this.produceSourceInfo = produceSourceInfo;
//	}
//
//	private void reportWarnings() {
//		boolean noWarnings = true;
//		for (WarningType warningType : WarningType.values()) {
//			List<String> warnings = warningMap.get(warningType);
//			if (!warnings.isEmpty()) {
//				noWarnings = false;
//				System.out.println("(" + warnings.size() + ")" + warningType.description);
//				int n = warnings.size();
//				for (int i = 0; i < n; i++) {
//					String warning = warnings.get(i);
//					System.out.println("\t" + warning);
//				}
//				System.out.println();
//			}
//		}
//		if (noWarnings) {
//			System.out.println("Test code is consistent with source code");
//		}
//	}
//
//	private void validateTestClassRecs() {
//		// Show that every test class links to a source class
//		for (TestClassRec testClassRec : testClassRecs.values()) {
//			if (!sourceClassRecs.containsKey(testClassRec.getSourceClass())) {
//				addWarning(WarningType.TEST_CLASS_LINKED_OUTSIDE_SOURCE, testClassRec.getTestClass().getCanonicalName());
//			}
//		}
//
//	}
//
//	private void validateSourceClassRecs() {
//		// show that every proxied source class rec leads to through to other
//		// source class recs, terminating in a non-proxied source class rec with
//		// each succeeding parent record having a non-decreasing status
//
//		for (SourceClassRec sourceClassRec : sourceClassRecs.values()) {
//			TestStatus testStatus = sourceClassRec.getTestStatus();
//			SourceClassRec s = sourceClassRec;
//			Set<SourceClassRec> visitedSourceClassRecs = new LinkedHashSet<>();
//			while (true) {
//				if (s == null) {
//					addWarning(WarningType.PROXY_LEADS_OUTSIDE_SOURCE_FOLDER, sourceClassRec.getSourceClass().getCanonicalName());
//					break;
//				}
//				TestStatus nextTestStatus = s.getTestStatus();
//				if (nextTestStatus.compareTo(testStatus) > 0) {
//					addWarning(WarningType.PROXY_HAS_LOWER_TEST_STATUS, sourceClassRec.getSourceClass().getCanonicalName());
//					break;
//				}
//				testStatus = nextTestStatus;
//				if (!visitedSourceClassRecs.add(s)) {
//					addWarning(WarningType.CIRCULAR_PROXIES, sourceClassRec.getSourceClass().getCanonicalName());
//					break;
//				}
//				if (s.getProxyClass() == null) {
//					// we have terminated in a non-proxy class
//					break;
//				}
//				s = sourceClassRecs.get(s.getProxyClass());
//			}
//		}
//
//	}
//
//	private void validateTestMethodRecs() {
//		// show that each test method links to a source method that is required
//		// and non-proxied
//		for (TestMethodRec testMethodRec : testMethodRecs.values()) {
//			SourceMethodRec sourceMethodRec = sourceMethodRecs.get(testMethodRec.getSourceMethod());
//			if (sourceMethodRec == null) {
//				addWarning(WarningType.TEST_METHOD_NOT_MAPPED_TO_PROPER_SOURCE_METHOD, testMethodRec.getTestMethod());
//			} else {
//				if (sourceMethodRec.isProxied()) {
//					addWarning(WarningType.TEST_METHOD_LINKED_TO_PROXIED_SOURCE, testMethodRec.getTestMethod());
//				} else {
//					if (sourceMethodRec.getTestStatus() != TestStatus.REQUIRED) {
//						addWarning(WarningType.TEST_METHOD_TESTS_SOURCE_METHOD_THAT_DOES_NOT_REQUIRE_A_TEST, testMethodRec.getTestMethod());
//					}
//				}
//			}
//		}
//	}
//
//	private void validateTestConstructorRecs() {
//		// show that each test constructor rec links to a source constructor
//		// that is required
//		// and non-proxied
//		for (TestConstructorRec testConstructorRec : testConstructorRecs.values()) {
//			SourceConstructorRec sourceConstructorRec = sourceConstructorRecs.get(testConstructorRec.getSourceConstructor());
//			if (sourceConstructorRec == null) {
//				addWarning(WarningType.TEST_METHOD_NOT_MAPPED_TO_PROPER_SOURCE_CONSTRUCTOR, testConstructorRec.getTestMethod());
//			} else {
//				if (sourceConstructorRec.isProxied()) {
//					addWarning(WarningType.TEST_METHOD_LINKED_TO_PROXIED_SOURCE, testConstructorRec.getTestMethod());
//				} else {
//					if (sourceConstructorRec.getTestStatus() != TestStatus.REQUIRED) {
//						addWarning(WarningType.TEST_METHOD_TESTS_SOURCE_CONSTRUCTOR_THAT_DOES_NOT_REQUIRE_A_TEST, testConstructorRec.getTestMethod());
//					}
//				}
//			}
//		}
//	}
//
//	private void validateSourceMethodRecs() {
//		for (SourceMethodRec sourceMethodRec : sourceMethodRecs.values()) {
//			if (!sourceMethodRec.isProxied() && sourceMethodRec.testStatus == TestStatus.REQUIRED) {
//				TestMethodRec testMethodRec = testMethodRecs.get(sourceMethodRec.getMethod());
//				if (testMethodRec == null) {
//					addWarning(WarningType.SOURCE_METHOD_REQUIRES_TEST, sourceMethodRec.getMethod());
//				}
//			}
//		}
//	}
//
//	private void validateSourceConstructorRecs() {
//		for (SourceConstructorRec sourceConstructorRec : sourceConstructorRecs.values()) {
//			if (!sourceConstructorRec.isProxied() && sourceConstructorRec.testStatus == TestStatus.REQUIRED) {
//				TestConstructorRec testConstructorRec = testConstructorRecs.get(sourceConstructorRec.getConstructor());
//				if (testConstructorRec == null) {
//					addWarning(WarningType.SOURCE_CONSTRUCTOR_REQUIRES_TEST, sourceConstructorRec.getConstructor());
//				}
//			}
//		}
//	}
//
//	private void loadSourceClasses() {
//		final SourceFileVisitor sourceFileVisitor = new SourceFileVisitor();
//		try {
//			Files.walkFileTree(sourcePath, sourceFileVisitor);
//		} catch (final IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	private void loadTestClasses() {
//		final TestFileVisitor testFileVisitor = new TestFileVisitor();
//		try {
//			Files.walkFileTree(testPath, testFileVisitor);
//		} catch (final IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	private void reportSourceInfo() {
//		if (produceSourceInfo) {
//
//			System.out.println(new StringBuilder()//
//													.append("source class")//
//													.append("\t")//
//													.append("class status")//
//													.append("\t")//
//													.append("proxy class")//
//													.toString());//
//			for (SourceClassRec sourceClassRec : sourceClassRecs.values()) {
//				String proxyClassName = "";
//				if (sourceClassRec.proxyClass != null) {
//					proxyClassName = sourceClassRec.proxyClass.getName();
//				}
//
//				System.out.println(new StringBuilder()//
//														.append(sourceClassRec.sourceClass.getName())//
//														.append("\t")//
//														.append(sourceClassRec.testStatus)//
//														.append("\t")//
//														.append(proxyClassName)//
//														.toString());//
//			}
//			System.out.println();
//
//			System.out.println(new StringBuilder()//
//													.append("source class").append("\t")//
//													.append("source method")//
//													.append("\t")//
//													.append("method status")//
//													.append("\t")//
//													.append("is proxied")//
//													.toString());//
//
//			for (SourceMethodRec sourceMethodRec : sourceMethodRecs.values()) {
//				System.out.println(new StringBuilder()//
//														.append(sourceMethodRec.method.getDeclaringClass().getName()).append("\t")//
//														.append(sourceMethodRec.method.getName())//
//														.append("\t")//
//														.append(sourceMethodRec.testStatus)//
//														.append("\t")//
//														.append(sourceMethodRec.isProxied)//
//														.toString());//
//			}
//
//			System.out.println();
//			System.out.println(new StringBuilder()//
//													.append("source class").append("\t")//
//													.append("source constructor")//
//													.append("\t")//
//													.append("constructor status")//
//													.append("\t")//
//													.append("is proxied")//
//													.toString());//
//
//			for (SourceConstructorRec sourceConstructorRec : sourceConstructorRecs.values()) {
//				System.out.println(new StringBuilder()//
//														.append(sourceConstructorRec.constructor.getDeclaringClass().getName()).append("\t")//
//														.append(sourceConstructorRec.constructor.getName())//
//														.append("\t")//
//														.append(sourceConstructorRec.testStatus)//
//														.append("\t")//
//														.append(sourceConstructorRec.isProxied)//
//														.toString());//
//			}
//			System.out.println();
//		}
//	}
//
//	private void execute() {
//
//		TimeElapser timeElapser = new TimeElapser();
//
//		loadSourceClasses();
//
//		loadTestClasses();
//
//		validateSourceClassRecs();
//
//		validateTestClassRecs();
//
//		validateTestMethodRecs();
//
//		validateTestConstructorRecs();
//
//		validateSourceMethodRecs();
//
//		validateSourceConstructorRecs();
//
//		reportSourceInfo();
//
//		reportWarnings();
//
//		System.out.println("Test plan execution time = " + timeElapser.getElapsedMilliSeconds() + " milliseconds");
//
//	}

}
