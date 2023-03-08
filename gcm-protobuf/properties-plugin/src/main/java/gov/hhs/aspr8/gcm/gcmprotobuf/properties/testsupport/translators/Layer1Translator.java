package gov.hhs.aspr8.gcm.gcmprotobuf.properties.testsupport.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr8.gcm.gcmprotobuf.core.AbstractTranslator;
import gov.hhs.aspr8.gcm.gcmprotobuf.properties.testsupport.simobjects.Layer1SimObject;
import plugins.properties.input.testsupport.Layer1;

public class Layer1Translator extends AbstractTranslator<Layer1, Layer1SimObject> {

    @Override
    protected Layer1SimObject convertInputObject(Layer1 inputObject) {
        Layer1SimObject simObject = new Layer1SimObject();

        simObject.setX(inputObject.getX());
        simObject.setY(inputObject.getY());
        simObject.setZ(inputObject.getZ());

        return simObject;
    }

    @Override
    protected Layer1 convertSimObject(Layer1SimObject simObject) {
        return Layer1.newBuilder().setX(simObject.getX()).setY(simObject.getY()).setZ(simObject.getZ()).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return Layer1.getDescriptor();
    }

    @Override
    public Layer1 getDefaultInstanceForInputObject() {
        return Layer1.getDefaultInstance();
    }

    @Override
    public Class<Layer1SimObject> getSimObjectClass() {
        return Layer1SimObject.class;
    }

    @Override
    public Class<Layer1> getInputObjectClass() {
        return Layer1.class;
    }

}
