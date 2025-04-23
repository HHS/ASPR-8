package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.Objects;
import java.util.function.Function;

import gov.hhs.aspr.ms.util.errors.ContractException;

public final class IdentifiableFunction<N> {

	private Object functionId;

	private Function<N, Object> function;

	public IdentifiableFunction(Object functionId, Function<N, Object> function) {
		if (functionId == null) {
			throw new ContractException(NucleusError.NULL_FUNCTION_ID);
		}
		if (function == null) {
			throw new ContractException(NucleusError.NULL_FUNCTION);
		}

		this.functionId = functionId;
		this.function = function;
	}

	public Function<N, Object> getFunction() {
		return function;
	}

	/**
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(functionId);
	}

	/**
	 * Two {@link IdentifiableFunction} instances are equal if and only if
	 * their functionIds are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IdentifiableFunction<?> other = (IdentifiableFunction<?>) obj;
		return Objects.equals(functionId, other.functionId);
	}
}
