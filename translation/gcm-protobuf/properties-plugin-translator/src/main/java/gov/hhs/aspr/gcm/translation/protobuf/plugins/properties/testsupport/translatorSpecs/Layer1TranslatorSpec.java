package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.input.Layer1;
import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.Layer1SimObject;

public class Layer1TranslatorSpec extends AbstractProtobufTranslatorSpec<Layer1, Layer1SimObject> {

    @Override
    protected Layer1SimObject convertInputObject(Layer1 inputObject) {
        Layer1SimObject simObject = new Layer1SimObject();

        simObject.setX(inputObject.getX());
        simObject.setY(inputObject.getY());
        simObject.setZ(inputObject.getZ());

        return simObject;
    }

    @Override
    protected Layer1 convertAppObject(Layer1SimObject simObject) {
        return Layer1.newBuilder().setX(simObject.getX()).setY(simObject.getY()).setZ(simObject.getZ()).build();
    }

    @Override
    public Class<Layer1SimObject> getAppObjectClass() {
        return Layer1SimObject.class;
    }

    @Override
    public Class<Layer1> getInputObjectClass() {
        return Layer1.class;
    }

}
