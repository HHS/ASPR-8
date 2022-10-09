package nucleus;

import java.util.function.Function;

import util.errors.ContractException;

public final class IdentifiableFunction<N> {
	
	private Object functionId;
	
	private Function<N, Object> eventFunction;

	
	public IdentifiableFunction(Object functionId, Function<N, Object> eventFunction) {
		if (functionId == null) {
			throw new ContractException(NucleusError.NULL_EVENT_FUNCTION_ID);
		}
		if (eventFunction == null) {
			throw new ContractException(NucleusError.NULL_EVENT_FUNCTION);
		}
		
		this.functionId = functionId;
		this.eventFunction = eventFunction;
	}

	public Function<N, Object> getEventFunction() {
		return eventFunction;
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
