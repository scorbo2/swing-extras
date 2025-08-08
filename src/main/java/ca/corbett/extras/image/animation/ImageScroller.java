package ca.corbett.extras.image.animation;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Displays an oversized image and slowly scrolls it either horizontally back-and-forth (for landscape
 * images) or vertically up-and-down (for portrait images). The trick here is that when a scroll limit
 * is reached, instead of just "bouncing" and immediately reversing the scroll direction at full speed,
 * the image scrolling will slow down as it approaches the scroll limit, bounce, and then slowly accelerate
 * as it moves away from the scroll limit. This makes the scrolling feel a lot more natural.
 * <p>
 *     <b>Controlling the bounce parameters</b>
 * </p>
 * <ul>
 *     <li>You can use <b>EasingStrength</b> to determine the "strength" of the bounce. The options are Linear,
 *     Quadratic, and Cubic, with a progressively pronounced effect. The default is Quadratic.
 *     <li><b>bounceZoneRatio</b> determines what percentage of the image area to use as the "bounce zone" - that
 *     is, the zone in which deceleration and acceleration will happen. The default value is 0.06, indicating
 *     the extreme 6% edges of the image will be used for the bounce zone. Valid values range from 0.0 (no bounce,
 *     just reverse direction at full speed) up to 0.5 (half the image).
 *     <li><b>ScrollSpeed</b> is used to determine the maximum speed of the image scrolling (i.e. when not
 *     decelerating or accelerating). Values range from VERY_SLOW (1) up to VERY_FAST (5). The numeric valud
 *     represents the number of pixels per frame that the image will move.
 *     <li><b>minSpeedRatio</b> determines the minimum scrolling speed (during a "bounce") as a percentage
 *     of the ScrollSpeed. Note that the image will scroll a minimum pixel value of 1 per frame regardless
 *     of minSpeedRatio. Fractional pixel values are rounded up to 1 to prevent the animation from freezing.
 * </ul>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a> (with help from Claude!)
 * @since swing-extras 2.3
 */
public class ImageScroller {

    /**
     * Determines pixels per frame of animation movement.
     */
    public enum ScrollSpeed {
        VERY_SLOW("Very slow", 1),
        SLOW("Slow", 2),
        MEDIUM("Medium", 3),
        FAST("Fast", 4),
        VERY_FAST("Very fast", 5);

        private final String label;
        private final int speed;

        ScrollSpeed(String label, int speed) {
            this.label = label;
            this.speed = speed;
        }

        @Override
        public String toString() {
            return label;
        }

        public int getSpeed() {
            return speed;
        }
    }

    /**
     * Determines the extent to which we slow down as we approach a scroll limit, and also
     * how quickly we speed up as we move away from a scroll limit. This makes for a nice
     * and natural "bounce" effect when we scroll right to the limit, instead of just reversing
     * direction at the same speed instantly.
     */
    public enum EasingStrength {
        LINEAR("Linear", 1.0f),
        QUADRATIC("Quadratic", 2.0f),
        CUBIC("Cubic", 3.0f);

        private final String label;
        private final float value;

        EasingStrength(String label, float value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }

        public float getValue() {
            return value;
        }
    }

    protected boolean isRunning;

    protected ScrollSpeed scrollSpeed;
    protected EasingStrength easingStrength;
    protected BufferedImage image;
    protected final int displayWidth;
    protected final int displayHeight;
    protected float zoomFactor;
    protected int xOffset;
    protected int yOffset;
    protected float xDelta;
    protected float yDelta;
    protected int xDirection = -1; // -1 for left, +1 for right
    protected int yDirection = -1; // -1 for up, +1 for down
    protected boolean scaleCalculationsDone;
    protected float bounceZoneRatio; // What fraction of the scrollable area is the "bounce zone"
    protected float minSpeedRatio;   // Minimum speed as a ratio of max speed (0.0 = complete stop, 1.0 = no slowdown)

    public ImageScroller(BufferedImage image, int displayWidth, int displayHeight) {
        scrollSpeed = ScrollSpeed.SLOW;
        easingStrength = EasingStrength.QUADRATIC;
        bounceZoneRatio = 0.06f; // arbitrary default
        minSpeedRatio = 0.1f; // arbitrary default
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        setImage(image);
    }

    public ScrollSpeed getScrollSpeed() {
        return scrollSpeed;
    }

    /**
     * This represents the <b>maximum</b> scrolling speed, in pixels per frame. The actual scrolling speed
     * may be less if the image is approaching or receding away from a "bounce point" (scroll limit).
     */
    public void setScrollSpeed(ScrollSpeed scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }

    public EasingStrength getEasingStrength() {
        return easingStrength;
    }

    /**
     * Sets the strength of the "bounce" calculations, to exaggerate the acceleration and deceleration effects.
     */
    public void setEasingStrength(EasingStrength easingStrength) {
        this.easingStrength = easingStrength;
    }

    public float getBounceZoneRatio() {
        return bounceZoneRatio;
    }

    /**
     * Sets the width of the "bounce zones", or edges of the image where we will slow down as we approach a
     * scroll limit and speed up as we scroll away from it. This is expressed as a percentage of the image
     * scroll dimension (width for landscape images and height for portrait images). Valid values are
     * 0.0 for no bounce effect up to 0.5 for a very exaggerated bounce effect. The default value is 0.06.
     */
    public void setBounceZoneRatio(float bounceZoneRatio) {
        this.bounceZoneRatio = Math.max(0.0f, bounceZoneRatio);
        this.bounceZoneRatio = Math.min(0.5f, this.bounceZoneRatio);
    }

    public float getMinSpeedRatio() {
        return minSpeedRatio;
    }

    /**
     * Sets the <b>minimum</b> allowable scroll speed during deceleration/acceleration, expressed as a percentage
     * of the maximum speed. Note that the image will always scroll by at least one pixel per frame, to prevent
     * the case where a fractional pixel movement value will be rounded down to 0 and the animation freezes.
     */
    public void setMinSpeedRatio(float minSpeedRatio) {
        this.minSpeedRatio = minSpeedRatio;
    }

    /**
     * Sets the image to be scrolled. Can be landscape (wider than tall) or portrait (taller than wide).
     * Large images are better! The whole point is to scroll within an image larger than the display area.
     * Think like 3000x1080 or something. The image will be scaled if needed so that it fills the display.
     * (For example, a 3000x960 image on a 1920x1080 display would be scaled up proportionally to 3375x1080
     * so that it vertically fills the display).
     */
    public void setImage(BufferedImage image) {
        stop();

        boolean isLandscape = image.getWidth() > image.getHeight();
        int scaledWidth;
        int scaledHeight;

        if (isLandscape) {
            // Scale based on display height - the constraining dimension
            double scaleFactor = (double) displayHeight / image.getHeight();
            scaledWidth = (int) Math.round(image.getWidth() * scaleFactor);
            scaledHeight = displayHeight;
        } else {
            // For portrait and square images, scale based on display width
            double scaleFactor = (double) displayWidth / image.getWidth();
            scaledWidth = displayWidth;
            scaledHeight = (int) Math.round(image.getHeight() * scaleFactor);
        }

        // Create the new scaled image
        this.image = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = this.image.createGraphics();

        // Enable high-quality rendering
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the original image scaled to the new dimensions
        g.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();

        reset();
    }

    public BufferedImage getImage() {
        return image;
    }

    protected void reset() {
        zoomFactor = 0f;
        xOffset = 0;
        yOffset = 0;
        xDelta = 0;
        yDelta = 0;
        scaleCalculationsDone = false;
        isRunning = true;
    }

    /**
     * Stops the scrolling and flushes the current image.
     */
    public void stop() {
        if (image != null) {
            image.flush();
        }
        isRunning = false;
    }

    /**
     * Renders a single frame of animation and handles scrolling the image by an appropriate amount.
     * Your animation loop may target whatever FPS your application requires, and this method
     * will scroll the image by the correct amount of pixels based on our ScrollSpeed.
     */
    public void renderFrame(Graphics2D g) {
        // If we're stopped (e.g. to load the next image), just return:
        if (! isRunning) {
            return;
        }

        // Only do the scale calculations once:
        if (!scaleCalculationsDone) {
            xOffset = 0;
            yOffset = 0;
            scaleCalculationsDone = true;
            boolean isPortrait = image.getHeight() > image.getWidth();
            zoomFactor = isPortrait ? (float)displayWidth / image.getWidth() : (float)displayHeight / image.getHeight();
            if (zoomFactor <= 0.0) {
                zoomFactor = 1;
            }
            if (isPortrait) {
                yDirection = -1; // start scrolling up
            }
            else {
                xDirection = -1; // start scrolling left
            }
            int imgWidth = (int)(image.getWidth() * zoomFactor);
            int imgHeight = (int)(image.getHeight() * zoomFactor);

            // Wonky case: if we scale it down and it ends up fitting inside the screen,
            // we can't scroll around inside it, so just center it instead:
            if (imgWidth <= displayWidth && imgHeight <= displayHeight) {
                xDelta = 0;
                yDelta = 0;
                xOffset = (displayWidth / 2) - (imgWidth / 2);
                yOffset = (displayHeight / 2) - (imgHeight / 2);
            }
        }
        int imgWidth = (int)(image.getWidth() * zoomFactor);
        int imgHeight = (int)(image.getHeight() * zoomFactor);
        g.drawImage(image, xOffset, yOffset, imgWidth, imgHeight, null);

        // Calculate base speed
        float baseSpeed = scrollSpeed.getSpeed();

        // Update horizontal movement
        if (imgWidth > displayWidth) {
            float speedMultiplier = calculateSpeedMultiplier(xOffset, displayWidth - imgWidth, displayWidth);
            xDelta = xDirection * baseSpeed * speedMultiplier;

            // Ensure minimum movement of 1 pixel to prevent animation from getting stuck
            if (xDirection == -1 && xDelta > -1) {
                xDelta = -1;
            }
            else if (xDirection == 1 && xDelta < 1) {
                xDelta = 1;
            }

            xOffset += (int)xDelta;

            // Check bounds and reverse direction if needed
            if (xOffset >= 0) {
                xOffset = 0;
                xDirection = -1;
            }
            else if (xOffset <= (displayWidth - imgWidth)) {
                xOffset = displayWidth - imgWidth;
                xDirection = 1;
            }
        }

        // Update vertical movement
        if (imgHeight > displayHeight) {
            float speedMultiplier = calculateSpeedMultiplier(yOffset, displayHeight - imgHeight, displayHeight);
            yDelta = yDirection * baseSpeed * speedMultiplier;

            // Ensure minimum movement of 1 pixel to prevent animation from getting stuck
            if (yDirection == -1 && yDelta > -1) {
                yDelta = -1;
            }
            else if (yDirection == 1 && yDelta < 1) {
                yDelta = 1;
            }

            yOffset += (int)yDelta;

            // Check bounds and reverse direction if needed
            if (yOffset >= 0) {
                yOffset = 0;
                yDirection = -1;
            }
            else if (yOffset <= (displayHeight - imgHeight)) {
                yOffset = displayHeight - imgHeight;
                yDirection = 1;
            }
        }
    }

    /**
     * Calculates speed multiplier based on distance from bounce points
     *
     * @param currentPos Current position (xOffset or yOffset)
     * @param minPos     Minimum position (boundary)
     * @param screenSize Screen dimension (width or height)
     * @return Speed multiplier between minSpeedRatio and 1.0
     */
    protected float calculateSpeedMultiplier(int currentPos, int minPos, int screenSize) {
        // Calculate total scrollable distance
        int totalDistance = Math.abs(minPos);
        if (totalDistance == 0) { return 1.0f; }

        // Calculate bounce zone size
        int bounceZoneSize = (int)(totalDistance * bounceZoneRatio);
        if (bounceZoneSize == 0) { return 1.0f; }

        // Distance from top boundary (0)
        int distanceFromTop = Math.abs(currentPos);

        // Distance from bottom boundary
        int distanceFromBottom = Math.abs(currentPos - minPos);

        // Find the minimum distance to any boundary
        int distanceFromNearestBound = Math.min(distanceFromTop, distanceFromBottom);

        // If we're outside the bounce zone, use full speed
        if (distanceFromNearestBound >= bounceZoneSize) {
            return 1.0f;
        }

        // Calculate easing factor (0.0 at boundary, 1.0 at edge of bounce zone)
        float easingFactor = (float)distanceFromNearestBound / bounceZoneSize;

        // Apply easing curve
        easingFactor = (float)Math.pow(easingFactor, easingStrength.getValue());

        // Interpolate between minimum and maximum speed
        return minSpeedRatio + (1.0f - minSpeedRatio) * easingFactor;
    }
}
