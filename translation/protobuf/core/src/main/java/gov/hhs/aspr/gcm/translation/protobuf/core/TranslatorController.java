package gov.hhs.aspr.gcm.translation.protobuf.core;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    protected <U extends Message.Builder> void readPluginDataInput(Reader reader, U builder) {
        PluginData pluginData = this.translatorCore.readJson(reader, builder);

        this.pluginDatas.add(pluginData);
    }

    protected <U extends Message.Builder> void readJsonInput(Reader reader, U builder) {
        Object simObject = this.translatorCore.readJson(reader, builder);

        this.objects.add(simObject);
    }

    protected <T extends PluginData> void writePluginDataOutput(Writer writer, T pluginData) {
        this.translatorCore.writeJson(writer, pluginData);
    }

    protected void writeJsonOutput(Writer writer, Object simObject) {
        this.translatorCore.writeJson(writer, simObject);
    }

    protected void writeJsonOutput(Writer writer, Object simObject, Class<?> superClass) {
        this.translatorCore.writeJson(writer, simObject, superClass);
    }

    // temporary pass through method
    public TranslatorCore getTranslatorCore() {
        if (this.translatorCore == null) {
            throw new RuntimeException("translatorCore is null");
        }

        return this.translatorCore;
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

        ReaderContext readerContext = new ReaderContext(this);

        this.data.translators.parallelStream().forEach((translator) -> {
            if (translator.hasInput()) {
                if (translator.inputIsPluginData()) {
                    translator.readPluginDataInput(readerContext);
                } else {
                    translator.readJsonInput(readerContext);
                }
            }

        });
        return this;
    }

    public TranslatorController readInput() {
        validateCoreTranslator();

        ReaderContext readerContext = new ReaderContext(this);

        for (Translator translator : this.data.translators) {
            if (!translator.hasInput())
                continue;
            if (translator.inputIsPluginData()) {
                translator.readPluginDataInput(readerContext);
                continue;
            }
            translator.readJsonInput(readerContext);
        }

        return this;
    }

    public void writeOutput() {
        validateCoreTranslator();

        WriterContext writerContext = new WriterContext(this);

        for (PluginData pluginData : this.pluginDatas) {
            Translator translator = this.appObjectClassToTranslatorMap.get(pluginData.getClass());
            translator.writePluginDataOutput(writerContext, pluginData);
        }

        for (Object simObject : this.objects) {
            Translator translator = this.appObjectClassToTranslatorMap.get(simObject.getClass());
            translator.writeJsonOutput(writerContext, simObject);
        }
    }

    public <T> void writeObjectOutput(List<T> objects) {
        for (Object object : objects) {
            writeObjectOutput(object);
        }
    }

    public void writeObjectOutput(Object object) {
        validateCoreTranslator();

        WriterContext writerContext = new WriterContext(this);

        Translator translator = this.appObjectClassToTranslatorMap.get(object.getClass());
        if (translator == null) {
            System.out.println("translator was null for: " + object.getClass());
            return;
        }
        translator.writeJsonOutput(writerContext, object);
    }

    public void writeObjectOutput(Object object, Integer scenarioId) {
        validateCoreTranslator();

        WriterContext writerContext = new WriterContext(this, scenarioId);

        Translator translator = this.appObjectClassToTranslatorMap.get(object.getClass());
        if (translator == null) {
            System.out.println("translator was null for: " + object.getClass());
            return;
        }
        translator.writeJsonOutput(writerContext, object);
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
        translator.writePluginDataOutput(writerContext, pluginData);
    }

    public void writePluginDataOutput(PluginData pluginData, Integer scenarioId) {
        validateCoreTranslator();

        WriterContext writerContext = new WriterContext(this, scenarioId);

        Translator translator = this.appObjectClassToTranslatorMap.get(pluginData.getClass());
        if (translator == null) {
            System.out.println("translator was null for: " + pluginData.getClass());
            return;
        }
        translator.writePluginDataOutput(writerContext, pluginData);
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
