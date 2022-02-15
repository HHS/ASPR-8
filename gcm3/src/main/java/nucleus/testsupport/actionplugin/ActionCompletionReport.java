package nucleus.testsupport.actionplugin;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class ActionCompletionReport {
	private final boolean complete;

	public ActionCompletionReport(boolean complete) {
		super();
		this.complete = complete;
	}

	public boolean isComplete() {
		return complete;
	}
	
}
