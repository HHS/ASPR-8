package tools.meta.warnings;

import java.lang.reflect.Constructor;

public final class ConstructorWarning {
	private final Constructor<?> constructor;
	private final WarningType warningType;
	private final String details;

	/**
	 * Constructs the constructor warning. Null details are replaced with an
	 * empty details string.
	 * 
	 * @throws NullPointerException
	 *             <li>if the constructor is null</li>
	 *             <li>if the warning type is null</li>
	 */
	public ConstructorWarning(Constructor<?> constructor, WarningType warningType, String details) {
		if (constructor == null) {
			throw new NullPointerException("constructor is null");
		}
		if (warningType == null) {
			throw new NullPointerException("warning type is null");
		}
		this.constructor = constructor;
		this.warningType = warningType;
		if (details == null) {
			this.details = "";
		} else {
			this.details = details;
		}
	}

	/**
	 * Constructs the constructor warning with a default empty string for
	 * details.
	 * 
	 * @throws NullPointerException
	 *             <li>if the constructor is null</li>
	 *             <li>if the warning type is null</li>
	 */
	public ConstructorWarning(Constructor<?> constructor, WarningType warningType) {
		if (constructor == null) {
			throw new NullPointerException("constructor is null");
		}
		if (warningType == null) {
			throw new NullPointerException("warning type is null");
		}
		this.constructor = constructor;
		this.warningType = warningType;
		this.details = "";
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	public WarningType getWarningType() {
		return warningType;
	}

	public String getDetails() {
		return details;
	}

}
