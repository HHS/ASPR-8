package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;

/**
 * Dimension implementation for setting a person property to a list of values in
 * a person properties plugin data.
 */
public final class PersonPropertyDimension implements Dimension {

    private final PersonPropertyDimensionData personPropertyDimensionData;

    public PersonPropertyDimension(PersonPropertyDimensionData personPropertyDimensionData) {
        this.personPropertyDimensionData = personPropertyDimensionData;
    }

    public DimensionData getDimensionData() {
        return personPropertyDimensionData;
    }

    @Override
    public List<String> getExperimentMetaData() {
        List<String> result = new ArrayList<>();
        result.add(personPropertyDimensionData.getPersonPropertyId().toString());
        return result;
    }

    @Override
    public int levelCount() {
        return personPropertyDimensionData.getLevelCount();
    }

    @Override
    public List<String> executeLevel(DimensionContext dimensionContext, int level) {
        PersonPropertiesPluginData personPropertiesPluginData = dimensionContext
                .getPluginData(PersonPropertiesPluginData.class);
        PersonPropertiesPluginData.Builder builder = dimensionContext
                .getPluginDataBuilder(PersonPropertiesPluginData.Builder.class);

        Object value = personPropertyDimensionData.getValue(level);
        PersonPropertyId personPropertyId = personPropertyDimensionData.getPersonPropertyId();

        PropertyDefinition existingPropDef = personPropertiesPluginData
                .getPersonPropertyDefinition(personPropertyId);

        PropertyDefinition newPropDef = PropertyDefinition.builder()//
                .setDefaultValue(value)//
                .setPropertyValueMutability(existingPropDef.propertyValuesAreMutable())//
                .setType(existingPropDef.getType())//
                .build();

        double existingTime = personPropertiesPluginData
                .getPropertyDefinitionTime(personPropertyId);

        boolean trackTimes = personPropertyDimensionData.getTrackTimes();

        builder.definePersonProperty(personPropertyId, newPropDef, existingTime, trackTimes);

        List<String> result = new ArrayList<>();
        String levelName = personPropertyDimensionData.getLevelName(level);
        result.add(levelName);
        result.add(value.toString());
        return result;
    }

    @Override
    public String toString() {
        return "PersonPropertyDimension [personPropertyDimensionData=" + personPropertyDimensionData + "]";
    }
}
