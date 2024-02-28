package gov.hhs.aspr.ms.gcm.nucleus;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((functionId == null) ? 0 : functionId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof IdentifiableFunction)) {
			return false;
		}
		IdentifiableFunction<?> other = (IdentifiableFunction<?>) obj;
		if (functionId == null) {
			if (other.functionId != null) {
				return false;
			}
		} else if (!functionId.equals(other.functionId)) {
			return false;
		}
		return true;
	}

}
