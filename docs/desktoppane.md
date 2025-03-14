<-- [Back to swing-extras documentation overview](../README.md)

# Customizable DesktopPane

The few times I used `JDesktopPane` when building Java Swing applications, I found
its lack of customization options to be a little frustrating. Specifically, I wanted
a way to customize the background and to have an optional application logo image
displayed as part of the background:

![DesktopPane](desktoppane_screenshot1.png "DesktopPane")

With just a few lines of code, you can change the background gradient, and
also the placement and opacity of the logo image:

![DesktopPane](desktoppane_screenshot2.png "DesktopPane2")

Because the gradient is dynamically generated rather than loaded from a static
image, it handles resizing the containing frame very nicely. Refer to the
gradient documentation for more information.

The logo position and transparency are also customizable:

```java
myDesktopPane.setLogoImagePlacement(LogoPlacement.TOP_RIGHT);
myDesktopPane.setLogoImageTransparency(0.5f);
```