package plugins.people.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * Container for values used to in the construction of an agent. Values are
 * retrievable by class type and are ordered.
 *
 */
@Immutable
public final class PersonConstructionData {

	private static class Data {
		private List<Object> values = new ArrayList<>();

		public Data() {
		}

		public Data(Data data) {
			values.addAll(data.values);
		}

	}

	private final Data data;

	private PersonConstructionData(Data data) {
		this.data = data;
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for {@link PersonConstructionData}
	 * 
	 *
	 */
	public static class Builder {
		private Data data = new Data();

		private Builder() {

		}

		/**
		 * Returns the {@link PersonConstructionData} formed from the inputs to
		 * this builder.
		 */
		public PersonConstructionData build() {
			return new PersonConstructionData(new Data(data));
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
			data.values.add(value);
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
		for (Object value : data.values) {
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
		for (Object value : data.values) {
			if (c.isAssignableFrom(value.getClass())) {
				result.add((T) value);
			}
		}
		return result;

	}

}
