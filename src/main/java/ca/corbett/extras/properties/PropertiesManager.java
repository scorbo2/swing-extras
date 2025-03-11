package ca.corbett.extras.properties;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTabbedPane;

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

  protected int headerFontSize = 16;
  protected boolean headerBold = true;
  protected int headerTopMargin = 10;
  protected int headerBottomMargin = 0;

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
   * PropertiesManager, in the order they were given to the constructor.We don't sort
   * them alphabetically so that applications can define their own preferred order.
   *
   * @return A list of one or more property categories.
   */
  public List<String> getCategories() {
    List<String> categories = new ArrayList<>();
    for (AbstractProperty prop : properties) {
      if (!prop.isExposed() || !prop.isEnabled) {
        continue;
      }
      if (!categories.contains(prop.getCategoryName())) {
        categories.add(prop.getCategoryName());
      }
    }
    return categories;
  }

  /**
   * Returns a list of all subcategories contained within the named property category.
   * If the specified category does not exist, the list will be empty. Otherwise, it will
   * always contain at least one category, as we provide a default "General" category in
   * the event that no property mentions a category.
   *
   * @param category The name of the category to check.
   * @return A list of 1 or more subcategories for that category, or empty if no such category.
   */
  public List<String> getSubcategories(String category) {
    List<String> subCategories = new ArrayList<>();
    for (AbstractProperty prop : properties) {
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
    List<AbstractProperty> props = new ArrayList<>();
    for (AbstractProperty prop : this.properties) {
      if (!prop.isExposed() || !prop.isEnabled()) {
        continue;
      }
      if (prop.getCategoryName().equals(category)
              && prop.getSubCategoryName().equals(subCategory)) {
        props.add(prop);
      }
    }
    return props;
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
      FormField field = dialog.findFormField(prop.getFullyQualifiedName());
      // TODO we could do dirty checking here to avoid saving if nothing was modified...
      if (field != null) {
        prop.loadFromFormField(field);
      }
    }
    save();
  }

  /**
   * Shorthand for generateDialog(owner,dialogTitle,leftAlign,8) - basically generate a dialog
   * with an 8 pixel left margin in the generated form panel(s).
   *
   * @param owner The owher frame for the dialog.
   * @param dialogTitle The title of the dialog.
   * @param leftAlign Should form fields be left-aligned within their container. False=center.
   * @return A PropertiesDialog instance, populated and ready to be shown.
   */
  public PropertiesDialog generateDialog(Frame owner, String dialogTitle, boolean leftAlign) {
    return generateDialog(owner, dialogTitle, leftAlign, 8);
  }

  /**
   * Generates a PropertiesDialog containing all the non-hidden properties managed by this
   * PropertiesManager.
   *
   * @param owner The owner frame for the dialog.
   * @param dialogTitle The title of the dialog.
   * @param leftAlign Should form fields be left-aligned within their container. False=center.
   * @param leftMargin If leftAlign is true, you can apply a pixel margin to the form's left side.
   * @return A PropertiesDialog instance, populated and ready to be shown.
   */
  public PropertiesDialog generateDialog(Frame owner, String dialogTitle, boolean leftAlign, int leftMargin) {
    List<String> categories = getCategories();

    // Should ideally never happen, but it is possible, if all our properties are hidden properties:
    if (categories.isEmpty()) {
      return null;
    }

    // If there's only one category, just wrap it in a single form panel:
    if (categories.size() == 1) {
      FormPanel formPanel = generateUnrenderedFormPanel(categories.get(0), leftAlign, leftMargin);
      formPanel.render();
      return new PropertiesDialog(this, owner, dialogTitle, formPanel);
    }

    // If there's more than one category, wrap it all in a tab pane:
    JTabbedPane tabPane = new JTabbedPane();
    for (String category : categories) {
      FormPanel formPanel = generateUnrenderedFormPanel(category, leftAlign, leftMargin);
      formPanel.render();
      tabPane.addTab(category, formPanel);
    }
    return new PropertiesDialog(this, owner, dialogTitle, tabPane);
  }

  /**
   * Shorthand for generateUnrenderedFormPanel(category,leftAlign,8) - basically, if a left
   * aligned form is generated, there will be an 8 pixel left margin by default.
   *
   * @param category The name of the category in question.
   * @param leftAlign Should form fields be left-aligned within their container. False=center.
   * @return A FormPanel object, NOT yet rendered.
   */
  public FormPanel generateUnrenderedFormPanel(String category, boolean leftAlign) {
    return generateUnrenderedFormPanel(category, leftAlign, 8);
  }

  /**
   * Generates a FormPanel for all the properties in all of the subcategories of the named Category.
   * The FormPanel is returned unrendered in case you want to set custom initial visibility
   * of form fields, or conditional logic such as field B should only be visible/editable if
   * field A contains some specific value, etc. If you don't need to do that, it's easier
   * to just go through generateDialog() and get all FormPanels prerendered and ready to go.
   *
   * @param category The name of the category in question.
   * @param leftAlign Should form properties be left-aligned within their container. False=center.
   * @param leftMargin A pixel margin to apply to the left of all form fields, ignored if centered.
   * @return A FormPanel object, NOT yet rendered.
   */
  public FormPanel generateUnrenderedFormPanel(String category, boolean leftAlign, int leftMargin) {
    List<String> subCategories = getSubcategories(category);
    FormPanel.Alignment formAlignment = FormPanel.Alignment.TOP_CENTER;
    if (leftAlign) {
      formAlignment = FormPanel.Alignment.TOP_LEFT;
    }

    FormPanel formPanel = new FormPanel(formAlignment);
    for (String subCategory : subCategories) {
      // Show a subcategory label header if there's more than one subcategory:
      if (subCategories.size() > 1) {
        FormField field = generateHeaderField(subCategory);
        if (leftAlign) {
          field.setLeftMargin(leftMargin);
        }
        formPanel.addFormField(field);
      }

      // Show all the properties in this subcategory:
      List<AbstractProperty> propList = getProperties(category, subCategory);
      for (AbstractProperty prop : propList) {
        FormField field = prop.generateFormField();
        if (leftAlign) {
          field.setLeftMargin(leftMargin);
        }
        formPanel.addFormField(field);
      }
    }

    return formPanel;
  }

  /**
   * Allows setting properties for generated header labels in properties forms. Values set here
   * will be used for generated forms going forward; calling this has no effect on forms
   * that have already been generated via this class.
   *
   * @param isBold Should the header labels be in bold text (default true).
   * @param topMargin Extra pixel margin to apply above the label (default 10).
   * @param bottomMargin Extra pixel margin to apply below the label (default 0).
   * @param pointSize Point size for header font (default 16).
   */
  public void setHeaderProperties(boolean isBold, int topMargin, int bottomMargin, int pointSize) {
    headerBold = isBold;
    headerTopMargin = topMargin;
    headerBottomMargin = bottomMargin;
    headerFontSize = pointSize;
  }

  /**
   * Generates a subCategory label.
   *
   * @param title The name of the sub category.
   * @return A LabelField instance
   */
  protected LabelField generateHeaderField(String title) {
    LabelField labelField = new LabelField(title);
    labelField.setExtraMargins(headerTopMargin, headerBottomMargin);
    labelField.setFont(labelField.getFieldLabelFont().deriveFont(headerBold ? Font.BOLD : 0, (float)headerFontSize));
    return labelField;
  }

}
