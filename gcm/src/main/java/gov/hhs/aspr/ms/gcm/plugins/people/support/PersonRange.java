package gov.hhs.aspr.ms.gcm.plugins.people.support;

import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * Represents a range of person id values
 */

@Immutable
public class PersonRange implements Comparable<PersonRange> {

	private final int lowPersonId;
	private final int highPersonId;

	/**
	 * Constructs an inclusive range of person id values
	 * 
	 * @throws util.errors.ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PersonError#NEGATIVE_PERSON_ID} if
	 *                           a negative id is used</li>
	 *                           <li></li>
	 *                           <li>{@linkplain PersonError#ILLEGAL_PERSON_RANGE}
	 *                           if the low person id exceeds the high person id
	 *                           </li>
	 *                           <li></li>
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

	/**
	 * Returns the lowest person id (inclusive) of this range
	 */
	public int getLowPersonId() {
		return lowPersonId;
	}

	/**
	 * Returns the highest person id (inclusive) of this range
	 */
	public int getHighPersonId() {
		return highPersonId;
	}

	/**
	 * Compares to another person range by ascending order for lower bound and then
	 * ascending order by upper bound
	 */
	@Override
	public int compareTo(PersonRange personRange) {
		int result = Integer.compare(this.lowPersonId, personRange.lowPersonId);
		if (result == 0) {
			result = Integer.compare(this.highPersonId, personRange.highPersonId);
		}
		return result;
	}

	/**
	 * Boiler plate implementation of hash code
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + highPersonId;
		result = prime * result + lowPersonId;
		return result;
	}

	/**
	 * Two person ranges are equal if and only if they have the same upper and lower
	 * bounds.
	 */
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

	/**
	 * Returns the string version of a person range in the form: PersonRange
	 * [lowPersonId=4, highPersonId=10]
	 */
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
