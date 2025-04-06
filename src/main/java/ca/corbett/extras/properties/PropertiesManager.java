package ca.corbett.extras.properties;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FormField;

import java.awt.Frame;
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
 * application preferences are handled in a consistent way, but this can be used
 * for managing properties of any type in a generic and configurable way.
 * See also generateDialog() and generateUnrenderedFormPanel() for ways to present
 * a UI to view or edit application properties.
 *
 * @author scorbo2
 * @since 2024-12-30
 */
public class PropertiesManager {

  protected static final Logger logger = Logger.getLogger(PropertiesManager.class.getName());
  protected final Properties propertiesInstance;
  protected final List<AbstractProperty> properties;
  protected final String name;

  /**
   * Creates a PropertiesManager instance backed onto the given File object, and a list
   * of AbstractProperty objects thatwe will manage.
   *
   * @param propsFile The properties file. Does not need to exist - will be created on save()
   * @param props A List of AbstractProperty instances to be managed by this class.
   * @param name A name for this property collection. Comment header for the props file.
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
   * @param props A List of AbstractProperty instances to be managed by this class.
   * @param name A name for this property collection. Comment header for the propsInstance file.
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
   * @param category The name of the property category to check.
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
   * constructor, overwriting any previously saved values.The output list will be
   * alphabetically sorted by fully qualified property name, making for a (hopefully) easy
   * to read properties file.
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
      // TODO we could do dirty checking here to avoid saving if nothing was modified...
      if (field != null) {
        prop.loadFromFormField(field);
      } else {
        logger.warning("PropertiesManager.updateFromDialog(): couldn't find the form field for property: " + prop.getFullyQualifiedName());
      }
    }
    save();
  }

  /**
   * Generates a PropertiesDialog for the current properties list with default dialog values.
   * That is, a left-aligned dialog with an 8 pixel left margin.
   *
   * @param owner The owner frame for the dialog.
   * @param dialogTitle The title of the dialog.
   * @return A PropertiesDialog instance, populated and ready to be shown.
   */
  public PropertiesDialog generateDialog(Frame owner, String dialogTitle) {
    return generateDialog(owner, dialogTitle, FormPanel.Alignment.TOP_LEFT, 8);
  }

  /**
   * Generates a PropertiesDialog containing all the non-hidden properties managed by this
   * PropertiesManager. Note that this method is shorthand for:
   * <blockquote><pre>
   *     panelList = generateUnrenderedFormPanels(alignment, leftMargin);
   *     return new PropertiesDialog(this, owner, dialogTitle, panelList);
   * </pre></blockquote>
   * You have the option of calling generateUnrenderedFormPanels() yourself to get the list
   * of FormPanels that are not yet rendered. The advantage of doing it that way is that you
   * can add custom logic to form fields on those panels to do things like showing/hiding
   * or enabling/disabling form fields based on the values contained in other fields. Your code
   * would look something like this:
   * <blockquote><pre>
   *     panelList = propsManager.generateUnrenderedFormPanels(alignment, leftMargin);
   *
   *     // Add some custom logic to the form:
   *     ComboField field1 = (ComboField)PropertiesManager.findFormField("my.field.one");
   *     FormField field2 = PropertiesManager.findFormField("my.field.two");
   *     field1.addValueChangedAction(new AbstractAction() {
   *          public void actionPerformed(ActionEvent e) {
   *              field2.setEnabled("Some special value".equals(field1.getSelectedItem()));
   *          }
   *     });
   *     field2.setEnabled(false); // set initial value
   *
   *     new PropertiesDialog(propsManager, owner, dialogTitle, panelList).setVisible(true);
   * </pre></blockquote>
   * <p>
   *     If you invoke this generateDialog() method to generate the dialog for you,
   *     instead of using the above approach, you will lose the ability to
   *     add those action handlers before the form panels are rendered. That might be
   *     a problem if you need to set certain fields to invisible or disabled before
   *     the dialog is shown based on current form values.
   * </p>
   *
   * @param owner The owner frame for the dialog.
   * @param dialogTitle The title of the dialog.
   * @param alignment How the form panel(s) on the generated dialog should align themselves.
   * @param leftMargin If form panels are left aligned, you can apply a pixel margin to the form's left side.
   * @return A PropertiesDialog instance, populated and ready to be shown.
   */
  public PropertiesDialog generateDialog(Frame owner, String dialogTitle, FormPanel.Alignment alignment, int leftMargin) {
    List<FormPanel> formPanelList = generateUnrenderedFormPanels(alignment, leftMargin);
    return new PropertiesDialog(this, owner, dialogTitle, formPanelList);
  }

  public List<FormPanel> generateUnrenderedFormPanels() {
    return generateUnrenderedFormPanels(properties, FormPanel.Alignment.TOP_LEFT, 8);
  }

  public List<FormPanel> generateUnrenderedFormPanels(FormPanel.Alignment alignment) {
    return generateUnrenderedFormPanels(properties, alignment, 8);
  }

  public List<FormPanel> generateUnrenderedFormPanels(FormPanel.Alignment alignment, int leftMargin) {
    return generateUnrenderedFormPanels(properties, alignment, leftMargin);
  }

  public static List<FormPanel> generateUnrenderedFormPanels(List<AbstractProperty> props) {
    return generateUnrenderedFormPanels(props, FormPanel.Alignment.TOP_LEFT, 8);
  }

  public static List<FormPanel> generateUnrenderedFormPanels(List<AbstractProperty> props, FormPanel.Alignment alignment) {
    return generateUnrenderedFormPanels(props, alignment, 8);
  }

  public static List<FormPanel> generateUnrenderedFormPanels(List<AbstractProperty> props, FormPanel.Alignment alignment, int leftMargin) {
    List<String> categories = getCategories(props);

    // Our list of categories might be empty if all properties are hidden or disabled,
    // of if we simply weren't given any properties. In that case, generate a simple
    // dialog with a single label field on it indicating that we have nothing to show.
    if (categories.isEmpty()) {
      props = List.of(LabelProperty.createLabel("defaultLabel", "There are no properties defined."));
      categories = getCategories(props);
    }

    // Now go through each category:
    List<FormPanel> formPanelList = new ArrayList<>();
    for (String category : categories) {
      List<String> subCategories = getSubcategories(category, props);
      FormPanel formPanel = new FormPanel(alignment);
      formPanel.setName(category);
      for (String subCategory : subCategories) {
        // Show a subcategory label header if there's more than one subcategory:
        if (subCategories.size() > 1) {
          FormField field = LabelProperty.createHeaderLabel(category + "." + subCategory + ".autoGeneratedHeaderLabel", subCategory).generateFormField();
          if (alignment.isLeftAligned()) {
            field.setLeftMargin(leftMargin);
          }
          formPanel.addFormField(field);
        }

        // Show all the properties in this subcategory:
        List<AbstractProperty> propList = getProperties(props, category, subCategory);
        for (AbstractProperty prop : propList) {
          FormField field = prop.generateFormField();
          if (alignment.isLeftAligned()) {
            field.setLeftMargin(leftMargin);
          }
          formPanel.addFormField(field);
        }
      }
      formPanelList.add(formPanel);
    }

    return formPanelList;
  }

  /**
   * A convenience method to find a specific named form field in a list of
   * unrendered form panels. If the field is not found, null is returned.
   * If you can't figure out why the field you're sure is there is returning
   * null, check if it is marked as hidden or disabled. Such fields are not
   * included in the generated form panels.
   *
   * @param fieldName            The fully qualified name of the field to look for.
   * @param unrenderedFormPanels A List of unrendered FormPanels, presumably from generateUnrenderedFormPanels()
   * @return The FormField in question, or null if not found.
   */
  public static FormField findFormField(String fieldName, List<FormPanel> unrenderedFormPanels) {
    for (FormPanel formPanel : unrenderedFormPanels) {
      List<FormField> fields = formPanel.getFormFields();
      for (FormField field : fields) {
        if (field.getIdentifier() != null && field.getIdentifier().equals(fieldName)) {
          return field;
        }
      }
    }
    return null;
  }

  /**
   * Extracts and returns a list of all top-level property categories for all non-hidden
   * properties in the given list, in the order that they are discovered within that list.
   * We don't sort the category list so that applications can define their own preferred order.
   *
   * @param props A list of properties to scan.
   * @return A List of unique top-level category names for all non-hidden properties that were found.
   */
  protected static List<String> getCategories(List<AbstractProperty> props) {
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
  protected static List<String> getSubcategories(String category, List<AbstractProperty> props) {
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
   * @param category    The toplevel category to check
   * @param subCategory The subcategory to check
   * @return A List of zero or more AbstractProperties that match the search parameters.
   */
  protected static List<AbstractProperty> getProperties(List<AbstractProperty> props, String category, String subCategory) {
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
