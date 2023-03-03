package plugins.stochastics.translators;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;

import base.AbstractTranslator;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginDataInput;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.SimpleRandomNumberGeneratorId;

public class StochasticsPluginDataTranslator
        extends AbstractTranslator<StochasticsPluginDataInput, StochasticsPluginData> {

    @Override
    protected StochasticsPluginData convertInputObject(StochasticsPluginDataInput inputObject) {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        builder.setSeed(inputObject.getSeed());

        for (Any randomGenIdInput : inputObject.getRandomNumberGeneratorIdsList()) {
            Object randomGenId = this.translator.getObjectFromAny(randomGenIdInput);
            RandomNumberGeneratorId generatorId = new SimpleRandomNumberGeneratorId(randomGenId);
            builder.addRandomGeneratorId(generatorId);
        }

        return builder.build();
    }

    @Override
    protected StochasticsPluginDataInput convertSimObject(StochasticsPluginData simObject) {
        StochasticsPluginDataInput.Builder builder = StochasticsPluginDataInput.newBuilder();

        builder.setSeed(simObject.getSeed());

        List<Any> randomNumberGeneratorIds = new ArrayList<>();

        for (RandomNumberGeneratorId randomNumberGeneratorId : simObject.getRandomNumberGeneratorIds()) {
            SimpleRandomNumberGeneratorId simpleRandomNumberGeneratorId = (SimpleRandomNumberGeneratorId) randomNumberGeneratorId;
            Any anyId = this.translator.getAnyFromObject(simpleRandomNumberGeneratorId.getValue());
            randomNumberGeneratorIds.add(anyId);
        }

        builder.addAllRandomNumberGeneratorIds(randomNumberGeneratorIds);

        return builder.build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return StochasticsPluginDataInput.getDescriptor();
    }

    @Override
    public StochasticsPluginDataInput getDefaultInstanceForInputObject() {
        return StochasticsPluginDataInput.getDefaultInstance();
    }

    @Override
    public Class<StochasticsPluginData> getSimObjectClass() {
        return StochasticsPluginData.class;
    }

    @Override
    public Class<StochasticsPluginDataInput> getInputObjectClass() {
        return StochasticsPluginDataInput.class;
    }

}
