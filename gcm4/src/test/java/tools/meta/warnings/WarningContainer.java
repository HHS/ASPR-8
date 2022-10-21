package tools.meta.warnings;

import java.util.ArrayList;
import java.util.List;

public final class WarningContainer {
	private static class Data {
		private List<ConstructorWarning> constructorWarnings = new ArrayList<>();
		private List<MethodWarning> methodWarnings = new ArrayList<>();
	}

	public final static Builder builder() {
		return new Builder();
	}

	public final static class Builder {
		private Builder() {

		}

		private Data data = new Data();

		public WarningContainer build() {
			try {
				return new WarningContainer(data);
			} finally {
				data = new Data();
			}
		}
		/**
		 * Adds a constructor warning
		 * 
		 * @throws NullPointerException
		 *             <li>if the constructor warning is null</li>
		 */
		public Builder addConstructorWarning(ConstructorWarning constructorWarning) {
			if (constructorWarning == null) {
				throw new NullPointerException("constructor warning is null");
			}
			data.constructorWarnings.add(constructorWarning);
			return this;
		}

		/**
		 * Adds a method warning
		 * 
		 * @throws NullPointerException
		 *             <li>if the method warning is null</li>
		 */
		public Builder addMethodWarning(MethodWarning methodWarning) {
			if (methodWarning == null) {
				throw new NullPointerException("method warning is null");
			}
			data.methodWarnings.add(methodWarning);
			return this;
		}
	}

	private final Data data;

	private WarningContainer(Data data) {
		this.data = data;
	}

	public List<MethodWarning> getMethodWarnings() {
		return new ArrayList<>(data.methodWarnings);
	}

	public List<ConstructorWarning> getConstructorWarnings() {
		return new ArrayList<>(data.constructorWarnings);
	}
}
