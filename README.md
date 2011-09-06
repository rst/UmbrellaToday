Umbrella Today, the Android App, shanghaied into service as a Scala demo
------------------------------------------------------------------------

A rewrite of an app to tell you whether you need your umbrella, today.

This version is written in Scala, using the Positronic Net library
(version 0.1).  The build procedure, using sbt (the "simple build
tool" --- or so they call it) are something like the following:

First, install sbt 0.10.0 per [instructions](https://github.com/harrah/xsbt/wiki/Setup).  

Next, install a current version of the [sbt-android-plugin](https://github.com/jberkel/android-plugin).  (Building and installing a snapshot may be required.) 

Then, get a copy of [Positronic Net itself](https://github.com/rst/positronic_net), sbt10 branch, and publish to your local ivy repo:

    $ cd [your workspace]
    $ git clone https://github.com/rst/positronic_net.git
    $ git checkout -b sbt10 origin/sbt10
    $ cd positronic_net
    $ sbt "project PositronicNetLib" publish-local

(This does just publish a jar file, containing the classes, and nothing but
the classes --- no resources.  Fortunately, the library doesn't declare any
resources, at least not yet, so this actually works.)

Lastly compile and build this app:

    $ cd [your workspace]
    $ git clone https://github.com/rst/UmbrellaToday.git
    $ cd UmbrellaToday
    $ sbt android:package-debug

If that all worked, you should wind up with an apk in `.../UmbrellaToday/target//umbrella-today-0.1.apk`

Ownerships
----------

Background image from http://www.flickr.com/photos/protectorrr/2406577829/

Umbrella Today Web app by thoughtbot

Original Java version written at a Boston Android hackfest; typed by Matt Horan.

Rewritten in Scala (with Positronic Net) by Robert Thau