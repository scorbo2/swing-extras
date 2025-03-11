package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;

/**
 * An extremely simple property which represents a static label.
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
    props.setString(fullyQualifiedName, propertyLabel);
  }

  @Override
  public void loadFromProps(Properties props) {
    propertyLabel = props.getString(fullyQualifiedName, propertyLabel);
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
