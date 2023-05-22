package plugins.partitions.testsupport;

import java.util.function.Function;

import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.AttributeLabeler;

/**
 * A Function-based implementor of AttributeLabeler. Not suitable for
 * serialization.
 */
public final class FunctionalAttributeLabeler extends AttributeLabeler {
	private final Function<Object, Object> labelingFunction;

	public FunctionalAttributeLabeler(AttributeId attributeId, Function<Object, Object> labelingFunction) {
		super(attributeId);
		this.labelingFunction = labelingFunction;
	}

	@Override
	protected Object getLabelFromValue(Object value) {
		return labelingFunction.apply(value);
	}

}
