package ca.corbett.extras.properties;

import ca.corbett.extras.properties.dialog.PropertiesDialog;
import ca.corbett.forms.fields.FormField;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a highly configurable wrapper around a set of related properties,
 * along with ways to manage them and present them to the user for viewing and modification.
 * The intention is that all applications go through an instance of this class so that
 * application preferences are handled in a consistent way. This can be used
 * for managing properties of any supported type in a generic and configurable way.
 * <p>
 * PropertiesManager can be used directly, but the more typical case is for applications
 * to use AppProperties (or a derived class), which binds together a PropertiesManager instance
 * with an ExtensionManager instance. AppProperties is extremely useful in saving a lot
 * of work for client code. Specifically, AppProperties can handle the auto-generation
 * of a PropertiesDialog for viewing and editing your application's properties.
 * </p>
 * <p>
 * For more information, refer to the <a href="https://www.corbett.ca/swing-extras-book/">swing-extras book</a>,
 * or check out the built-in demo application that comes with this library!
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 1.8, 2024-12-30
 */
public class PropertiesManager {

    protected static final Logger logger = Logger.getLogger(PropertiesManager.class.getName());
    protected final Properties propertiesInstance;
    protected final List<AbstractProperty> properties;
    protected final String name;

    /**
     * Creates a PropertiesManager instance backed onto the given File object, and a list
     * of AbstractProperty objects that we will manage.
     *
     * @param propsFile The properties file. Does not need to exist - will be created on save()
     * @param props     A List of AbstractProperty instances to be managed by this class.
     * @param name      A name for this property collection. Comment header for the props file.
     */
    public PropertiesManager(File propsFile, List<AbstractProperty> props, String name) {
        this.propertiesInstance = new FileBasedProperties(propsFile);
        this.properties = props;
        this.name = name;

        try {
            if (propsFile.exists()) {
                ((FileBasedProperties)propertiesInstance).load();
            }
            else {
                ((FileBasedProperties)propertiesInstance).save();
            }
        }
        catch (IOException ioe) {
            logger.log(Level.SEVERE, "Caught exception loading properties: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Creates a PropertiesManager instance backed onto the given Properties object (which
     * may be a FileBasedProperties instance for disk persistence, or a Properties instance
     * for in-memory), and a list of AbstractProperty objects that we will manage.
     *
     * @param propsInstance Any instance of Properties. Will be used for storing and retrieving.
     * @param props         A List of AbstractProperty instances to be managed by this class.
     * @param name          A name for this property collection. Comment header for the propsInstance file.
     */
    public PropertiesManager(Properties propsInstance, List<AbstractProperty> props, String name) {
        this.propertiesInstance = propsInstance;
        this.properties = props;
        this.name = name;
    }

    /**
     * Returns the name of this PropertiesManager. This String is used as a comment header
     * when writing properties files (assuming our Properties instance is a FileBasedProperties).
     *
     * @return The name of this PropertiesManager.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a list of all property categories of all non-hidden properties in this
     * PropertiesManager, in the order they were given to the constructor. We don't sort
     * them alphabetically so that applications can define their own preferred order.
     *
     * @return A list of one or more property categories.
     */
    public List<String> getCategories() {
        return getCategories(properties);
    }

    /**
     * Returns a list of all subcategories contained within the named property category.
     * If the specified category does not exist, the list will be empty.
     *
     * @param category The name of the category to check.
     * @return A list of zero or more subcategories for the named category.
     */
    public List<String> getSubcategories(String category) {
        return getSubcategories(category, properties);
    }

    /**
     * Returns the underlying Properties instance that this manager manages.
     * This can be a shorthand for getting access to specific properties.
     * For example, instead of:
     * <blockquote><pre>
     *   IntegerProperty someInt = (IntegerProperty)propsManager.getProperty("a.b.c");
     *   if (someInt != null) {
     *     return someInt.getValue();
     *   }
     *   return someDefaultValue;
     * </pre></blockquote>
     * You can instead do:
     * <blockquote><pre>
     *   return propsManager.getPropertiesInstance().getInteger("a.b.c", someDefaultValue);
     * </pre></blockquote>
     *
     * @return the Properties instance that this manager manages.
     */
    public Properties getPropertiesInstance() {
        return propertiesInstance;
    }

    /**
     * Returns a list of all non-hidden PropertyFields in the given category and subcategory.
     * The list may be empty if either the category or subcategory do not exist.
     *
     * @param category    The name of the property category to check.
     * @param subCategory The name of the subcategory to check.
     * @return A list of all AbstractProperty objects in that category/subcategory.
     */
    public List<AbstractProperty> getProperties(String category, String subCategory) {
        return getProperties(properties, category, subCategory);
    }

    /**
     * Returns a specific AbstractProperty by its fully qualified name, if it exists.
     *
     * @param fullyQualifiedName The identifier of the property in question.
     * @return The property object, or null if not found.
     */
    public AbstractProperty getProperty(String fullyQualifiedName) {
        for (AbstractProperty props : properties) {
            if (props.getFullyQualifiedName().equals(fullyQualifiedName)) {
                return props;
            }
        }
        return null;
    }

    /**
     * Loads the value of each of our properties from the Properties instance we were supplied in
     * our constructor, overwriting any current values. The current values of our properties
     * are used as default values in the event that the property in question is not present
     * in the Properties instance.
     *
     * @throws Exception If our Properties instance is file based, we might get an IOException.
     */
    public void load() throws Exception {
        if (propertiesInstance instanceof FileBasedProperties) {
            ((FileBasedProperties)propertiesInstance).load();
        }

        // Look up each of our properties in the propertiesInstance instance:
        for (AbstractProperty prop : properties) {
            prop.loadFromProps(propertiesInstance);
        }
    }

    /**
     * Saves the value of each of our properties to the Properties instance we were supplied in the
     * constructor, overwriting any previously saved values. The output list will be
     * alphabetically sorted by fully qualified property name, making for a (hopefully) easy
     * to read properties file.
     * <p>
     * <b>Note:</b> If our Properties instance is not a FileBasedProperties instance, then this
     * method will update the Properties instance in memory, but will not persist anything to disk.
     * Persistence is up to the caller in that case.
     * </p>
     */
    public void save() {
        // Save each of our properties to our propertiesInstance instance:
        for (AbstractProperty prop : properties) {
            prop.saveToProps(propertiesInstance);
        }

        if (propertiesInstance instanceof FileBasedProperties) {
            ((FileBasedProperties)propertiesInstance).setCommentHeader(name);
            ((FileBasedProperties)propertiesInstance).saveWithoutException();
        }
    }

    /**
     * Copies all property values from the given PropertiesDialog (assuming the dialog
     * was validated and closed via the OK button) and updates our properties with those values.
     * This will also invoke save() automatically, so if our Properties instance is file based,
     * the updated values are persisted to disk.
     *
     * @param dialog The PropertiesDialog in question.
     */
    public void updateFromDialog(PropertiesDialog dialog) {
        if (dialog == null || !dialog.wasOkayed()) {
            logger.severe("PropertiesManager: attempt to save from a PropertiesDialog that wasn't validated.");
            return;
        }
        for (AbstractProperty prop : properties) {
            if (!prop.isEnabled() || !prop.isExposed()) {
                continue; // we won't find hidden or disabled fields as they aren't added to the form in the first place
            }
            FormField field = dialog.findFormField(prop.getFullyQualifiedName());
            if (field != null) {
                prop.loadFromFormField(field);
            }
            else {
                logger.warning("PropertiesManager.updateFromDialog(): couldn't find the form field for property: "
                                       + prop.getFullyQualifiedName());
            }
        }
        save();
    }

    /**
     * Generates a PropertiesDialog for the current properties list.
     * You can use setAlignment() and setBorderPanel() on the generated dialog to customize
     * the FormPanel layout options.
     * <p>
     *     <b>Note:</b> As of swing-extras 2.8, this method will return a PropertiesDialog
     *     based on ActionPanel navigation. Use the generateClassicDialog() method if you
     *     want the older tabbed pane style dialog instead.
     * </p>
     *
     * @param owner       The owner Window for the dialog.
     * @param dialogTitle The title of the dialog.
     * @return A PropertiesDialog instance, populated and ready to be shown.
     */
    public PropertiesDialog generateDialog(Window owner, String dialogTitle) {
        return generateDialog(owner, dialogTitle, false);
    }

    /**
     * Generates a PropertiesDialog for the current properties list.
     * You can use setAlignment() and setBorderPanel() on the generated dialog to customize
     * the FormPanel layout options.
     * <p>
     * <b>Note:</b> As of swing-extras 2.8, this method will return a PropertiesDialog
     * based on ActionPanel navigation. Use the generateClassicDialog() method if you want the older tabbed pane style dialog instead.
     * </p>
     *
     * @param owner           The owner Window for the dialog.
     * @param dialogTitle     The title of the dialog.
     * @param addPanelHeaders true to show auto-generated header labels for each FormPanel.
     * @return A PropertiesDialog instance, populated and ready to be shown.
     */
    public PropertiesDialog generateDialog(Window owner, String dialogTitle, boolean addPanelHeaders) {
        return PropertiesDialog.createActionPanelDialog(owner, dialogTitle, properties, addPanelHeaders);
    }

    /**
     * Generates a PropertiesDialog for the current properties list.
     * You can use setAlignment() and setBorderPanel() on the generated dialog to customize
     * the FormPanel layout options.
     * <p>
     * <b>Note:</b> This method generates a PropertiesDialog based on the older tabbed pane style dialog.
     * For the newer, ActionPanel-based dialog, use the generateDialog() method instead.
     * </p>
     *
     * @param owner                       The owner Window for the dialog.
     * @param dialogTitle                 The title of the dialog.
     * @param alwaysShowSubcategoryLabels true to show subcategory labels even if there's only one subcategory in a category.
     * @return A PropertiesDialog instance, populated and ready to be shown.
     */
    public PropertiesDialog generateClassicDialog(Window owner, String dialogTitle, boolean alwaysShowSubcategoryLabels) {
        return PropertiesDialog.createClassicDialog(owner, dialogTitle, properties, false);
    }

    /**
     * Extracts and returns a list of all top-level property categories for all non-hidden
     * properties in the given list, in the order that they are discovered within that list.
     * We don't sort the category list so that applications can define their own preferred order.
     *
     * @param props A list of properties to scan.
     * @return A List of unique top-level category names for all non-hidden properties that were found.
     */
    public static List<String> getCategories(List<AbstractProperty> props) {
        List<String> categories = new ArrayList<>();
        for (AbstractProperty prop : props) {
            if (!prop.isExposed() || !prop.isEnabled()) {
                continue;
            }
            if (!categories.contains(prop.getCategoryName())) {
                categories.add(prop.getCategoryName());
            }
        }
        return categories;
    }

    /**
     * Scans the given property list and returns a list of all subcategories within the named
     * category have at least one non-hidden property. If the specified category does not exist,
     * the list will be empty.
     *
     * @param category The name of the category to check.
     * @param props    The list of properties to scan. Hidden or disabled properties will be ignored.
     * @return A list of zero or more subcategories for the named category.
     */
    public static List<String> getSubcategories(String category, List<AbstractProperty> props) {
        List<String> subCategories = new ArrayList<>();
        for (AbstractProperty prop : props) {
            if (!prop.isExposed() || !prop.isEnabled()) {
                continue;
            }
            if (prop.getCategoryName().equals(category)
                    && !subCategories.contains(prop.getSubCategoryName())) {
                subCategories.add(prop.getSubCategoryName());
            }
        }
        return subCategories;
    }

    /**
     * Scans the given properties list and returns a list of all non-hidden properties
     * that belong to the given category and subcategory.
     *
     * @param props       The list of properties to scan
     * @param category    The top-level category to check
     * @param subCategory The subcategory to check
     * @return A List of zero or more AbstractProperties that match the search parameters.
     */
    public static List<AbstractProperty> getProperties(List<AbstractProperty> props, String category, String subCategory) {
        List<AbstractProperty> propList = new ArrayList<>();
        for (AbstractProperty prop : props) {
            if (!prop.isExposed() || !prop.isEnabled()) {
                continue;
            }
            if (prop.getCategoryName().equals(category)
                    && prop.getSubCategoryName().equals(subCategory)) {
                propList.add(prop);
            }
        }
        return propList;
    }
}
