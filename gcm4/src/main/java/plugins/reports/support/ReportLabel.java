package plugins.reports.support;

import net.jcip.annotations.ThreadSafe;

/**
 * Marker interface for the unique report labels. Report items are marked
 * with a report label that allows an output manager to determine the final
 * disposition of the report item. Report labels must be thread-safe.
 * 
 *
 */
@ThreadSafe
public interface ReportLabel {
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
