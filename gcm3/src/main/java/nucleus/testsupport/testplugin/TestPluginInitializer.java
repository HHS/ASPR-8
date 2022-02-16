package nucleus.testsupport.actionplugin;

import java.util.List;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.PluginInitializer;

/**
 * Test Support plugin that is designed to work with a unit testing framework.
 * It provides for 1)the injection of behavior into agents to test various
 * simulation behaviors in a function/system setting and 2) the collection of
 * observed events for comparison to expectations.
 * 
 * @author Shawn Hatch
 *
 */
public class ActionPluginInitializer implements PluginInitializer {

	

	/**
	 * Initializes a simulation via the given context. Using an ActionPluginData
	 * retrieved from the context, this initializer adds ActionAgent and
	 * ActionDataManager instances that are used in testing. It also creates an
	 * ActionPluginDataManager that is used internally to this plugin to help
	 * manage data for the aforementioned agents and data managers.
	 * 
	 * @throws RuntimeException
	 *             <li>if the pluginContext is null</li>
	 */
	public void init(PluginContext pluginContext) {
		if (pluginContext == null) {
			throw new RuntimeException("null plugin context");
		}

		ActionPluginData actionPluginData = pluginContext.getPluginData(ActionPluginData.class).get();

		ActionPluginDataManager actionPluginDataManager = new ActionPluginDataManager(actionPluginData);
		pluginContext.addDataManager(actionPluginDataManager);

		List<Class<? extends ActionDataManager>> actionDataManagerTypes = actionPluginData.getActionDataManagerTypes();
		for (Class<? extends ActionDataManager> c : actionDataManagerTypes) {
			ActionDataManager actionDataManager;
			try {
				actionDataManager = c.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			pluginContext.addDataManager(actionDataManager);
		}

		for (Object alias : actionPluginData.getAgentsRequiringConstruction()) {
			pluginContext.addAgent(new ActionAgent(alias)::init);
		}

	}


	@Override
	public PluginId getPluginId() {
		return ActionPluginId.PLUGIN_ID;
	}

}
