package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;

/**
 * An extremely simple property which represents a static label.
 * Labels are not input fields, so they do not save or load anything
 * to or from properties. If you are looking for a way to display
 * some read-only programmatically configurable field on a properties
 * dialog, you should use a read-only text field instead of a label.
 *
 * @author scorbo2
 * @since 2024-12-30
 */
public class LabelProperty extends AbstractProperty {

  private int extraTopMargin;
  private int extraBottomMargin;
  private Font labelFont;
  private Color labelColor;

  public LabelProperty(String name, String label) {
    super(name, label);
    extraTopMargin = 4;
    extraBottomMargin = 4;
  }

  @Override
  public void saveToProps(Properties props) {
    // Labels are static form fields, so there's literally nothing to do here.
  }

  @Override
  public void loadFromProps(Properties props) {
    // Labels are static form fields, so there's literally nothing to do here.
  }

  public void setExtraMargins(int top, int bottom) {
    extraTopMargin = top;
    extraBottomMargin = bottom;
  }

  public void setFont(Font f) {
    labelFont = f;
  }

  public void setColor(Color c) {
    labelColor = c;
  }

  @Override
  public FormField generateFormField() {
    LabelField field = new LabelField(propertyLabel);
    field.setExtraMargins(extraTopMargin, extraBottomMargin);
    if (labelFont != null) {
      field.setFont(labelFont);
    }
    if (labelColor != null) {
      ((JLabel)field.getFieldComponent()).setForeground(labelColor);
    }
    field.setIdentifier(fullyQualifiedName);
    return field;
  }

  @Override
  public void loadFromFormField(FormField field) {
    // Labels are static form fields, so there's literally nothing to do here.
  }

}
