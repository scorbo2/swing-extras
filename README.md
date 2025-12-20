# swing-extras

This is swing-extras, a collection of custom components and utilities for Java Swing 
applications. The library includes a demo application so you can try out the various components and
utilities within swing-extras:

![DemoApp](demo-app.png "Demo app")
TODO update screenshot for 2.6 release

## Documentation

You can browse the full documentation online:
- <http://www.corbett.ca/swing-extras-book/>
- The [javadocs](http://www.corbett.ca/swing-extras-javadocs/) are also available.

## TODO release date here - swing extras 2.6 relese is here!
This release offers a few minor new features and several bug fixes and improvements.
TODO details here when 2.6 is ready.

## NEWS! December 1, 2025 - swing-extras 2.5 release is here!

The 2.5 release of swing-extras is HUGE, and contains not only some new form fields and properties, but
also a major addition to app-extensions: the ability for applications built with swing-extras to
allow dynamic discovery, download, installation, and updating of application extensions! Check out
the new `UpdateManager` class and also the [ext-packager](https://github.com/scorbo2/ext-packager) 
project for details on how to set this up!

Other notable additions in swing-extras 2.5:
- new FormField: SliderField
- new FormField: CollapsiblePanelField
- new FormField: ImageListField
- much cleaner demo application with code snippets!

There are also numerous bug fixes and minor improvements throughout the library in this release.
Refer to the [release notes](src/main/resources/swing-extras/releaseNotes.txt) for a complete list.

## How do I get it?

`swing-extras` is available in Maven Central, so you can simply list it as a dependency:

```xml
<dependencies>
  <dependency>
    <groupId>ca.corbett</groupId>
    <artifactId>swing-extras</artifactId>
    <version>2.6.0</version>
  </dependency>
</dependencies>
```

If you want to run the demo app, or if you want to play with the code locally,
for example to generate the javadocs locally, then you can clone the repo:

```shell
git clone https://github.com/scorbo2/swing-extras.git
cd swing-extras
mvn package

# Run the built-in demo app:
java -jar target/swing-extras-2.6.0-jar-with-dependencies.jar 
```

## License

swing-extras is made available under the MIT license: https://opensource.org/license/mit

## Revision history

The swing-extras library has been under development since 2012 or so. 

View the [full release notes and version history](src/main/resources/swing-extras/releaseNotes.txt)
