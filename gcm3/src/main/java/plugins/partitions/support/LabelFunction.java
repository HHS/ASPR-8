package plugins.partitions.support;

import nucleus.Context;
import plugins.people.support.PersonId;

public interface LabelFunction {
	public Object getLabel(Context context, PersonId personId);
}