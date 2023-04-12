package gov.hhs.aspr.gcm.translation.protobuf.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

import nucleus.PluginData;
import util.graph.Graph;
import util.graph.GraphDepthEvaluator;
import util.graph.Graphs;
import util.graph.MutableGraph;

public class TranslatorController {
    private final Data data;
    private TranslatorCore translatorCore;
    private final List<PluginData> pluginDatas = Collections.synchronizedList(new ArrayList<>());
    private final List<Object> objects = Collections.synchronizedList(new ArrayList<>());
    private final Map<Class<?>, Translator> appObjectClassToTranslatorMap = new LinkedHashMap<>();
    private Translator focalTranslator = null;
    private TranslatorId focalTranslatorId = null;

    private TranslatorController(Data data) {
        this.data = data;
    }

    private static class Data {
        private TranslatorCore.Builder translatorCoreBuilder = TranslatorCore.builder();
        private final List<Translator> translators = new ArrayList<>();
        private final Map<Reader, Class<?>> readerMap = new LinkedHashMap<>();
        private final Map<Pair<Class<?>, Integer>, Writer> writerMap = new LinkedHashMap<>();
        private final Map<Class<?>, Class<?>> markerInterfaceClassMap = new LinkedHashMap<>();

        private Data() {
        }
    }

    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        public TranslatorController build() {
            TranslatorController translatorController = new TranslatorController(this.data);

            translatorController.init();
            return translatorController;
        }

        public Builder addReader(Path filePath, Class<?> classRef) {
            try {
                this.data.readerMap.put(new FileReader(filePath.toFile()), classRef);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to create Reader", e);
            }

            return this;
        }

        public Builder addWriter(Path filePath, Class<?> classRef) {
            return this.addWriterForScenario(filePath, classRef, 0);
        }

        public Builder addWriterForScenario(Path filePath, Class<?> classRef, Integer scenarioId) {
            Pair<Class<?>, Integer> key = new Pair<>(classRef, scenarioId);

            if (this.data.writerMap.containsKey(key)) {
                throw new RuntimeException("Attempted to overwrite an existing output file.");
            }
            try {
                this.data.writerMap.put(key, new FileWriter(filePath.toFile()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to create Writer", e);
            }
            return this;
        }

        public <T, U extends T> Builder addMarkerInterface(Class<U> classRef, Class<T> markerInterface) {

            if (!markerInterface.isAssignableFrom(classRef)) {
                throw new RuntimeException("cannot cast " + classRef.getName() + " to " + markerInterface.getName());
            }

            this.data.markerInterfaceClassMap.put(classRef, markerInterface);
            return this;
        }

        public Builder addTranslator(Translator translator) {
            this.data.translators.add(translator);
            return this;
        }

        public <I, S> Builder addTranslatorSpec(AbstractTranslatorSpec<I, S> translatorSpec) {
            this.data.translatorCoreBuilder.addTranslatorSpec(translatorSpec);
            return this;
        }

        public Builder setIgnoringUnknownFields(boolean ignoringUnknownFields) {
            this.data.translatorCoreBuilder.setIgnoringUnknownFields(ignoringUnknownFields);
            return this;
        }

        public Builder setIncludingDefaultValueFields(boolean includingDefaultValueFields) {
            this.data.translatorCoreBuilder.setIncludingDefaultValueFields(includingDefaultValueFields);
            return this;
        }

    }

    public static Builder builder() {
        return new Builder(new Data());
    }

    protected <I, S> void addTranslatorSpec(AbstractTranslatorSpec<I, S> translatorSpec) {
        this.data.translatorCoreBuilder.addTranslatorSpec(translatorSpec);

        this.appObjectClassToTranslatorMap.put(translatorSpec.getAppObjectClass(), this.focalTranslator);
    }

    protected void addFieldToIncludeDefaultValue(FieldDescriptor fieldDescriptor) {
        this.data.translatorCoreBuilder.addFieldToIncludeDefaultValue(fieldDescriptor);
    }

    protected <T, U extends T> void addMarkerInterface(Class<U> classRef, Class<T> markerInterface) {

        if (!markerInterface.isAssignableFrom(classRef)) {
            throw new RuntimeException("cannot cast " + classRef.getName() + " to " + markerInterface.getName());
        }

        this.data.markerInterfaceClassMap.put(classRef, markerInterface);
    }

    protected <U> void readJsonInput(Reader reader, Class<U> inputClassRef) {
        Object simObject = this.translatorCore.readJson(reader, inputClassRef);

        if (simObject instanceof PluginData) {
            this.pluginDatas.add((PluginData) simObject);
        } else {
            this.objects.add(simObject);
        }
    }

    protected <T> void writeJson(Writer writer, T simObject) {
        this.translatorCore.writeJson(writer, simObject, Optional.empty());
    }

    protected <T extends U, U> void writeJsonOutput(Writer writer, T simObject, Class<U> superClass) {
        this.translatorCore.writeJson(writer, simObject, Optional.of(superClass));
    }

    private void validateCoreTranslator() {
        if (this.translatorCore == null || !this.translatorCore.isInitialized()) {
            throw new RuntimeException(
                    "Trying to call readInput() or writeInput() before calling init on the TranslatorController.");
        }
    }

    private TranslatorController init() {
        TranslatorContext translatorContext = new TranslatorContext(this);

        List<Translator> orderedTranslators = this.getOrderedTranslators();

        for (Translator translator : orderedTranslators) {
            this.focalTranslator = translator;
            translator.getInitializer().accept(translatorContext);
            this.focalTranslator = null;
        }

        this.translatorCore = this.data.translatorCoreBuilder.build();

        this.translatorCore.init();

        return this;
    }

    public TranslatorController readInputParrallel() {
        validateCoreTranslator();

        return this;
    }

    public TranslatorController readInput() {
        validateCoreTranslator();

        for (Reader reader : this.data.readerMap.keySet()) {
            Class<?> classRef = this.data.readerMap.get(reader);

            this.readJsonInput(reader, classRef);
        }

        return this;
    }

    public void writeOutput() {
        validateCoreTranslator();

        int scenarioId = 0;

        for (PluginData pluginData : this.pluginDatas) {
            Class<?> classRef = pluginData.getClass();
            Writer writer = this.data.writerMap.get(new Pair<Class<?>, Integer>(classRef, scenarioId));

            this.writeJson(writer, pluginData);
        }

        for (Object simObject : this.objects) {

            Class<?> classRef = simObject.getClass();

            if (this.data.markerInterfaceClassMap.containsKey(classRef)) {
                classRef = this.data.markerInterfaceClassMap.get(classRef);
            }

            Writer writer = this.data.writerMap.get(new Pair<Class<?>, Integer>(classRef, scenarioId));

            this.writeJson(writer, simObject);
        }
    }

    public <T> void writeObjectOutput(List<T> objects) {
        for (Object object : objects) {
            writeObjectOutput(object);
        }
    }

    public void writeObjectOutput(Object object) {
        this.writeObjectOutput(object, 0);
    }

    public void writeObjectOutput(Object object, Integer scenarioId) {
        validateCoreTranslator();

        Writer writer = this.data.writerMap.get(new Pair<Class<?>, Integer>(object.getClass(), 0));

        this.writeJson(writer, object);
    }

    public void writePluginDataOutput(List<PluginData> pluginDatas) {
        for (PluginData pluginData : pluginDatas) {
            writePluginDataOutput(pluginData);
        }
    }

    public void writePluginDataOutput(PluginData pluginData) {
        validateCoreTranslator();

        WriterContext writerContext = new WriterContext(this);

        Translator translator = this.appObjectClassToTranslatorMap.get(pluginData.getClass());
        if (translator == null) {
            System.out.println("translator was null for: " + pluginData.getClass());
            return;
        }
        // translator.writePluginDataOutput(writerContext, pluginData);
    }

    public void writePluginDataOutput(PluginData pluginData, Integer scenarioId) {
        validateCoreTranslator();

        WriterContext writerContext = new WriterContext(this, scenarioId);

        Translator translator = this.appObjectClassToTranslatorMap.get(pluginData.getClass());
        if (translator == null) {
            System.out.println("translator was null for: " + pluginData.getClass());
            return;
        }
        // translator.writePluginDataOutput(writerContext, pluginData);
    }

    public List<PluginData> getPluginDatas() {
        return this.pluginDatas;
    }

    public List<Object> getObjects() {
        return this.objects;
    }

    private List<Translator> getOrderedTranslators() {

        MutableGraph<TranslatorId, Object> mutableGraph = new MutableGraph<>();

        Map<TranslatorId, Translator> translatorMap = new LinkedHashMap<>();

        /*
         * Add the nodes to the graph, check for duplicate ids, build the
         * mapping from plugin id back to plugin
         */
        for (Translator translator : this.data.translators) {
            focalTranslatorId = translator.getTranslatorId();
            translatorMap.put(focalTranslatorId, translator);
            // ensure that there are no duplicate plugins
            if (mutableGraph.containsNode(focalTranslatorId)) {
                throw new RuntimeException("Duplicate Translator");
            }
            mutableGraph.addNode(focalTranslatorId);
            focalTranslatorId = null;
        }

        // Add the edges to the graph
        for (Translator translator : this.data.translators) {
            focalTranslatorId = translator.getTranslatorId();
            for (TranslatorId translatorId : translator.getTranslatorDependencies()) {
                mutableGraph.addEdge(new Object(), focalTranslatorId, translatorId);
            }
            focalTranslatorId = null;
        }

        /*
         * Check for missing plugins from the plugin dependencies that were
         * collected from the known plugins.
         */
        for (TranslatorId translatorId : mutableGraph.getNodes()) {
            if (!translatorMap.containsKey(translatorId)) {
                List<Object> inboundEdges = mutableGraph.getInboundEdges(translatorId);
                StringBuilder sb = new StringBuilder();
                sb.append("cannot locate instance of ");
                sb.append(translatorId);
                sb.append(" needed for ");
                boolean first = true;
                for (Object edge : inboundEdges) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    TranslatorId dependentTranslatorId = mutableGraph.getOriginNode(edge);
                    sb.append(dependentTranslatorId);
                }
                throw new RuntimeException("Missing Translator: " + sb.toString());
            }
        }

        /*
         * Determine whether the graph is acyclic and generate a graph depth
         * evaluator for the graph so that we can determine the order of
         * initialization.
         */
        Optional<GraphDepthEvaluator<TranslatorId>> optional = GraphDepthEvaluator
                .getGraphDepthEvaluator(mutableGraph.toGraph());

        if (!optional.isPresent()) {
            /*
             * Explain in detail why there is a circular dependency
             */

            Graph<TranslatorId, Object> g = mutableGraph.toGraph();

            g = Graphs.getSourceSinkReducedGraph(g);
            g = Graphs.getEdgeReducedGraph(g);
            g = Graphs.getSourceSinkReducedGraph(g);

            List<Graph<TranslatorId, Object>> cutGraphs = Graphs.cutGraph(g);
            StringBuilder sb = new StringBuilder();
            String lineSeparator = System.getProperty("line.separator");
            sb.append(lineSeparator);
            boolean firstCutGraph = true;

            for (Graph<TranslatorId, Object> cutGraph : cutGraphs) {
                if (firstCutGraph) {
                    firstCutGraph = false;
                } else {
                    sb.append(lineSeparator);
                }
                sb.append("Dependency group: ");
                sb.append(lineSeparator);
                Set<TranslatorId> nodes = cutGraph.getNodes().stream()
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                for (TranslatorId node : nodes) {
                    sb.append("\t");
                    sb.append(node);
                    sb.append(" requires:");
                    sb.append(lineSeparator);
                    for (Object edge : cutGraph.getInboundEdges(node)) {
                        TranslatorId dependencyNode = cutGraph.getOriginNode(edge);
                        if (nodes.contains(dependencyNode)) {
                            sb.append("\t");
                            sb.append("\t");
                            sb.append(dependencyNode);
                            sb.append(lineSeparator);
                        }
                    }
                }
            }
            throw new RuntimeException("Circular translator dependencies: " + sb.toString());
        }

        // the graph is acyclic, so the depth evaluator is present
        GraphDepthEvaluator<TranslatorId> graphDepthEvaluator = optional.get();

        List<TranslatorId> orderedTranslatorIds = graphDepthEvaluator.getNodesInRankOrder();

        List<Translator> orderedTranslators = new ArrayList<>();
        for (TranslatorId translatorId : orderedTranslatorIds) {
            orderedTranslators.add(translatorMap.get(translatorId));
        }

        return orderedTranslators;
    }
}
