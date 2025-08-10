JKstat - a java jni interface to Solaris/illumos kstats.


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

JKstat is licensed under the CDDL, just like the bulk of OpenSolaris - see
the file LICENSES/CDDL-1.0.txt (also as help/CDDL.txt) for details

JKstat incorporates JFreeChart, Copyright 2000-present by David Gilbert and
Contributors. JFreeChart is covered by the LGPL - see the file
LICENSES/LGPL-2.1+.txt - and can be obtained from
https://www.jfree.org/jfreechart/.

JKstat incorporates a couple of sets of utility classes. See
http://www.petertribble.co.uk/Solaris/jingle.html
http://www.petertribble.co.uk/Solaris/jumble.html

This product includes software developed at
The Apache Software Foundation (https://www.apache.org/).

Specifically, Apache XML-RPC, Copyright 1999-2009 The Apache Software
Foundation, and Apache HttpComponents Client, Copyright 1999-2012 The
Apache Software Foundation. See the file LICENSES/Apache-2.0.txt for the
details of the license for these products.

JKstat incorporates openjson https://github.com/openjson/openjson
See the file LICENSES/Apache-2.0.txt for the details of the license
for openjson.

The javascript client uses Flot https://www.flotcharts.org/
and jsTree https://www.jstree.com/, both under the MIT License.
