# swing-extras

This is swing-extras, a collection of custom components and utilities for Java Swing 
applications. The library includes a demo application so you can try out the various components and
utilities within swing-extras:

![DemoApp](demo-app.png "Demo app")
TODO update screenshot for 2.7

## Documentation

You can browse the full documentation online:
- <http://www.corbett.ca/swing-extras-book/>
- The [javadocs](http://www.corbett.ca/swing-extras-javadocs/) are also available.

## NEWS! (TODO release date here) - swing-extras 2.7 is here!

This is a maintenance release of swing-extras with a few new features and some bug fixes.
The details will go here once the release is ready. If you're reading this sentence, it means
I forgot to update the README before cutting the release. Oops!

Check out the [archived release announcements](ReleaseAnnouncements.md) for details on previous releases.

## Getting started

### Option 1: use the Maven archetype to create a new project

There is a [swing-extras Maven archetype](https://github.com/scorbo2/swing-extras-archetype/) in Maven Central
that you can use to very quickly bootstrap a new Java Swing project that uses swing-extras. Many key components
are provided out of the box:

- A skeletal MainWindow with easily-extensible menu bar, keyboard shortcuts, and an About dialog.
- Application extension support, with one example extension and documentation to get you started writing extensions!
- Application settings support, with example settings and support for a reloadable UI when settings change.
- Easy resource loading via ResourceLoader, with an example implementation class provided.
- Look and Feel support, with multiple LaFs to choose from and the ability to switch LaFs at runtime.
- SingleInstanceManager already set up so that only one instance of your app can run at a time.
- Basic logging setup (with optional LogConsole support).
- Support for generating Linux installer tarballs for your application (if you have the [install scripts](https://github.com/scorbo2/install-scripts/) installed - this is auto-detected in `pom.xml`!)

To use the archetype, run the following command:

```shell
mvn archetype:generate \
  -DarchetypeGroupId=ca.corbett \
  -DarchetypeArtifactId=swing-extras-archetype \
  -DarchetypeVersion=2.7.0 \
  -DgroupId=com.example \
  -DartifactId=my-app \
  -Dversion=1.0.0 \
  -DartifactNamePascalCase=MyApp
```

Remember to set the `groupId`, `artifactId`, `version`, and `artifactNamePascalCase` properties appropriately.
Always use the latest version of the archetype! The major and minor versions of the archetype tell you which
version of `swing-extras` it will use, but there may be multiple patch releases of the archetype for each
`swing-extras` version. The latest one is always the best choice.

Alternatively, you can also use the archetype in your IDE - most modern IDEs support Maven archetypes.
Try "New Project" and look for Maven archetypes. Instructions for IntelliJ IDEA can be found on the
[project GitHub page](https://github.com/scorbo2/swing-extras-archetype/).

### Option 2: Add swing-extras as a dependency in your existing Maven project

`swing-extras` is available in Maven Central, so you can simply list it as a dependency in your Maven `pom.xml` and
then start from scratch building your Swing application using the various components and utilities:

```xml
<dependencies>
  <dependency>
    <groupId>ca.corbett</groupId>
    <artifactId>swing-extras</artifactId>
    <version>2.7.0</version>
  </dependency>
</dependencies>
```

Refer to the [swing-extras-book](http://www.corbett.ca/swing-extras-book/) for detailed documentation on the
various `swing-extras` components and features, or refer to the [Javadocs](http://www.corbett.ca/swing-extras-javadocs/)
for API details. If you run into any issues, please check the [GitHub issues page](https://github.com/scorbo2/swing-extras/issues),
and open a new issue if your problem or suggestion hasn't been reported yet!

### Option 3: Clone the repo and build locally

If you want to run the demo app, or if you just want to play with the code locally,
for example to generate the Javadocs locally, then you can clone the repo:

```shell
git clone https://github.com/scorbo2/swing-extras.git
cd swing-extras
mvn package

# Run the built-in demo app:
java -jar target/swing-extras-2.7.0-jar-with-dependencies.jar 
```

## License

swing-extras is made available under the MIT license: https://opensource.org/license/mit

## Revision history

The swing-extras library has been under development since 2012 or so. 

View the [full release notes and version history](src/main/resources/swing-extras/releaseNotes.txt)
