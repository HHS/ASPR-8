package plugins.people;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import nucleus.util.ContractException;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonError;

/**
 * An immutable container of the initial state of people containing person ids.
 * All other person initialization data is provided by other plugins.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class PeoplePluginData implements PluginData {
	private static class Data {
		private List<BulkPersonConstructionData> bulkPersonConstructionDatas = new ArrayList<>();

		public Data() {
		}

		public Data(Data data) {
			this.bulkPersonConstructionDatas.addAll(data.bulkPersonConstructionDatas);
		}
	}

	private final Data data;

	/**
	 * Returns a new builder instance for this class
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Builder class for PeoplePluginData
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;

		}

		/**
		 * Returns the PeopleInitialData resulting from the person ids collected
		 * by this builder.
		 */
		public PeoplePluginData build() {
			try {
				return new PeoplePluginData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds a person.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_BULK_PERSON_CONTRUCTION_DATA}
		 *             if the bulk person construction data is null</li>
		 *            
		 * 
		 */
		public Builder addBulkPersonContructionData(BulkPersonConstructionData bulkPersonConstructionData) {
			validateBulkPersonContructionDataNotNull(bulkPersonConstructionData);			
			data.bulkPersonConstructionDatas.add(bulkPersonConstructionData);
			return this;
		}
	}
	
	private static void validateBulkPersonContructionDataNotNull(BulkPersonConstructionData bulkPersonConstructionData) {
		if (bulkPersonConstructionData == null) {
			throw new ContractException(PersonError.NULL_BULK_PERSON_CONTRUCTION_DATA);
		}
	}

	private PeoplePluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the set of person ids stored in this container
	 */
	public List<BulkPersonConstructionData> getBulkPersonConstructionDatas() {
		return new ArrayList<>(data.bulkPersonConstructionDatas);
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(new Data(data));
	}

}
