package lesson.plugins.family.events;

import lesson.plugins.family.support.FamilyId;
import net.jcip.annotations.Immutable;
import nucleus.Event;

@Immutable
public final class FamilyAdditionEvent implements Event{

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
