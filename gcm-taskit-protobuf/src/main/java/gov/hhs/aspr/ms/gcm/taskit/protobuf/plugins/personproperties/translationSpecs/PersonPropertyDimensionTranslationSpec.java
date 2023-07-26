package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.translationSpecs;

import com.google.protobuf.Any;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyDimensionInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyDimension;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyId;

public class PersonPropertyDimensionTranslationSpec
        extends ProtobufTranslationSpec<PersonPropertyDimensionInput, PersonPropertyDimension> {

    @Override
    protected PersonPropertyDimension convertInputObject(PersonPropertyDimensionInput inputObject) {
        PersonPropertyDimension.Builder builder = PersonPropertyDimension.builder();

        PersonPropertyId personPropertyId = this.translationEngine.convertObject(inputObject.getPersonPropertyId());

        builder
                .setPersonPropertyId(personPropertyId)
                .setTrackTimes(inputObject.getTrackTimes());

        for (Any anyValue : inputObject.getValuesList()) {
            Object value = this.translationEngine.getObjectFromAny(anyValue);
            builder.addValue(value);
        }

        return builder.build();
    }

    @Override
    protected PersonPropertyDimensionInput convertAppObject(PersonPropertyDimension appObject) {
        PersonPropertyDimensionInput.Builder builder = PersonPropertyDimensionInput.newBuilder();

        PersonPropertyIdInput personPropertyIdInput = this.translationEngine
                .convertObjectAsSafeClass(appObject.getPersonPropertyId(), PersonPropertyId.class);

        builder
                .setPersonPropertyId(personPropertyIdInput)
                .setTrackTimes(appObject.getTrackTimes());

        for (Object objValue : appObject.getValues()) {
            builder.addValues(this.translationEngine.getAnyFromObject(objValue));
        }

        return builder.build();
    }

    @Override
    public Class<PersonPropertyDimension> getAppObjectClass() {
        return PersonPropertyDimension.class;
    }

    @Override
    public Class<PersonPropertyDimensionInput> getInputObjectClass() {
        return PersonPropertyDimensionInput.class;
    }

}
