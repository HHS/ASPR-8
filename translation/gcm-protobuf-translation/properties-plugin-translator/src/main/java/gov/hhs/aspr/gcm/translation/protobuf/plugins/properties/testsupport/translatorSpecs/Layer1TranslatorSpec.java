package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.input.Layer1;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.Layer1SimObject;

public class Layer1TranslatorSpec extends ProtobufTranslationSpec<Layer1, Layer1SimObject> {

    @Override
    protected Layer1SimObject convertInputObject(Layer1 inputObject) {
        Layer1SimObject appObject = new Layer1SimObject();

        appObject.setX(inputObject.getX());
        appObject.setY(inputObject.getY());
        appObject.setZ(inputObject.getZ());

        return appObject;
    }

    @Override
    protected Layer1 convertAppObject(Layer1SimObject appObject) {
        return Layer1.newBuilder().setX(appObject.getX()).setY(appObject.getY()).setZ(appObject.getZ()).build();
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
