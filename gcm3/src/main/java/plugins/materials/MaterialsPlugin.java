package plugins.materials;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.components.ComponentPlugin;
import plugins.materials.initialdata.MaterialsInitialData;
import plugins.materials.resolvers.MaterialsEventResolver;
import plugins.materials.support.MaterialsProducerId;
import plugins.properties.PropertiesPlugin;
import plugins.regions.RegionPlugin;
import plugins.reports.ReportPlugin;
import plugins.resources.ResourcesPlugin;
import util.ContractError;

/**
 *
 * <p>
 * <b>Summary</b> A nucleus plugin for the materials and resource production
 * modeling. A materials producer creates batches of materials and organizes
 * those materials onto stages. Stages can be traded between producers or
 * converted into resources.
 * </p>
 *
 * <p>
 * <b>Events </b> See each event class for details.
 * <ul>
 * 
 * 
 *
 * <li><b>BatchConstructionEvent</b>: Creates a batch with property values and
 * places it into a materials producers inventory</li>
 * <li><b>BatchContentShiftEvent</b>: Transfers material from one batch to
 * another</li>
 * <li><b>BatchCreationEvent</b>: Creates a batch</li>
 * <li><b>BatchPropertyValueAssignmentEvent</b>: Assigns a batch property
 * value</li>
 * <li><b>BatchRemovalRequestEvent</b>: Removes a batch from the simulation</li>
 * <li><b>MaterialsProducerPropertyValueAssignmentEvent</b>: Assigns a property
 * value to a materials producer</li>
 * <li><b>MoveBatchToInventoryEvent</b>: Moves a batch from a stage into
 * inventory</li>
 * <li><b>MoveBatchToStageEvent</b>: Moves a batch from inventory onto a
 * stage</li>
 * <li><b>OfferedStageTransferToMaterialsProducerEvent</b>: Transfers a offered
 * stage from one materials producer to another</li>
 * <li><b>ProducedResourceTransferToRegionEvent</b>:Transfers a resource amount
 * from a materials producer to a region</li>
 * <li><b>StageCreationEvent</b>: Creates a stage</li>
 * <li><b>StageOfferEvent</b>: Updates the offer state of a stage</li>
 * <li><b>StageRemovalRequestEvent</b>: Removes a stage and possibly any
 * associated batches from the simulation</li>
 * <li><b>StageToBatchConversionEvent</b>: Converts a non-offered stage into a
 * batch</li>
 * <li><b>StageToResourceConversionEvent</b>:Converts a non-offered stage into
 * an amount of resource</li>
 * 
 * <li><b>BatchAmountChangeObservationEvent</b>:Notifies subscribed observers of
 * a change to a batch's material amount</li>
 * <li><b>BatchCreationObservationEvent</b>:Notifies subscribed observers of a
 * batch creation</li>
 * <li><b>BatchImminentRemovalObservationEvent</b>:Notifies subscribed observers
 * of a batch removal</li>
 * <li><b>BatchPropertyChangeObservationEvent</b>:Notifies subscribed observers
 * of a batch property change</li>
 * <li><b>MaterialsProducerPropertyChangeObservationEvent</b>:Notifies
 * subscribed observers of a materials producer property change</li>
 * <li><b>MaterialsProducerResourceChangeObservationEvent</b>:Notifies
 * subscribed observers of a materials producer resource level change</li>
 * <li><b>StageCreationObservationEvent</b>:Notifies subscribed observers of a
 * stage creation</li>
 * <li><b>StageImminentRemovalObservationEvent</b>:Notifies subscribed observers
 * of a stage removal</li>
 * <li><b>StageMaterialsProducerChangeObservationEvent</b>:Notifies subscribed
 * observers of a transfer of custody for stage from one materials producer to
 * another</li>
 * <li><b>StageMembershipAdditionObservationEvent</b>:Notifies subscribed
 * observers of an addition of a batch to a stage</li>
 * <li><b>StageMembershipRemovalObservationEvent</b>:Notifies subscribed
 * observers of a removal of a batch from a stage</li>
 * <li><b>StageOfferChangeObservationEvent</b>:Notifies subscribed observers of
 * a change to the offer state of a stage</li>
 * 
 *
 * 
 * </ul>
 * </p>
 *
 * <p>
 * <b>Resolvers</b>
 * <ul>
 * <li><b>MaterialsEventResolver</b>: Uses initializing data to create and
 * publish data views. Handles all plugin-defined events.
 * </ul>
 * </p>
 *
 * <p>
 * <b>Data Views</b>
 * <ul>
 * <li><b>Materials Data View</b>: Supplies materials producer id values and all
 * materials related information</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Reports</b>
 * <ul>
 * 
 * <li><b>BatchStatusReport </b> A trace report showing the evolving state of
 * all batches</li>
 * <li><b>MaterialsProducerPropertyReport</b>A trace report of materials
 * producer property values</li>
 * <li><b>MaterialsProducerResourceReport</b>A trace report of materials
 * producer resource levels</li>
 * <li><b>StageReport</b>A trace report showing the evolving state of all
 * batches</li>
 * 
 * </ul>
 * </p>
 *
 * <p>
 * <b>Agents: </b>This plugin does not provide any agent implementations. Each
 * {@linkplain MaterialsProducerId} defined by the
 * {@linkplain MaterialsInitialData} becomes an agent.
 *
 * </p>
 *
 * <p>
 * <b>Initializing data:</b> An immutable container of the initial state of
 * materials producers.
 * </ul>
 *
 *
 * </p>
 *
 * <p>
 * <b>Support classes</b>
 * <ul>
 * 
 * <li><b>BatchConstructionInfo</b>A builder based means for specifying the
 * properties of a batch that spares the cost of multiple independent property
 * value assignment events</li>
 * <li><b>BatchId</b>Defines a typed, int-based, id for batches</li>
 * <li><b>BatchPropertyId</b>Defines a typed id for batch properties,
 * differentiated by material ids</li>
 * <li><b>MaterialId</b>Defines a typed id for batch material types</li>
 * <li><b>MaterialsError</b>Enumeration implementing {@linkplain ContractError}
 * for this plugin</li>
 * <li><b>MaterialsProducerId</b>Defines a typed id materials producers</li>
 * <li><b>MaterialsProducerPropertyId</b>Defines a typed id for materials
 * producer properties</li>
 * <li><b>StageId</b>Defines a typed, int-based, id for stages</li>
 * <li><b>MaterialsActionSupport</b>Test support class that provides methods for
 * building the common boiler plate plugin dependencies required for material
 * plugin testing</li>
 * <li><b>TestBatchPropertyId</b> Test support enumeration that provides
 * BatchPropertyId implementations</li>
 * <li><b>TestMaterialId</b>Test support enumeration that provides MaterialId
 * implementations</li>
 * <li><b>TestMaterialsProducerId</b>Test support enumeration that provides
 * MaterialsProducerId implementations</li>
 * <li><b>TestMaterialsProducerPropertyId</b>Test support enumeration that
 * provides MaterialsProducerPropertyId implementations</li>
 * 
 * </ul>
 * </p>
 *
 * <p>
 * <b>Required Plugins</b>
 * <ul>
 * <li><b>ComponentPlugin:</b> Used for the construction of materials producers.
 * <li><b>PropertiesPlugin:</b> Used to manage property values for materials
 * producers and batches.
 * <li><b>ReportPlugin:</b> Used to support reports in this plugin.
 * <li><b>RegionPlugin:</b> Used to support the transfer of resources from
 * materials producers to regions.
 * <li><b>ResourcesPlugin:</b> Used to support the conversion of staged
 * materials into resources that can be distributed to regions.
 * </ul>
 * </p>
 *
 * @author Shawn Hatch
 *
 */
public final class MaterialsPlugin {
	public final static PluginId PLUGIN_ID = new SimplePluginId(MaterialsPlugin.class);
	private final MaterialsInitialData materialsInitialData;

	public MaterialsPlugin(MaterialsInitialData materialsInitialData) {
		this.materialsInitialData = materialsInitialData;
	}

	public void init(PluginContext pluginContext) {
		pluginContext.defineResolver(new SimpleResolverId(MaterialsEventResolver.class), new MaterialsEventResolver(materialsInitialData)::init);

		pluginContext.addPluginDependency(ComponentPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(PropertiesPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(ReportPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(RegionPlugin.PLUGIN_ID);
		pluginContext.addPluginDependency(ResourcesPlugin.PLUGIN_ID);
	}

}
