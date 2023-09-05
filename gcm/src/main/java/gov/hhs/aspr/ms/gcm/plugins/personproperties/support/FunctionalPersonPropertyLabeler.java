package gov.hhs.aspr.ms.gcm.plugins.personproperties.support;

import java.util.function.Function;

/**
 * 
 * A function-based implementor of PersonPropertyLabeler.
 * 
 */
public class FunctionalPersonPropertyLabeler extends PersonPropertyLabeler {
	private final Function<Object, Object> labelingFunction;

	public FunctionalPersonPropertyLabeler(PersonPropertyId personPropertyId,
			Function<Object, Object> labelingFunction) {
		super(personPropertyId);
		this.labelingFunction = labelingFunction;
	}

	@Override
	protected Object getLabelFromValue(Object value) {
		return labelingFunction.apply(value);
	}
}