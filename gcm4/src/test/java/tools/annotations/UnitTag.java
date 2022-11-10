package tools.annotations;

public enum UnitTag {
	/*
	 *  The test should be run as a manual test
	 */
	MANUAL,

	/*
	 *  The test is covered by another test in the same unit test
	 */
	LOCAL_PROXY,

	/*
	 * The test is covered by other test(s) for other classes. This is used for
	 * classes that are not expected to be directly exposed to a client of GCM
	 * and where the class is used exclusively by GCM.
	 */
	CLASS_PROXY,

	/*
	 *  The test is not yet implemented
	 */
	EMPTY//
}
