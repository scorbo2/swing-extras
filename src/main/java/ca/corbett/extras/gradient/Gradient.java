package ca.corbett.extras.gradient;

import java.awt.Color;

public record Gradient(GradientType type, Color color1, Color color2) {

    public static Gradient createDefault() {
        return new Gradient(GradientType.VERTICAL_STRIPE, Color.WHITE, Color.BLACK);
    }
}
