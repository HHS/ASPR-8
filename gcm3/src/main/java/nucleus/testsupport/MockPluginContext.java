package nucleus.testsupport;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.ResolverContext;
import nucleus.ResolverId;

/**
 * A mock implementation of a {@link PluginContext} that allows for the
 * retrieval of plugin dependencies and resolvers recorded during the
 * initialization phase of a plugin.
 */
public final class MockPluginContext implements PluginContext {

	private Set<PluginId> pluginDependencies = new LinkedHashSet<>();
	private Map<ResolverId, Consumer<ResolverContext>> resolverMap = new LinkedHashMap<>();

	/**
	 * Returns the set of plugin id values collected by this context.
	 */
	public Set<PluginId> getPluginDependencies() {
		return new LinkedHashSet<>(pluginDependencies);
	}

	/**
	 * Returns a map from resolver id to context consumer collected by this
	 * context
	 */
	public Map<ResolverId, Consumer<ResolverContext>> getResolverMap() {
		return new LinkedHashMap<>(resolverMap);
	}

	/**
	 * Adds a plugin id to this context
	 * 
	 * @throws RuntimeException
	 * <li>if the plugin id is null</li>
	 * 
	 */
	@Override
	public void addPluginDependency(PluginId pluginId) {
		if(pluginId==null) {
			throw new RuntimeException("null plugin id");
		}
		pluginDependencies.add(pluginId);
	}

	/**
	 * Adds an event resolver for the given resolver id.
	 * 
	 * @throws RuntimeException
	 * <li>if the resolver id is null</li>
	 * <li>if the context consumer is null</li>
	 * 
	 */
	@Override
	public void defineResolver(ResolverId resolverId, Consumer<ResolverContext> init) {
		if(resolverId == null) {
			throw new RuntimeException("null resolver id");
		}
		if(init == null) {
			throw new RuntimeException("null context consumer");
		}
		resolverMap.put(resolverId, init);
	}

}
