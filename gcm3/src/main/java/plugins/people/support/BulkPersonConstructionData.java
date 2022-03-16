package plugins.people.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.jcip.annotations.Immutable;
import nucleus.util.ContractException;


/**
 * A collection of {@link PersonContructionData} that represents multiple person
 * additions combined with a collection of unspecified data types that can be
 * used as auxiliary data about those people.
 *
 */
@Immutable
public final class BulkPersonConstructionData {

	private static class Data {
		private List<PersonContructionData> personContructionDatas = new ArrayList<>();
		private List<Object> values = new ArrayList<>();
	}

	private final Data data;

	private BulkPersonConstructionData(Data data) {
		this.data = data;
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for {@link BulkPersonConstructionData}
	 */
	public static class Builder {
		private Data data = new Data();

		private Builder() {

		}

		/**
		 * Returns a {@link BulkPersonConstructionData} formed from the collected
		 * data. Clears the state of the builder.
		 */
		public BulkPersonConstructionData build() {
			try {
				return new BulkPersonConstructionData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds the construction data for a single person
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_BULK_PERSON_CONTRUCTION_DATA}
		 *             if the person construction data is null</li>
		 * 
		 */
		public Builder add(PersonContructionData personContructionData) {
			if (personContructionData == null) {
				throw new ContractException(PersonError.NULL_PERSON_CONTRUCTION_DATA);
			}
			data.personContructionDatas.add(personContructionData);
			return this;
		}

		/**
		 * Adds data that relates to the entire group of people being
		 * constructed.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_AUXILIARY_DATA} if the
		 *             auxiliary data is null</li>
		 */
		public Builder addAuxiliaryData(Object auxiliaryData) {
			if (auxiliaryData == null) {
				throw new NullPointerException("only non-null values allowed");
			}
			data.values.add(auxiliaryData);
			return this;
		}

	}

	/**
	 * Returns the PersonContructionData in the order of their addition to the
	 * builder
	 */
	public List<PersonContructionData> getPersonContructionDatas() {
		return Collections.unmodifiableList(data.personContructionDatas);
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
