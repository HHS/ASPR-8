package util.meta.unittestcoverage.warnings;

import java.lang.reflect.Method;

public final class MethodWarning {
	private final Method method;
	private final WarningType warningType;
	private final String details;

	/**
	 * Constructs the method warning. Null details are replaced with an empty
	 * details string.
	 * 
	 * @throws NullPointerException
	 *             <li>if the method is null</li>
	 *             <li>if the warning type is null</li>
	 */
	public MethodWarning(Method method, WarningType warningType, String details) {
		if (method == null) {
			throw new NullPointerException("method is null");
		}
		if (warningType == null) {
			throw new NullPointerException("warning type is null");
		}
		this.method = method;
		this.warningType = warningType;
		if (details == null) {
			this.details = "";
		} else {
			this.details = details;
		}
	}
	/**
	 * Constructs the method warning with a default empty string for
	 * details.
	 * 
	 * @throws NullPointerException
	 *             <li>if the method is null</li>
	 *             <li>if the warning type is null</li>
	 */
	public MethodWarning(Method method, WarningType warningType) {
		if (method == null) {
			throw new NullPointerException("method is null");
		}
		if (warningType == null) {
			throw new NullPointerException("warning type is null");
		}
		this.method = method;
		this.warningType = warningType;
		this.details = "";
	}

	public Method getMethod() {
		return method;
	}

	public WarningType getWarningType() {
		return warningType;
	}

	public String getDetails() {
		return details;
	}

}
