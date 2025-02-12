/*
 * SPDX-License-Identifier: CDDL-1.0
 *
 * CDDL HEADER START
 *
 * This file and its contents are supplied under the terms of the
 * Common Development and Distribution License ("CDDL"), version 1.0.
 * You may only use this file in accordance with the terms of version
 * 1.0 of the CDDL.
 *
 * A full copy of the text of the CDDL should have accompanied this
 * source. A copy of the CDDL is also available via the Internet at
 * http://www.illumos.org/license/CDDL.
 *
 * CDDL HEADER END
 *
 * Copyright 2025 Peter Tribble
 *
 */

package uk.co.petertribble.jkstat.gui;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.parse.ParseableJSONZipJKstat;

/**
 * A graphical chart of a kstat, showing one or multiple statistics, output
 * directly as a png image to a file.
 *
 * @author Peter Tribble
 */
public class KstatPngImage {

    /**
     * Create a graphical chart of a kstat, showing the rate of change of the
     * given statistic for the given Kstats.
     *
     * @param jkstat a {@code JKstat}
     * @param kss a {@code KstatSet} containing the Kstats to be charted
     * @param statistic the statistic to be charted
     * @param f the {@code File} to be written to
     */
    public KstatPngImage(JKstat jkstat, KstatSet kss,
				String statistic, File f) {
	this(jkstat, kss, statistic, f, true);
    }

    /**
     * Create a graphical chart of a kstat, showing the given statistic for
     * the given Kstats.
     *
     * @param jkstat a {@code JKstat}
     * @param kss a {@code KstatSet} containing the Kstats to be charted
     * @param statistic the statistic to be charted
     * @param f the {@code File} to be written to
     * @param rates if true, show rates rather than absolute values
     */
    public KstatPngImage(JKstat jkstat, KstatSet kss, String statistic,
		File f, boolean rates) {
	saveImage(f, new KstatSetChart(jkstat, kss, statistic, rates));
    }

    /**
     * Create a graphical chart of a kstat aggregate, showing the rate of
     * change of the given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ksa a {@code KstatAggregate} to be charted
     * @param statistic the statistic to be charted
     * @param f the {@code File} to be written to
     */
    public KstatPngImage(JKstat jkstat, KstatAggregate ksa,
				String statistic, File f) {
	this(jkstat, ksa, statistic, f, true);
    }

    /**
     * Create a graphical chart of a kstat aggregate, showing the given
     * statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ksa a {@code KstatAggregate} to be charted
     * @param statistic the statistic to be charted
     * @param f the {@code File} to be written to
     * @param rates if true, show rates rather than absolute values
     */
    public KstatPngImage(JKstat jkstat, KstatAggregate ksa, String statistic,
		File f, boolean rates) {
	saveImage(f, new KstatAggregateChart(jkstat, ksa, statistic, rates));
    }

    /**
     * Create a graphical chart of a Kstat, showing the rate of change of the
     * given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ks a {@code Kstat} to be charted
     * @param statistic the statistic to be charted
     * @param f the {@code File} to be written to
     */
    public KstatPngImage(JKstat jkstat, Kstat ks, String statistic, File f) {
	this(jkstat, ks, statistic, f, true);
    }

    /**
     * Create a graphical chart of a Kstat, showing the given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ks a {@code Kstat} to be charted
     * @param statistic the statistic to be charted
     * @param f the {@code File} to be written to
     * @param rates if true, show rates rather than absolute values
     */
    public KstatPngImage(JKstat jkstat, Kstat ks, String statistic,
		File f, boolean rates) {
	saveImage(f, new KstatChart(jkstat, ks, statistic, rates));
    }

    /**
     * Create a graphical chart of a kstat aggregate, showing the rate of
     * change of the given statistics.
     *
     * @param jkstat a {@code JKstat}
     * @param ksa a {@code KstatAggregate} to be charted
     * @param statistics the {@code List} of statistics to be charted
     * @param f the {@code File} to be written to
     */
    public KstatPngImage(JKstat jkstat, KstatAggregate ksa,
				List<String> statistics, File f) {
	this(jkstat, ksa, statistics, f, true);
    }

    /**
     * Create a graphical chart of a kstat aggregate, showing the given
     * statistics.
     *
     * @param jkstat a {@code JKstat}
     * @param ksa a {@code KstatAggregate} to be charted
     * @param statistics the {@code List} of statistics to be charted
     * @param f the {@code File} to be written to
     * @param rates if true, show rates rather than absolute values
     */
    public KstatPngImage(JKstat jkstat, KstatAggregate ksa,
		List<String> statistics, File f, boolean rates) {
	saveImage(f, new KstatAggregateChart(jkstat, ksa, statistics, rates));
    }

    /**
     * Create a graphical chart of a Kstat, showing the rate of change of the
     * given statistics.
     *
     * @param jkstat a {@code JKstat}
     * @param ks a {@code Kstat} to be charted
     * @param statistics the {@code List} of statistics to be charted
     * @param f the {@code File} to be written to
     */
    public KstatPngImage(JKstat jkstat, Kstat ks, List<String> statistics,
		File f) {
	this(jkstat, ks, statistics, f, true);
    }

    /**
     * Create a graphical chart of a Kstat, showing the given statistics.
     *
     * @param jkstat a {@code JKstat}
     * @param ks a {@code Kstat} to be charted
     * @param statistics the {@code List} of statistics to be charted
     * @param f the {@code File} to be written to
     * @param rates if true, show rates rather than absolute values
     */
    public KstatPngImage(JKstat jkstat, Kstat ks,
		List<String> statistics, File f, boolean rates) {
	saveImage(f, new KstatChart(jkstat, ks, statistics, rates));
    }

    private static void usage(String message) {
	System.err.println("ERROR: " + message);
	System.err.println("Usage: png -z source -o output_file kstat_spec");
	System.err.println("  where kstat_spec is of the form");
	System.err.println("    module:instance:name:statistic");
	System.err.println("  or");
	System.err.println("    module:instance:name statistic [statistic...]");
	System.exit(1);
    }

    /*
     * Saves the current chart as an image in png format.
     *
     * @param f the File to be written to
     */
    private void saveImage(File f, KstatBaseChart kbc) {
	BufferedImage bi = kbc.getChart().createBufferedImage(500, 300);
	try {
	    ImageIO.write(bi, "png", f);
	    /*
	     * According to the API docs this should throw an IOException on
	     * error, but this doesn't seem to be the case. As a result it's
	     * necessary to catch exceptions more generally. Even this doesn't
	     * work properly, but at least we manage to convey the message to
	     * the user that the write failed, even if the error itself isn't
	     * handled.
	     */
	} catch (Exception ioe) {
	    System.err.println(ioe.toString());
	}
    }

    /**
     * Create a graph according to the command line arguments.
     *
     * @param args command line arguments
     * @param jkstat a {@code JKstat} object to supply the data
     * @param f the output {@code File}
     */
    public static void makeGraph(String[] args, JKstat jkstat, File f) {
	makeGraph(args, jkstat, f, true);
    }

    /**
     * Create a graph according to the command line arguments.
     *
     * @param args command line arguments
     * @param jkstat a {@code JKstat} object to supply the data
     * @param f the output {@code File}
     * @param rates if true, show rates, else show absolute values
     */
    public static void makeGraph(String[] args, JKstat jkstat, File f,
				boolean rates) {
	/*
	 * We can be called in two ways. The short form is
	 * module:instance:name:statistic
	 * and the long form is
	 * module:instance:name statistic1 statistic2 ...
	 */
	/*
	 * Need to specify a limit to split so we don't chomp a trailing ::
	 */
	String[] ss = args[0].split(":", 4);
	if (ss.length == 4) {
	    // short form, only one argument
	    if (args.length != 1) {
		usage("Invalid arguments.");
	    }
	    if ((ss[0].length() == 0) || (ss[1].length() == 0)
						|| (ss[2].length() == 0)) {
		// wildcard, construct a Set
		KstatAggregate ksa = KstatUtil.makeAggr(jkstat, ss[0], ss[1],
							ss[2]);
		new KstatPngImage(jkstat, ksa, ss[3], f, rates);
	    } else {
		Kstat ks = KstatUtil.makeKstat(ss[0], ss[1], ss[2]);
		if (ks == null) {
		    usage("Invalid instance - must be numeric.");
		} else {
		    new KstatPngImage(jkstat, ks, ss[3], f, rates);
		}
	    }
	} else if (ss.length == 3) {
	    // long form, need more arguments
	    if (args.length == 1) {
		usage("Invalid arguments.");
	    }
	    List<String> arglist = new ArrayList<>();
	    for (int i = 1; i < args.length; i++) {
		arglist.add(args[i]);
	    }
	    if ((ss[0].length() == 0) || (ss[1].length() == 0)
						|| (ss[2].length() == 0)) {
		// wildcard, construct a Set
		KstatAggregate ksa = KstatUtil.makeAggr(jkstat, ss[0], ss[1],
							ss[2]);
		new KstatPngImage(jkstat, ksa, arglist, f, rates);
	    } else {
		Kstat ks = KstatUtil.makeKstat(ss[0], ss[1], ss[2]);
		if (ks == null) {
		    usage("Invalid instance - must be numeric.");
		} else {
		    new KstatPngImage(jkstat, ks, arglist, f, rates);
		}
	    }
	} else {
	    usage("Invalid kstat.");
	}
    }

    private static void doMain(String oflag, String oname, String[] args,
			JKstat jkstat) {
	if ("-o".equals(oflag)) {
	    makeGraph(args, jkstat, new File(oname));
	} else {
	    usage("Invalid arguments.");
	}
    }


    /**
     * Show a chart according to the command line arguments.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
	if (args.length < 5) {
	    usage("Missing arguments.");
	}
	String[] nargs = new String[args.length - 4];
	System.arraycopy(args, 4, nargs, 0, args.length - 4);
	if ("-z".equals(args[0])) {
	    try {
		doMain(args[2], args[3], nargs,
				new ParseableJSONZipJKstat(args[1]));
	    } catch (IOException ioe) {
		usage("Invalid zip file");
	    }
	} else {
	    usage("Invalid arguments.");
	}
    }
}
