package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FontStyleField;
import ca.corbett.forms.fields.FormField;

import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property field that allows storing a Font.
 *
 * @author scorbo2
 */
public class FontProperty extends AbstractProperty {

  private static final Logger logger = Logger.getLogger(FontProperty.class.getName());

  protected String fontName;
  protected boolean isBold;
  protected boolean isItalic;
  protected int pointSize;

  public FontProperty(String name, String label, String fontName, boolean isBold, boolean isItalic, int pointSize) {
    super(name, label);
    this.fontName = fontName;
    this.isBold = isBold;
    this.isItalic = isItalic;
    this.pointSize = pointSize;
  }

  public void setFontName(String name) {
    fontName = name;
  }

  public String getFontName() {
    return fontName;
  }

  public boolean isIsBold() {
    return isBold;
  }

  public void setIsBold(boolean isBold) {
    this.isBold = isBold;
  }

  public boolean isIsItalic() {
    return isItalic;
  }

  public void setIsItalic(boolean isItalic) {
    this.isItalic = isItalic;
  }

  public int getPointSize() {
    return pointSize;
  }

  public void setPointSize(int pointSize) {
    this.pointSize = pointSize;
  }

  public Font createFont() {
    int style = Font.PLAIN;
    if (isBold) {
      style = style | Font.BOLD;
    }
    if (isItalic) {
      style = style | Font.ITALIC;
    }
    return new Font(fontName, style, pointSize);
  }

  @Override
  public void saveToProps(Properties props) {
    props.setString(fullyQualifiedName + ".name", fontName);
    props.setBoolean(fullyQualifiedName + ".isBold", isBold);
    props.setBoolean(fullyQualifiedName + ".isItalic", isItalic);
    props.setInteger(fullyQualifiedName + ".pointSize", pointSize);
  }

  @Override
  public void loadFromProps(Properties props) {
    fontName = props.getString(fullyQualifiedName + ".name", fontName);
    isBold = props.getBoolean(fullyQualifiedName + ".isBold", isBold);
    isItalic = props.getBoolean(fullyQualifiedName + ".isItalic", isItalic);
    pointSize = props.getInteger(fullyQualifiedName + ".pointSize", pointSize);
  }

  @Override
  public FormField generateFormField() {
    FontStyleField field = new FontStyleField(propertyLabel, fontName);
    field.setBold(isBold);
    field.setItalic(isItalic);
    field.setFontSize(pointSize);
    field.setIdentifier(fullyQualifiedName);
    field.setEnabled(!isReadOnly);
    return field;
  }

  @Override
  public void loadFromFormField(FormField field) {
    if (field.getIdentifier() == null
            || !field.getIdentifier().equals(fullyQualifiedName)
            || !(field instanceof FontStyleField)) {
      logger.log(Level.SEVERE, "FontProperty.loadFromFormField: received the wrong field \"{0}\"", field.getIdentifier());
      return;
    }

    FontStyleField fontField = (FontStyleField)field;
    fontName = fontField.getFontName();
    isBold = fontField.isBold();
    isItalic = fontField.isItalic();
    pointSize = fontField.getFontSize();
  }

}
