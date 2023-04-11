package plugins.people.support;

import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * Represents a range of person id values
 * 
 *
 *
 */

@Immutable
public class PersonRange implements Comparable<PersonRange> {

	private final int lowPersonId;
	private final int highPersonId;

	/**
	 * Constructs an inclusive range of person id values
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NEGATIVE_PERSON_ID} if a negative
	 *             id is used
	 *             <li>
	 *             <li>{@linkplain PersonError#ILLEGAL_PERSON_RANGE} if the low
	 *             person id exceeds the high person id
	 *             <li>
	 */
	public PersonRange(int lowPersonId, int highPersonId) {

		if (lowPersonId > highPersonId) {
			throw new ContractException(PersonError.ILLEGAL_PERSON_RANGE);
		}
		if (lowPersonId < 0) {
			throw new ContractException(PersonError.NEGATIVE_PERSON_ID);
		}
		this.lowPersonId = lowPersonId;
		this.highPersonId = highPersonId;
	}

	public int getLowPersonId() {
		return lowPersonId;
	}

	public int getHighPersonId() {
		return highPersonId;
	}

	@Override
	public int compareTo(PersonRange personRange) {
		int result = Integer.compare(this.lowPersonId, personRange.lowPersonId);
		if (result == 0) {
			result = Integer.compare(this.highPersonId, personRange.highPersonId);
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + highPersonId;
		result = prime * result + lowPersonId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PersonRange)) {
			return false;
		}
		PersonRange other = (PersonRange) obj;
		if (highPersonId != other.highPersonId) {
			return false;
		}
		if (lowPersonId != other.lowPersonId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PersonRange [lowPersonId=");
		builder.append(lowPersonId);
		builder.append(", highPersonId=");
		builder.append(highPersonId);
		builder.append("]");
		return builder.toString();
	}

}
