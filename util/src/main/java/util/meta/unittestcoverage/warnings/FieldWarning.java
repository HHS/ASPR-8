package util.meta.unittestcoverage.warnings;

import java.lang.reflect.Field;

public final class FieldWarning {
	private final Field field;
	private final WarningType warningType;
	private final String details;

	/**
	 * Constructs the field warning. Null details are replaced with an empty
	 * details string.
	 * 
	 * @throws NullPointerException
	 *             <li>if the field is null</li>
	 *             <li>if the warning type is null</li>
	 */
	public FieldWarning(Field field, WarningType warningType, String details) {
		if (field == null) {
			throw new NullPointerException("field is null");
		}
		if (warningType == null) {
			throw new NullPointerException("warning type is null");
		}
		this.field = field;
		this.warningType = warningType;
		if (details == null) {
			this.details = "";
		} else {
			this.details = details;
		}
	}
	/**
	 * Constructs the field warning with a default empty string for
	 * details.
	 * 
	 * @throws NullPointerException
	 *             <li>if the field is null</li>
	 *             <li>if the warning type is null</li>
	 */
	public FieldWarning(Field field, WarningType warningType) {
		if (field == null) {
			throw new NullPointerException("field is null");
		}
		if (warningType == null) {
			throw new NullPointerException("warning type is null");
		}
		this.field = field;
		this.warningType = warningType;
		this.details = "";
	}

	public Field getField() {
		return field;
	}

	public WarningType getWarningType() {
		return warningType;
	}

	public String getDetails() {
		return details;
	}

}
