package tools.dependencyanalysis.classgraph.support;

import net.jcip.annotations.Immutable;

@Immutable
public final class JavaRef {
	private final String className;
	private final String packageName;

	public JavaRef(String className, String packageName) {		
		this.className = className;
		this.packageName = packageName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof JavaRef)) {
			return false;
		}
		JavaRef other = (JavaRef) obj;
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		if (packageName == null) {
			if (other.packageName != null) {
				return false;
			}
		} else if (!packageName.equals(other.packageName)) {
			return false;
		}
		return true;
	}

	public String getClassName() {
		return className;
	}

	public String getPackageName() {
		return packageName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JavaRef [packageName=");
		builder.append(packageName);
		builder.append(", className=");
		builder.append(className);
		builder.append("]");
		return builder.toString();
	}
	
	public String getFullName() {
		return packageName+"."+className+".java";
	}

	
}
