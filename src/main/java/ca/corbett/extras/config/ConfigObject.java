package ca.corbett.extras.config;

import ca.corbett.extras.properties.Properties;

/**
 * Implement this interface to signify that your class is capable of loading
 * and saving its current state to or from a Properties instance.
 *
 * @deprecated Consider using AbstractProperty instead! This class may be removed in a future release!
 * @author scorbo2
 * @since 2018-02-04
 */
public interface ConfigObject {

    /**
     * Loads all settings from the given Properties object. You can use the optional "prefix"
     * parameter to put a given label at the start of each property name. This allows you
     * to save multiple instances to the same Properties instance, keeping
     * them separated by prefix. For example, if a property name is "enableX" and you
     * supply a prefix of "instance1.", the property value will be saved under the name
     * "instance1.enableX". If you specify null or an empty string for prefix, property names
     * will be specified as-is, and will overwrite any previous value.
     *
     * @param props  The Properties instance from which to load.
     * @param prefix An optional string prefix to apply to all property names, or null.
     */
    void loadFromProps(Properties props, String prefix);

    /**
     * Saves all settings to the given Properties object. You can use the optional "prefix"
     * parameter to put a given label at the start of each property name. This allows you
     * to save multiple instances to the same Properties instance, keeping
     * them separated by prefix. For example, if a property name is "enableX" and you
     * supply a prefix of "instance1.", the property value will be saved under the name
     * "instance1.enableX". If you specify null or an empty string for prefix, property names
     * will be specified as-is, and will overwrite any previous value.
     *
     * @param props  The Properties instance to which to save.
     * @param prefix An optional string prefix to apply to all property names, or null.
     */
    void saveToProps(Properties props, String prefix);

}
