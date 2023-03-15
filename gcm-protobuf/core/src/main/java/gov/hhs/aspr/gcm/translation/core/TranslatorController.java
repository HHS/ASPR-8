package gov.hhs.aspr.gcm.translation.core;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.Descriptors.FieldDescriptor;

import nucleus.PluginData;
import util.graph.Graph;
import util.graph.GraphDepthEvaluator;
import util.graph.Graphs;
import util.graph.MutableGraph;

public class TranslatorController {
    private final Data data;
    private TranslatorCore masterTranslator;
    private final List<PluginData> pluginDatas = new ArrayList<>();
    private final List<Object> objects = new ArrayList<>();
    private final Map<Class<?>, TranslatorModule> simObjectClassToPluginBundleMap = new LinkedHashMap<>();
    private TranslatorModule focalBundle = null;
    private TranslatorModuleId focalPluginBundleId = null;

    private TranslatorController(Data data) {
        this.data = data;
    }

    private static class Data {
        private TranslatorCore.Builder masterTranslatorBuilder = TranslatorCore.builder();
        private final List<TranslatorModule> pluginBundles = new ArrayList<>();

        private Data() {
        }
    }

    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        public TranslatorController build() {
            return new TranslatorController(this.data);
        }

        public Builder addBundle(TranslatorModule pluginBundle) {
            this.data.pluginBundles.add(pluginBundle);
            return this;
        }

        public <I extends Message, S> Builder addTranslator(Translator<I, S> translator) {
            this.data.masterTranslatorBuilder.addTranslator(translator);
            return this;
        }

        public <I extends ProtocolMessageEnum, S> Builder addTranslator(EnumTranslator<I, S> translator) {
            this.data.masterTranslatorBuilder.addTranslator(translator);
            return this;
        }

        public Builder setIgnoringUnknownFields(boolean ignoringUnknownFields) {
            this.data.masterTranslatorBuilder.setIgnoringUnknownFields(ignoringUnknownFields);
            return this;
        }

        public Builder setIncludingDefaultValueFields(boolean includingDefaultValueFields) {
            this.data.masterTranslatorBuilder.setIncludingDefaultValueFields(includingDefaultValueFields);
            return this;
        }

    }

    public static Builder builder() {
        return new Builder(new Data());
    }

    protected <I extends Message, S> void addTranslator(Translator<I, S> translator) {
        this.data.masterTranslatorBuilder.addTranslator(translator);

        this.simObjectClassToPluginBundleMap.put(translator.getSimObjectClass(), this.focalBundle);
    }

    protected <I extends ProtocolMessageEnum, S> void addTranslator(EnumTranslator<I, S> translator) {
        this.data.masterTranslatorBuilder.addTranslator(translator);

        this.simObjectClassToPluginBundleMap.put(translator.getSimObjectClass(), this.focalBundle);
    }

    protected void addFieldToIncludeDefaultValue(FieldDescriptor fieldDescriptor) {
        this.data.masterTranslatorBuilder.addFieldToIncludeDefaultValue(fieldDescriptor);
    }

    protected <U extends Message.Builder> void readPluginDataInput(Reader reader, U builder) {
        PluginData pluginData = this.masterTranslator.readJson(reader, builder);

        this.simObjectClassToPluginBundleMap.putIfAbsent(pluginData.getClass(), this.focalBundle);
        this.pluginDatas.add(pluginData);
    }

    protected <U extends Message.Builder> void readJson(Reader reader, U builder) {
        Object simObject = this.masterTranslator.readJson(reader, builder);

        this.simObjectClassToPluginBundleMap.putIfAbsent(simObject.getClass(), this.focalBundle);
        this.objects.add(simObject);
    }

    protected <T extends PluginData> void writePluginDataOutput(Writer writer, T pluginData) {
        this.masterTranslator.printJson(writer, pluginData);
    }

    protected void writeOutput(Writer writer, Object simObject) {
        this.masterTranslator.printJson(writer, simObject);
    }

    // temporary pass through method
    public TranslatorCore getMasterTranslator() {
        if (this.masterTranslator == null) {
            throw new RuntimeException("master translator is null");
        }

        return this.masterTranslator;
    }

    private void validateMasterTranslator() {
        if (this.masterTranslator == null || !this.masterTranslator.isInitialized()) {
            throw new RuntimeException(
                    "Trying to call readInput() or writeInput() before calling init on the TranslatorController.");
        }
    }

    public TranslatorController init() {
        TranslatorContext translatorContext = new TranslatorContext(this);

        List<TranslatorModule> orderedBundles = this.getOrderedPluginBundles();

        for (TranslatorModule pluginBundle : orderedBundles) {
            this.focalBundle = pluginBundle;
            pluginBundle.getInitializer().accept(translatorContext);
            this.focalBundle = null;
        }

        this.masterTranslator = this.data.masterTranslatorBuilder.build();

        this.masterTranslator.init();

        return this;
    }

    public TranslatorController readInput() {
        validateMasterTranslator();

        ReaderContext readerContext = new ReaderContext(this);

        for (TranslatorModule pluginBundle : this.data.pluginBundles) {
            this.focalBundle = pluginBundle;
            if (!pluginBundle.hasInput())
                continue;
            if (pluginBundle.inputIsPluginData()) {
                pluginBundle.readPluginDataInput(readerContext);
                continue;
            }
            pluginBundle.readInput(readerContext);

            this.focalBundle = null;
        }

        return this;
    }

    public void writeOutput() {
        validateMasterTranslator();

        WriterContext writerContext = new WriterContext(this);

        for (PluginData pluginData : this.pluginDatas) {
            TranslatorModule pluginBundle = this.simObjectClassToPluginBundleMap.get(pluginData.getClass());
            pluginBundle.writePluginDataOutput(writerContext, pluginData);
        }

        for (Object simObject : this.objects) {
            TranslatorModule pluginBundle = this.simObjectClassToPluginBundleMap.get(simObject.getClass());
            pluginBundle.writeOutput(writerContext, simObject);
        }
    }

    public void writeOutput(List<PluginData> pluginDatas) {
        for (PluginData pluginData : pluginDatas) {
            writeOutput(pluginData);
        }
    }

    public void writeOutput(PluginData pluginData) {
        validateMasterTranslator();

        WriterContext writerContext = new WriterContext(this);

        TranslatorModule pluginBundle = this.simObjectClassToPluginBundleMap.get(pluginData.getClass());
        this.focalBundle = pluginBundle;
        pluginBundle.writePluginDataOutput(writerContext, pluginData);
        this.focalBundle = null;
    }

    public List<PluginData> getPluginDatas() {
        return this.pluginDatas;
    }

    public List<Object> getObjects() {
        return this.objects;
    }

    private List<TranslatorModule> getOrderedPluginBundles() {

        MutableGraph<TranslatorModuleId, Object> mutableGraph = new MutableGraph<>();

        Map<TranslatorModuleId, TranslatorModule> pluginBundleMap = new LinkedHashMap<>();

        /*
         * Add the nodes to the graph, check for duplicate ids, build the
         * mapping from plugin id back to plugin
         */
        for (TranslatorModule pluginBundle : this.data.pluginBundles) {
            focalPluginBundleId = pluginBundle.getPluginBundleId();
            pluginBundleMap.put(focalPluginBundleId, pluginBundle);
            // ensure that there are no duplicate plugins
            if (mutableGraph.containsNode(focalPluginBundleId)) {
                // throw new ContractException(NucleusError.DUPLICATE_PLUGIN, focalPluginId);
                throw new RuntimeException("Duplicate PluginBundle");
            }
            mutableGraph.addNode(focalPluginBundleId);
            focalPluginBundleId = null;
        }

        // Add the edges to the graph
        for (TranslatorModule pluginBundle : this.data.pluginBundles) {
            focalPluginBundleId = pluginBundle.getPluginBundleId();
            for (TranslatorModuleId pluginBundleId : pluginBundle.getPluginBundleDependencies()) {
                mutableGraph.addEdge(new Object(), focalPluginBundleId, pluginBundleId);
            }
            focalPluginBundleId = null;
        }

        /*
         * Check for missing plugins from the plugin dependencies that were
         * collected from the known plugins.
         */
        for (TranslatorModuleId pluginBundleId : mutableGraph.getNodes()) {
            if (!pluginBundleMap.containsKey(pluginBundleId)) {
                List<Object> inboundEdges = mutableGraph.getInboundEdges(pluginBundleId);
                StringBuilder sb = new StringBuilder();
                sb.append("cannot locate instance of ");
                sb.append(pluginBundleId);
                sb.append(" needed for ");
                boolean first = true;
                for (Object edge : inboundEdges) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    TranslatorModuleId dependentPluginBundleId = mutableGraph.getOriginNode(edge);
                    sb.append(dependentPluginBundleId);
                }
                // throw new ContractException(NucleusError.MISSING_PLUGIN, sb.toString());
                throw new RuntimeException("Missing Plugin Bundle: " + sb.toString());
            }
        }

        /*
         * Determine whether the graph is acyclic and generate a graph depth
         * evaluator for the graph so that we can determine the order of
         * initialization.
         */
        Optional<GraphDepthEvaluator<TranslatorModuleId>> optional = GraphDepthEvaluator
                .getGraphDepthEvaluator(mutableGraph.toGraph());

        if (!optional.isPresent()) {
            /*
             * Explain in detail why there is a circular dependency
             */

            Graph<TranslatorModuleId, Object> g = mutableGraph.toGraph();

            g = Graphs.getSourceSinkReducedGraph(g);
            g = Graphs.getEdgeReducedGraph(g);
            g = Graphs.getSourceSinkReducedGraph(g);

            List<Graph<TranslatorModuleId, Object>> cutGraphs = Graphs.cutGraph(g);
            StringBuilder sb = new StringBuilder();
            String lineSeparator = System.getProperty("line.separator");
            sb.append(lineSeparator);
            boolean firstCutGraph = true;

            for (Graph<TranslatorModuleId, Object> cutGraph : cutGraphs) {
                if (firstCutGraph) {
                    firstCutGraph = false;
                } else {
                    sb.append(lineSeparator);
                }
                sb.append("Dependency group: ");
                sb.append(lineSeparator);
                Set<TranslatorModuleId> nodes = cutGraph.getNodes().stream()
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                for (TranslatorModuleId node : nodes) {
                    sb.append("\t");
                    sb.append(node);
                    sb.append(" requires:");
                    sb.append(lineSeparator);
                    for (Object edge : cutGraph.getInboundEdges(node)) {
                        TranslatorModuleId dependencyNode = cutGraph.getOriginNode(edge);
                        if (nodes.contains(dependencyNode)) {
                            sb.append("\t");
                            sb.append("\t");
                            sb.append(dependencyNode);
                            sb.append(lineSeparator);
                        }
                    }
                }
            }
            // throw new ContractException(NucleusError.CIRCULAR_PLUGIN_DEPENDENCIES,
            // sb.toString());
            throw new RuntimeException("Circular plugin bundle dependencies");
        }

        // the graph is acyclic, so the depth evaluator is present
        GraphDepthEvaluator<TranslatorModuleId> graphDepthEvaluator = optional.get();

        List<TranslatorModuleId> orderedPluginIds = graphDepthEvaluator.getNodesInRankOrder();

        List<TranslatorModule> orderedPlugins = new ArrayList<>();
        for (TranslatorModuleId pluginId : orderedPluginIds) {
            orderedPlugins.add(pluginBundleMap.get(pluginId));
        }

        return orderedPlugins;
    }
}
