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

import util.errors.ContractException;
import util.graph.Graph;
import util.graph.GraphDepthEvaluator;
import util.graph.Graphs;
import util.graph.MutableGraph;

/**
 * The TranslatorController serves as the master of cerimonies for translating
 * between two
 * types of objects. Additionally, it has the ability to distribute Input/Output
 * files for reading and writing.
 */
public class TranslationController {
    private final Data data;
    private TranslationEngine translatorCore;
    private final List<Object> objects = Collections.synchronizedList(new ArrayList<>());
    private TranslatorId focalTranslatorId = null;

    private TranslationController(Data data) {
        this.data = data;
    }

    private static class Data {
        private TranslationEngine.Builder translatorCoreBuilder;
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

        private void validateClassRefNotNull(Class<?> classRef) {
            if (classRef == null) {
                throw new ContractException(CoreTranslationError.NULL_CLASS_REF);
            }
        }

        private void validateFilePathNotNull(Path filePath) {
            if (filePath == null) {
                throw new ContractException(CoreTranslationError.NULL_PATH);
            }
        }

        private void validatePathNotDuplicate(Path filePath, boolean in, boolean out) {
            if (in && this.data.inputFilePathMap.containsKey(filePath)) {
                throw new ContractException(CoreTranslationError.DUPLICATE_INPUT_PATH);
            }

            if (out && this.data.outputFilePathMap.values().contains(filePath)) {
                throw new ContractException(CoreTranslationError.DUPLICATE_OUTPUT_PATH);
            }
        }

        private void validateTranslatorNotNull(Translator translator) {
            if (translator == null) {
                throw new ContractException(CoreTranslationError.NULL_TRANSLATOR);
            }
        }

        private void validateTranslatorCoreBuilderNotNull(TranslationEngine.Builder translatorCoreBuilder) {
            if (translatorCoreBuilder == null) {
                throw new ContractException(CoreTranslationError.NULL_TRANSLATORCORE_BUILDER);
            }
        }

        /**
         * Builds the TranslatorController.
         * 
         * Calls the initializer on each added {@link Translator}
         * 
         * @throws ContractException
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATORCORE_BUILDER}
         *                           if translatorCoreBuilder has not been set</li>
         */
        public TranslationController build() {
            validateTranslatorCoreBuilderNotNull(this.data.translatorCoreBuilder);

            TranslationController translatorController = new TranslationController(this.data);

            translatorController.initTranslators();
            return translatorController;
        }

        /**
         * Adds the path and class ref to be read from after building via
         * {@link TranslationController#readInput()}
         * 
         * @throws ContractException
         *                           <li>{@linkplain CoreTranslationError#NULL_PATH}
         *                           if filePath is null</li>
         *                           <li>{@linkplain CoreTranslationError#NULL_CLASS_REF}
         *                           if classRef is null</li>
         *                           <li>{@linkplain CoreTranslationError#DUPLICATE_INPUT_PATH}
         *                           if filePath has already been added</li>
         *                           <li>{@linkplain CoreTranslationError#INVALID_INPUT_PATH}
         *                           if filePath does not exist on the system</li>
         */
        public Builder addInputFilePath(Path filePath, Class<?> classRef) {
            validateFilePathNotNull(filePath);
            validateClassRefNotNull(classRef);
            validatePathNotDuplicate(filePath, true, false);

            if (!filePath.toFile().exists()) {
                throw new ContractException(CoreTranslationError.INVALID_INPUT_PATH);
            }

            this.data.inputFilePathMap.put(filePath, classRef);
            return this;
        }

        /**
         * Adds the path and class ref to be written to after building via
         * {@link TranslationController#writeOutput} with a scenario id of 0
         * 
         * @throws ContractException
         *                           <li>{@linkplain CoreTranslationError#NULL_PATH}
         *                           if filePath is null</li>
         *                           <li>{@linkplain CoreTranslationError#NULL_CLASS_REF}
         *                           if classRef is null</li>
         *                           <li>{@linkplain CoreTranslationError#DUPLICATE_OUTPUT_PATH}
         *                           if filePath has already been added</li>
         *                           <li>{@linkplain CoreTranslationError#DUPLICATE_CLASSREF_SCENARIO_PAIR}
         *                           if the classRef and scenarioId pair has already
         *                           been added</li>
         *                           <li>{@linkplain CoreTranslationError#INVALID_OUTPUT_PATH}
         *                           if filePath does not exist on the system</li>
         */
        public Builder addOutputFilePath(Path filePath, Class<?> classRef) {
            return this.addOutputFilePath(filePath, classRef, 0);
        }

        /**
         * Adds the path and class ref to be written to after building via
         * {@link TranslationController#writeOutput} with the given scenarioId
         * 
         * @throws ContractException
         *                           <li>{@linkplain CoreTranslationError#NULL_PATH}
         *                           if filePath is null</li>
         *                           <li>{@linkplain CoreTranslationError#NULL_CLASS_REF}
         *                           if classRef is null</li>
         *                           <li>{@linkplain CoreTranslationError#DUPLICATE_OUTPUT_PATH}
         *                           if filePath has already been added</li>
         *                           <li>{@linkplain CoreTranslationError#DUPLICATE_CLASSREF_SCENARIO_PAIR}
         *                           if the classRef and scenarioId pair has already
         *                           been added</li>
         *                           <li>{@linkplain CoreTranslationError#INVALID_OUTPUT_PATH}
         *                           if filePath does not exist on the system</li>
         */
        public Builder addOutputFilePath(Path filePath, Class<?> classRef, Integer scenarioId) {
            validateFilePathNotNull(filePath);
            validateClassRefNotNull(classRef);
            validatePathNotDuplicate(filePath, false, true);

            Pair<Class<?>, Integer> key = new Pair<>(classRef, scenarioId);

            if (this.data.outputFilePathMap.containsKey(key)) {
                throw new ContractException(CoreTranslationError.DUPLICATE_CLASSREF_SCENARIO_PAIR);
            }

            if (!filePath.getParent().toFile().exists()) {
                throw new ContractException(CoreTranslationError.INVALID_OUTPUT_PATH);
            }

            this.data.outputFilePathMap.put(key, filePath);
            return this;
        }

        /**
         * Adds the given classRef markerInterace mapping.
         * 
         * <li>explicitly used when calling
         * {@link TranslationController#writeOutput} with a class for which a classRef
         * ScenarioId pair does not exist and/or the need to output the given class as
         * the markerInterface instead of the concrete class
         * 
         * @param <M> the childClass
         * @param <U> the parentClass/MarkerInterfaceClass
         * 
         * @throws ContractException
         *                           <li>{@linkplain CoreTranslationError#NULL_CLASS_REF}
         *                           if classRef is null or if markerInterface is
         *                           null</li>
         */
        public <M extends U, U> Builder addMarkerInterface(Class<M> classRef, Class<U> markerInterface) {
            validateClassRefNotNull(classRef);
            validateClassRefNotNull(markerInterface);

            if (this.data.markerInterfaceClassMap.containsKey(classRef)) {
                throw new ContractException(CoreTranslationError.DUPLICATE_CLASSREF);
            }

            this.data.markerInterfaceClassMap.put(classRef, markerInterface);
            return this;
        }

        /**
         * Add a {@link Translator}
         * 
         * @throws ContractException
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATOR}
         *                           if translator is null</li>
         */
        public Builder addTranslator(Translator translator) {
            validateTranslatorNotNull(translator);

            if (this.data.translators.contains(translator)) {
                throw new ContractException(CoreTranslationError.DUPLICATE_TRANSLATOR);
            }

            this.data.translators.add(translator);
            return this;
        }

        /**
         * Sets the {@link TranslationEngine.Builder}
         * 
         * @throws ContractException
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATORCORE_BUILDER}
         *                           if translatorCoreBuilder is null</li>
         */
        public Builder setTranslatorCoreBuilder(TranslationEngine.Builder translatorCoreBuilder) {
            validateTranslatorCoreBuilderNotNull(translatorCoreBuilder);

            this.data.translatorCoreBuilder = translatorCoreBuilder;
            return this;
        }
    }

    /**
     * Returns a new instance of Builder
     */
    public static Builder builder() {
        return new Builder(new Data());
    }

    /**
     * Returns the translatorCoreBuilder if and only if it is set, has not had it
     * build method called, has not had it's init method called and is of the same
     * type as the given classRef
     * 
     * @param <T> the class type of the TranslatorCore.Builder
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#INVALID_TRANSLATORCORE_BUILDER}
     *                           if the given classRef does not match the class of
     *                           the translatorCoreBuilder
     *                           null</li>
     */
    protected <T extends TranslationEngine.Builder> T getTranslatorCoreBuilder(Class<T> classRef) {
        if (this.translatorCore == null) {
            if (this.data.translatorCoreBuilder.getClass() == classRef) {
                return classRef.cast(this.data.translatorCoreBuilder);
            }

            throw new ContractException(CoreTranslationError.INVALID_TRANSLATORCORE_BUILDER,
                    "The TranslatorCore is of type: " + this.data.translatorCoreBuilder.getClass().getName()
                            + " and the given classRef was: " + classRef.getName());
        }
        // This should never happen, therefore it is an actual runtime exception and not
        // a contract exception
        throw new RuntimeException(
                "Trying to get TranslatorCoreBuilder after it was built and/or initialized");

    }

    /**
     * Adds the given classRef and MarkerInterface to the internal list.
     * Only callable through a {@link TranslatorContext} via the
     * {@link Translator#getInitializer()} consumer
     * 
     * @param <M> the childClass
     * @param <U> the parentClass/MarkerInterfaceClass
     */
    protected <M extends U, U> void addMarkerInterface(Class<M> classRef, Class<U> markerInterface) {
        this.data.markerInterfaceClassMap.put(classRef, markerInterface);
    }

    /**
     * Passes the given reader and inputClassRef to the built {@link TranslationEngine}
     * to read, parse and translate the inputData.
     * 
     * @param <U> the classType associated with the reader
     */
    private <U> void readInput(Reader reader, Class<U> inputClassRef) {
        Object appObject = this.translatorCore.readInput(reader, inputClassRef);

        this.objects.add(appObject);
    }

    /**
     * Passes the given writer object and optional superClass to the built
     * {@link TranslationEngine}
     * to translate and write to the outputFile
     * 
     * @param <M> the class of the object to write to the outputFile
     * @param <U> the optional parent class of the object to write to the outputFile
     */
    private <M extends U, U> void writeOutput(Writer writer, M object, Optional<Class<U>> superClass) {
        this.translatorCore.writeOutput(writer, object, superClass);
    }

    private void validateCoreTranslator() {
        if (this.translatorCore == null) {
            throw new ContractException(CoreTranslationError.NULL_TRANSLATORCORE);
        }

        /*
         * Because the translatorCore's init method is called within the
         * initTranslators() method, this should never happen, thus it is a
         * RuntimeException and not a ContractException
         */
        if (!this.translatorCore.isInitialized()) {
            throw new RuntimeException("TranslatorCore has been built but has not been initialized.");
        }
    }

    /*
     * First gets an ordered list of translators based on their dependencies
     * Then calls each translator's init callback method
     * Then builds the translatorCore
     * Then calls the init method on the translatorCore
     * Verifies that all translatorSpecs have been initialized
     */
    private TranslationController initTranslators() {
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

    /**
     * Reads all provided inputFilePaths in a Parrallel manner via a parallelStream
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATORCORE}
     *                           if translatorCore is null</li>
     */
    public TranslationController readInputParrallel() {
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

    /**
     * Creates readers for each inputFilePath and passes the reader and classRef to
     * the TranslatorCore via {@link TranslationController#readInput(Reader, Class)}
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATORCORE}
     *                           if translatorCore is null</li>
     */
    public TranslationController readInput() {
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

    /**
     * Given the classRef and scenarioId, find the given outputFilePath.
     * If the classRef Scenario pair has been added, that is returned.
     * 
     * Otherwise, checks to see if the classRef exists in the
     * markerInterfaceClassMap and if so, returns the resulting classRef scenarioId
     * pair
     * 
     * @param <M> the childClass
     * @param <U> the optional parentClass/MarkerInterfaceClass
     */
    private <M extends U, U> Pair<Path, Optional<Class<U>>> getOutputPath(Class<M> classRef, Integer scenarioId) {
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

        throw new ContractException(CoreTranslationError.INVALID_OUTPUT_CLASSREF,
                "No path was provided for " + classRef.getName());
    }

    /**
     * takes the list of objects and writes each object out to it's corresponding
     * outputFilePath, if it exists
     * 
     * <li>internally calls {@link TranslationController#writeOutput(Object)}
     * 
     * @param <T> the type of the list of obects to write to output
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#INVALID_OUTPUT_CLASSREF}
     *                           if the class of an object in the list does not have
     *                           a associated outputFilePath</li>
     *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATORCORE}
     *                           if translatorCore is null</li>
     */
    public <T> void writeOutput(List<T> objects) {
        for (T object : objects) {
            this.writeOutput(object);
        }
    }

    /**
     * takes the list of objects with the specified scenarioId and writes each
     * object out to it's corresponding
     * outputFilePath, if it exists
     * 
     * <li>internally calls
     * {@link TranslationController#writeOutput(Object, Integer)}
     * 
     * @param <T> the type of the list of obects to write to output
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#INVALID_OUTPUT_CLASSREF}
     *                           if the class of an object in the list paired with
     *                           the scenarioId does not have
     *                           a associated outputFilePath</li>
     *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATORCORE}
     *                           if translatorCore is null</li>
     */
    public <T> void writeOutput(List<T> objects, Integer scenarioId) {
        for (T object : objects) {
            this.writeOutput(object, scenarioId);
        }
    }

    /**
     * takes the given object and writes it out to it's corresponding
     * outputFilePath, if it exists
     * <li>internally calls
     * {@link TranslationController#writeOutput(Object, Integer)}
     * with a scenarioId of 0
     * 
     * @param <T> the type of the list of obects to write to output
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#INVALID_OUTPUT_CLASSREF}
     *                           if the class of the object does not have a
     *                           associated outputFilePath</li>
     *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATORCORE}
     *                           if translatorCore is null</li>
     */
    public <T> void writeOutput(T object) {
        this.writeOutput(object, 0);
    }

    /**
     * takes the given object and scenarioId pair and writes it out to it's
     * corresponding
     * outputFilePath, if it exists
     * <li>internally calls
     * {@link TranslationController#writeOutput(Object, Integer)}
     * with a scenarioId of 0
     * 
     * @param <M> the classType of the object
     * @param <U> the optional type of the parent class of the object
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#INVALID_OUTPUT_CLASSREF}
     *                           if the class of the object paired with the
     *                           scenarioId does not have a associated
     *                           outputFilePath</li>
     *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATORCORE}
     *                           if translatorCore is null</li>
     */
    @SuppressWarnings("unchecked")
    public <M extends U, U> void writeOutput(M object, Integer scenarioId) {
        validateCoreTranslator();
        // this gives an unchecked warning, surprisingly
        Class<M> classRef = (Class<M>) object.getClass();

        Pair<Path, Optional<Class<U>>> pathPair = getOutputPath(classRef, scenarioId);
        Path path = pathPair.getFirst();

        try {
            this.writeOutput(new FileWriter(path.toFile()), object, pathPair.getSecond());
        } catch (IOException e) {
            throw new RuntimeException("Unable to create writer for file: " + path.toString(), e);
        }
    }

    /**
     * Searches the list of read in objects and returns the first Object found of
     * the given classRef
     * 
     * @param <T> the type of the obect to get
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_CLASSREF}
     *                           if no object with the specified class is found</li>
     */
    public <T> T getFirstObject(Class<T> classRef) {
        for (Object object : this.objects) {
            if (classRef.isAssignableFrom(object.getClass())) {
                return classRef.cast(object);
            }
        }

        throw new ContractException(CoreTranslationError.UNKNOWN_CLASSREF);
    }

    /**
     * Searches the list of read in objects and returns all Objects found with the
     * given classRef
     * 
     * @param <T> the type of the obect to get
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_CLASSREF}
     *                           if no object with the specified class is found</li>
     */
    public <T> List<T> getObjects(Class<T> classRef) {
        List<T> objects = new ArrayList<>();
        for (Object object : this.objects) {
            if (classRef.isAssignableFrom(object.getClass())) {
                objects.add(classRef.cast(object));
            }
        }

        if (objects.size() == 0) {
            throw new ContractException(CoreTranslationError.UNKNOWN_CLASSREF);
        }

        return objects;
    }

    /**
     * Returns the entire list of read in objects
     */
    public List<Object> getObjects() {
        return this.objects;
    }

    /*
     * Goes through the list of translators and orders them based on their
     * dependencies
     */
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
