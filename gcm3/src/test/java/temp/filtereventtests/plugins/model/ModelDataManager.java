package temp.filtereventtests.plugins.model;

import nucleus.DataManager;
import nucleus.DataManagerContext;

public class ModelDataManager extends DataManager {
	private final ModelPluginData modelPluginData;

	public ModelDataManager(ModelPluginData modelPluginData) {
		this.modelPluginData = modelPluginData;
	}

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
	}

	public int getEventCount() {
		return modelPluginData.getEventCount();
	}

	public boolean getUseEventFilters() {
		return modelPluginData.getUseEventFilters();
	}

}
