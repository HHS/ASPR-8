package gov.hhs.aspr.translation.core.unittestcoverage;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.translation.core.unittestcoverage.warnings.ConstructorWarning;
import gov.hhs.aspr.translation.core.unittestcoverage.warnings.FieldWarning;
import gov.hhs.aspr.translation.core.unittestcoverage.warnings.MethodWarning;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestField;
import util.annotations.UnitTestMethod;

public final class MetaInfoContainer {
	private static class Data {
		private List<ConstructorWarning> constructorWarnings = new ArrayList<>();
		private List<MethodWarning> methodWarnings = new ArrayList<>();
		private List<FieldWarning> fieldWarnings = new ArrayList<>();
		private List<String> generalWarnings = new ArrayList<>();
		private List<UnitTestField> validatedUnitTestFields = new ArrayList<>();
		private List<UnitTestMethod> validatedUnitTestMethods = new ArrayList<>();
		private List<UnitTestConstructor> validatedUnitTestConstructors = new ArrayList<>();
	}

	public final static Builder builder() {
		return new Builder();
	}

	public final static class Builder {
		private Builder() {

		}

		private Data data = new Data();

		public MetaInfoContainer build() {			
			return new MetaInfoContainer(data);			
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

		/**
		 * Adds a validated UnitTestField. UnitTestField instances added here
		 * should not correspond to a warning.
		 * 
		 * @throws NullPointerException
		 *             <li>if the unitTestField is null</li>
		 */
		public Builder addUnitTestField(UnitTestField unitTestField) {
			if (unitTestField == null) {
				throw new NullPointerException("unitTestField is null");
			}
			data.validatedUnitTestFields.add(unitTestField);
			return this;
		}

		/**
		 * Adds a validated UnitTestMethod. UnitTestMethod instances added here
		 * should not correspond to a warning.
		 * 
		 * @throws NullPointerException
		 *             <li>if the unitTestMethod is null</li>
		 */
		public Builder addUnitTestMethod(UnitTestMethod unitTestMethod) {
			if (unitTestMethod == null) {
				throw new NullPointerException("unitTestMethod is null");
			}
			data.validatedUnitTestMethods.add(unitTestMethod);
			return this;
		}

		/**
		 * Adds a validated UnitTestConstructor. UnitTestConstructor instances
		 * added here should not correspond to a warning.
		 * 
		 * @throws NullPointerException
		 *             <li>if the unitTestConstructor is null</li>
		 */
		public Builder addUnitTestConstructor(UnitTestConstructor unitTestConstructor) {
			if (unitTestConstructor == null) {
				throw new NullPointerException("unitTestConstructor is null");
			}
			data.validatedUnitTestConstructors.add(unitTestConstructor);
			return this;
		}

		/**
		 * Adds a field warning
		 * 
		 * @throws NullPointerException
		 *             <li>if the field warning is null</li>
		 */
		public Builder addFieldWarning(FieldWarning fieldWarning) {
			if (fieldWarning == null) {
				throw new NullPointerException("field warning is null");
			}
			data.fieldWarnings.add(fieldWarning);
			return this;
		}

		/**
		 * Adds a method warning
		 * 
		 * @throws NullPointerException
		 *             <li>if the general warning is null</li>
		 */
		public Builder addGeneralWarning(String generalWarning) {
			if (generalWarning == null) {
				throw new NullPointerException("general warning is null");
			}
			data.generalWarnings.add(generalWarning);
			return this;
		}
	}

	private final Data data;

	private MetaInfoContainer(Data data) {
		this.data = data;
	}

	public List<FieldWarning> getFieldWarnings() {
		return new ArrayList<>(data.fieldWarnings);
	}

	public List<MethodWarning> getMethodWarnings() {
		return new ArrayList<>(data.methodWarnings);
	}

	public List<ConstructorWarning> getConstructorWarnings() {
		return new ArrayList<>(data.constructorWarnings);
	}

	public List<String> getGeneralWarnings() {
		return new ArrayList<>(data.generalWarnings);
	}

	public List<UnitTestField> getUnitTestFields() {
		return new ArrayList<>(data.validatedUnitTestFields);
	}

	public List<UnitTestMethod> getUnitTestMethods() {
		return new ArrayList<>(data.validatedUnitTestMethods);
	}

	public List<UnitTestConstructor> getUnitTestConstructors() {
		return new ArrayList<>(data.validatedUnitTestConstructors);
	}

}
