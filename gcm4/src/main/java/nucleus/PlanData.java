package nucleus;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public interface PlanData {
	
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
