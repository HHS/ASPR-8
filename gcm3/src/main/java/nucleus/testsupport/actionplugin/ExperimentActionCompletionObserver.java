package nucleus.testsupport.actionplugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.jcip.annotations.ThreadSafe;
import nucleus.ExperimentContext;

@ThreadSafe
public class ExperimentActionCompletionObserver {
	
	private Map<Integer,ActionCompletionReport> actionCompletionReports = new LinkedHashMap<>(); 

	public synchronized void init(ExperimentContext experimentContext) {
		experimentContext.subscribeToOutput(ActionCompletionReport.class, this::asdf);
	}

	private synchronized void asdf(ExperimentContext experimentContext, Integer scenarioId, ActionCompletionReport actionCompletionReport) {
		actionCompletionReports.put(scenarioId, actionCompletionReport);
	}
	
	public Optional<ActionCompletionReport> getActionCompletionReport(Integer scenarioId) {
		return Optional.ofNullable(actionCompletionReports.get(scenarioId));
	}
	
	

}
