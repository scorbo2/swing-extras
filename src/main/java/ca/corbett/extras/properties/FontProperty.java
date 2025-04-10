package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.FormField;

import java.awt.Color;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property field that allows storing a Font,
 * along with associated style and optional color attributes.
 *
 * @author scorbo2
 */
public class FontProperty extends AbstractProperty {

  private static final Logger logger = Logger.getLogger(FontProperty.class.getName());

  protected String fontName;
  protected boolean isBold;
  protected boolean isItalic;
  protected int pointSize;
  protected Color textColor;
  protected Color bgColor;

  public FontProperty(String name, String label, String fontName, boolean isBold, boolean isItalic, int pointSize) {
    this(name, label, fontName, isBold, isItalic, pointSize, null, null);
  }

  public FontProperty(String name, String label, String fontName, boolean isBold, boolean isItalic, int pointSize, Color textColor) {
    this(name, label, fontName, isBold, isItalic, pointSize, textColor, null);
  }

  public FontProperty(String name, String label, String fontName, boolean isBold, boolean isItalic, int pointSize, Color textColor, Color bgColor) {
    super(name, label);
    this.fontName = fontName;
    this.isBold = isBold;
    this.isItalic = isItalic;
    this.pointSize = pointSize;
    this.textColor = textColor;
    this.bgColor = bgColor;
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

  public Color getTextColor() {
    return textColor;
  }

  public Color getBgColor() {
    return bgColor;
  }

  public void setTextColor(Color color) {
    textColor = color;
  }

  public void setBgColor(Color color) {
    bgColor = color;
  }

  public Font getFont() {
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
    if (textColor != null) {
      props.setColor(fullyQualifiedName + ".textColor", textColor);
    } else {
      props.remove(fullyQualifiedName + ".textColor");
    }
    if (bgColor != null) {
      props.setColor(fullyQualifiedName + ".bgColor", bgColor);
    } else {
      props.remove(fullyQualifiedName + ".bgColor");
    }
  }

  @Override
  public void loadFromProps(Properties props) {
    fontName = props.getString(fullyQualifiedName + ".name", fontName);
    isBold = props.getBoolean(fullyQualifiedName + ".isBold", isBold);
    isItalic = props.getBoolean(fullyQualifiedName + ".isItalic", isItalic);
    pointSize = props.getInteger(fullyQualifiedName + ".pointSize", pointSize);
    textColor = props.getColor(fullyQualifiedName + ".textColor", textColor);
    bgColor = props.getColor(fullyQualifiedName + ".bgColor", bgColor);
  }

  @Override
  public FormField generateFormField() {
    FontField field = new FontField(propertyLabel, getFont(), textColor, bgColor);
    field.setIdentifier(fullyQualifiedName);
    field.setEnabled(!isReadOnly);
    return field;
  }

  @Override
  public void loadFromFormField(FormField field) {
    if (field.getIdentifier() == null
            || !field.getIdentifier().equals(fullyQualifiedName)
            || !(field instanceof FontField)) {
      logger.log(Level.SEVERE, "FontProperty.loadFromFormField: received the wrong field \"{0}\"", field.getIdentifier());
      return;
    }

    FontField fontField = (FontField) field;
    fontName = fontField.getSelectedFont().getFontName();
    isBold = fontField.getSelectedFont().isBold();
    isItalic = fontField.getSelectedFont().isItalic();
    pointSize = fontField.getSelectedFont().getSize();
    textColor = fontField.getTextColor();
    bgColor = fontField.getBgColor();
  }

}
