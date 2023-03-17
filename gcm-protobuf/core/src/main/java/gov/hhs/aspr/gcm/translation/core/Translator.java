package gov.hhs.aspr.gcm.translation.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.util.Pair;

import com.google.protobuf.Message;

import nucleus.PluginData;

public final class Translator {
    private final Data data;

    private Translator(Data data) {
        this.data = data;
    }

    private static class Data {
        private TranslatorId translatorId;
        private final Map<Reader, Message> readers = new LinkedHashMap<>();
        private final Map<Pair<Class<?>, Integer>, Writer> writers = new LinkedHashMap<>();
        private boolean hasInput = false;
        private boolean hasOutput = false;
        private boolean inputIsPluginData = true;
        private boolean outputIsPluginData = true;
        private Consumer<TranslatorContext> initializer;
        private final Set<TranslatorId> dependencies = new LinkedHashSet<>();

        private Data() {

        }

    }

    public static Builder builder() {
        return new Builder(new Data());
    }

    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        public Translator build() {

            if (this.data.translatorId == null) {
                throw new RuntimeException("No TranslatorId was set for this Translator");
            }

            return new Translator(data);
        }

        public Builder setTranslatorId(TranslatorId translatorId) {
            this.data.translatorId = translatorId;

            return this;
        }

        public Builder addInputFile(String inputFileName, Message inputMessageType) {
            try {
                this.data.readers.put(new FileReader(Paths.get(inputFileName).toFile()), inputMessageType);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to create Reader", e);
            }

            this.data.hasInput = true;
            return this;
        }

        public Builder addOutputFile(String outputFileName, Class<?> classRef, Integer scenarioId) {
            Pair<Class<?>, Integer> key = new Pair<>(classRef, scenarioId);
            if(this.data.writers.containsKey(key)) {
                throw new RuntimeException("Attempted to overwrite an existing output file.");
            }
            try {
                this.data.writers.put(key, new FileWriter(Paths.get(outputFileName).toFile()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to create Writer", e);
            }

            this.data.hasOutput = true;
            return this;
        }

        public Builder addOutputFile(String outputFileName, Class<?> classRef) {
            Pair<Class<?>, Integer> key = new Pair<>(classRef, 0);
            if(this.data.writers.containsKey(key)) {
                throw new RuntimeException("Attempted to overwrite an existing output file.");
            }
            try {
                this.data.writers.put(key, new FileWriter(Paths.get(outputFileName).toFile()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to create Writer", e);
            }

            this.data.hasOutput = true;
            return this;
        }

        public Builder setInitializer(Consumer<TranslatorContext> initConsumer) {
            this.data.initializer = initConsumer;

            return this;
        }

        public Builder setInputIsPluginData(boolean inputIsPluginData) {
            this.data.inputIsPluginData = inputIsPluginData;

            return this;
        }

        public Builder setOutputIsPluginData(boolean outputIsPluginData) {
            this.data.outputIsPluginData = outputIsPluginData;

            return this;
        }

        public Builder addDependency(TranslatorId dependency) {
            this.data.dependencies.add(dependency);

            return this;
        }
    }

    public Consumer<TranslatorContext> getInitializer() {
        return this.data.initializer;
    }

    public boolean inputIsPluginData() {
        return this.data.inputIsPluginData;
    }

    public boolean outputIsPluginData() {
        return this.data.outputIsPluginData;
    }

    public boolean hasInput() {
        return this.data.hasInput;
    }

    public boolean hasOutput() {
        return this.data.hasOutput;
    }

    public TranslatorId getTranslatorId() {
        return this.data.translatorId;
    }

    public Set<TranslatorId> getTranslatorDependencies() {
        return this.data.dependencies;
    }

    public void readJsonInput(ReaderContext readerContext) {
        validateHasInput();

        if (this.data.inputIsPluginData) {
            throw new RuntimeException(
                    "The input data for this translator is a plugin data, and should be read via the readPluginDataInput() method.");
        }

        Set<Reader> readers = this.data.readers.keySet();

        for (Reader reader : readers) {
            readerContext.readJsonInput(reader, this.data.readers.get(reader).newBuilderForType());
        }

    }

    public void readPluginDataInput(ReaderContext readerContext) {
        validateHasInput();

        if (!this.data.inputIsPluginData) {
            throw new RuntimeException(
                    "The input data for this translator is not plugin data, and should be read via the readJsonInput() method.");
        }
        Set<Reader> readers = this.data.readers.keySet();

        if(readers.size() > 1) {
            throw new RuntimeException(
                    "There should be at most 1 plugin data file for a given translator.");
        }

        for (Reader reader : readers) {
            readerContext.readPluginDataInput(reader, this.data.readers.get(reader).newBuilderForType());
        }
    }

    public void writeJsonOutput(WriterContext writerContext, Object simObject) {
        validateHasOutput();

        if (this.data.outputIsPluginData) {
            throw new RuntimeException(
                    "The output data for this translator is a plugin data, and should be written via the writePluginDataOutput() method.");
        }

        Pair<Class<?>, Integer> key = new Pair<>(simObject.getClass(), writerContext.getScenarioId());
        if(!this.data.writers.containsKey(key)) {
            throw new RuntimeException("No writer exists for type: " + simObject.getClass() + " and scenario : " + writerContext.getScenarioId());
        }

        writerContext.writeJsonOutput(this.data.writers.get(key), simObject);
    }

    public <T extends PluginData> void writePluginDataOutput(WriterContext writerContext, T pluginData) {
        validateHasOutput();

        if (!this.data.outputIsPluginData) {
            throw new RuntimeException(
                    "The output data for this translator is not a plugin data, and should be written via the writeOutput() method.");
        }

        Pair<Class<?>, Integer> key = new Pair<>(pluginData.getClass(), writerContext.getScenarioId());
        if(!this.data.writers.containsKey(key)) {
            throw new RuntimeException("No writer exists for type: " + pluginData.getClass() + " and scenario : " + writerContext.getScenarioId());
        }

        writerContext.writePluginDataOutput(this.data.writers.get(key), pluginData);
    }

    private void validateHasInput() {
        if (!this.data.hasInput) {
            throw new RuntimeException("Trying to read data of which there is no input file for.");
        }
    }

    private void validateHasOutput() {
        if (!this.data.hasOutput) {
            throw new RuntimeException("Trying to write data of which there is no output file for.");
        }
    }

}
