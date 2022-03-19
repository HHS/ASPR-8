package plugins.people.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.jcip.annotations.Immutable;
import nucleus.util.ContractException;

/**
 * Container for values used to in the construction of an agent. Values are
 * retrievable by class type and are ordered.
 *
 */
@Immutable
public final class PersonConstructionData {

	private final List<Object> values;

	private PersonConstructionData(List<Object> auxiliaryData) {
		this.values = auxiliaryData;
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for {@link PersonConstructionData}
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {
		private List<Object> values = new ArrayList<>();

		private Builder() {

		}

		/**
		 * Returns the {@link PersonConstructionData} formed from the inputs to
		 * this builder.
		 */
		public PersonConstructionData build() {
			try {
				return new PersonConstructionData(values);
			} finally {
				values = new ArrayList<>();
			}
		}

		/**
		 * Adds a value to the builder.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_AUXILIARY_DATA} if the
		 *             value is null</li>
		 */
		public Builder add(Object value) {
			if (value == null) {
				throw new ContractException(PersonError.NULL_AUXILIARY_DATA);
			}
			values.add(value);
			return this;
		}

	}

	/**
	 * Returns the first auxiliary object that is an instance of the given
	 * class. Should be used only getValues() is expected to have at most one
	 * member.
	 */
	@SuppressWarnings("unchecked")
	public <T> Optional<T> getValue(Class<T> c) {
		T result = null;
		for (Object value : values) {
			if (c.isAssignableFrom(value.getClass())) {
				result = (T) value;
				break;
			}
		}
		return Optional.ofNullable(result);

	}

	/**
	 * Returns the auxiliary objects that are instances of the given class in
	 * the order of their addition to the builder.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getValues(Class<T> c) {
		List<T> result = new ArrayList<>();
		for (Object value : values) {
			if (c.isAssignableFrom(value.getClass())) {
				result.add((T) value);
			}
		}
		return result;

	}

}
