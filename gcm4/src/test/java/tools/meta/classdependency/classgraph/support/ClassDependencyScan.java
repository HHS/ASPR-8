package tools.meta.classdependency.classgraph.support;

import java.util.LinkedHashSet;
import java.util.Set;

public class ClassDependencyScan {

	private ClassDependencyScan(Data data) {
		this.data = data;
	}

	private final Data data;

	private static class Data {
		private Set<JavaDependency> javaDependencies = new LinkedHashSet<>();
		private Set<String> localPackageNames = new LinkedHashSet<>();
		
		public Data() {}
		public Data(Data data) {
			javaDependencies.addAll(data.javaDependencies);
			localPackageNames.addAll(localPackageNames);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Data data = new Data();

		private Builder() {

		}

		public ClassDependencyScan build() {
			return new ClassDependencyScan(new Data(data));
		}

		public Builder addJavaDependency(JavaDependency javaDependency) {
			data.javaDependencies.add(javaDependency);
			return this;
		}

		public Builder addLocalPackageName(String localPackageName) {
			data.localPackageNames.add(localPackageName);
			return this;
		}
	}

	public Set<JavaDependency> getJavaDependencies() {
		return new LinkedHashSet<>(data.javaDependencies);
	}

	public Set<String> getLocalPackageNames() {
		return new LinkedHashSet<>(data.localPackageNames);
	}

}
