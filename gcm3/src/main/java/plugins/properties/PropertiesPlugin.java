package plugins.properties;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import util.ContractException;

/**
 *
 * <p>
 * <b>Summary</b> A nucleus plugin that provides various support classes for
 * managing collections of typed data that are more memory efficient than maps
 * while maintaining reasonable performance.
 * </p>
 *
 * <p>
 * <b>Events </b> The plugin supports no events.
 *
 * <p>
 * <b>Resolvers</b> The plugin has no event resolvers.
 * </p>
 *
 * <p>
 * <b>Data Views</b> The plugin has no data views
 * </p>
 *
 * <p>
 * <b>Reports</b> The plugin defines no reports
 * </p>
 *
 * <p>
 * <b>Agents: </b>This plugin provides no agent implementations.
 * </p>
 *
 * <p>
 * <b>Initializing data:</b> The plugin has no initializing data
 * </p>
 *
 * <p>
 * <b>Support classes</b>
 * <ul>
 * 
 * <li><b>AbstractIndexedPropertyManager</b></li> Abstract base class for all
 * IndexedPropertyManager implementors
 * 
 * <li><b>BooleanPropertyManager</b></li>Implementor of IndexedPropertyManager
 * that compresses Boolean property values into a bit-based data structure.
 * 
 * <li><b>DoublePropertyManager</b></li>Implementor of IndexedPropertyManager
 * that compresses Double property values into a double[]-based data structure.
 * 
 * <li><b>EnumPropertyManager</b></li>Implementor of IndexedPropertyManager that
 * compresses Enum property values into a byte-based data structure of the
 * various int-like primitives.
 * 
 * <li><b>FloatPropertyManager</b></li>Implementor of IndexedPropertyManager
 * that compresses Float property values into a float[]-based data structure.
 * 
 * <li><b>IndexedPropertyManager</b></li>Common interface to all property
 * managers. A property manager manages property values associated with
 * int-based identifiers.
 * 
 * <li><b>IntPropertyManager</b></li>Implementor of IndexedPropertyManager that
 * compresses Byte, Short, Integer or Long property values into a byte-based
 * array data structure.
 * 
 * <li><b>ObjectPropertyManager</b></li>Implementor of IndexedPropertyManager
 * that stores Object property values in an Object array based data structure.
 * 
 * <li><b>PropertyDefinition</b></li>A thread-safe, immutable class that defines
 * a property, but does not indicate the role that property is playing or the
 * identifier of the property.
 * 
 * <li><b>PropertyError</b></li>An enumeration supporting
 * {@link ContractException} that acts as a general description of the
 * exception.
 * 
 * <li><b>PropertyValueRecord</b></li>A utility class for holding the value and
 * assignment time for a property.
 * 
 * <li><b>TimeTrackingPolicy</b></li>An enumeration used to control the tracking
 * of assignment times of properties and other values. * 
 * 
 * </ul>
 * </p>
 *
 * <p>
 * <b>Required Plugins</b> This plugin has no plugin dependencies
 * </p>
 *
 * @author Shawn Hatch
 *
 */
public final class PropertiesPlugin {
	public final static PluginId PLUGIN_ID = new SimplePluginId(PropertiesPlugin.class);

	public void init(PluginContext pluginContext) {

	}
}
