package tools.meta.warnings;

public enum WarningType {

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
	
	public String getDescription() {
		return description;
	}
}
