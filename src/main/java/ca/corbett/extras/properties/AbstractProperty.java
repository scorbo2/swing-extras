package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;

import java.util.Objects;

/**
 * A generic abstract base class for property fields, to be used with PropertiesManager.
 * The available field types correspond pretty tightly to the available types in
 * the ca.corbett.forms.fields package.
 * <p>
 *     The goal here is to make managing application preferences easy and
 *     consistent across apps. Instead of each application having code to
 *     load and save preferences, as well as provide a UI for the user to
 *     view and change them, the classes in this package allow applications
 *     to just define the preferences by type and name, and then you can
 *     use PropertiesManager to handle the loading and saving of them,
 *     and PropertiesDialog to handle the UI for them. The burden on the
 *     application itself is MUCH lighter.
 * </p>
 * <p>
 *     The naming of preferences is important for organizing preferences
 *     into user-visible groups. Every property has a fully-qualified
 *     dot-separated name that is used by this package to determine
 *     what category a property belongs to. Refer to the constructor
 *     for example usages.
 * </p>
 * <p>
 *     Note: I use the terms "property" and "preference" pretty much
 *     interchangeably throughout these docs. In hindsight maybe
 *     I should have called these "Preferences".
 * </p>
 *
 * @author scorbo2
 * @since 2024-12-30
 */
public abstract class AbstractProperty {

  /**
   * If a property is not explicitly given a category, it will
   * be placed in a default category called "General".
   */
  public static final String DEFAULT_CATEGORY = "General";

  /**
   * If a property is not explicitly given a name, it
   * will show up as "(unnamed property)".
   */
  public static final String DEFAULT_PROPERTY_NAME = "(unnamed property)";

  protected final String fullyQualifiedName;
  protected String helpText;
  protected String categoryName;
  protected String subCategoryName;
  protected String propertyName;
  protected String propertyLabel;
  protected boolean isExposed;
  protected boolean isEnabled;
  protected boolean isReadOnly;

  /**
   * Each property has a fully qualified name that can optionally specify a
   * top-level category and up to one subcategory. The name should be dot separated
   * to make use of category and subcategory. The format is as follows:
   * <blockquote>[category.[subcategory.]]propertyName</blockquote>
   *
   * If category name is not specified, a default name of "General" will be used.
   * If subcategory is not specified, a default name of "General" will be used Some examples:
   * <ul>
   * <li><b>UI.windowState</b> - creates a property called "windowState" belonging to an
   * implied subcategory of "General" within the "UI" category.</li>
   * <li><b>UI.window.state</b> - creates a property called "state" in the subcategory
   * of "window" within the top-level category of "UI".</li>
   * <li><b>windowState</b> - creates a property called "windowState" in an implied
   * top-level category of "General" with an implied subcategory of "General"</li>
   * <li><b>UI.window.state.isMaximized</b> - creates a property called "state.isMaximized"
   * within the "window" subcategory in the "UI" top-level category. Note that further
   * dots after the second one are basically ignored and are considered part of the
   * property name. So, you can't have sub-sub-categories.</li>
   * </ul>
   *
   * @param name A fully qualified name as described above. If null, will be "(unnamed field)".
   * @param label A human-readable label for this field. Largely ignored by this code.
   */
  public AbstractProperty(String name, String label) {
    if (name == null) {
      categoryName = DEFAULT_CATEGORY;
      subCategoryName = DEFAULT_CATEGORY;
      propertyName = DEFAULT_PROPERTY_NAME;
    }

    else {
      if (name.contains(".")) {
        String[] parts = name.split("\\.");
        categoryName = parts[0];
        if (parts.length == 2) {
          propertyName = parts[1];
          subCategoryName = DEFAULT_CATEGORY;
        }
        else {
          subCategoryName = parts[1];
          StringBuilder sb = new StringBuilder();
          for (int i = 2; i < parts.length; i++) {
            sb.append(parts[i]);
            sb.append(".");
          }
          propertyName = sb.toString();
          propertyName = propertyName.substring(0, propertyName.length() - 1);
        }
      }
      else {
        categoryName = DEFAULT_CATEGORY;
        subCategoryName = DEFAULT_CATEGORY;
        propertyName = name;
      }
    }

    fullyQualifiedName = categoryName + "." + subCategoryName + "." + propertyName;
    this.propertyLabel = label;
    this.isExposed = true; // arbitrary default
    this.isEnabled = true; // arbitrary default
    this.helpText = "";
  }

  /**
   * Returns the fully qualified name of this property as it was passed to the constructor.
   * See also getCategoryName(), getSubCategoryName(), and getPropertyName() to retrieve the
   * individual components of the name separately.
   *
   * @return The fully qualified name of this property as it was passed to the constructor.
   */
  public String getFullyQualifiedName() {
    return fullyQualifiedName;
  }

  /**
   * Returns the category name for this property. If none was supplied to the constructor,
   * then this will return the default category name of "General".
   *
   * @return The property's category name.
   */
  public String getCategoryName() {
    return categoryName;
  }

  /**
   * Returns the subcategory name for this property, if one is defined.
   *
   * @return The property's subcategory name, or null if there is no subcategory for this property.
   */
  public String getSubCategoryName() {
    return subCategoryName;
  }

  /**
   * Returns the property name without category or subcategory. Most of the time, you probably
   * want to call getFullyQualifiedName() instead of this. For example, for a property named
   * "UI.window.state", this method will return "state".
   *
   * @return The bare field name of this property.
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Returns the human-readable label for this property.
   *
   * @return A human-readable label.
   */
  public String getPropertyLabel() {
    return propertyLabel;
  }

  /**
   * Sets or updates the human-readable label for this property.
   *
   * @param label The new label value.
   */
  public void setPropertyLabel(String label) {
    propertyLabel = label;
  }

  /**
   * Sets whether this property is to be exposed in the UI. Generally this is set once
   * when the property is created and does not change, as opposed to isEnabled, which
   * can vary at runtime. A field is only visible in the UI if (isExposed &amp;&amp; isEnabled).
   * <p>
   *     <b>Question: why would I want to mark a property as not exposed?</b> - There
   *     may be internal preferences that your app uses that are never shown to the
   *     user directly. This might include stuff like storing window state (maximized,
   *     screen location, width, height, etc) that would make no sense to show in
   *     an application preferences dialog. You can use this method to "hide" a
   *     property in the PropertiesDialog while still being able to save and load
   *     it via PropertiesManager.
   * </p>
   *
   * @param expose True to expose this field in PropertiesDialog.
   */
  public void setExposed(boolean expose) {
    isExposed = expose;
  }

  /**
   * Reports whether this property is meant to be exposed in the UI.
   * Marking a property as not exposed is useful for storing additional internal
   * information that is not meant to be fiddled with by the user directly (but can
   * still be manipulated by the application).
   *
   * @return true if this field is to be shown in the PropertiesDialog (default true)
   */
  public boolean isExposed() {
    return isExposed;
  }

  /**
   * Setting a property to disabled will prevent this property from appearing
   * in the PropertiesDialog. It can therefore no longer be directly manipulated
   * by the user. The difference between setEnabled(false) and setExposed(false)
   * is that enabled/disabled are intended to be short term and dynamic,
   * while exposed/not exposed are intended to be permanent attributes of that
   * property. Basically isExposed=false means the property will never appear
   * in the UI, whereas isEnabled=false means the property will temporarily
   * ot appear in the UI (until enabled).
   *
   * @param enable Whether to enable or disable this property.
   */
  public void setEnabled(boolean enable) {
    isEnabled = enable;
  }

  /**
   * Reports whether this property is currently enabled. If false, the property should be
   * hidden temporarily in the UI (but may still contain a valid value).
   *
   * @return Whether this property is currently enabled.
   */
  public boolean isEnabled() {
    return isEnabled;
  }

  /**
   * Reports whether this property will be editable when shown in the PropertiesDialog.
   * <p>
   * <b>Question: Why would I want a property to be read only?</b> - a common
   * use case with forms is to make certain controls visible/editable only
   * if certain conditions are met elsewhere on the form. You can use
   * isReadOnly to set the initial state of this property when it is added
   * to the PropertiesDialog (it can still be changed at runtime as a result
   * of action handlers on other form fields).
   * </p>
   * <p>
   * Another use case is for displaying programmatically configurable properties
   * that can be set by the code but not by the user. It may still be desirable
   * to show these properties on the PropertiesDialog and their current value,
   * without allowing the user to change them. Unlike using a static label for
   * this purpose, these properties will still save and load their values
   * to and from properties.
   * </p>
   */
  public boolean isReadOnly() {
    return isReadOnly;
  }

  /**
   * Set whether this property is to be editable by default when shown on a
   * PropertiesDialog. Note that this reflects the <b>initial</b> state of the property
   * on the PropertiesDialog. This can be changed at runtime by action handlers
   * on other form fields (for example, field B is only editable if field A contains
   * a specific value).
   */
  public void setReadOnly(boolean readOnly) {
    isReadOnly = readOnly;
  }

  /**
   * Returns the optional help text for this property, if any is set, otherwise
   * a blank string.
   *
   * @return The help text for this property, or empty string if not set.
   */
  public String getHelpText() {
    return helpText;
  }

  /**
   * Sets optional help text for this property. This will be used when generating
   * the form field for this property, to give the user an informational icon on
   * the generated form. Multi-line tooltips are supported by wrapping the
   * contents in an &lt;html&gt; tag and using &lt;br&gt; to separate lines.
   *
   * @param helpText The new help text, or null to unset it.
   */
  public void setHelpText(String helpText) {
    this.helpText = (helpText == null) ? "" : helpText;
  }

  /**
   * Saves the current value(s) of this property to the given Properties instance.
   *
   * @param props Any Properties instance which will receive the value(s) of this property.
   */
  public abstract void saveToProps(Properties props);

  /**
   * Loads the value(s) for this property from the given Properties instance, overwriting
   * any current value. The current value of this property will be used as a default value
   * in the event that this property does not exist in the given Properties instance.
   *
   * @param props Any Properties instance which contains value(s) for this property.
   */
  public abstract void loadFromProps(Properties props);

  /**
   * Generates a FormField instance for this AbstractProperty, depending on our type.
   * The returned FormField will be populated based on the current value of this property.
   * There's no guarantee that it will pass form validation, though, as the default value
   * of the property is out of our control and may or may not actually be valid.
   *
   * @return A FormField representing this AbstractProperty.
   */
  public abstract FormField generateFormField();

  /**
   * Populates this Property's value(s) from the given form field, assuming the
   * field is of the correct type.
   *
   * @param field The FormField containing a value for this property.
   */
  public abstract void loadFromFormField(FormField field);

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 67 * hash + Objects.hashCode(this.fullyQualifiedName);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AbstractProperty other = (AbstractProperty)obj;
    return Objects.equals(this.fullyQualifiedName, other.fullyQualifiedName);
  }
}
