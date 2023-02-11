package tools.meta.classdependency.classgraph.support;

import net.jcip.annotations.Immutable;

@Immutable
public class JavaDependency {

	private final JavaRef dependentRef;
	private final JavaRef supportRef;

	public JavaDependency(JavaRef dependentRef, JavaRef supportRef) {
		super();
		this.dependentRef = dependentRef;
		this.supportRef = supportRef;
	}

	public JavaRef getDependentRef() {
		return dependentRef;
	}

	public JavaRef getSupportRef() {
		return supportRef;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dependentRef == null) ? 0 : dependentRef.hashCode());
		result = prime * result + ((supportRef == null) ? 0 : supportRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof JavaDependency)) {
			return false;
		}
		JavaDependency other = (JavaDependency) obj;
		if (dependentRef == null) {
			if (other.dependentRef != null) {
				return false;
			}
		} else if (!dependentRef.equals(other.dependentRef)) {
			return false;
		}
		if (supportRef == null) {
			if (other.supportRef != null) {
				return false;
			}
		} else if (!supportRef.equals(other.supportRef)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JavaDependency [dependentRef=");
		builder.append(dependentRef);
		builder.append(", supportRef=");
		builder.append(supportRef);
		builder.append("]");
		return builder.toString();
	}

}
