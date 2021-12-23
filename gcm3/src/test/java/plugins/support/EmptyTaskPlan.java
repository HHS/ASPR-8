package plugins.support;

import plugins.gcm.agents.Environment;

public class EmptyTaskPlan extends TaskPlan {

	public EmptyTaskPlan(final double scheduledTime, Object key) {
		super(scheduledTime, key, new Task() {

			@Override
			public void execute(Environment environment) {

			}
		});
	}

}
