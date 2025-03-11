/**
 * Contains a custom Properties implementation to augment the one provided by java.
 * In particular, the FileBasedProperties class contains handy methods to abstract
 * the saving and loading of properties to and from disk. In general, the advantage
 * of this Properties class over Java's is the addition of convenience methods
 * for setting objects of various types other than String, and abstracting away
 * the logic of converting them to/from String values.
 * <p>
 * Additionally, the PropertiesManager class provides a very easy way to specify
 * groups of related properties, and provides a tight coupling with the Forms API
 * to give a very easy way to generate a UI for interacting with these properties.
 * Client code generally just needs to instantiate a list of properties (using
 * AbstractProperty's descendant classes), tie them to a Properties or
 * FileBasedProperties instance, and then PropertiesManager does the rest.
 * </p>
 */
package ca.corbett.extras.properties;
