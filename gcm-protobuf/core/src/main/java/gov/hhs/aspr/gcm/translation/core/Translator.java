package gov.hhs.aspr.gcm.translation.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.google.protobuf.Message;

import nucleus.PluginData;

public final class Translator {
    private Data data;

    private Translator(Data data) {
        this.data = data;
    }

    private static class Data {
        private TranslatorId translatorId;
        private Reader reader;
        private Writer writer;
        private Message inputObjectType;
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
                throw new RuntimeException("No PluginBundleId was set for this PluginBundle");
            }

            return new Translator(data);
        }

        public Builder setTranslatorId(TranslatorId translatorId) {
            this.data.translatorId = translatorId;

            return this;
        }

        public Builder setInputFileName(String inputFileName) {
            try {
                this.data.reader = new FileReader(Paths.get(inputFileName).toFile());
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to create Reader", e);
            }

            this.data.hasInput = true;
            return this;
        }

        public Builder setOutputFileName(String outputFileName) {
            try {
                this.data.writer = new FileWriter(Paths.get(outputFileName).toFile());
            } catch (IOException e) {
                throw new RuntimeException("Failed to create Writer", e);
            }

            this.data.hasOutput = true;
            return this;
        }

        public Builder setInputObjectType(Message message) {
            this.data.inputObjectType = message;
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

        readerContext.readJsonInput(this.data.reader, this.data.inputObjectType.newBuilderForType());
    }

    public void readPluginDataInput(ReaderContext readerContext) {
        validateHasInput();

        if (!this.data.inputIsPluginData) {
            throw new RuntimeException(
                    "The input data for this translator is not plugin data, and should be read via the readJsonInput() method.");
        }
        readerContext.readPluginDataInput(this.data.reader, this.data.inputObjectType.newBuilderForType());
    }

    public void writeJsonOutput(WriterContext writerContext, Object simObject) {
        validateHasOutput();

        if (this.data.outputIsPluginData) {
            throw new RuntimeException(
                    "The output data for this translator is a plugin data, and should be written via the writePluginDataOutput() method.");
        }

        writerContext.writeJsonOutput(this.data.writer, simObject);
    }

    public <T extends PluginData> void writePluginDataOutput(WriterContext writerContext, T pluginData) {
        validateHasOutput();

        if (!this.data.outputIsPluginData) {
            throw new RuntimeException(
                    "The output data for this translator is not a plugin data, and should be written via the writeOutput() method.");
        }

        writerContext.writePluginDataOutput(this.data.writer, pluginData);
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
