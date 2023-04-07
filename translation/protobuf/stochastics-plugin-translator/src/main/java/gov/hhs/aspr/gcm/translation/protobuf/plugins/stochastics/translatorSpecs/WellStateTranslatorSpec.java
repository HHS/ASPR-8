package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.WellStateInput;
import plugins.stochastics.support.WellState;

public class WellStateTranslatorSpec
        extends AbstractTranslatorSpec<WellStateInput, WellState> {

    @Override
    protected WellState convertInputObject(WellStateInput inputObject) {
        WellState.Builder builder = WellState.builder();

        builder.setSeed(inputObject.getSeed());

        if (inputObject.hasVArray()) {
            int[] vArray = decode(inputObject.getVArray());
            int index = inputObject.getIndex();

            builder.setInternals(index, vArray);
        }

        return builder.build();
    }

    @Override
    protected WellStateInput convertAppObject(WellState simObject) {
        WellStateInput.Builder builder = WellStateInput.newBuilder();

        builder.setSeed(simObject.getSeed())
                .setIndex(simObject.getIndex());
        if (simObject.getVArray().length > 0) {
            ByteString byteString = encode(simObject.getVArray());
            builder.setVArray(byteString);
        }

        return builder.build();
    }

    @Override
    public WellStateInput getDefaultInstanceForInputObject() {
        return WellStateInput.getDefaultInstance();
    }

    @Override
    public Class<WellState> getAppObjectClass() {
        return WellState.class;
    }

    @Override
    public Class<WellStateInput> getInputObjectClass() {
        return WellStateInput.class;
    }

    private ByteString encode(int[] vArrayToEncode) {
        JsonArray jsonArray = new JsonArray();
        for (int v : vArrayToEncode) {
            jsonArray.add(v);
        }

        return ByteString.copyFromUtf8(jsonArray.toString());
    }

    private int[] decode(ByteString encodedString) {
        String decodedString = encodedString.toStringUtf8();
        JsonArray jsonArray = JsonParser.parseString(decodedString).getAsJsonArray();

        int[] retArray = new int[jsonArray.size()];

        int index = 0;
        for (JsonElement element : jsonArray) {
            retArray[index++] = element.getAsInt();
        }

        return retArray;
    }

}
