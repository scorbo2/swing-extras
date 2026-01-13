package ca.corbett.extras.image.animation;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.plaf.LayerUI;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A custom LayerUI that can apply a fun "falling snow" effect to a JPanel
 * by drawing little animated snowflakes over it. The underlying panel remains
 * fully interactive while the snow is falling - it's a purely cosmetic effect.
 * <p>
 * <b>USAGE:</b> Create a JLayer with an instance of this SnowLayerUI,
 * wrapping the JPanel you want to snow on. Call letItSnow(true) to
 * start the snow animation, and letItSnow(false) to stop it.
 * </p>
 * <pre>
 *     JPanel contentPanel = ...;
 *     SnowLayerUI snowLayerUI = new SnowLayerUI();
 *     JLayer<JPanel> layeredPanel = new JLayer<>(contentPanel, snowLayerUI);
 *     containerPanel.add(layeredPanel);
 *
 *     // Set your snow options, or live with the defaults:
 *     snowLayerUI.setQuantity(SnowLayerUI.Quantity.Strong);
 *     snowLayerUI.setWind(SnowLayerUI.Wind.MildRight);
 *
 *     // Then, in a button handler or whatever:
 *     snowLayerUI.letItSnow(true); // To start snowing
 *     snowLayerUI.letItSnow(false); // To stop snowing
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a> with claude.ai
 * @since swing-extras 2.7
 */
public class SnowLayerUI extends LayerUI<JPanel> {

    public static final Quantity DEFAULT_QUANTITY = Quantity.Medium;
    public static final Color DEFAULT_SNOW_COLOR = new Color(255, 255, 255);
    public static final Wind DEFAULT_WIND = Wind.DeadCalm;

    /**
     * Controls the likelihood of a new snowflake appearing each frame.
     */
    public enum Quantity {
        VeryGentle(0.1f),
        Gentle(0.2f),
        Medium(0.3f),
        Strong(0.4f),
        VeryStrong(0.5f),
        Blizzard(0.75f);

        private final float probability;

        Quantity(float probability) {
            this.probability = probability;
        }

        public float getProbability() {
            return probability;
        }
    }

    /**
     * Controls the "wind", or the amount of horizontal drift of the snowflakes and their direction.
     */
    public enum Wind {
        StrongLeft(-3.5f),
        MildLeft(-1.5f),
        DeadCalm(0f),
        MildRight(1.5f),
        StrongRight(3.5f);

        private final float driftAmount;

        Wind(float driftAmount) {
            this.driftAmount = driftAmount;
        }

        public float getDriftAmount() {
            return driftAmount;
        }
    }

    private final List<Snowflake> snowflakes = new CopyOnWriteArrayList<>(); // higher overhead but thread-safe
    private final Random random = new Random();
    private JComponent targetComponent;
    private final Timer animationTimer;
    private Quantity quantity;
    private volatile Wind wind;
    private volatile Color snowColor;
    private volatile Color internalSnowColor;
    private volatile Color internalSnowSparkleColor;

    /**
     * Creates a new SnowLayerUI with default settings.
     */
    public SnowLayerUI() {
        targetComponent = null;
        quantity = DEFAULT_QUANTITY;
        wind = DEFAULT_WIND;
        setSnowColor(DEFAULT_SNOW_COLOR);

        // Create our timer but don't start it yet:
        animationTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (targetComponent != null) {
                    updateSnowflakes(targetComponent);
                    targetComponent.repaint();
                }
            }
        });
    }

    /**
     * Reports whether the snow animation is currently active.
     */
    public boolean isSnowing() {
        return animationTimer.isRunning();
    }

    /**
     * Let it snow! Or not, depending on the parameter.
     */
    public SnowLayerUI letItSnow(boolean isSnowing) {

        // Make sure we interact with our timer on the EDT!
        SwingUtilities.invokeLater(() -> {
            if (isSnowing) {
                if (!animationTimer.isRunning()) {
                    animationTimer.start();
                }
            }
            else {
                if (animationTimer.isRunning()) {
                    animationTimer.stop();
                }
            }
        });

        return this;
    }

    /**
     * Returns the current snow quantity setting.
     */
    public Quantity getQuantity() {
        return quantity;
    }

    /**
     * Returns the current wind setting.
     */
    public Wind getWind() {
        return wind;
    }

    /**
     * Sets the wind effect for the snowflakes.
     */
    public void setWind(Wind wind) {
        if (wind == null) {
            throw new IllegalArgumentException("Wind setting cannot be null! Use Wind.DeadCalm for no wind.");
        }

        // Make sure we update our snowflakes on the EDT!
        SwingUtilities.invokeLater(() -> {
            this.wind = wind;

            // The above setting will automatically apply to all new snowflakes.
            // But it just looks weird if we don't also apply this to all existing snowflakes.
            // So, let's recompute the drift for each one:
            for (Snowflake snowflake : snowflakes) {
                snowflake.drift = (random.nextFloat() - 0.5f) * 0.5f + wind.getDriftAmount();
            }
        });
    }

    /**
     * Returns the current snow color.
     */
    public Color getSnowColor() {
        return snowColor;
    }

    /**
     * Why would you choose anything other than white for snow?
     */
    public SnowLayerUI setSnowColor(Color snowColor) {
        if (snowColor == null) {
            throw new IllegalArgumentException("Snow color cannot be null! Just use white, it's snow after all!");
        }

        // Make sure we update our snowflakes on the EDT!
        SwingUtilities.invokeLater(() -> {
            this.snowColor = snowColor;

            // We can pre-set our internal colors with the right alpha values:
            this.internalSnowColor = new Color(snowColor.getRed(),
                                               snowColor.getGreen(),
                                               snowColor.getBlue(),
                                               200);
            this.internalSnowSparkleColor = new Color(snowColor.getRed(),
                                                      snowColor.getGreen(),
                                                      snowColor.getBlue(),
                                                      100);
        });

        return this;
    }

    /**
     * Sets the snow quantity setting.
     */
    public void setQuantity(Quantity quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null! Use letItSnow(false) to stop snowing.");
        }
        this.quantity = quantity;
    }

    /**
     * Invoked when this UI delegate is being installed on a JLayer.
     * We don't automatically start snowing when added! We wait for
     * an explicit call to letItSnow(true).
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.targetComponent = c;

        // NOTE: We don't call setLayerEventMask here!
        // This means events pass through to underlying components
        // (i.e. the underlying panel remains fully interactive).
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        this.targetComponent = null;
        letItSnow(false);
    }

    private void updateSnowflakes(JComponent c) {
        // Add new snowflakes randomly
        if (random.nextFloat() < quantity.getProbability()) {
            float x = random.nextFloat() * c.getWidth();
            float speed = 1f + random.nextFloat() * 2f;
            float size = 3f + random.nextFloat() * 5f;
            float drift = (random.nextFloat() - 0.5f) * 0.5f + wind.getDriftAmount();
            snowflakes.add(new Snowflake(x, -10, speed, size, drift, internalSnowColor, internalSnowSparkleColor));
        }

        // Update existing snowflakes
        int leftEdge = -20;
        int rightEdge = c.getWidth() + 20;
        int bottomEdge = c.getHeight() + 20;
        snowflakes.removeIf(flake -> {
            flake.update();
            return flake.y >= bottomEdge || flake.x <= leftEdge || flake.x >= rightEdge;
        });
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        // First paint the wrapped component normally
        super.paint(g, c);

        // If it's not snowing, we're done:
        if (!isSnowing()) {
            return;
        }

        // Now we can draw our snowflakes:
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        for (Snowflake flake : snowflakes) {
            flake.draw(g2);
        }

        g2.dispose();
    }

    /**
     * A simple class to represent a single snowflake.
     * They say no two snowflakes are alike, but our equals() method might beg to differ!
     */
    private static class Snowflake {
        float x, y;
        float speed; // Speed could be made caller-configurable...
        float size;  // So could size, if you want to get really crazy with it.
        float drift; // Horizontal drift, or "wind" effect
        Color color;
        Color sparkleColor;

        Snowflake(float x, float y, float speed, float size, float drift, Color color, Color sparkleColor) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = size;
            this.drift = drift;
            this.color = color;
            this.sparkleColor = sparkleColor;
        }

        void update() {
            y += speed;
            x += drift;
        }

        void draw(Graphics2D g2) {
            g2.setColor(color);
            g2.fillOval((int)x, (int)y, (int)size, (int)size);

            // Add sparkle effect
            g2.setColor(sparkleColor);
            g2.fillOval((int)x + (int)size / 4, (int)y + (int)size / 4,
                        (int)size / 2, (int)size / 2);
        }
    }
}
