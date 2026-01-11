# Release Announcements

Older release announcements for swing-extras releases will be archived here.

## December 31, 2025 - swing extras 2.6 release is here!

Happy New Year! The 2.6 release of swing-extras is now available.
The biggest change in 2.6 is that  ExtensionManager no longer insists on an exact version match when loading extensions,
instead just looking at the major version. So, no more mandatory re-releases of extensions for every minor
application release!

Other notable additions in swing-extras 2.6:
- new FormFields: HtmlLabelField, ListSubsetField, ButtonField
- new Utility classes: HyperlinkUtil, TextFileDetector, SingleInstanceManager
- and a few minor bug fixes.

Refer to the [release notes](src/main/resources/swing-extras/releaseNotes.txt) for a complete list.

## December 1, 2025 - swing-extras 2.5 release is here!

The 2.5 release of swing-extras is HUGE, and contains not only some new form fields and properties, but
also a major addition to app-extensions: the ability for applications built with swing-extras to
allow dynamic discovery, download, installation, and updating of application extensions! Check out
the new `UpdateManager` class and also the [ext-packager](https://github.com/scorbo2/ext-packager)
project for details on how to set this up!

Other notable additions in swing-extras 2.5:
- new FormFields: SliderField, CollapsiblePanelField, ImageListField
- much cleaner demo application with code snippets!

There are also numerous bug fixes and minor improvements throughout the library in this release.
Refer to the [release notes](src/main/resources/swing-extras/releaseNotes.txt) for a complete list.

## September 1, 2025 - swing-extras 2.4 release is here!

The 2.4 release of swing-extras includes a near-complete rewrite of swing-forms, making it
both easier to use, and also easier to extend with custom FormFields. This release also
(finally) upgrades swing-extras from Java 11 to Java 17! Behind the scenes, hundreds of unit tests
have been added across swing-forms and the associated properties system, improving code quality and
stability.

Other notable additions in swing-extras 2.4:
- The old ConfigObject/ConfigPanel combination has been deprecated and merged into the properties system.
- Properties now expose their generated form fields in a much cleaner way, making it easier to work with them.
- ComboProperty/ComboField can now be typed to avoid treating everything as strings.
- TextField has been split into ShortTextField and LongTextField for better clarity.
- Numerous minor bug fixes and improvements throughout the library!

## July 20, 2025 - swing-extras 2.3 release is here!

This is a maintenance release with a few minor new features and a handful of bug fixes.

Notable additions in swing-extras 2.3:
- New FormField: ListField, for wrapping a JList of items.
- New Property: ListProperty, for storing a list of items in the properties system.
- Improvements to ImageUtil, and the addition of some animation-capable code.
- Some minor bug fixes in the extensions and progress classes.

## June 10, 2025 - swing-extras 2.2 release is here!

This release of swing-extras brings a number of improvements to the ExtensionManagerDialog and
to the extension system in general. Now, application extensions can share config properties.
There is also a way to preview extension configuration properties in the ExtensionManagerDialog,
and there's also (finally) a way to control the order in which extensions are loaded!

## May 17, 2025 - swing-extras 2.1 release is here!

This release of swing-extras brings first-class Look and Feel support to the library, with the
addition of LookAndFeelManager and LookAndFeelProperty. Applications can now easily
allow users to select their preferred Look and Feel from a list of installed options.
Check out the complete list of supported Look and Feels in the updated demo application!

Additionally, a number of smaller improvements and bug fixes have been made throughout the library.

## April 13, 2025 - swing-extras 2.0 release is here!

This is a HUGE release for swing-extras! Both the `app-extension` library and the `swing-forms` library
have been merged into swing-extras, making it a one-stop shop for building Swing applications with
configurable forms and extension support! With a single Maven dependency, you can now build
Swing applications that support both dynamic forms and application extensions! This will make development
and extension of these formerly-separate libraries much easier going forward.
