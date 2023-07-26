package gov.hhs.aspr.ms.taskit.core.testsupport;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import gov.hhs.aspr.ms.taskit.core.TranslationEngine;
import gov.hhs.aspr.ms.taskit.core.TranslationSpec;

public class TestTranslationEngine extends TranslationEngine {
    private final Data data;

    private TestTranslationEngine(Data data) {
        super(data);
        this.data = data;
    }

    protected static class Data extends TranslationEngine.Data {
        Gson gson = new Gson();

        protected Data() {
            super();
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

    }

    public static class Builder extends TranslationEngine.Builder {
        private TestTranslationEngine.Data data;

        private Builder(TestTranslationEngine.Data data) {
            super(data);
            this.data = data;
        }

        @Override
        public TestTranslationEngine build() {
            return new TestTranslationEngine(this.data);
        }

        @Override
        public <I, A> Builder addTranslationSpec(TranslationSpec<I, A> translationSpec) {
            super.addTranslationSpec(translationSpec);

            return this;
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

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
