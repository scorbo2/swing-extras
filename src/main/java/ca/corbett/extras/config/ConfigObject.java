package ca.corbett.extras.config;

import ca.corbett.extras.properties.Properties;

/**
 * Provides a generic, abstract base class for any kind of configuration object.
 * Provides methods for loading and saving these config objects to and from
 * a java.util.Properties instance.
 *
 * @author scorbo2
 * @since 2018-02-04
 */
public abstract class ConfigObject {

    /**
     * Loads all settings from the given Properties object. You can use the optional "prefix"
     * parameter to put a given label at the start of each property name. This allows you
     * to save multiple instances to the same Properties instance, keeping
     * them separated by prefix. For example, if a property name is "enableX" and you
     * supply a prefix of "instance1.", the property value will be saved under the name
     * "instance1.enableX". If you specify null or an empty string for prefix, property names
     * will be specified as-is, and will overwrite any previous value.
     *
     * @param props The Properties instance from which to load.
     * @param prefix An optional string prefix to apply to all property names, or null.
     */
    public abstract void loadFromProps(Properties props, String prefix);

    /**
     * Saves all settings to the given Properties object. You can use the optional "prefix"
     * parameter to put a given label at the start of each property name. This allows you
     * to save multiple instances to the same Properties instance, keeping
     * them separated by prefix. For example, if a property name is "enableX" and you
     * supply a prefix of "instance1.", the property value will be saved under the name
     * "instance1.enableX". If you specify null or an empty string for prefix, property names
     * will be specified as-is, and will overwrite any previous value.
     *
     * @param props The Properties instance to which to save.
     * @param prefix An optional string prefix to apply to all property names, or null.
     */
    public abstract void saveToProps(Properties props, String prefix);

}
