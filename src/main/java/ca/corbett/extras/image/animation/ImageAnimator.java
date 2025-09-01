package ca.corbett.extras.image.animation;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Smoothly animates an image from a source location to a destination location, with optional
 * acceleration and deceleration parameters on either end of the image movement.
 * <p>
 *     <b>Controlling the animation</b>
 * </p>
 * <ul>
 *     <li><b>maxSpeed</b> is how many pixels the image will be moved per frame when it has
 *     attained maximum speed (i.e. after acceleration).
 *     <li><b>easingStrength</b> describes the strength of the "easing" function, which determines
 *     how quickly the image accelerates from a standstill up to maxSpeed, and how quickly it
 *     decelerates from max speed back down to a standstill.
 *     <li><b>easingZonePercentage</b> this is the distance (expressed as a percentage of the
 *     distance between source location and destination location) that the image will spend
 *     in acceleration or deceleration. For example, a value of 0.1 (ten percent) means that
 *     the image will spend the first ten percent of its voyage accelerating up to full speed,
 *     then spend 80 percent of the voyage at full speed, and then spend the last ten percent
 *     of its voyage decelerating down to zero. Valid values are 0.0 (instance acceleration)
 *     up to 0.5 (first half of the trip is acceleration, second half of the trip is slowing down).
 *     <li><b>easingType</b> allows you to choose whether easing occurs only at the start
 *     of the voyage (EASE_IN), only at the end of the voyage (EASE_OUT), or at each end
 *     of the voyage (EASE_IN_OUT). You can also disable easing altogether with LINEAR.
 * </ul>
 * You can specify an alpha value if you wish the image to be drawn partially transparent.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a> (with help from Claude!)
 * @since swing-extras 2.3
 */
public class ImageAnimator {
    protected BufferedImage image;
    protected double startX, startY;
    protected double destX, destY;
    protected double currentX, currentY;
    protected double maxSpeed;
    protected double easingStrength;
    protected double easingZonePercentage; // Percentage of travel distance for easing zones
    protected float transparency;

    protected long lastUpdateTime;
    protected boolean movementComplete;
    protected double totalDistance;
    protected double directionX, directionY; // Unit vector for direction

    // Easing types
    public enum EasingType {
        EASE_IN_OUT,
        EASE_IN,
        EASE_OUT,
        LINEAR
    }

    protected EasingType easingType;

    /**
     * Creates an ImageAnimator with default easing settings.
     */
    public ImageAnimator(BufferedImage image, double startX, double startY,
                         double destX, double destY, double maxSpeed) {
        this(image, startX, startY, destX, destY, maxSpeed, 2.0, EasingType.EASE_IN_OUT, 0.1);
    }

    /**
     * Creates an ImageAnimator with configurable easing.
     *
     * @param image The BufferedImage to animate
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param destX Destination X coordinate
     * @param destY Destination Y coordinate
     * @param maxSpeed Maximum movement speed in pixels per second
     * @param easingStrength Strength of easing effect (1.0 = linear, higher = more curved)
     * @param easingType Type of easing to apply
     * @param easingZonePercentage Percentage of travel distance for easing zones (0.0 to 0.5)
     */
    public ImageAnimator(BufferedImage image, double startX, double startY,
                         double destX, double destY, double maxSpeed,
                         double easingStrength, EasingType easingType, double easingZonePercentage) {
        this.image = image;
        this.startX = startX;
        this.startY = startY;
        this.destX = destX;
        this.destY = destY;
        this.currentX = startX;
        this.currentY = startY;
        this.maxSpeed = maxSpeed;
        this.easingStrength = Math.max(1.0, easingStrength);
        this.easingType = easingType;
        this.easingZonePercentage = Math.max(0.0, Math.min(0.5, easingZonePercentage));
        this.transparency = 1.0f;

        this.lastUpdateTime = System.nanoTime();
        this.movementComplete = false;

        calculateMovementParameters();
    }

    protected void calculateMovementParameters() {
        double deltaX = destX - startX;
        double deltaY = destY - startY;
        this.totalDistance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (totalDistance > 0) {
            this.directionX = deltaX / totalDistance;
            this.directionY = deltaY / totalDistance;
        } else {
            this.directionX = 0;
            this.directionY = 0;
            this.movementComplete = true;
        }
    }

    protected double calculateEasingWithZones(double progress) {
        // Clamp progress to [0, 1]
        progress = Math.max(0.0, Math.min(1.0, progress));

        // If easing zone is 0, return full speed (instant acceleration)
        if (easingZonePercentage == 0.0) {
            return 1.0;
        }

        // Calculate zone boundaries
        double accelerationZoneEnd = easingZonePercentage;
        double decelerationZoneStart = 1.0 - easingZonePercentage;

        if (progress <= accelerationZoneEnd) {
            // In acceleration zone - ease from 0 to full speed
            double zoneProgress = progress / easingZonePercentage;
            return calculateEasing(zoneProgress, true, false); // ease-out for acceleration
        } else if (progress >= decelerationZoneStart) {
            // In deceleration zone - ease from full speed to 0
            double zoneProgress = (progress - decelerationZoneStart) / easingZonePercentage;
            return calculateEasing(1.0 - zoneProgress, false, true); // ease-in for deceleration
        } else {
            // In constant speed zone
            return 1.0;
        }
    }

    protected double calculateEasing(double progress) {
        return calculateEasing(progress, false, false);
    }

    protected double calculateEasing(double progress, boolean forceEaseOut, boolean forceEaseIn) {
        // Clamp progress to [0, 1]
        progress = Math.max(0.0, Math.min(1.0, progress));

        EasingType typeToUse = easingType;
        if (forceEaseOut) {
            typeToUse = EasingType.EASE_OUT;
        } else if (forceEaseIn) {
            typeToUse = EasingType.EASE_IN;
        }

        switch (typeToUse) {
            case LINEAR:
                return 1.0;

            case EASE_IN:
                return Math.pow(progress, easingStrength);

            case EASE_OUT:
                return 1.0 - Math.pow(1.0 - progress, easingStrength);

            case EASE_IN_OUT:
            default:
                if (progress < 0.5) {
                    return 0.5 * Math.pow(2.0 * progress, easingStrength);
                } else {
                    return 1.0 - 0.5 * Math.pow(2.0 * (1.0 - progress), easingStrength);
                }
        }
    }

    /**
     * Updates movement of the image and renders it at its new position.
     */
    public void renderFrame(Graphics2D g) {
        if (! movementComplete) {
            long currentTime = System.nanoTime();
            double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0; // Convert to seconds
            lastUpdateTime = currentTime;

            // Calculate current distance from start
            double currentDistanceFromStart = Math.sqrt(
                (currentX - startX) * (currentX - startX) +
                    (currentY - startY) * (currentY - startY)
            );

            // Calculate progress (0.0 to 1.0)
            double progress = totalDistance > 0 ? currentDistanceFromStart / totalDistance : 1.0;

            // Apply easing function with zones to get speed multiplier
            double speedMultiplier = calculateEasingWithZones(progress);

            // Calculate movement for this frame
            double frameDistance = maxSpeed * speedMultiplier * deltaTime;

            // Guarantee minimum movement of 1 pixel per frame (when not at destination)
            double remainingDistance = Math.sqrt(
                (destX - currentX) * (destX - currentX) +
                    (destY - currentY) * (destY - currentY)
            );

            if (remainingDistance > 0) {
                // Ensure we move at least 1 pixel per frame, but not more than remaining distance
                frameDistance = Math.max(frameDistance, 1.0);
                frameDistance = Math.min(frameDistance, remainingDistance);
            }

            // Calculate new position
            double newX = currentX + directionX * frameDistance;
            double newY = currentY + directionY * frameDistance;

            // Check if we've reached the destination
            if (frameDistance >= remainingDistance || remainingDistance <= 0.5) {
                // We've reached the destination (within half a pixel)
                currentX = destX;
                currentY = destY;
                movementComplete = true;
            }
            else {
                currentX = newX;
                currentY = newY;
            }
        }

        if (image != null) {
            // Render with transparency if requested:
            if (transparency < 1.0f) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
            }

            g.drawImage(image, (int)Math.round(currentX), (int)Math.round(currentY), null);

            // Clear transparency setting when finished:
            if (transparency < 1.0f) {
                g.setComposite(AlphaComposite.Clear);
            }
        }
    }

    /**
     * @return true if the image has reached its destination
     */
    public boolean isMovementComplete() {
        return movementComplete;
    }

    /**
     * Sets a new destination for the image. The image will start moving towards
     * this new destination from its current position.
     */
    public void setDestination(double newDestX, double newDestY) {
        this.startX = this.currentX;
        this.startY = this.currentY;
        this.destX = newDestX;
        this.destY = newDestY;
        this.movementComplete = false;
        this.lastUpdateTime = System.nanoTime();
        calculateMovementParameters();
    }

    /**
     * Immediately positions the image at the specified coordinates without animation.
     */
    public void setPosition(double x, double y) {
        this.currentX = x;
        this.currentY = y;
        this.startX = x;
        this.startY = y;
    }

    /**
     * @return current X position of the image
     */
    public double getCurrentX() {
        return currentX;
    }

    /**
     * @return current Y position of the image
     */
    public double getCurrentY() {
        return currentY;
    }

    /**
     * @return the BufferedImage being animated
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Sets a new image to animate (maintains current position and destination).
     */
    public void setImage(BufferedImage newImage) {
        this.image = newImage;
    }

    /**
     * @return current movement progress as a value between 0.0 and 1.0
     */
    public double getProgress() {
        if (totalDistance == 0) return 1.0;

        double currentDistanceFromStart = Math.sqrt(
            (currentX - startX) * (currentX - startX) +
                (currentY - startY) * (currentY - startY)
        );
        return Math.min(1.0, currentDistanceFromStart / totalDistance);
    }

    /**
     * Updates the maximum movement speed.
     */
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     * Updates the easing strength.
     */
    public void setEasingStrength(double easingStrength) {
        this.easingStrength = Math.max(1.0, easingStrength);
    }

    /**
     * Updates the easing type.
     */
    public void setEasingType(EasingType easingType) {
        this.easingType = easingType;
    }

    /**
     * Updates the easing zone percentage.
     * @param easingZonePercentage Percentage of travel distance for easing zones (0.0 to 0.5)
     */
    public void setEasingZonePercentage(double easingZonePercentage) {
        this.easingZonePercentage = Math.max(0.0, Math.min(0.5, easingZonePercentage));
    }

    /**
     * @return current easing zone percentage
     */
    public double getEasingZonePercentage() {
        return easingZonePercentage;
    }

    public float getTransparency() {
        return transparency;
    }

    /**
     * Sets the alpha value to use when drawing the image. A value of 0.0 means the image will
     * be fully transparent (invisible). A value of 1.0 (the default value) means the image will
     * be fully opaque (will completely obstruct whatever is behind it. Values in between 0 and 1
     * will cause partial transparency.
     */
    public void setTransparency(float transparency) {
        this.transparency = transparency;
    }
}