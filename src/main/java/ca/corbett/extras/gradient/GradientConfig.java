package ca.corbett.extras.gradient;

import ca.corbett.extras.config.ConfigObject;
import ca.corbett.extras.properties.Properties;

import java.awt.Color;

/**
 * Represents configuration for a gradient fill on some image.
 *
 * @author scorbo2
 * @since 2022-05-10
 */
public final class GradientConfig implements ConfigObject {

    private GradientUtil.GradientType gradientType;
    private Color color1;
    private Color color2;

    public GradientConfig() {
        resetToDefaults();
    }

    public GradientConfig(GradientConfig other) {
        resetToDefaults();
        if (other != null) {
            gradientType = other.gradientType;
            color1 = other.color1;
            color2 = other.color2;
        }
    }

    public void resetToDefaults() {
        gradientType = GradientUtil.GradientType.VERTICAL_LINEAR;
        color1 = Color.WHITE;
        color2 = Color.BLACK;
    }

    public void setGradientType(GradientUtil.GradientType gradientType) {
        this.gradientType = gradientType;
    }

    public GradientUtil.GradientType getGradientType() {
        return gradientType;
    }

    public Color getColor1() {
        return color1;
    }

    public Color getColor2() {
        return color2;
    }

    public void setColor1(Color color1) {
        this.color1 = color1;
    }

    public void setColor2(Color color2) {
        this.color2 = color2;
    }

    @Override
    public void loadFromProps(Properties props, String prefix) {
        String pfx = (prefix == null) ? "" : prefix;
        resetToDefaults();

        gradientType = GradientUtil.GradientType.valueOf(props.getString(pfx + "gradientType", gradientType.name()));
        color1 = props.getColor(pfx + "color1", color1);
        color2 = props.getColor(pfx + "color2", color2);
    }

    @Override
    public void saveToProps(Properties props, String prefix) {
        String pfx = (prefix == null) ? "" : prefix;

        props.setString(pfx + "gradientType", gradientType.name());
        props.setColor(pfx + "color1", color1);
        props.setColor(pfx + "color2", color2);
    }

}
