package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.translationSpecs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.support.input.WellStateInput;
import plugins.stochastics.support.WellState;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain WellStateInput} and
 * {@linkplain WellState}
 */
public class WellStateTranslationSpec
        extends ProtobufTranslationSpec<WellStateInput, WellState> {

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
    protected WellStateInput convertAppObject(WellState appObject) {
        WellStateInput.Builder builder = WellStateInput.newBuilder();

        builder.setSeed(appObject.getSeed());

        builder.setIndex(appObject.getIndex());

        ByteString byteString = encode(appObject.getVArray());
        builder.setVArray(byteString);

        return builder.build();
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
