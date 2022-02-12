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
public class ActionPluginInitializer implements PluginInitializer{
	
	private ActionPluginDataManager actionPluginDataManager;
	/**
	 * Provides a consumer of PluginContext. The plugin defines a single
	 * resolver that 1)handles AliasAssignmentEvent events, 2)initializes
	 * publishes and maintains the ActionDataView, 3)creates client agents, 4)
	 * creates client reports, 5) creates client resolvers, 6) publishes client
	 * data views, 7) adds client event labelers.
	 * 
	 * @throws RuntimeException
	 *             <li>if the pluginContext is null</li>
	 */
	public void init(PluginContext pluginContext) {
		if (pluginContext == null) {
			throw new RuntimeException("null plugin context");
		}
	
		ActionPluginData actionPluginData = pluginContext.getPluginData(ActionPluginData.class).get();
		
		actionPluginDataManager = new ActionPluginDataManager(actionPluginData);
		pluginContext.addDataManager(actionPluginDataManager);
		
		List<Class<? extends ActionDataManager>> actionDataManagerTypes = actionPluginData.getActionDataManagerTypes();
		for(Class<? extends ActionDataManager> c : actionDataManagerTypes) {
			ActionDataManager actionDataManager;
			try {
				actionDataManager = c.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException(e);				
			}
			pluginContext.addDataManager(actionDataManager);
		}
		
		for(Object alias :  actionPluginData.getAgentsRequiringConstruction()) {
			pluginContext.addAgent(new ActionAgent(alias)::init);
		}

	}

	/**
	 * Return true if and only if all actions that are stored in this action
	 * data view have been executed. Indicates that all injected behaviors in a
	 * unit test were actually executed. RETURNS FALSE IF THERE WERE NO ACTIONS
	 * STORED.
	 */
	public boolean allActionsExecuted() {		
		return actionPluginDataManager.allActionsExecuted();		
	}

	@Override
	public PluginId getPluginId() {
		return ActionPluginId.PLUGIN_ID;
	}

}
