package manual.pubsub.globalcomponents;

import plugins.globals.support.GlobalComponentId;

public class DeltaId implements GlobalComponentId {
	private final int id;

	public DeltaId(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeltaId other = (DeltaId) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeltaId [id=");
		builder.append(id);
		builder.append("]");
		return builder.toString();
	}

}
