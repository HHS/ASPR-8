package gov.hhs.aspr.ms.gcm.lessons.plugins.family.events;

import gov.hhs.aspr.ms.gcm.lessons.plugins.family.support.FamilyId;
import gov.hhs.aspr.ms.gcm.nucleus.Event;
import net.jcip.annotations.Immutable;

@Immutable
public final class FamilyAdditionEvent implements Event {

	private final FamilyId familyId;

	public FamilyAdditionEvent(FamilyId familyId) {
		super();
		this.familyId = familyId;
	}

	public FamilyId getFamilyId() {
		return familyId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FamilyAdditionEvent [familyId=");
		builder.append(familyId);
		builder.append("]");
		return builder.toString();
	}

}
