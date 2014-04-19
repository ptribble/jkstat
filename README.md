JKstat - a java jni interface to Solaris/OpenSolaris kstats.


http://www.petertribble.co.uk/Solaris/jkstat.html

The simplest way to start is using the kstat browser:

./jkstat browser

which should allow you to browse through the available kstats.

To see what subcommands are available, just type ./jkstat without any
arguments.

If you have any problems, suggestions for ways in which JKstat can be
improved, or new uses to which it can be put, or interesting ways in
which kstats can be visualized, let me know.

To compile yourself, see the build script.

peter.tribble@gmail.com


Licensing
=========

JKstat is licensed under CDDL, just like the bulk of OpenSolaris - see
the file CDDL.txt

JKstat incorporates JFreeChart, (C)opyright 2000-2011 by Object
Refinery Limited and Contributors. JFreeChart is covered by the LGPL -
see the file LGPL.txt, and can be obtained from
http://www.jfree.org/jfreechart/.

JKstat incorporates a couple of sets of utility classes. See
http://www.petertribble.co.uk/Solaris/jingle.html
http://www.petertribble.co.uk/Solaris/jumble.html

The SpringUtilities and TableSorter classes are lifted straight from
the Swing tutorial. (The latter isn't needed for Java 6, but I regard
Java 5 support as important.)

This product includes software developed at
The Apache Software Foundation (http://www.apache.org/).

Specifically, Apache XML-RPC, Copyright 1999-2009 The Apache Software
Foundation, and Apache HttpComponents Client, Copyright 1999-2012 The
Apache Software Foundation. See the file APACHE-LICENSE.txt for the
details of the license for these products.

The javascript client uses Flot http://www.flotcharts.org/
and jsTree http://www.jstree.com/, both under the MIT License.
