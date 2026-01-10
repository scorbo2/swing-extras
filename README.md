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

## How do I get it?

`swing-extras` is available in Maven Central, so you can simply list it as a dependency:

```xml
<dependencies>
  <dependency>
    <groupId>ca.corbett</groupId>
    <artifactId>swing-extras</artifactId>
    <version>2.7.0</version>
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
java -jar target/swing-extras-2.7.0-jar-with-dependencies.jar 
```

## License

swing-extras is made available under the MIT license: https://opensource.org/license/mit

## Revision history

The swing-extras library has been under development since 2012 or so. 

View the [full release notes and version history](src/main/resources/swing-extras/releaseNotes.txt)
