package plugins.gcm.input;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.jcip.annotations.NotThreadSafe;
import nucleus.AgentContext;
import nucleus.ReportContext;
import nucleus.ReportId;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.components.support.ComponentId;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalPropertyId;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.stochastics.support.RandomNumberGeneratorId;

/**
 * A structured builder class for Scenario. Modelers should use the
 * {@link UnstructuredScenarioBuilder}
 *
 * The structured builder requires that invocations to its various methods be
 * ordered such that dependencies are supplied in order. For example, to add a
 * person to a group the person and group must be added via the builder prior to
 * the addition. Similarly, the group cannot be added until the group type for
 * that group has been added.
 *
 *
 * Each of the builder's data collection methods use the following pattern:
 * 
 * 1) Establish a lock that will block reentrant mutation that could corrupt the
 * state of the builder.
 * 
 * 2) Validate all arguments to ensure they are consistent with previous
 * entries, obey range restrictions, relational restrictions and block
 * redundancy.
 * 
 * 3) Integrate the new data into the ScenarioData instance
 * 
 * 4) Release the lock
 * 
 * 
 * Validation is conducted by separate methods and these in turn invoke
 * exception throwing methods if data are invalid.
 *
 * @author Shawn Hatch
 *
 */
@NotThreadSafe
public final class StructuredScenarioBuilder implements ScenarioBuilder {

	/*
	 * A container class for holding the data of a scenario.
	 */
	private final static class ScenarioData {
		// compartments
		private final Map<CompartmentId, Map<CompartmentPropertyId, PropertyDefinition>> compartmentPropertyDefinitions = new LinkedHashMap<>();

		private final Map<CompartmentId, Map<CompartmentPropertyId, Object>> compartmentPropertyValues = new LinkedHashMap<>();

		private final Map<CompartmentId, Supplier<Consumer<AgentContext>>> compartmentIds = new LinkedHashMap<>();

		private final Map<ReportId, Supplier<Consumer<ReportContext>>> reportIds = new LinkedHashMap<>();

		private TimeTrackingPolicy compartmentArrivalTimeTrackingPolicy;

		private final Map<PersonId, CompartmentId> personCompartments = new LinkedHashMap<>();

		// groups
		private final Map<GroupTypeId, Map<GroupPropertyId, PropertyDefinition>> groupPropertyDefinitions = new LinkedHashMap<>();

		private final Map<GroupId, Map<GroupPropertyId, Object>> groupPropertyValues = new LinkedHashMap<>();

		private final Set<GroupTypeId> groupTypeIds = new LinkedHashSet<>();

		private final Set<GroupId> groupIds = new LinkedHashSet<>();

		private final Map<GroupId, Set<PersonId>> groupMemberships = new LinkedHashMap<>();

		private final Map<GroupId, GroupTypeId> groupTypes = new LinkedHashMap<>();

		// globals
		private final Map<GlobalPropertyId, PropertyDefinition> globalPropertyDefinitions = new LinkedHashMap<>();

		private final Map<GlobalComponentId, Supplier<Consumer<AgentContext>>> globalComponentIds = new LinkedHashMap<>();

		private final Map<GlobalPropertyId, Object> globalPropertyValues = new LinkedHashMap<>();

		// materials
		private final Map<MaterialsProducerPropertyId, PropertyDefinition> materialsProducerPropertyDefinitions = new LinkedHashMap<>();

		private final Map<MaterialsProducerId, Supplier<Consumer<AgentContext>>> materialsProducerIds = new LinkedHashMap<>();

		private final Map<MaterialId, Map<BatchPropertyId, PropertyDefinition>> batchPropertyDefinitions = new LinkedHashMap<>();

		private final Map<MaterialsProducerId, Map<MaterialsProducerPropertyId, Object>> materialsProducerPropertyValues = new LinkedHashMap<>();

		private final Map<BatchId, Map<BatchPropertyId, Object>> batchPropertyValues = new LinkedHashMap<>();

		private final Map<StageId, MaterialsProducerId> stageMaterialsProducers = new LinkedHashMap<>();

		private final Map<StageId, Set<BatchId>> stageBatches = new LinkedHashMap<>();

		private final Map<BatchId, StageId> batchStages = new LinkedHashMap<>();

		private final Set<BatchId> batchIds = new LinkedHashSet<>();

		private final Set<MaterialId> materialIds = new LinkedHashSet<>();

		private final Set<StageId> stageIds = new LinkedHashSet<>();

		private final Map<BatchId, MaterialId> batchMaterials = new LinkedHashMap<>();

		private final Map<BatchId, Double> batchAmounts = new LinkedHashMap<>();

		private final Map<StageId, Boolean> stageOffers = new LinkedHashMap<>();

		private final Map<BatchId, MaterialsProducerId> batchMaterialsProducers = new LinkedHashMap<>();

		private final Map<MaterialsProducerId, Map<ResourceId, Long>> materialsProducerResourceLevels = new LinkedHashMap<>();

		private final Set<PersonId> personIds = new LinkedHashSet<>();

		// personproperties

		private final Map<PersonPropertyId, PropertyDefinition> personPropertyDefinitions = new LinkedHashMap<>();

		private final Map<PersonId, Map<PersonPropertyId, Object>> personPropertyValues = new LinkedHashMap<>();

		// regions

		private final Map<RegionPropertyId, PropertyDefinition> regionPropertyDefinitions = new LinkedHashMap<>();

		private final Map<RegionId, Supplier<Consumer<AgentContext>>> regionIds = new LinkedHashMap<>();

		private final Map<PersonId, RegionId> personRegions = new LinkedHashMap<>();

		private TimeTrackingPolicy regionArrivalTimeTrackingPolicy;

		private final Map<RegionId, Map<RegionPropertyId, Object>> regionPropertyValues = new LinkedHashMap<>();

		// resources

		private final Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> resourcePropertyDefinitions = new LinkedHashMap<>();

		private final Map<ResourceId, Map<ResourcePropertyId, Object>> resourcePropertyValues = new LinkedHashMap<>();

		private final Map<PersonId, Map<ResourceId, Long>> personResourceLevels = new LinkedHashMap<>();

		private final Set<ResourceId> resourceIds = new LinkedHashSet<>();

		private final Map<RegionId, Map<ResourceId, Long>> regionResourceLevels = new LinkedHashMap<>();

		private final Map<ResourceId, TimeTrackingPolicy> resourceTimeTrackingPolicies = new LinkedHashMap<>();

		// stochastics
		private final Set<RandomNumberGeneratorId> randomNumberGeneratorIds = new LinkedHashSet<>();

		/**
		 * Boilerplate implementation
		 */

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((batchAmounts == null) ? 0 : batchAmounts.hashCode());
			result = prime * result + ((batchIds == null) ? 0 : batchIds.hashCode());
			result = prime * result + ((batchMaterials == null) ? 0 : batchMaterials.hashCode());
			result = prime * result + ((batchMaterialsProducers == null) ? 0 : batchMaterialsProducers.hashCode());
			result = prime * result + ((batchPropertyDefinitions == null) ? 0 : batchPropertyDefinitions.hashCode());
			result = prime * result + ((batchPropertyValues == null) ? 0 : batchPropertyValues.hashCode());
			result = prime * result + ((batchStages == null) ? 0 : batchStages.hashCode());
			result = prime * result + ((compartmentArrivalTimeTrackingPolicy == null) ? 0 : compartmentArrivalTimeTrackingPolicy.hashCode());
			result = prime * result + ((compartmentIds == null) ? 0 : compartmentIds.hashCode());
			result = prime * result + ((compartmentPropertyDefinitions == null) ? 0 : compartmentPropertyDefinitions.hashCode());
			result = prime * result + ((compartmentPropertyValues == null) ? 0 : compartmentPropertyValues.hashCode());
			result = prime * result + ((globalComponentIds == null) ? 0 : globalComponentIds.hashCode());
			result = prime * result + ((globalPropertyDefinitions == null) ? 0 : globalPropertyDefinitions.hashCode());
			result = prime * result + ((globalPropertyValues == null) ? 0 : globalPropertyValues.hashCode());
			result = prime * result + ((groupIds == null) ? 0 : groupIds.hashCode());
			result = prime * result + ((groupMemberships == null) ? 0 : groupMemberships.hashCode());
			result = prime * result + ((groupPropertyDefinitions == null) ? 0 : groupPropertyDefinitions.hashCode());
			result = prime * result + ((groupPropertyValues == null) ? 0 : groupPropertyValues.hashCode());
			result = prime * result + ((groupTypeIds == null) ? 0 : groupTypeIds.hashCode());
			result = prime * result + ((groupTypes == null) ? 0 : groupTypes.hashCode());
			result = prime * result + ((materialIds == null) ? 0 : materialIds.hashCode());
			result = prime * result + ((materialsProducerIds == null) ? 0 : materialsProducerIds.hashCode());
			result = prime * result + ((materialsProducerPropertyDefinitions == null) ? 0 : materialsProducerPropertyDefinitions.hashCode());
			result = prime * result + ((materialsProducerPropertyValues == null) ? 0 : materialsProducerPropertyValues.hashCode());
			result = prime * result + ((materialsProducerResourceLevels == null) ? 0 : materialsProducerResourceLevels.hashCode());
			result = prime * result + ((personCompartments == null) ? 0 : personCompartments.hashCode());
			result = prime * result + ((personIds == null) ? 0 : personIds.hashCode());
			result = prime * result + ((personPropertyDefinitions == null) ? 0 : personPropertyDefinitions.hashCode());
			result = prime * result + ((personPropertyValues == null) ? 0 : personPropertyValues.hashCode());
			result = prime * result + ((personRegions == null) ? 0 : personRegions.hashCode());
			result = prime * result + ((personResourceLevels == null) ? 0 : personResourceLevels.hashCode());
			result = prime * result + ((regionArrivalTimeTrackingPolicy == null) ? 0 : regionArrivalTimeTrackingPolicy.hashCode());
			result = prime * result + ((regionIds == null) ? 0 : regionIds.hashCode());
			result = prime * result + ((regionPropertyDefinitions == null) ? 0 : regionPropertyDefinitions.hashCode());
			result = prime * result + ((regionPropertyValues == null) ? 0 : regionPropertyValues.hashCode());
			result = prime * result + ((regionResourceLevels == null) ? 0 : regionResourceLevels.hashCode());
			result = prime * result + ((resourceIds == null) ? 0 : resourceIds.hashCode());
			result = prime * result + ((resourcePropertyDefinitions == null) ? 0 : resourcePropertyDefinitions.hashCode());
			result = prime * result + ((resourcePropertyValues == null) ? 0 : resourcePropertyValues.hashCode());
			result = prime * result + ((resourceTimeTrackingPolicies == null) ? 0 : resourceTimeTrackingPolicies.hashCode());
			result = prime * result + ((stageBatches == null) ? 0 : stageBatches.hashCode());
			result = prime * result + ((stageIds == null) ? 0 : stageIds.hashCode());
			result = prime * result + ((stageMaterialsProducers == null) ? 0 : stageMaterialsProducers.hashCode());
			result = prime * result + ((stageOffers == null) ? 0 : stageOffers.hashCode());			
			return result;
		}

		/**
		 * Boilerplate implementation
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ScenarioData)) {
				return false;
			}
			ScenarioData other = (ScenarioData) obj;
			if (batchAmounts == null) {
				if (other.batchAmounts != null) {
					return false;
				}
			} else if (!batchAmounts.equals(other.batchAmounts)) {
				return false;
			}
			if (batchIds == null) {
				if (other.batchIds != null) {
					return false;
				}
			} else if (!batchIds.equals(other.batchIds)) {
				return false;
			}
			if (batchMaterials == null) {
				if (other.batchMaterials != null) {
					return false;
				}
			} else if (!batchMaterials.equals(other.batchMaterials)) {
				return false;
			}
			if (batchMaterialsProducers == null) {
				if (other.batchMaterialsProducers != null) {
					return false;
				}
			} else if (!batchMaterialsProducers.equals(other.batchMaterialsProducers)) {
				return false;
			}
			if (batchPropertyDefinitions == null) {
				if (other.batchPropertyDefinitions != null) {
					return false;
				}
			} else if (!batchPropertyDefinitions.equals(other.batchPropertyDefinitions)) {
				return false;
			}
			if (batchPropertyValues == null) {
				if (other.batchPropertyValues != null) {
					return false;
				}
			} else if (!batchPropertyValues.equals(other.batchPropertyValues)) {
				return false;
			}
			if (batchStages == null) {
				if (other.batchStages != null) {
					return false;
				}
			} else if (!batchStages.equals(other.batchStages)) {
				return false;
			}
			if (compartmentArrivalTimeTrackingPolicy != other.compartmentArrivalTimeTrackingPolicy) {
				return false;
			}
			if (compartmentIds == null) {
				if (other.compartmentIds != null) {
					return false;
				}
			} else if (!compartmentIds.equals(other.compartmentIds)) {
				return false;
			}
			if (compartmentPropertyDefinitions == null) {
				if (other.compartmentPropertyDefinitions != null) {
					return false;
				}
			} else if (!compartmentPropertyDefinitions.equals(other.compartmentPropertyDefinitions)) {
				return false;
			}
			if (compartmentPropertyValues == null) {
				if (other.compartmentPropertyValues != null) {
					return false;
				}
			} else if (!compartmentPropertyValues.equals(other.compartmentPropertyValues)) {
				return false;
			}
			if (globalComponentIds == null) {
				if (other.globalComponentIds != null) {
					return false;
				}
			} else if (!globalComponentIds.equals(other.globalComponentIds)) {
				return false;
			}
			if (globalPropertyDefinitions == null) {
				if (other.globalPropertyDefinitions != null) {
					return false;
				}
			} else if (!globalPropertyDefinitions.equals(other.globalPropertyDefinitions)) {
				return false;
			}
			if (globalPropertyValues == null) {
				if (other.globalPropertyValues != null) {
					return false;
				}
			} else if (!globalPropertyValues.equals(other.globalPropertyValues)) {
				return false;
			}
			if (groupIds == null) {
				if (other.groupIds != null) {
					return false;
				}
			} else if (!groupIds.equals(other.groupIds)) {
				return false;
			}
			if (groupMemberships == null) {
				if (other.groupMemberships != null) {
					return false;
				}
			} else if (!groupMemberships.equals(other.groupMemberships)) {
				return false;
			}
			if (groupPropertyDefinitions == null) {
				if (other.groupPropertyDefinitions != null) {
					return false;
				}
			} else if (!groupPropertyDefinitions.equals(other.groupPropertyDefinitions)) {
				return false;
			}
			if (groupPropertyValues == null) {
				if (other.groupPropertyValues != null) {
					return false;
				}
			} else if (!groupPropertyValues.equals(other.groupPropertyValues)) {
				return false;
			}
			if (groupTypeIds == null) {
				if (other.groupTypeIds != null) {
					return false;
				}
			} else if (!groupTypeIds.equals(other.groupTypeIds)) {
				return false;
			}
			if (groupTypes == null) {
				if (other.groupTypes != null) {
					return false;
				}
			} else if (!groupTypes.equals(other.groupTypes)) {
				return false;
			}
			if (materialIds == null) {
				if (other.materialIds != null) {
					return false;
				}
			} else if (!materialIds.equals(other.materialIds)) {
				return false;
			}
			if (materialsProducerIds == null) {
				if (other.materialsProducerIds != null) {
					return false;
				}
			} else if (!materialsProducerIds.equals(other.materialsProducerIds)) {
				return false;
			}
			if (materialsProducerPropertyDefinitions == null) {
				if (other.materialsProducerPropertyDefinitions != null) {
					return false;
				}
			} else if (!materialsProducerPropertyDefinitions.equals(other.materialsProducerPropertyDefinitions)) {
				return false;
			}
			if (materialsProducerPropertyValues == null) {
				if (other.materialsProducerPropertyValues != null) {
					return false;
				}
			} else if (!materialsProducerPropertyValues.equals(other.materialsProducerPropertyValues)) {
				return false;
			}
			if (materialsProducerResourceLevels == null) {
				if (other.materialsProducerResourceLevels != null) {
					return false;
				}
			} else if (!materialsProducerResourceLevels.equals(other.materialsProducerResourceLevels)) {
				return false;
			}
			if (personCompartments == null) {
				if (other.personCompartments != null) {
					return false;
				}
			} else if (!personCompartments.equals(other.personCompartments)) {
				return false;
			}
			if (personIds == null) {
				if (other.personIds != null) {
					return false;
				}
			} else if (!personIds.equals(other.personIds)) {
				return false;
			}
			if (personPropertyDefinitions == null) {
				if (other.personPropertyDefinitions != null) {
					return false;
				}
			} else if (!personPropertyDefinitions.equals(other.personPropertyDefinitions)) {
				return false;
			}
			if (personPropertyValues == null) {
				if (other.personPropertyValues != null) {
					return false;
				}
			} else if (!personPropertyValues.equals(other.personPropertyValues)) {
				return false;
			}
			if (personRegions == null) {
				if (other.personRegions != null) {
					return false;
				}
			} else if (!personRegions.equals(other.personRegions)) {
				return false;
			}
			if (personResourceLevels == null) {
				if (other.personResourceLevels != null) {
					return false;
				}
			} else if (!personResourceLevels.equals(other.personResourceLevels)) {
				return false;
			}
			if (regionArrivalTimeTrackingPolicy != other.regionArrivalTimeTrackingPolicy) {
				return false;
			}
			if (regionIds == null) {
				if (other.regionIds != null) {
					return false;
				}
			} else if (!regionIds.equals(other.regionIds)) {
				return false;
			}
			if (regionPropertyDefinitions == null) {
				if (other.regionPropertyDefinitions != null) {
					return false;
				}
			} else if (!regionPropertyDefinitions.equals(other.regionPropertyDefinitions)) {
				return false;
			}
			if (regionPropertyValues == null) {
				if (other.regionPropertyValues != null) {
					return false;
				}
			} else if (!regionPropertyValues.equals(other.regionPropertyValues)) {
				return false;
			}
			if (regionResourceLevels == null) {
				if (other.regionResourceLevels != null) {
					return false;
				}
			} else if (!regionResourceLevels.equals(other.regionResourceLevels)) {
				return false;
			}
			if (resourceIds == null) {
				if (other.resourceIds != null) {
					return false;
				}
			} else if (!resourceIds.equals(other.resourceIds)) {
				return false;
			}
			if (resourcePropertyDefinitions == null) {
				if (other.resourcePropertyDefinitions != null) {
					return false;
				}
			} else if (!resourcePropertyDefinitions.equals(other.resourcePropertyDefinitions)) {
				return false;
			}
			if (resourcePropertyValues == null) {
				if (other.resourcePropertyValues != null) {
					return false;
				}
			} else if (!resourcePropertyValues.equals(other.resourcePropertyValues)) {
				return false;
			}
			if (resourceTimeTrackingPolicies == null) {
				if (other.resourceTimeTrackingPolicies != null) {
					return false;
				}
			} else if (!resourceTimeTrackingPolicies.equals(other.resourceTimeTrackingPolicies)) {
				return false;
			}

			if (stageBatches == null) {
				if (other.stageBatches != null) {
					return false;
				}
			} else if (!stageBatches.equals(other.stageBatches)) {
				return false;
			}
			if (stageIds == null) {
				if (other.stageIds != null) {
					return false;
				}
			} else if (!stageIds.equals(other.stageIds)) {
				return false;
			}
			if (stageMaterialsProducers == null) {
				if (other.stageMaterialsProducers != null) {
					return false;
				}
			} else if (!stageMaterialsProducers.equals(other.stageMaterialsProducers)) {
				return false;
			}
			if (stageOffers == null) {
				if (other.stageOffers != null) {
					return false;
				}
			} else if (!stageOffers.equals(other.stageOffers)) {
				return false;
			}
			
			return true;
		}

	}

	/*
	 * Private implementor class for Scenario.
	 */
	private final static class ScenarioImpl implements Scenario {

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((scenarioData == null) ? 0 : scenarioData.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ScenarioImpl other = (ScenarioImpl) obj;
			if (scenarioData == null) {
				return other.scenarioData == null;
			} else
				return scenarioData.equals(other.scenarioData);
		}

		private final ScenarioData scenarioData;

		private ScenarioImpl(final ScenarioData scenarioData) {
			this.scenarioData = scenarioData;
		}

		@Override
		public Double getBatchAmount(final BatchId batchId) {
			validateBatchExists(scenarioData, batchId);
			return scenarioData.batchAmounts.get(batchId);
		}

		@Override
		public Set<BatchId> getBatchIds() {
			return new LinkedHashSet<>(scenarioData.batchIds);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getBatchMaterial(final BatchId batchId) {
			validateBatchExists(scenarioData, batchId);
			return (T) scenarioData.batchMaterials.get(batchId);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends MaterialsProducerId> T getBatchMaterialsProducer(final BatchId batchId) {
			validateBatchExists(scenarioData, batchId);
			return (T) scenarioData.batchMaterialsProducers.get(batchId);
		}

		@Override
		public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
			validateMaterialExists(scenarioData, materialId);
			validateBatchPropertyIsDefined(scenarioData, materialId, batchPropertyId);

			final Map<BatchPropertyId, PropertyDefinition> map = scenarioData.batchPropertyDefinitions.get(materialId);
			final PropertyDefinition propertyDefinition = map.get(batchPropertyId);
			return propertyDefinition;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId) {
			validateMaterialExists(scenarioData, materialId);
			final Set<T> result = new LinkedHashSet<>();
			final Map<BatchPropertyId, PropertyDefinition> map = scenarioData.batchPropertyDefinitions.get(materialId);
			if (map != null) {
				final Set<BatchPropertyId> batchPropertyIds = map.keySet();
				for (BatchPropertyId batchPropertyId : batchPropertyIds) {
					result.add((T) batchPropertyId);
				}
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId) {
			validateBatchExists(scenarioData, batchId);

			final MaterialId materialId = scenarioData.batchMaterials.get(batchId);
			validateBatchPropertyIsDefined(scenarioData, materialId, batchPropertyId);

			Object result = null;
			final Map<BatchPropertyId, Object> map = scenarioData.batchPropertyValues.get(batchId);
			if (map != null) {
				result = map.get(batchPropertyId);
			}
			if (result == null) {
				final Map<BatchPropertyId, PropertyDefinition> defMap = scenarioData.batchPropertyDefinitions.get(materialId);
				final PropertyDefinition propertyDefinition = defMap.get(batchPropertyId);
				if (propertyDefinition.getDefaultValue().isPresent()) {
					result = propertyDefinition.getDefaultValue().get();
				}
			}
			return (T) result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends CompartmentId> Set<T> getCompartmentIds() {
			Set<T> result = new LinkedHashSet<>(scenarioData.compartmentIds.keySet().size());
			for (CompartmentId compartmentId : scenarioData.compartmentIds.keySet()) {
				result.add((T) compartmentId);
			}
			return result;
		}

		@Override
		public PropertyDefinition getCompartmentPropertyDefinition(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
			validateCompartmentExists(scenarioData, compartmentId);
			validateCompartmentPropertyIsDefined(scenarioData, compartmentId, compartmentPropertyId);
			return scenarioData.compartmentPropertyDefinitions.get(compartmentId).get(compartmentPropertyId);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends CompartmentPropertyId> Set<T> getCompartmentPropertyIds(final CompartmentId compartmentId) {
			Set<T> result = new LinkedHashSet<>();
			validateCompartmentExists(scenarioData, compartmentId);
			Map<CompartmentPropertyId, PropertyDefinition> map = scenarioData.compartmentPropertyDefinitions.get(compartmentId);
			if (map != null) {
				for (CompartmentPropertyId compartmentPropertyId : map.keySet()) {
					result.add((T) compartmentPropertyId);
				}
			}
			return result;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
			validateCompartmentExists(scenarioData, compartmentId);
			validateCompartmentPropertyIsDefined(scenarioData, compartmentId, compartmentPropertyId);
			Object result = null;
			final Map<CompartmentPropertyId, Object> propertyValueMap = scenarioData.compartmentPropertyValues.get(compartmentId);
			if (propertyValueMap != null) {
				result = propertyValueMap.get(compartmentPropertyId);
			}
			if (result == null) {
				final PropertyDefinition propertyDefinition = scenarioData.compartmentPropertyDefinitions.get(compartmentId).get(compartmentPropertyId);
				if (propertyDefinition.getDefaultValue().isPresent()) {
					result = propertyDefinition.getDefaultValue().get();
				}
			}
			return (T) result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends GlobalComponentId> Set<T> getGlobalComponentIds() {
			Set<T> result = new LinkedHashSet<>(scenarioData.globalComponentIds.keySet().size());
			for (GlobalComponentId globalComponentId : scenarioData.globalComponentIds.keySet()) {
				result.add((T) globalComponentId);
			}
			return result;
		}

		@Override
		public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
			if (globalPropertyId == null) {
				throwNullInputException(ScenarioErrorType.NULL_GLOBAL_PROPERTY_ID);
			}
			final PropertyDefinition propertyDefinition = scenarioData.globalPropertyDefinitions.get(globalPropertyId);
			if (propertyDefinition == null) {
				throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_GLOBAL_PROPERTY_ID, globalPropertyId);
			}
			return propertyDefinition;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds() {
			Set<T> result = new LinkedHashSet<>();
			for (GlobalPropertyId globalPropertyId : scenarioData.globalPropertyDefinitions.keySet()) {
				result.add((T) globalPropertyId);
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getGlobalPropertyValue(final GlobalPropertyId globalPropertyId) {
			validateGlobalPropertyIsDefined(scenarioData, globalPropertyId);
			Object result = scenarioData.globalPropertyValues.get(globalPropertyId);
			if (result == null) {
				final PropertyDefinition propertyDefinition = scenarioData.globalPropertyDefinitions.get(globalPropertyId);
				if (propertyDefinition.getDefaultValue().isPresent()) {
					result = propertyDefinition.getDefaultValue().get();
				}
			}
			return (T) result;
		}

		@Override
		public Set<GroupId> getGroupIds() {
			return new LinkedHashSet<>(scenarioData.groupIds);
		}

		@Override
		public Set<PersonId> getGroupMembers(final GroupId groupId) {
			validateGroupExists(scenarioData, groupId);
			final Set<PersonId> result = new LinkedHashSet<>();
			final Set<PersonId> set = scenarioData.groupMemberships.get(groupId);
			if (set != null) {
				result.addAll(set);
			}
			return result;
		}

		@Override
		public PropertyDefinition getGroupPropertyDefinition(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
			validateGroupTypeExists(scenarioData, groupTypeId);
			validateGroupPropertyIsDefined(scenarioData, groupTypeId, groupPropertyId);

			final Map<GroupPropertyId, PropertyDefinition> map = scenarioData.groupPropertyDefinitions.get(groupTypeId);
			final PropertyDefinition propertyDefinition = map.get(groupPropertyId);
			return propertyDefinition;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId) {
			validateGroupExists(scenarioData, groupId);
			final GroupTypeId groupTypeId = scenarioData.groupTypes.get(groupId);
			validateGroupPropertyIsDefined(scenarioData, groupTypeId, groupPropertyId);

			Object result = null;
			final Map<GroupPropertyId, Object> map = scenarioData.groupPropertyValues.get(groupId);
			if (map != null) {
				result = map.get(groupPropertyId);
			}
			if (result == null) {
				final Map<GroupPropertyId, PropertyDefinition> defMap = scenarioData.groupPropertyDefinitions.get(groupTypeId);
				final PropertyDefinition propertyDefinition = defMap.get(groupPropertyId);
				if (propertyDefinition.getDefaultValue().isPresent()) {
					result = propertyDefinition.getDefaultValue().get();
				}
			}
			return (T) result;

		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends GroupTypeId> T getGroupTypeId(final GroupId groupId) {
			validateGroupExists(scenarioData, groupId);
			final GroupTypeId result = scenarioData.groupTypes.get(groupId);
			return (T) result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends GroupTypeId> Set<T> getGroupTypeIds() {
			Set<T> result = new LinkedHashSet<>(scenarioData.groupTypeIds.size());
			for (GroupTypeId groupTypeId : scenarioData.groupTypeIds) {
				result.add((T) groupTypeId);
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(final GroupTypeId groupTypeId) {
			validateGroupTypeExists(scenarioData, groupTypeId);

			final Set<T> result = new LinkedHashSet<>();
			final Map<GroupPropertyId, PropertyDefinition> map = scenarioData.groupPropertyDefinitions.get(groupTypeId);
			if (map != null) {
				for (GroupPropertyId groupPropertyId : map.keySet()) {
					result.add((T) groupPropertyId);
				}
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends MaterialId> Set<T> getMaterialIds() {
			Set<T> result = new LinkedHashSet<>(scenarioData.materialIds.size());
			for (MaterialId materialId : scenarioData.materialIds) {
				result.add((T) materialId);
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends MaterialsProducerId> Set<T> getMaterialsProducerIds() {
			Set<T> result = new LinkedHashSet<>(scenarioData.materialsProducerIds.keySet().size());
			for (MaterialsProducerId materialsProducerId : scenarioData.materialsProducerIds.keySet()) {
				result.add((T) materialsProducerId);
			}
			return result;
		}

		@Override
		public PropertyDefinition getMaterialsProducerPropertyDefinition(final MaterialsProducerPropertyId materialsProducerPropertyId) {
			if (materialsProducerPropertyId == null) {
				throwNullInputException(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_ID);
			}
			final PropertyDefinition propertyDefinition = scenarioData.materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
			if (propertyDefinition == null) {
				throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, materialsProducerPropertyId);
			}
			return propertyDefinition;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds() {
			Set<T> result = new LinkedHashSet<>();
			for (MaterialsProducerPropertyId materialsProducerPropertyId : scenarioData.materialsProducerPropertyDefinitions.keySet()) {
				result.add((T) materialsProducerPropertyId);
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
			validateMaterialsProducerExists(scenarioData, materialsProducerId);
			validateMaterialsProducerPropertyIsDefined(scenarioData, materialsProducerPropertyId);
			Object result = null;
			final Map<MaterialsProducerPropertyId, Object> map = scenarioData.materialsProducerPropertyValues.get(materialsProducerId);
			if (map != null) {
				result = map.get(materialsProducerPropertyId);
			}
			if (result == null) {
				final PropertyDefinition propertyDefinition = scenarioData.materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
				if (propertyDefinition.getDefaultValue().isPresent()) {
					result = propertyDefinition.getDefaultValue().get();
				}
			}
			return (T) result;
		}

		@Override
		public Long getMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
			validateMaterialsProducerExists(scenarioData, materialsProducerId);
			validateResourceExists(scenarioData, resourceId);
			Long result = null;
			final Map<ResourceId, Long> map = scenarioData.materialsProducerResourceLevels.get(materialsProducerId);
			if (map != null) {
				result = map.get(resourceId);
			}
			if (result == null) {
				result = 0L;
			}
			return result;
		}

		@Override
		public Set<PersonId> getPeopleIds() {
			return new LinkedHashSet<>(scenarioData.personIds);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends CompartmentId> T getPersonCompartment(final PersonId personId) {
			validatePersonExists(scenarioData, personId);
			return (T) scenarioData.personCompartments.get(personId);
		}

		@Override
		public TimeTrackingPolicy getPersonCompartmentArrivalTrackingPolicy() {
			return scenarioData.compartmentArrivalTimeTrackingPolicy;
		}

		@Override
		public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
			if (personPropertyId == null) {
				throwNullInputException(ScenarioErrorType.NULL_PERSON_PROPERTY_ID);
			}
			final PropertyDefinition propertyDefinition = scenarioData.personPropertyDefinitions.get(personPropertyId);
			if (propertyDefinition == null) {
				throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_PERSON_PROPERTY_ID, personPropertyId);
			}
			return propertyDefinition;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends PersonPropertyId> Set<T> getPersonPropertyIds() {
			Set<T> result = new LinkedHashSet<>();
			for (PersonPropertyId personPropertyId : scenarioData.personPropertyDefinitions.keySet()) {
				result.add((T) personPropertyId);
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId) {
			validatePersonExists(scenarioData, personId);
			validatePersonPropertyIsDefined(scenarioData, personPropertyId);
			Object result = null;
			final Map<PersonPropertyId, Object> map = scenarioData.personPropertyValues.get(personId);
			if (map != null) {
				result = map.get(personPropertyId);
			}
			if (result == null) {
				final PropertyDefinition propertyDefinition = scenarioData.personPropertyDefinitions.get(personPropertyId);
				if (propertyDefinition.getDefaultValue().isPresent()) {
					result = propertyDefinition.getDefaultValue().get();
				}
			}
			return (T) result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends RegionId> T getPersonRegion(final PersonId personId) {
			validatePersonExists(scenarioData, personId);
			return (T) scenarioData.personRegions.get(personId);
		}

		@Override
		public TimeTrackingPolicy getPersonRegionArrivalTrackingPolicy() {
			return scenarioData.regionArrivalTimeTrackingPolicy;
		}

		@Override
		public Long getPersonResourceLevel(final PersonId personId, final ResourceId resourceId) {
			validatePersonExists(scenarioData, personId);
			validateResourceExists(scenarioData, resourceId);
			Long result = null;
			final Map<ResourceId, Long> map = scenarioData.personResourceLevels.get(personId);
			if (map != null) {
				result = map.get(resourceId);
			}
			if (result == null) {
				result = 0L;
			}

			return result;
		}

		@Override
		public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(final ResourceId resourceId) {
			validateResourceExists(scenarioData, resourceId);
			TimeTrackingPolicy result = scenarioData.resourceTimeTrackingPolicies.get(resourceId);
			if (result == null) {
				result = TimeTrackingPolicy.DO_NOT_TRACK_TIME;
			}
			return result;
		}

		@Override
		public Set<RegionId> getRegionIds() {
			return new LinkedHashSet<>(scenarioData.regionIds.keySet());
		}

		@Override
		public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId) {
			validateRegionPropertyIsDefined(scenarioData, regionPropertyId);
			final PropertyDefinition propertyDefinition = scenarioData.regionPropertyDefinitions.get(regionPropertyId);
			return propertyDefinition;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends RegionPropertyId> Set<T> getRegionPropertyIds() {
			Set<T> result = new LinkedHashSet<>();
			for (RegionPropertyId regionPropertyId : scenarioData.regionPropertyDefinitions.keySet()) {
				result.add((T) regionPropertyId);
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId) {
			validateRegionExists(scenarioData, regionId);
			validateRegionPropertyIsDefined(scenarioData, regionPropertyId);
			Object result = null;
			final Map<RegionPropertyId, Object> map = scenarioData.regionPropertyValues.get(regionId);
			if (map != null) {
				result = map.get(regionPropertyId);
			}
			if (result == null) {
				final PropertyDefinition propertyDefinition = scenarioData.regionPropertyDefinitions.get(regionPropertyId);
				if (propertyDefinition.getDefaultValue().isPresent()) {
					result = propertyDefinition.getDefaultValue().get();
				}
			}
			return (T) result;
		}

		@Override
		public Long getRegionResourceLevel(final RegionId regionId, final ResourceId resourceId) {
			validateRegionExists(scenarioData, regionId);
			validateResourceExists(scenarioData, resourceId);

			Long result = null;
			final Map<ResourceId, Long> map = scenarioData.regionResourceLevels.get(regionId);

			if (map != null) {
				result = map.get(resourceId);
			}

			if (result == null) {
				result = 0L;
			}

			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends ResourceId> Set<T> getResourceIds() {
			Set<T> result = new LinkedHashSet<>(scenarioData.resourceIds.size());
			for (ResourceId resourceId : scenarioData.resourceIds) {
				result.add((T) resourceId);
			}
			return result;
		}

		@Override
		public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
			validateResourcePropertyIsDefined(scenarioData, resourceId, resourcePropertyId);
			final Map<ResourcePropertyId, PropertyDefinition> defMap = scenarioData.resourcePropertyDefinitions.get(resourceId);
			final PropertyDefinition propertyDefinition = defMap.get(resourcePropertyId);
			return propertyDefinition;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId) {
			Set<T> result = new LinkedHashSet<>();
			Map<ResourcePropertyId, PropertyDefinition> defMap = scenarioData.resourcePropertyDefinitions.get(resourceId);
			if (defMap != null) {
				for (ResourcePropertyId resourcePropertyId : defMap.keySet()) {
					result.add((T) resourcePropertyId);
				}
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
			validateResourceExists(scenarioData, resourceId);
			validateResourcePropertyIsDefined(scenarioData, resourceId, resourcePropertyId);
			Object result = null;
			final Map<ResourcePropertyId, Object> map = scenarioData.resourcePropertyValues.get(resourceId);
			if (map != null) {
				result = map.get(resourcePropertyId);
			}
			if (result == null) {
				final Map<ResourcePropertyId, PropertyDefinition> defMap = scenarioData.resourcePropertyDefinitions.get(resourceId);
				final PropertyDefinition propertyDefinition = defMap.get(resourcePropertyId);
				if (propertyDefinition.getDefaultValue().isPresent()) {
					result = propertyDefinition.getDefaultValue().get();
				}
			}
			return (T) result;
		}

		@Override
		public Set<BatchId> getStageBatches(final StageId stageId) {
			validateStageExists(scenarioData, stageId);
			final Set<BatchId> result = new LinkedHashSet<>();
			final Set<BatchId> set = scenarioData.stageBatches.get(stageId);
			if (set != null) {
				result.addAll(set);
			}
			return result;
		}

		@Override
		public Set<StageId> getStageIds() {
			return new LinkedHashSet<>(scenarioData.stageIds);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getStageMaterialsProducer(final StageId stageId) {
			validateStageExists(scenarioData, stageId);
			return (T) scenarioData.stageMaterialsProducers.get(stageId);
		}

		@Override
		public Boolean isStageOffered(final StageId stageId) {
			validateStageExists(scenarioData, stageId);
			return scenarioData.stageOffers.get(stageId);
		}

		@Override
		public Supplier<Consumer<AgentContext>> getCompartmentInitialBehaviorSupplier(CompartmentId compartmentId) {
			validateCompartmentExists(scenarioData, compartmentId);
			return scenarioData.compartmentIds.get(compartmentId);
		}

		@Override
		public Supplier<Consumer<AgentContext>> getGlobalInitialBehaviorSupplier(GlobalComponentId globalComponentId) {
			validateGlobalComponentExists(scenarioData, globalComponentId);
			return scenarioData.globalComponentIds.get(globalComponentId);
		}

		@Override
		public Supplier<Consumer<AgentContext>> getMaterialsProducerInitialBehaviorSupplier(MaterialsProducerId materialsProducerId) {
			validateMaterialsProducerExists(scenarioData, materialsProducerId);
			return scenarioData.materialsProducerIds.get(materialsProducerId);
		}

		@Override
		public Supplier<Consumer<AgentContext>> getRegionInitialBehaviorSupplier(RegionId regionId) {
			validateRegionExists(scenarioData, regionId);
			return scenarioData.regionIds.get(regionId);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends RandomNumberGeneratorId> Set<T> getRandomNumberGeneratorIds() {
			Set<T> result = new LinkedHashSet<>(scenarioData.randomNumberGeneratorIds.size());
			for (RandomNumberGeneratorId randomNumberGeneratorId : scenarioData.randomNumberGeneratorIds) {
				result.add((T) randomNumberGeneratorId);
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends ReportId> Set<T> getReportIds() {
			Set<T> result = new LinkedHashSet<>(scenarioData.reportIds.keySet().size());
			for (ReportId reportId : scenarioData.reportIds.keySet()) {
				result.add((T) reportId);
			}
			return result;
		}

		@Override
		public Supplier<Consumer<ReportContext>> getReportInitialBehaviorSupplier(ReportId reportId) {
			validateReportExists(scenarioData, reportId);
			return scenarioData.reportIds.get(reportId);
		}

	}

	private static void throwBatchAndStageHaveDifferentOwners(final StageId stageId, final BatchId batchId) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Batch ");
		sb.append(batchId);
		sb.append(" has different materials producer from ");
		sb.append(" stage ");
		sb.append(stageId);
		throw new ScenarioException(ScenarioErrorType.BATCH_STAGED_TO_DIFFERENT_OWNER, sb.toString());
	}

	private static void throwBatchPreviouslyStaged(final BatchId batchId) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Batch ");
		sb.append(batchId);
		sb.append(" was previously staged");
		throw new ScenarioException(ScenarioErrorType.BATCH_ALREADY_STAGED, sb.toString());
	}

	private static void throwIncompatiblePropertyValueException(final ActionType dataType, final Object propertyId, final Object propertyValue) {
		final StringBuilder sb = new StringBuilder();
		sb.append(" Scenario build property ");
		sb.append(dataType);
		sb.append(".");
		sb.append(propertyId);
		sb.append(" with value ");
		sb.append(propertyValue);
		sb.append(" was incompatible with the property's definition");
		throw new ScenarioException(ScenarioErrorType.INCOMPATIBLE_VALUE, sb.toString());
	}

	private static void throwNegativeValueException(ScenarioErrorType scenarioErrorType, final Object value) {
		final StringBuilder sb = new StringBuilder();
		sb.append(scenarioErrorType.getDescription());
		sb.append(": ");
		sb.append(value);
		throw new ScenarioException(scenarioErrorType, sb.toString());
	}

	private static void throwNullInputException(ScenarioErrorType scenarioErrorType) {
		throw new ScenarioException(scenarioErrorType);
	}

	private static void throwPreviouslyAddedException(final ActionType actionType, final Object value) {
		final StringBuilder sb = new StringBuilder();
		sb.append(actionType);
		sb.append(" identifier ");
		sb.append(value);
		sb.append(" was previously added ");
		throw new ScenarioException(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, sb.toString());
	}

	private static void throwPreviouslyAssignedValueException(final ActionType actionType, final Object propertyId, final Object identifier) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Property value for ");
		sb.append(actionType);
		sb.append(".");
		sb.append(propertyId);
		sb.append(" was previously assigned for ");
		sb.append(identifier);
		throw new ScenarioException(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, sb.toString());
	}

	private static void throwPreviouslyAssignedValueException(final String description) {
		final StringBuilder sb = new StringBuilder();
		sb.append(description);
		sb.append(" was previously assigned a value");
		throw new ScenarioException(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, sb.toString());
	}

	private static void throwPreviouslyDefinedException(ScenarioErrorType scenarioErrorType, final Object propertyId) {
		final StringBuilder sb = new StringBuilder();
		sb.append(scenarioErrorType.getDescription());
		sb.append(": property ");
		sb.append(propertyId);
		sb.append(" was previously defined ");
		throw new ScenarioException(scenarioErrorType, sb.toString());
	}

	private static void throwUnknownIdentifierException(final ScenarioErrorType scenarioErrorType, final Object value) {
		final StringBuilder sb = new StringBuilder();
		sb.append(scenarioErrorType.getDescription());
		sb.append(": unknown value ");
		sb.append(value);
		throw new ScenarioException(scenarioErrorType, sb.toString());
	}

	private static void throwInsufficientPropertyValueAssignment(Object propertyId) {
		throw new ScenarioException(ScenarioErrorType.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, propertyId.getClass().getSimpleName() + "." + propertyId);
	}

	private static void throwNullDefaultValue(Object propertyId) {
		throw new ScenarioException(ScenarioErrorType.NULL_DEFAULT_VALUE, propertyId.getClass().getSimpleName() + "." + propertyId);
	}

	private static void throwNonFiniteValueException(ScenarioErrorType scenarioErrorType, final Object value) {
		final StringBuilder sb = new StringBuilder();
		sb.append(scenarioErrorType.getDescription());
		sb.append(": ");
		sb.append(value);
		throw new ScenarioException(scenarioErrorType, sb.toString());
	}

	private static void validateBatchAmount(final ScenarioData scenarioData, final double amount) {
		if (!Double.isFinite(amount)) {
			throwNonFiniteValueException(ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT, amount);
		}
		if (amount < 0) {
			throwNegativeValueException(ScenarioErrorType.NEGATIVE_MATERIAL_AMOUNT, amount);
		}

	}

	/*
	 * Batch and stage must exist
	 */
	private static void validateBatchAndStageShareMaterialsProducer(final ScenarioData scenarioData, final StageId stageId, final BatchId batchId) {
		final MaterialsProducerId batchMaterialsProducer = scenarioData.batchMaterialsProducers.get(batchId);
		final MaterialsProducerId stageMaterialsProducer = scenarioData.stageMaterialsProducers.get(stageId);
		if (!batchMaterialsProducer.equals(stageMaterialsProducer)) {
			throwBatchAndStageHaveDifferentOwners(stageId, batchId);
		}
	}

	private static void validateBatchDoesNotExist(final ScenarioData scenarioData, final Object batchId) {
		if (scenarioData.batchIds.contains(batchId)) {
			throwPreviouslyAddedException(ActionType.BATCH_ID_ADDITION, batchId);
		}
	}

	private static void validateBatchExists(final ScenarioData scenarioData, final Object batchId) {
		if (batchId == null) {
			throwNullInputException(ScenarioErrorType.NULL_BATCH_ID);
		}
		if (!scenarioData.batchIds.contains(batchId)) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_BATCH_ID, batchId);
		}
	}

	private static void validateBatchNotStaged(final ScenarioData scenarioData, final BatchId batchId) {
		if (scenarioData.batchStages.get(batchId) != null) {
			throwBatchPreviouslyStaged(batchId);
		}
	}

	private static void validateBatchPropertyIsDefined(final ScenarioData scenarioData, final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		validateNotNull(ScenarioErrorType.NULL_BATCH_PROPERTY_ID, batchPropertyId);
		final Map<BatchPropertyId, PropertyDefinition> map = scenarioData.batchPropertyDefinitions.get(materialId);
		if (map == null) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_BATCH_PROPERTY_ID, batchPropertyId);
		}
		final PropertyDefinition propertyDefinition = map.get(batchPropertyId);
		if (propertyDefinition == null) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_BATCH_PROPERTY_ID, batchPropertyId);
		}
	}

	private static void validateBatchPropertyIsNotDefined(final ScenarioData scenarioData, final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		final Map<BatchPropertyId, PropertyDefinition> map = scenarioData.batchPropertyDefinitions.get(materialId);
		if (map != null) {
			final PropertyDefinition propertyDefinition = map.get(batchPropertyId);
			if (propertyDefinition != null) {
				throwPreviouslyDefinedException(ScenarioErrorType.DUPLICATE_BATCH_PROPERTY_DEFINITION, batchPropertyId);
			}
		}
	}

	private static void validateBatchPropertyValueNotSet(final ScenarioData scenarioData, final BatchId batchId, final BatchPropertyId batchPropertyId) {
		final Map<BatchPropertyId, Object> propertyMap = scenarioData.batchPropertyValues.get(batchId);
		if (propertyMap != null) {
			if (propertyMap.containsKey(batchPropertyId)) {
				throwPreviouslyAssignedValueException(ActionType.BATCH_PROPERTY_VALUE_ASSIGNMENT, batchPropertyId, batchId);
			}
		}
	}

	private static void validateGlobalComponentExists(final ScenarioData scenarioData, final GlobalComponentId globalComponentId) {
		if (globalComponentId == null) {
			throwNullInputException(ScenarioErrorType.NULL_GLOBAL_COMPONENT_ID);
		}
		if (!scenarioData.globalComponentIds.containsKey(globalComponentId)) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_GLOBAL_COMPONENT_ID, globalComponentId);
		}
	}

	private static void validateCompartmentPropertyIsDefined(final ScenarioData scenarioData, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		validateNotNull(ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_ID, compartmentPropertyId);
		Map<CompartmentPropertyId, PropertyDefinition> map = scenarioData.compartmentPropertyDefinitions.get(compartmentId);
		if (map == null) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_COMPARTMENT_PROPERTY_ID, compartmentPropertyId);
		}
		final PropertyDefinition propertyDefinition = map.get(compartmentPropertyId);
		if (propertyDefinition == null) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_COMPARTMENT_PROPERTY_ID, compartmentPropertyId);
		}
	}

	private static void validateCompartmentPropertyIsNotDefined(final ScenarioData scenarioData, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		Map<CompartmentPropertyId, PropertyDefinition> map = scenarioData.compartmentPropertyDefinitions.get(compartmentId);
		if (map != null) {
			final PropertyDefinition propertyDefinition = map.get(compartmentPropertyId);
			if (propertyDefinition != null) {
				throwPreviouslyDefinedException(ScenarioErrorType.DUPLICATE_COMPARTMENT_PROPERTY_DEFINITION, compartmentPropertyId);
			}
		}
	}

	private static void validateCompartmentPropertyValueNotSet(final ScenarioData scenarioData, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		final Map<CompartmentPropertyId, Object> propertyMap = scenarioData.compartmentPropertyValues.get(compartmentId);
		if (propertyMap != null) {
			if (propertyMap.containsKey(compartmentPropertyId)) {
				throwPreviouslyAssignedValueException(ActionType.COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT, compartmentPropertyId, compartmentId);
			}
		}
	}

	private static void validateGlobalPropertyIsDefined(final ScenarioData scenarioData, final GlobalPropertyId globalPropertyId) {
		validateNotNull(ScenarioErrorType.NULL_GLOBAL_PROPERTY_ID, globalPropertyId);
		final PropertyDefinition propertyDefinition = scenarioData.globalPropertyDefinitions.get(globalPropertyId);
		if (propertyDefinition == null) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_GLOBAL_PROPERTY_ID, globalPropertyId);
		}
	}

	private static void validateGlobalPropertyIsNotDefined(final ScenarioData scenarioData, final GlobalPropertyId globalPropertyId) {
		final PropertyDefinition propertyDefinition = scenarioData.globalPropertyDefinitions.get(globalPropertyId);
		if (propertyDefinition != null) {
			throwPreviouslyDefinedException(ScenarioErrorType.DUPLICATE_GLOBAL_PROPERTY_DEFINITION, globalPropertyId);
		}
	}

	private static void validateGlobalPropertyValueNotSet(final ScenarioData scenarioData, final GlobalPropertyId globalPropertyId) {
		if (scenarioData.globalPropertyValues.containsKey(globalPropertyId)) {
			throwPreviouslyAssignedValueException(ActionType.GLOBAL_PROPERTY_VALUE_ASSIGNMENT, globalPropertyId, globalPropertyId);
		}
	}

	private static void validateGroupDoesNotExist(final ScenarioData scenarioData, final GroupId groupId) {
		if (scenarioData.groupIds.contains(groupId)) {
			throwPreviouslyAddedException(ActionType.GROUP_ID_ADDITION, groupId);
		}
	}

	private static void validateGroupExists(final ScenarioData scenarioData, final GroupId groupId) {
		if (groupId == null) {
			throwNullInputException(ScenarioErrorType.NULL_GROUP_ID);
		}
		if (!scenarioData.groupIds.contains(groupId)) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_GROUP_ID, groupId);
		}
	}

	private static void validateGroupPropertyIsDefined(final ScenarioData scenarioData, final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		validateNotNull(ScenarioErrorType.NULL_GROUP_PROPERTY_ID, groupPropertyId);
		final Map<GroupPropertyId, PropertyDefinition> map = scenarioData.groupPropertyDefinitions.get(groupTypeId);
		if (map == null) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_GROUP_PROPERTY_ID, groupPropertyId);
		}
		final PropertyDefinition propertyDefinition = map.get(groupPropertyId);
		if (propertyDefinition == null) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_GROUP_PROPERTY_ID, groupPropertyId);
		}
	}

	private static void validateGroupPropertyIsNotDefined(final ScenarioData scenarioData, final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		final Map<GroupPropertyId, PropertyDefinition> map = scenarioData.groupPropertyDefinitions.get(groupTypeId);
		if (map != null) {
			final PropertyDefinition propertyDefinition = map.get(groupPropertyId);
			if (propertyDefinition != null) {
				throwPreviouslyDefinedException(ScenarioErrorType.DUPLICATE_GROUP_PROPERTY_DEFINITION, groupTypeId + ":" + groupPropertyId);
			}
		}
	}

	private static void validateGroupPropertyValueNotSet(final ScenarioData scenarioData, final GroupId groupId, final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		final Map<GroupPropertyId, Object> propertyMap = scenarioData.groupPropertyValues.get(groupId);
		if (propertyMap != null) {
			if (propertyMap.containsKey(groupPropertyId)) {
				throwPreviouslyAssignedValueException(ActionType.GROUP_PROPERTY_VALUE_ASSIGNMENT, groupTypeId + "." + groupPropertyId, groupId);
			}
		}
	}

	private static void validateGroupTypeDoesNotExist(final ScenarioData scenarioData, final GroupTypeId groupTypeId) {

		if (scenarioData.groupTypeIds.contains(groupTypeId)) {
			throwPreviouslyAddedException(ActionType.GROUP_TYPE_ID_ADDITION, groupTypeId);
		}
	}

	private static void validateGroupTypeExists(final ScenarioData scenarioData, final GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throwNullInputException(ScenarioErrorType.NULL_GROUP_TYPE_ID);
		}
		if (!scenarioData.groupTypeIds.contains(groupTypeId)) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}
	}

	private static void validateMaterialDoesNotExist(final ScenarioData scenarioData, final MaterialId materialId) {

		if (scenarioData.materialIds.contains(materialId)) {
			throwPreviouslyAddedException(ActionType.MATERIAL_ID_ADDITION, materialId);
		}
	}

	private static void validateRandomNumberGeneratorIdDoesNotExist(final ScenarioData scenarioData, final RandomNumberGeneratorId randomNumberGeneratorId) {

		if (scenarioData.randomNumberGeneratorIds.contains(randomNumberGeneratorId)) {
			throwPreviouslyAddedException(ActionType.RANDOM_NUMBER_GENERATOR_ID_ADDITION, randomNumberGeneratorId);
		}
	}

	private static void validateMaterialExists(final ScenarioData scenarioData, final MaterialId materialId) {
		if (materialId == null) {
			throwNullInputException(ScenarioErrorType.NULL_MATERIAL_ID);
		}
		if (!scenarioData.materialIds.contains(materialId)) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_MATERIAL_ID, materialId);
		}
	}

	private static void validateMaterialsProducerExists(final ScenarioData scenarioData, final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			throwNullInputException(ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID);
		}
		if (!scenarioData.materialsProducerIds.containsKey(materialsProducerId)) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID, materialsProducerId);
		}
	}

	private static void validateMaterialsProducerPropertyIsDefined(final ScenarioData scenarioData, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		validateNotNull(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_ID, materialsProducerPropertyId);
		final PropertyDefinition propertyDefinition = scenarioData.materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
		if (propertyDefinition == null) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, materialsProducerPropertyId);
		}
	}

	private static void validateMaterialsProducerPropertyIsNotDefined(final ScenarioData scenarioData, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		final PropertyDefinition propertyDefinition = scenarioData.materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
		if (propertyDefinition != null) {
			throwPreviouslyDefinedException(ScenarioErrorType.DUPLICATE_MATERIALS_PRODUCER_PROPERTY_DEFINITION, materialsProducerPropertyId);
		}
	}

	private static void validateMaterialsProducerPropertyNotSet(final ScenarioData scenarioData, final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		final Map<MaterialsProducerPropertyId, Object> propertyMap = scenarioData.materialsProducerPropertyValues.get(materialsProducerId);
		if (propertyMap != null) {
			if (propertyMap.containsKey(materialsProducerPropertyId)) {
				throwPreviouslyAssignedValueException(ActionType.MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT, materialsProducerPropertyId, materialsProducerId);
			}
		}
	}

	private static void validateMaterialsProducerResourceLevelNotSet(final ScenarioData scenarioData, final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		final Map<ResourceId, Long> resourceLevelMap = scenarioData.materialsProducerResourceLevels.get(materialsProducerId);
		if (resourceLevelMap != null) {
			if (resourceLevelMap.containsKey(resourceId)) {
				throwPreviouslyAssignedValueException(ActionType.MATERIALS_PRODUCER_RESOURCE_ASSIGNMENT, resourceId, materialsProducerId);
			}
		}
	}

	private static void validateNotNull(ScenarioErrorType scenarioErrorType, final Object value) {
		if (value == null) {
			throwNullInputException(scenarioErrorType);
		}
	}

	private static void validatePersonDoesNotExist(final ScenarioData scenarioData, final PersonId personId) {
		if (scenarioData.personIds.contains(personId)) {
			throwPreviouslyAddedException(ActionType.PERSON_ID_ADDITION, personId);
		}
	}

	private static void validatePersonExists(final ScenarioData scenarioData, final PersonId personId) {
		if (personId == null) {
			throwNullInputException(ScenarioErrorType.NULL_PERSON_ID);
		}
		if (!scenarioData.personIds.contains(personId)) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_PERSON_ID, personId);
		}
	}

	private static void validatePersonNotInGroup(final ScenarioData scenarioData, final GroupId groupId, final PersonId personId) {
		final Set<PersonId> people = scenarioData.groupMemberships.get(groupId);
		if (people != null) {
			if (people.contains(personId)) {
				throwPreviouslyAddedGroupMembershipException(groupId, personId);
			}
		}
	}

	private static void throwPreviouslyAddedGroupMembershipException(final GroupId groupId, final PersonId personId) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Person ");
		sb.append(personId);
		sb.append(" was previously added to group ");
		sb.append(groupId);
		throw new ScenarioException(ScenarioErrorType.DUPLICATE_GROUP_MEMBERSHIP, sb.toString());
	}

	private static void validatePersonPropertyIsDefined(final ScenarioData scenarioData, final PersonPropertyId personPropertyId) {
		validateNotNull(ScenarioErrorType.NULL_PERSON_PROPERTY_ID, personPropertyId);
		final PropertyDefinition propertyDefinition = scenarioData.personPropertyDefinitions.get(personPropertyId);
		if (propertyDefinition == null) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_PERSON_PROPERTY_ID, personPropertyId);
		}
	}

	private static void validatePersonPropertyIsNotDefined(final ScenarioData scenarioData, final PersonPropertyId personPropertyId) {
		final PropertyDefinition propertyDefinition = scenarioData.personPropertyDefinitions.get(personPropertyId);
		if (propertyDefinition != null) {
			throwPreviouslyDefinedException(ScenarioErrorType.DUPLICATE_PERSON_PROPERTY_DEFINITION, personPropertyId);
		}
	}

	private static void validatePersonPropertyNotAssigned(final ScenarioData scenarioData, final PersonId personId, final PersonPropertyId personPropertyId) {
		final Map<PersonPropertyId, Object> propertyMap = scenarioData.personPropertyValues.get(personId);
		if (propertyMap != null) {
			if (propertyMap.containsKey(personPropertyId)) {
				throwPreviouslyAssignedValueException(ActionType.PERSON_PROPERTY_VALUE_ASSIGNMENT, personPropertyId, personId);
			}
		}
	}

	private static void validatePersonResourceLevelNotSet(final ScenarioData scenarioData, final PersonId personId, final ResourceId resourceId) {
		final Map<ResourceId, Long> resourceLevelMap = scenarioData.personResourceLevels.get(personId);
		if (resourceLevelMap != null) {
			if (resourceLevelMap.containsKey(resourceId)) {
				throwPreviouslyAssignedValueException(ActionType.PERSON_RESOURCE_ASSIGNMENT, resourceId, personId);
			}
		}
	}

	private static void validateRegionExists(final ScenarioData scenarioData, final RegionId regionId) {
		if (regionId == null) {
			throwNullInputException(ScenarioErrorType.NULL_REGION_ID);
		}
		if (!scenarioData.regionIds.containsKey(regionId)) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_REGION_ID, regionId);
		}
	}

	private static void validateCompartmentExists(final ScenarioData scenarioData, final CompartmentId compartmentId) {
		if (compartmentId == null) {
			throwNullInputException(ScenarioErrorType.NULL_COMPARTMENT_ID);
		}
		if (!scenarioData.compartmentIds.containsKey(compartmentId)) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_COMPARTMENT_ID, compartmentId);
		}
	}
	
	private static void validateReportExists(final ScenarioData scenarioData, final ReportId reportId) {
		if (reportId == null) {
			throwNullInputException(ScenarioErrorType.NULL_REPORT_IDENTIFIER);
		}
		if (!scenarioData.reportIds.containsKey(reportId)) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_REPORT_ID, reportId);
		}
	}

	private static void validateRegionPropertyIsDefined(final ScenarioData scenarioData, final RegionPropertyId regionPropertyId) {
		validateNotNull(ScenarioErrorType.NULL_REGION_PROPERTY_ID, regionPropertyId);
		final PropertyDefinition propertyDefinition = scenarioData.regionPropertyDefinitions.get(regionPropertyId);
		if (propertyDefinition == null) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_REGION_PROPERTY_ID, regionPropertyId);
		}
	}

	private static void validateRegionPropertyIsNotDefined(final ScenarioData scenarioData, final RegionPropertyId regionPropertyId) {
		final PropertyDefinition propertyDefinition = scenarioData.regionPropertyDefinitions.get(regionPropertyId);
		if (propertyDefinition != null) {
			throwPreviouslyDefinedException(ScenarioErrorType.DUPLICATE_REGION_PROPERTY_DEFINITION, regionPropertyId);
		}
	}

	private static void validateRegionPropertyValueNotSet(final ScenarioData scenarioData, final RegionId regionId, final RegionPropertyId regionPropertyId) {
		final Map<RegionPropertyId, Object> propertyMap = scenarioData.regionPropertyValues.get(regionId);
		if (propertyMap != null) {
			if (propertyMap.containsKey(regionPropertyId)) {
				throwPreviouslyAssignedValueException(ActionType.REGION_PROPERTY_VALUE_ASSIGNMENT, regionPropertyId, regionId);
			}
		}
	}

	private static void validateRegionResourceNotSet(final ScenarioData scenarioData, final RegionId regionId, final ResourceId resourceId) {
		final Map<ResourceId, Long> resourceLevelMap = scenarioData.regionResourceLevels.get(regionId);
		if (resourceLevelMap != null) {
			if (resourceLevelMap.containsKey(resourceId)) {
				throwPreviouslyAssignedValueException(ActionType.REGION_RESOURCE_ASSIGNMENT, resourceId, regionId);
			}
		}
	}

	private static void validateResourceAmount(final ScenarioData scenarioData, final long amount) {
		if (amount < 0) {
			throwNegativeValueException(ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT, amount);
		}
	}

	private static void validateResourceDoesNotExist(final ScenarioData scenarioData, final ResourceId resourceId) {
		if (scenarioData.resourceIds.contains(resourceId)) {
			throwPreviouslyAddedException(ActionType.RESOURCE_ID_ADDITION, resourceId);
		}
	}

	private static void validateResourceExists(final ScenarioData scenarioData, final ResourceId resourceId) {
		if (resourceId == null) {
			throwNullInputException(ScenarioErrorType.NULL_RESOURCE_ID);
		}
		if (!scenarioData.resourceIds.contains(resourceId)) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	private static void validateResourcePropertyIsDefined(final ScenarioData scenarioData, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		if (resourcePropertyId == null) {
			throwNullInputException(ScenarioErrorType.NULL_RESOURCE_PROPERTY_ID);
		}
		Map<ResourcePropertyId, PropertyDefinition> map = scenarioData.resourcePropertyDefinitions.get(resourceId);
		if (map == null) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_RESOURCE_PROPERTY_ID, resourceId);
		}

		final PropertyDefinition propertyDefinition = map.get(resourcePropertyId);
		if (propertyDefinition == null) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_RESOURCE_PROPERTY_ID, resourcePropertyId);
		}
	}

	private static void validateResourcePropertyIsNotDefined(final ScenarioData scenarioData, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		final Map<ResourcePropertyId, PropertyDefinition> defMap = scenarioData.resourcePropertyDefinitions.get(resourceId);
		if (defMap != null) {
			final PropertyDefinition propertyDefinition = defMap.get(resourcePropertyId);
			if (propertyDefinition != null) {
				throwPreviouslyDefinedException(ScenarioErrorType.DUPLICATE_RESOURCE_PROPERTY_DEFINITION, resourcePropertyId);
			}
		}
	}

	private static void validateResourcePropertyValueNotSet(final ScenarioData scenarioData, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		final Map<ResourcePropertyId, Object> propertyMap = scenarioData.resourcePropertyValues.get(resourceId);
		if (propertyMap != null) {
			if (propertyMap.containsKey(resourcePropertyId)) {
				throwPreviouslyAssignedValueException(ActionType.RESOURCE_PROPERTY_VALUE_ASSIGNMENT, resourcePropertyId, resourceId);
			}
		}
	}

	private static void validateResourceTimeTrackingNotSet(final ScenarioData scenarioData, final ResourceId resourceId) {
		if (scenarioData.resourceTimeTrackingPolicies.get(resourceId) != null) {
			throwPreviouslyAssignedValueException("resource time tracking for " + resourceId);
		}
	}

	private static void validateStageDoesNotExist(final ScenarioData scenarioData, final Object stageId) {

		if (scenarioData.stageIds.contains(stageId)) {
			throwPreviouslyAddedException(ActionType.STAGE_ID_ADDITION, stageId);
		}
	}

	private static void validateStageExists(final ScenarioData scenarioData, final Object stageId) {
		if (stageId == null) {
			throwNullInputException(ScenarioErrorType.NULL_STAGE_ID);
		}
		if (!scenarioData.stageIds.contains(stageId)) {
			throwUnknownIdentifierException(ScenarioErrorType.UNKNOWN_STAGE_ID, stageId);
		}
	}

	private static void validateUniqueComponentIdentifier(final ScenarioData scenarioData, final ActionType actionType, final ComponentId componentId) {
		validateNotNull(ScenarioErrorType.NULL_COMPONENT_IDENTIFIER, componentId);

		boolean duplicateComponentIdentifier = scenarioData.globalComponentIds.containsKey(componentId);

		duplicateComponentIdentifier |= scenarioData.regionIds.containsKey(componentId);

		duplicateComponentIdentifier |= scenarioData.compartmentIds.containsKey(componentId);

		duplicateComponentIdentifier |= scenarioData.materialsProducerIds.containsKey(componentId);

		if (duplicateComponentIdentifier) {
			throwPreviouslyAddedException(actionType, componentId);
		}
	}

	private static void validateUniqueReportIdentifier(final ScenarioData scenarioData, final ReportId reportId) {
		validateNotNull(ScenarioErrorType.NULL_REPORT_IDENTIFIER, reportId);

		if (scenarioData.reportIds.containsKey(reportId)) {
			throw new ScenarioException(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, reportId.toString());
		}
	}

	/*
	 * Only the value may be null.
	 */
	private static void validateValueCompatibility(final ScenarioData scenarioData, ActionType actionType, final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		switch (actionType) {
		case BATCH_PROPERTY_VALUE_ASSIGNMENT:
			validateNotNull(ScenarioErrorType.NULL_BATCH_PROPERTY_VALUE, propertyValue);
			break;
		case COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT:
			validateNotNull(ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_VALUE, propertyValue);
			break;
		case GLOBAL_PROPERTY_VALUE_ASSIGNMENT:
			validateNotNull(ScenarioErrorType.NULL_GLOBAL_PROPERTY_VALUE, propertyValue);
			break;
		case GROUP_PROPERTY_VALUE_ASSIGNMENT:
			validateNotNull(ScenarioErrorType.NULL_GROUP_PROPERTY_VALUE, propertyValue);
			break;
		case MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT:
			validateNotNull(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_VALUE, propertyValue);
			break;
		case PERSON_PROPERTY_VALUE_ASSIGNMENT:
			validateNotNull(ScenarioErrorType.NULL_PERSON_PROPERTY_VALUE, propertyValue);
			break;
		case REGION_PROPERTY_VALUE_ASSIGNMENT:
			validateNotNull(ScenarioErrorType.NULL_REGION_PROPERTY_VALUE, propertyValue);
			break;
		case RESOURCE_PROPERTY_VALUE_ASSIGNMENT:
			validateNotNull(ScenarioErrorType.NULL_RESOURCE_PROPERTY_VALUE, propertyValue);
			break;
		default:
			throw new RuntimeException("unhandled property type case " + actionType);
		}

		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throwIncompatiblePropertyValueException(actionType, propertyId, propertyValue);
		}
	}

	private ScenarioData scenarioData = new ScenarioData();

	@Override
	public ScenarioBuilder addBatch(final BatchId batchId, final MaterialId materialId, final double amount, final MaterialsProducerId materialsProducerId) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_BATCH_ID, batchId);
			validateBatchDoesNotExist(scenarioData, batchId);
			validateMaterialExists(scenarioData, materialId);
			validateBatchAmount(scenarioData, amount);
			validateMaterialsProducerExists(scenarioData, materialsProducerId);
			scenarioData.batchIds.add(batchId);
			scenarioData.batchMaterials.put(batchId, materialId);
			scenarioData.batchMaterialsProducers.put(batchId, materialsProducerId);
			scenarioData.batchAmounts.put(batchId, amount);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder addBatchToStage(final StageId stageId, final BatchId batchId) {
		acquireLock();
		try {
			validateStageExists(scenarioData, stageId);
			validateBatchExists(scenarioData, batchId);
			validateBatchAndStageShareMaterialsProducer(scenarioData, stageId, batchId);
			validateBatchNotStaged(scenarioData, batchId);

			Set<BatchId> batches = scenarioData.stageBatches.get(stageId);
			if (batches == null) {
				batches = new LinkedHashSet<>();
				scenarioData.stageBatches.put(stageId, batches);
			}
			batches.add(batchId);
			scenarioData.batchStages.put(batchId, stageId);

		} finally {
			releaseLock();
		}
		return this;
	}

	private static void validateSupplier(Supplier<Consumer<AgentContext>> supplier) {
		if (supplier == null) {
			throwSupplierException();
		}
	}

	private static void validateReportSupplier(Supplier<Consumer<ReportContext>> supplier) {
		if (supplier == null) {
			throwSupplierException();
		}
	}

	private static void throwSupplierException() {
		throw new ScenarioException(ScenarioErrorType.NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER);
	}

	@Override
	public ScenarioBuilder addCompartmentId(final CompartmentId compartmentId, Supplier<Consumer<AgentContext>> supplier) {
		acquireLock();
		try {
			validateUniqueComponentIdentifier(scenarioData, ActionType.COMPARTMENT_COMPONENT_ID_ADDITION, compartmentId);
			validateSupplier(supplier);
			scenarioData.compartmentIds.put(compartmentId, supplier);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder addGlobalComponentId(final GlobalComponentId globalComponentId, Supplier<Consumer<AgentContext>> supplier) {
		acquireLock();
		try {
			validateUniqueComponentIdentifier(scenarioData, ActionType.GLOBAL_COMPONENT_ID_ADDITION, globalComponentId);
			validateSupplier(supplier);
			scenarioData.globalComponentIds.put(globalComponentId, supplier);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder addGroup(final GroupId groupId, final GroupTypeId groupTypeId) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_GROUP_ID, groupId);
			validateGroupTypeExists(scenarioData, groupTypeId);
			validateGroupDoesNotExist(scenarioData, groupId);
			scenarioData.groupIds.add(groupId);
			scenarioData.groupTypes.put(groupId, groupTypeId);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder addGroupTypeId(final GroupTypeId groupTypeId) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_GROUP_TYPE_ID, groupTypeId);
			validateGroupTypeDoesNotExist(scenarioData, groupTypeId);
			scenarioData.groupTypeIds.add(groupTypeId);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder addMaterial(final MaterialId materialId) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_MATERIAL_ID, materialId);
			validateMaterialDoesNotExist(scenarioData, materialId);
			scenarioData.materialIds.add(materialId);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder addMaterialsProducerId(final MaterialsProducerId materialsProducerId, Supplier<Consumer<AgentContext>> supplier) {
		acquireLock();
		try {
			validateUniqueComponentIdentifier(scenarioData, ActionType.MATERIALS_PRODUCER_COMPONENT_ID_ADDITION, materialsProducerId);
			validateSupplier(supplier);
			scenarioData.materialsProducerIds.put(materialsProducerId, supplier);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder addPerson(final PersonId personId, final RegionId regionId, final CompartmentId compartmentId) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_PERSON_ID, personId);
			validateCompartmentExists(scenarioData, compartmentId);
			validateRegionExists(scenarioData, regionId);
			validatePersonDoesNotExist(scenarioData, personId);
			scenarioData.personIds.add(personId);
			scenarioData.personCompartments.put(personId, compartmentId);
			scenarioData.personRegions.put(personId, regionId);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder addPersonToGroup(final GroupId groupId, final PersonId personId) {
		acquireLock();
		try {
			validateGroupExists(scenarioData, groupId);
			validatePersonExists(scenarioData, personId);
			validatePersonNotInGroup(scenarioData, groupId, personId);
			Set<PersonId> people = scenarioData.groupMemberships.get(groupId);
			if (people == null) {
				people = new LinkedHashSet<>();
				scenarioData.groupMemberships.put(groupId, people);
			}
			people.add(personId);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder addRegionId(final RegionId regionId, Supplier<Consumer<AgentContext>> supplier) {
		acquireLock();
		try {
			validateUniqueComponentIdentifier(scenarioData, ActionType.REGION_COMPONENT_ID_ADDITION, regionId);
			validateSupplier(supplier);
			scenarioData.regionIds.put(regionId, supplier);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder addResource(final ResourceId resourceId) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_RESOURCE_ID, resourceId);
			validateResourceDoesNotExist(scenarioData, resourceId);
			scenarioData.resourceIds.add(resourceId);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder addStage(final StageId stageId, final boolean offered, final MaterialsProducerId materialsProducerId) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_STAGE_ID, stageId);
			validateMaterialsProducerExists(scenarioData, materialsProducerId);
			validateStageDoesNotExist(scenarioData, stageId);
			scenarioData.stageIds.add(stageId);
			scenarioData.stageMaterialsProducers.put(stageId, materialsProducerId);
			scenarioData.stageOffers.put(stageId, offered);
		} finally {
			releaseLock();
		}
		return this;
	}

	private boolean locked;

	@Override
	public Scenario build() {

		try {

			if (scenarioData.compartmentArrivalTimeTrackingPolicy == null) {
				scenarioData.compartmentArrivalTimeTrackingPolicy = TimeTrackingPolicy.DO_NOT_TRACK_TIME;
			}

			if (scenarioData.regionArrivalTimeTrackingPolicy == null) {
				scenarioData.regionArrivalTimeTrackingPolicy = TimeTrackingPolicy.DO_NOT_TRACK_TIME;
			}

			for (final ResourceId resourceId : scenarioData.resourceIds) {
				final TimeTrackingPolicy timeTrackingPolicy = scenarioData.resourceTimeTrackingPolicies.get(resourceId);
				if (timeTrackingPolicy == null) {
					scenarioData.resourceTimeTrackingPolicies.put(resourceId, TimeTrackingPolicy.DO_NOT_TRACK_TIME);
				}
			}

			/*
			 * For every global property definition that has a null default
			 * value, ensure that there is a corresponding global property value
			 * assignment and put that initial assignment on the property
			 * definition and repair the definition.
			 */
			for (GlobalPropertyId globalPropertyId : scenarioData.globalPropertyDefinitions.keySet()) {
				PropertyDefinition propertyDefinition = scenarioData.globalPropertyDefinitions.get(globalPropertyId);
				if (!propertyDefinition.getDefaultValue().isPresent()) {
					Object propertyValue = scenarioData.globalPropertyValues.get(globalPropertyId);
					if (propertyValue == null) {
						throwInsufficientPropertyValueAssignment(globalPropertyId);
					}
				}
			}

			/*
			 * For every compartment property definition that has a null default
			 * value, ensure that there is a corresponding compartment property
			 * value assignment and repair the definition.
			 */
			for (CompartmentId compartmentId : scenarioData.compartmentIds.keySet()) {
				Map<CompartmentPropertyId, PropertyDefinition> propertyDefinitions = scenarioData.compartmentPropertyDefinitions.get(compartmentId);
				if (propertyDefinitions != null) {
					for (CompartmentPropertyId compartmentPropertyId : propertyDefinitions.keySet()) {
						PropertyDefinition propertyDefinition = propertyDefinitions.get(compartmentPropertyId);
						if (!propertyDefinition.getDefaultValue().isPresent()) {
							Map<CompartmentPropertyId, Object> compartmentPropertyMap = scenarioData.compartmentPropertyValues.get(compartmentId);
							Object propertyValue = null;
							if (compartmentPropertyMap != null) {
								propertyValue = compartmentPropertyMap.get(compartmentPropertyId);
							}
							if (propertyValue == null) {
								throwInsufficientPropertyValueAssignment(compartmentId);
							}
						}
					}
				}
			}
			/*
			 * For every materials producer property definition that has a null
			 * default value, ensure that all corresponding materials producer
			 * property values are not null and repair the definition.
			 */
			for (MaterialsProducerPropertyId materialsProducerPropertyId : scenarioData.materialsProducerPropertyDefinitions.keySet()) {
				PropertyDefinition propertyDefinition = scenarioData.materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
				if (!propertyDefinition.getDefaultValue().isPresent()) {
					for (MaterialsProducerId materialsProducerId : scenarioData.materialsProducerIds.keySet()) {
						Object propertyValue = null;
						Map<MaterialsProducerPropertyId, Object> propertyValueMap = scenarioData.materialsProducerPropertyValues.get(materialsProducerId);
						if (propertyValueMap != null) {
							propertyValue = propertyValueMap.get(materialsProducerPropertyId);

						}
						if (propertyValue == null) {
							throwInsufficientPropertyValueAssignment(materialsProducerPropertyId);
						}
					}
				}
			}

			/*
			 * For every region property definition that has a null default
			 * value, ensure that there all corresponding region property values
			 * are not null and repair the definition.
			 */
			for (RegionPropertyId regionPropertyId : scenarioData.regionPropertyDefinitions.keySet()) {
				PropertyDefinition propertyDefinition = scenarioData.regionPropertyDefinitions.get(regionPropertyId);
				if (!propertyDefinition.getDefaultValue().isPresent()) {
					for (RegionId regionId : scenarioData.regionIds.keySet()) {
						Object propertyValue = null;
						Map<RegionPropertyId, Object> propertyValueMap = scenarioData.regionPropertyValues.get(regionId);
						if (propertyValueMap != null) {
							propertyValue = propertyValueMap.get(regionPropertyId);
						}
						if (propertyValue == null) {
							throwInsufficientPropertyValueAssignment(regionPropertyId);
						}
					}
				}
			}

			/*
			 * For every resource property definition that has a null default
			 * value, ensure that there all corresponding resource property
			 * values are not null and repair the definition.
			 */
			for (ResourceId resourceId : scenarioData.resourceIds) {
				Map<ResourcePropertyId, PropertyDefinition> propertyDefinitionMap = scenarioData.resourcePropertyDefinitions.get(resourceId);
				if (propertyDefinitionMap != null) {
					for (ResourcePropertyId resourcePropertyId : propertyDefinitionMap.keySet()) {
						PropertyDefinition propertyDefinition = propertyDefinitionMap.get(resourcePropertyId);
						if (!propertyDefinition.getDefaultValue().isPresent()) {
							Object propertyValue = null;
							Map<ResourcePropertyId, Object> propertyValueMap = scenarioData.resourcePropertyValues.get(resourceId);
							if (propertyValueMap != null) {
								propertyValue = propertyValueMap.get(resourcePropertyId);
							}
							if (propertyValue == null) {
								throwInsufficientPropertyValueAssignment(resourcePropertyId);
							}
						}
					}
				}
			}

			/*
			 * All batch property definitions must have default values since
			 * batches may be created dynamically in the simulation
			 */
			for (MaterialId materialId : scenarioData.materialIds) {
				Map<BatchPropertyId, PropertyDefinition> propertyDefinitionMap = scenarioData.batchPropertyDefinitions.get(materialId);
				if (propertyDefinitionMap != null) {
					for (BatchPropertyId batchPropertyId : propertyDefinitionMap.keySet()) {
						PropertyDefinition propertyDefinition = propertyDefinitionMap.get(batchPropertyId);
						if (!propertyDefinition.getDefaultValue().isPresent()) {
							throwNullDefaultValue(batchPropertyId);
						}
					}
				}
			}

			/*
			 * All group property definitions must have default values since
			 * groups may be created dynamically in the simulation
			 */
			for (GroupTypeId groupTypeId : scenarioData.groupTypeIds) {
				Map<GroupPropertyId, PropertyDefinition> propertyDefinitionMap = scenarioData.groupPropertyDefinitions.get(groupTypeId);
				if (propertyDefinitionMap != null) {
					for (GroupPropertyId groupPropertyId : propertyDefinitionMap.keySet()) {
						PropertyDefinition propertyDefinition = propertyDefinitionMap.get(groupPropertyId);
						if (!propertyDefinition.getDefaultValue().isPresent()) {
							throwNullDefaultValue(groupPropertyId);
						}
					}
				}
			}
			/*
			 * All person property definitions must have default values since
			 * people may be created dynamically in the simulation
			 * 
			 */
			for (PersonPropertyId personPropertyId : scenarioData.personPropertyDefinitions.keySet()) {
				PropertyDefinition propertyDefinition = scenarioData.personPropertyDefinitions.get(personPropertyId);
				if (!propertyDefinition.getDefaultValue().isPresent()) {
					throwNullDefaultValue(personPropertyId);
				}
			}

			return new ScenarioImpl(scenarioData);
		} finally {
			scenarioData = new ScenarioData();
			locked = false;
		}
	}

	private void acquireLock() {
		if (locked) {
			throw new RuntimeException("Reentrant access blocked by guard");
		}
		locked = true;
	}

	private void releaseLock() {
		locked = false;
	}

	@Override
	public ScenarioBuilder defineBatchProperty(final MaterialId materialId, final BatchPropertyId batchPropertyId, final PropertyDefinition propertyDefinition) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_BATCH_PROPERTY_ID, batchPropertyId);
			validateMaterialExists(scenarioData, materialId);
			validateNotNull(ScenarioErrorType.NULL_BATCH_PROPERTY_DEFINITION, propertyDefinition);
			validateBatchPropertyIsNotDefined(scenarioData, materialId, batchPropertyId);
			Map<BatchPropertyId, PropertyDefinition> propertyDefinitionsMap = scenarioData.batchPropertyDefinitions.get(materialId);
			if (propertyDefinitionsMap == null) {
				propertyDefinitionsMap = new LinkedHashMap<>();
				scenarioData.batchPropertyDefinitions.put(materialId, propertyDefinitionsMap);
			}
			propertyDefinitionsMap.put(batchPropertyId, propertyDefinition);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder defineCompartmentProperty(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final PropertyDefinition propertyDefinition) {
		acquireLock();
		try {
			validateCompartmentExists(scenarioData, compartmentId);
			validateNotNull(ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_ID, compartmentPropertyId);
			validateNotNull(ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_DEFINITION, propertyDefinition);
			validateCompartmentPropertyIsNotDefined(scenarioData, compartmentId, compartmentPropertyId);
			Map<CompartmentPropertyId, PropertyDefinition> map = scenarioData.compartmentPropertyDefinitions.get(compartmentId);
			if (map == null) {
				map = new LinkedHashMap<>();
				scenarioData.compartmentPropertyDefinitions.put(compartmentId, map);
			}
			map.put(compartmentPropertyId, propertyDefinition);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder defineGlobalProperty(final GlobalPropertyId globalPropertyId, final PropertyDefinition propertyDefinition) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_GLOBAL_PROPERTY_ID, globalPropertyId);
			validateNotNull(ScenarioErrorType.NULL_GLOBAL_PROPERTY_DEFINITION, propertyDefinition);
			validateGlobalPropertyIsNotDefined(scenarioData, globalPropertyId);
			scenarioData.globalPropertyDefinitions.put(globalPropertyId, propertyDefinition);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder defineGroupProperty(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId, final PropertyDefinition propertyDefinition) {
		acquireLock();
		try {
			validateGroupTypeExists(scenarioData, groupTypeId);
			validateNotNull(ScenarioErrorType.NULL_GROUP_PROPERTY_ID, groupPropertyId);
			validateNotNull(ScenarioErrorType.NULL_GROUP_PROPERTY_DEFINITION, propertyDefinition);
			validateGroupPropertyIsNotDefined(scenarioData, groupTypeId, groupPropertyId);
			Map<GroupPropertyId, PropertyDefinition> propertyDefinitionsMap = scenarioData.groupPropertyDefinitions.get(groupTypeId);
			if (propertyDefinitionsMap == null) {
				propertyDefinitionsMap = new LinkedHashMap<>();
				scenarioData.groupPropertyDefinitions.put(groupTypeId, propertyDefinitionsMap);
			}
			propertyDefinitionsMap.put(groupPropertyId, propertyDefinition);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder defineMaterialsProducerProperty(final MaterialsProducerPropertyId materialsProducerPropertyId, final PropertyDefinition propertyDefinition) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_ID, materialsProducerPropertyId);
			validateNotNull(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_DEFINITION, propertyDefinition);
			validateMaterialsProducerPropertyIsNotDefined(scenarioData, materialsProducerPropertyId);
			scenarioData.materialsProducerPropertyDefinitions.put(materialsProducerPropertyId, propertyDefinition);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder definePersonProperty(final PersonPropertyId personPropertyId, final PropertyDefinition propertyDefinition) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_PERSON_PROPERTY_ID, personPropertyId);
			validateNotNull(ScenarioErrorType.NULL_PERSON_PROPERTY_DEFINITION, propertyDefinition);
			validatePersonPropertyIsNotDefined(scenarioData, personPropertyId);
			scenarioData.personPropertyDefinitions.put(personPropertyId, propertyDefinition);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder defineRegionProperty(final RegionPropertyId regionPropertyId, final PropertyDefinition propertyDefinition) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_REGION_PROPERTY_ID, regionPropertyId);
			validateNotNull(ScenarioErrorType.NULL_REGION_PROPERTY_DEFINITION, propertyDefinition);
			validateRegionPropertyIsNotDefined(scenarioData, regionPropertyId);
			scenarioData.regionPropertyDefinitions.put(regionPropertyId, propertyDefinition);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder defineResourceProperty(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final PropertyDefinition propertyDefinition) {
		acquireLock();
		try {

			validateResourceExists(scenarioData, resourceId);
			validateNotNull(ScenarioErrorType.NULL_RESOURCE_PROPERTY_ID, resourcePropertyId);
			validateNotNull(ScenarioErrorType.NULL_RESOURCE_PROPERTY_DEFINITION, propertyDefinition);
			validateResourcePropertyIsNotDefined(scenarioData, resourceId, resourcePropertyId);
			Map<ResourcePropertyId, PropertyDefinition> map = scenarioData.resourcePropertyDefinitions.get(resourceId);
			if (map == null) {
				map = new LinkedHashMap<>();
				scenarioData.resourcePropertyDefinitions.put(resourceId, map);
			}
			map.put(resourcePropertyId, propertyDefinition);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId, final Object batchPropertyValue) {
		acquireLock();
		try {
			validateBatchExists(scenarioData, batchId);
			final MaterialId materialId = scenarioData.batchMaterials.get(batchId);
			validateBatchPropertyIsDefined(scenarioData, materialId, batchPropertyId);
			validateBatchPropertyValueNotSet(scenarioData, batchId, batchPropertyId);
			final Map<BatchPropertyId, PropertyDefinition> map = scenarioData.batchPropertyDefinitions.get(materialId);
			final PropertyDefinition propertyDefinition = map.get(batchPropertyId);
			validateValueCompatibility(scenarioData, ActionType.BATCH_PROPERTY_VALUE_ASSIGNMENT, batchPropertyId, propertyDefinition, batchPropertyValue);
			Map<BatchPropertyId, Object> propertyMap = scenarioData.batchPropertyValues.get(batchId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				scenarioData.batchPropertyValues.put(batchId, propertyMap);
			}
			propertyMap.put(batchPropertyId, batchPropertyValue);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final Object compartmentPropertyValue) {
		acquireLock();
		try {
			validateCompartmentExists(scenarioData, compartmentId);
			validateCompartmentPropertyIsDefined(scenarioData, compartmentId, compartmentPropertyId);
			validateCompartmentPropertyValueNotSet(scenarioData, compartmentId, compartmentPropertyId);
			Map<CompartmentPropertyId, PropertyDefinition> map = scenarioData.compartmentPropertyDefinitions.get(compartmentId);
			final PropertyDefinition propertyDefinition = map.get(compartmentPropertyId);
			validateValueCompatibility(scenarioData, ActionType.COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT, compartmentPropertyId, propertyDefinition, compartmentPropertyValue);
			Map<CompartmentPropertyId, Object> propertyMap = scenarioData.compartmentPropertyValues.get(compartmentId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				scenarioData.compartmentPropertyValues.put(compartmentId, propertyMap);
			}
			propertyMap.put(compartmentPropertyId, compartmentPropertyValue);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object globalPropertyValue) {
		acquireLock();
		try {
			validateGlobalPropertyIsDefined(scenarioData, globalPropertyId);
			validateGlobalPropertyValueNotSet(scenarioData, globalPropertyId);
			final PropertyDefinition propertyDefinition = scenarioData.globalPropertyDefinitions.get(globalPropertyId);
			validateValueCompatibility(scenarioData, ActionType.GLOBAL_PROPERTY_VALUE_ASSIGNMENT, globalPropertyId, propertyDefinition, globalPropertyValue);
			scenarioData.globalPropertyValues.put(globalPropertyId, globalPropertyValue);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue) {
		acquireLock();
		try {
			validateGroupExists(scenarioData, groupId);
			final GroupTypeId groupTypeId = scenarioData.groupTypes.get(groupId);
			validateGroupPropertyIsDefined(scenarioData, groupTypeId, groupPropertyId);
			validateGroupPropertyValueNotSet(scenarioData, groupId, groupTypeId, groupPropertyId);
			final Map<GroupPropertyId, PropertyDefinition> map = scenarioData.groupPropertyDefinitions.get(groupTypeId);
			final PropertyDefinition propertyDefinition = map.get(groupPropertyId);
			validateValueCompatibility(scenarioData, ActionType.GROUP_PROPERTY_VALUE_ASSIGNMENT, groupPropertyId, propertyDefinition, groupPropertyValue);
			Map<GroupPropertyId, Object> propertyMap = scenarioData.groupPropertyValues.get(groupId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				scenarioData.groupPropertyValues.put(groupId, propertyMap);
			}
			propertyMap.put(groupPropertyId, groupPropertyValue);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId, final Object materialsProducerPropertyValue) {
		acquireLock();
		try {
			validateMaterialsProducerExists(scenarioData, materialsProducerId);
			validateMaterialsProducerPropertyIsDefined(scenarioData, materialsProducerPropertyId);
			validateMaterialsProducerPropertyNotSet(scenarioData, materialsProducerId, materialsProducerPropertyId);
			final PropertyDefinition propertyDefinition = scenarioData.materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
			validateValueCompatibility(scenarioData, ActionType.MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT, materialsProducerPropertyId, propertyDefinition, materialsProducerPropertyValue);
			Map<MaterialsProducerPropertyId, Object> propertyMap = scenarioData.materialsProducerPropertyValues.get(materialsProducerId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				scenarioData.materialsProducerPropertyValues.put(materialsProducerId, propertyMap);
			}
			propertyMap.put(materialsProducerPropertyId, materialsProducerPropertyValue);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount) {
		acquireLock();
		try {
			validateMaterialsProducerExists(scenarioData, materialsProducerId);
			validateResourceExists(scenarioData, resourceId);
			validateResourceAmount(scenarioData, amount);
			validateMaterialsProducerResourceLevelNotSet(scenarioData, materialsProducerId, resourceId);

			Map<ResourceId, Long> resourceLevelMap = scenarioData.materialsProducerResourceLevels.get(materialsProducerId);
			if (resourceLevelMap == null) {
				resourceLevelMap = new LinkedHashMap<>();
				scenarioData.materialsProducerResourceLevels.put(materialsProducerId, resourceLevelMap);
			}
			resourceLevelMap.put(resourceId, amount);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setPersonCompartmentArrivalTracking(final TimeTrackingPolicy trackPersonCompartmentArrivalTimes) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_COMPARTMENT_TRACKING_POLICY, trackPersonCompartmentArrivalTimes);
			validatePersonCompartmentArrivalTrackingNotSet(scenarioData);
			scenarioData.compartmentArrivalTimeTrackingPolicy = trackPersonCompartmentArrivalTimes;
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		acquireLock();
		try {
			validatePersonExists(scenarioData, personId);
			validatePersonPropertyIsDefined(scenarioData, personPropertyId);
			validatePersonPropertyNotAssigned(scenarioData, personId, personPropertyId);
			final PropertyDefinition propertyDefinition = scenarioData.personPropertyDefinitions.get(personPropertyId);
			validateValueCompatibility(scenarioData, ActionType.PERSON_PROPERTY_VALUE_ASSIGNMENT, personPropertyId, propertyDefinition, personPropertyValue);
			Map<PersonPropertyId, Object> propertyMap = scenarioData.personPropertyValues.get(personId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				scenarioData.personPropertyValues.put(personId, propertyMap);
			}
			propertyMap.put(personPropertyId, personPropertyValue);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setPersonRegionArrivalTracking(final TimeTrackingPolicy trackPersonRegionArrivalTimes) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_REGION_TRACKING_POLICY, trackPersonRegionArrivalTimes);
			validatePersonRegionArrivalTrackingNotSet(scenarioData);
			scenarioData.regionArrivalTimeTrackingPolicy = trackPersonRegionArrivalTimes;
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setPersonResourceLevel(final PersonId personId, final ResourceId resourceId, final long amount) {
		acquireLock();
		try {
			validatePersonExists(scenarioData, personId);
			validateResourceExists(scenarioData, resourceId);
			validateResourceAmount(scenarioData, amount);
			validatePersonResourceLevelNotSet(scenarioData, personId, resourceId);
			Map<ResourceId, Long> resourceLevelMap = scenarioData.personResourceLevels.get(personId);
			if (resourceLevelMap == null) {
				resourceLevelMap = new LinkedHashMap<>();
				scenarioData.personResourceLevels.put(personId, resourceLevelMap);
			}
			resourceLevelMap.put(resourceId, amount);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue) {
		acquireLock();
		try {
			validateRegionExists(scenarioData, regionId);
			validateRegionPropertyIsDefined(scenarioData, regionPropertyId);
			validateRegionPropertyValueNotSet(scenarioData, regionId, regionPropertyId);
			final PropertyDefinition propertyDefinition = scenarioData.regionPropertyDefinitions.get(regionPropertyId);
			validateValueCompatibility(scenarioData, ActionType.REGION_PROPERTY_VALUE_ASSIGNMENT, regionPropertyId, propertyDefinition, regionPropertyValue);
			Map<RegionPropertyId, Object> propertyMap = scenarioData.regionPropertyValues.get(regionId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				scenarioData.regionPropertyValues.put(regionId, propertyMap);
			}
			propertyMap.put(regionPropertyId, regionPropertyValue);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount) {
		acquireLock();
		try {
			validateRegionExists(scenarioData, regionId);
			validateResourceExists(scenarioData, resourceId);
			validateRegionResourceNotSet(scenarioData, regionId, resourceId);
			validateResourceAmount(scenarioData, amount);
			Map<ResourceId, Long> resourceLevelMap = scenarioData.regionResourceLevels.get(regionId);
			if (resourceLevelMap == null) {
				resourceLevelMap = new LinkedHashMap<>();
				scenarioData.regionResourceLevels.put(regionId, resourceLevelMap);
			}
			resourceLevelMap.put(resourceId, amount);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final Object resourcePropertyValue) {
		acquireLock();
		try {
			validateResourceExists(scenarioData, resourceId);
			validateResourcePropertyIsDefined(scenarioData, resourceId, resourcePropertyId);
			validateResourcePropertyValueNotSet(scenarioData, resourceId, resourcePropertyId);
			Map<ResourcePropertyId, PropertyDefinition> defMap = scenarioData.resourcePropertyDefinitions.get(resourceId);
			final PropertyDefinition propertyDefinition = defMap.get(resourcePropertyId);
			validateValueCompatibility(scenarioData, ActionType.RESOURCE_PROPERTY_VALUE_ASSIGNMENT, resourcePropertyId, propertyDefinition, resourcePropertyValue);
			Map<ResourcePropertyId, Object> propertyMap = scenarioData.resourcePropertyValues.get(resourceId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				scenarioData.resourcePropertyValues.put(resourceId, propertyMap);
			}
			propertyMap.put(resourcePropertyId, resourcePropertyValue);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder setResourceTimeTracking(final ResourceId resourceId, final TimeTrackingPolicy trackValueAssignmentTimes) {
		acquireLock();
		try {
			validateResourceExists(scenarioData, resourceId);
			validateNotNull(ScenarioErrorType.NULL_RESOURCE_TRACKING_POLICY, trackValueAssignmentTimes);
			validateResourceTimeTrackingNotSet(scenarioData, resourceId);
			scenarioData.resourceTimeTrackingPolicies.put(resourceId, trackValueAssignmentTimes);
		} finally {
			releaseLock();
		}
		return this;
	}

	private void validatePersonCompartmentArrivalTrackingNotSet(final ScenarioData scenarioData) {
		if (scenarioData.compartmentArrivalTimeTrackingPolicy != null) {
			throwPreviouslyAssignedValueException("Compartment Arrival Time Tracking Policy");
		}
	}

	private void validatePersonRegionArrivalTrackingNotSet(final ScenarioData scenarioData) {
		if (scenarioData.regionArrivalTimeTrackingPolicy != null) {
			throwPreviouslyAssignedValueException("Region Arrival Time Tracking Policy");
		}
	}

	@Override
	public ScenarioBuilder addRandomNumberGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
		acquireLock();
		try {
			validateNotNull(ScenarioErrorType.NULL_RANDOM_NUMBER_GENERATOR_ID, randomNumberGeneratorId);
			validateRandomNumberGeneratorIdDoesNotExist(scenarioData, randomNumberGeneratorId);
			scenarioData.randomNumberGeneratorIds.add(randomNumberGeneratorId);
		} finally {
			releaseLock();
		}
		return this;
	}

	@Override
	public ScenarioBuilder addReportId(ReportId reportId, Supplier<Consumer<ReportContext>> supplier) {
		acquireLock();
		try {
			validateUniqueReportIdentifier(scenarioData, reportId);
			validateReportSupplier(supplier);
			scenarioData.reportIds.put(reportId, supplier);
		} finally {
			releaseLock();
		}
		return this;
	}

}