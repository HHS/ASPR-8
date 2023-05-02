package gov.hhs.aspr.translation.core.testsupport;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import gov.hhs.aspr.translation.core.TranslationEngine;

public class TestTranslationEngine extends TranslationEngine {
    private final Data data;

    private TestTranslationEngine(Data data) {
        super(data);
        this.data = data;
    }

    private static class Data extends TranslationEngine.Data {
        Gson gson = new Gson();

        private Data() {
            super();
        }
    }

    public static class Builder extends TranslationEngine.Builder {
        private Data data;

        private Builder(Data data) {
            super(data);
            this.data = data;
        }

        public TestTranslationEngine build() {
            return new TestTranslationEngine(this.data);
        }
    }

    public static Builder builder() {
        return new Builder(new Data());
    }

    @Override
    public <U, M extends U> void writeOutput(Writer writer, M appObject, Optional<Class<U>> superClass) {
        Object outputObject;
        if (superClass.isPresent()) {
            outputObject = convertObjectAsSafeClass(appObject, superClass.get());
        } else {
            outputObject = convertObject(appObject);
        }

        String stringToWrite = this.data.gson.toJson(outputObject);

        try {
            writer.write(stringToWrite);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T, U> T readInput(Reader reader, Class<U> inputClassRef) {
        JsonObject jsonObject = JsonParser.parseReader(new JsonReader(reader)).getAsJsonObject();

        return convertObject(this.data.gson.fromJson(jsonObject.toString(), inputClassRef));
    }

}
