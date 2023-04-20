package gov.hhs.aspr.translation.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
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

import util.graph.Graph;
import util.graph.GraphDepthEvaluator;
import util.graph.Graphs;
import util.graph.MutableGraph;

public class TranslatorController {
    private final Data data;
    private TranslatorCore translatorCore;
    private final List<Object> objects = Collections.synchronizedList(new ArrayList<>());
    private TranslatorId focalTranslatorId = null;

    private TranslatorController(Data data) {
        this.data = data;
    }

    private static class Data {
        private TranslatorCore.Builder translatorCoreBuilder;
        private final List<Translator> translators = new ArrayList<>();
        private final Map<Path, Class<?>> inputFilePathMap = new LinkedHashMap<>();
        private final Map<Pair<Class<?>, Integer>, Path> outputFilePathMap = new LinkedHashMap<>();
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
            if (this.data.translatorCoreBuilder == null) {
                throw new RuntimeException("Did not set the TranslatorCore Builder");
            }
            TranslatorController translatorController = new TranslatorController(this.data);

            translatorController.initTranslators();
            return translatorController;
        }

        public Builder addInputFilePath(Path filePath, Class<?> classRef) {
            this.data.inputFilePathMap.put(filePath, classRef);
            return this;
        }

        public Builder addOutputFilePath(Path filePath, Class<?> classRef) {
            return this.addOutputFilePath(filePath, classRef, 0);
        }

        public Builder addOutputFilePath(Path filePath, Class<?> classRef, Integer scenarioId) {
            Pair<Class<?>, Integer> key = new Pair<>(classRef, scenarioId);

            if (this.data.outputFilePathMap.containsKey(key)) {
                throw new RuntimeException("Attempted to overwrite an existing output file.");
            }

            this.data.outputFilePathMap.put(key, filePath);
            return this;
        }

        public <U, M extends U> Builder addMarkerInterface(Class<M> classRef, Class<U> markerInterface) {
            this.data.markerInterfaceClassMap.put(classRef, markerInterface);
            return this;
        }

        public Builder addTranslator(Translator translator) {
            this.data.translators.add(translator);
            return this;
        }

        public Builder setTranslatorCoreBuilder(TranslatorCore.Builder translatorCoreBuilder) {
            this.data.translatorCoreBuilder = translatorCoreBuilder;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder(new Data());
    }

    protected <T extends TranslatorCore.Builder> T getTranslatorCoreBuilder(Class<T> classRef) {
        if (this.translatorCore == null) {
            if (this.data.translatorCoreBuilder.getClass() == classRef) {
                return classRef.cast(this.data.translatorCoreBuilder);
            }

            throw new RuntimeException(
                    "The TranslatorCore is of type: " + this.data.translatorCoreBuilder.getClass().getName()
                            + " and the given classRef was: " + classRef.getName());
        }
        throw new RuntimeException(
                "Trying to get TranslatorCoreBuilder after it was built and/or initialized");

    }

    protected <T, U extends T> void addMarkerInterface(Class<U> classRef, Class<T> markerInterface) {
        this.data.markerInterfaceClassMap.put(classRef, markerInterface);
    }

    protected <U> void readInput(Reader reader, Class<U> inputClassRef) {
        Object simObject = this.translatorCore.readInput(reader, inputClassRef);

        this.objects.add(simObject);
    }

    protected <M extends U, U> void writeOutput(Writer writer, M simObject, Optional<Class<U>> superClass) {
        this.translatorCore.writeOutput(writer, simObject, superClass);
    }

    private void validateCoreTranslator() {
        if (this.translatorCore == null) {
            throw new RuntimeException(
                    "TranslatorCore has not been built");
        }

        if (!this.translatorCore.isInitialized()) {
            throw new RuntimeException("TranslatorCore has been built but has not been initialized.");
        }
    }

    private TranslatorController initTranslators() {
        TranslatorContext translatorContext = new TranslatorContext(this);

        List<Translator> orderedTranslators = this.getOrderedTranslators();

        for (Translator translator : orderedTranslators) {
            translator.getInitializer().accept(translatorContext);
        }

        this.translatorCore = this.data.translatorCoreBuilder.build();

        this.translatorCore.init();

        this.translatorCore.translatorSpecsAreInitialized();

        return this;
    }

    public TranslatorController readInputParrallel() {
        validateCoreTranslator();

        this.data.inputFilePathMap.keySet().parallelStream().forEach(path -> {
            Class<?> classRef = this.data.inputFilePathMap.get(path);
            Reader reader;
            try {
                reader = new FileReader(path.toFile());
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to create Reader", e);
            }

            this.readInput(reader, classRef);
        });

        return this;
    }

    public TranslatorController readInput() {
        validateCoreTranslator();

        for (Path path : this.data.inputFilePathMap.keySet()) {
            Class<?> classRef = this.data.inputFilePathMap.get(path);

            Reader reader;
            try {
                reader = new FileReader(path.toFile());
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to create Reader", e);
            }

            this.readInput(reader, classRef);
        }

        return this;
    }

    private <U, M extends U> Pair<Path, Optional<Class<U>>> getOutputPath(Class<M> classRef, Integer scenarioId) {
        Pair<Class<?>, Integer> key = new Pair<>(classRef, scenarioId);

        if (this.data.outputFilePathMap.containsKey(key)) {
            return new Pair<>(this.data.outputFilePathMap.get(key), Optional.empty());
        }

        if (this.data.markerInterfaceClassMap.containsKey(classRef)) {
            // can safely cast because of type checking when adding to the
            // markerInterfaceClassMap
            @SuppressWarnings("unchecked")
            Class<U> markerInterfaceClass = (Class<U>) this.data.markerInterfaceClassMap.get(classRef);

            key = new Pair<>(markerInterfaceClass, scenarioId);

            if (this.data.outputFilePathMap.containsKey(key)) {
                return new Pair<>(this.data.outputFilePathMap.get(key), Optional.of(markerInterfaceClass));
            }
        }

        throw new RuntimeException("No path was provided for " + classRef.getName());
    }

    @SuppressWarnings("unchecked")
    public <U, M extends U> void writeOutput() {
        validateCoreTranslator();

        if (this.objects.isEmpty()) {
            throw new RuntimeException("Calling this method without having also called readInput() is not allowed.");
        }

        int scenarioId = 0;

        for (int i = 0; i < this.objects.size(); i++) {
            // use generics instead of Object for consistency
            M object = (M) this.objects.get(i);
            Class<M> classRef = (Class<M>) object.getClass();

            Pair<Path, Optional<Class<U>>> pathPair = getOutputPath(classRef, scenarioId);
            Path path = pathPair.getFirst();

            try {
                this.writeOutput(new FileWriter(path.toFile()), object, pathPair.getSecond());
            } catch (IOException e) {
                throw new RuntimeException("Unable to create writer for file: " + path.toString(), e);
            }
        }

    }

    public <T> void writeOutput(List<T> objects) {
        for (T object : objects) {
            this.writeOutput(object);
        }
    }

    public <T> void writeOutput(List<T> objects, Integer scenarioId) {
        for (T object : objects) {
            this.writeOutput(object, scenarioId);
        }
    }

    public <T> void writeOutput(T object) {
        this.writeOutput(object, 0);
    }

    @SuppressWarnings("unchecked")
    public <U, T extends U> void writeOutput(T object, Integer scenarioId) {
        validateCoreTranslator();
        // this gives an unchecked warning, surprisingly
        Class<T> classRef = (Class<T>) object.getClass();

        Pair<Path, Optional<Class<U>>> pathPair = getOutputPath(classRef, scenarioId);
        Path path = pathPair.getFirst();

        try {
            this.writeOutput(new FileWriter(path.toFile()), object, pathPair.getSecond());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create writer for file: " + path.toString(), e);
        }
    }

    public <T> T getObject(Class<T> classRef) {
        for (Object object : this.objects) {
            if (classRef.isAssignableFrom(object.getClass())) {
                return classRef.cast(object);
            }
        }

        throw new RuntimeException("Unable to find the specified Object");
    }

    public <T> List<T> getObjects(Class<T> classRef) {
        List<T> objects = new ArrayList<>();
        for (Object object : this.objects) {
            if (classRef.isAssignableFrom(object.getClass())) {
                objects.add(classRef.cast(object));
            }
        }

        return objects;
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
