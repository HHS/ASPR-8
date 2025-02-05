package gov.hhs.aspr.ms.gcm.simulation.plugins.people.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * Container for values used to in the construction of an agent. Values are
 * retrievable by class type and are ordered.
 */
@Immutable
public final class PersonConstructionData {

	private static class Data {
		private List<Object> values = new ArrayList<>();
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			values.addAll(data.values);
			locked = data.locked;
		}

	}

	private final Data data;

	private PersonConstructionData(Data data) {
		this.data = data;
	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Builder class for {@link PersonConstructionData}
	 */
	public static class Builder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the {@link PersonConstructionData} formed from the inputs to this
		 * builder.
		 */
		public PersonConstructionData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new PersonConstructionData(data);
		}

		/**
		 * Adds a value to the builder.
		 * 
		 * @throws ContractException {@linkplain PersonError#NULL_AUXILIARY_DATA} if the
		 *                           value is null
		 */
		public Builder add(Object value) {
			ensureDataMutability();
			if (value == null) {
				throw new ContractException(PersonError.NULL_AUXILIARY_DATA);
			}
			data.values.add(value);
			return this;
		}

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}

		private void validateData() {
			for (Object object : data.values) {
				if (object == null) {
					throw new ContractException(PersonError.NULL_AUXILIARY_DATA);
				}
			}
		}
	}

	/**
	 * Returns the first auxiliary object that is an instance of the given class.
	 * Should be used only getValues() is expected to have at most one member.
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
	 * Returns the auxiliary objects that are instances of the given class in the
	 * order of their addition to the builder.
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

	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}

}
