/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
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
 * directly as a png image to a file. The statistic is charted by area, and
 * multiple statistics are stacked.
 *
 * @author Peter Tribble
 */
public class KstatAreaPngImage {

    /**
     * Create an area Chart of the rate of change of the given statistic.
     *
     * @param jkstat a JKstat
     * @param kss the KstatSet supplying the data
     * @param statistic the statistic to be charted
     * @param f the File to be written to
     */
    public KstatAreaPngImage(JKstat jkstat, KstatSet kss,
				String statistic, File f) {
	this(jkstat, kss, statistic, f, true);
    }

    /**
     * Create an area Chart of the given aggregated statistic.
     *
     * @param jkstat a JKstat
     * @param kss the KstatSet supplying the data
     * @param statistic the statistic to be charted
     * @param f the File to be written to
     * @param rates if true, show rates, else show absolute values
     */
    public KstatAreaPngImage(JKstat jkstat, KstatSet kss,
				String statistic, File f, boolean rates) {
	saveImage(f, new KstatSetAreaChart(jkstat, kss, statistic, rates));
    }

    /**
     * Create an area Chart of the rate of change of the given aggregated
     * statistic.
     *
     * @param jkstat a JKstat
     * @param ksa the KstatAggregate supplying the data
     * @param statistic the statistic to be charted
     * @param f the File to be written to
     */
    public KstatAreaPngImage(JKstat jkstat, KstatAggregate ksa,
				String statistic, File f) {
	this(jkstat, ksa, statistic, f, true);
    }

    /**
     * Create an area Chart of the given aggregated statistic.
     *
     * @param jkstat a JKstat
     * @param ksa the KstatAggregate supplying the data
     * @param statistic the statistic to be charted
     * @param f the File to be written to
     * @param rates if true, show rates, else show absolute values
     */
    public KstatAreaPngImage(JKstat jkstat, KstatAggregate ksa,
				String statistic, File f, boolean rates) {
	saveImage(f, new KstatAggregateAreaChart(jkstat, ksa, statistic,
						rates));
    }

    /**
     * Create an area Chart of the rate of change of the given statistic.
     *
     * @param jkstat a JKstat
     * @param ks the Kstat supplying the data
     * @param statistic the statistic to be charted
     * @param f the File to be written to
     */
    public KstatAreaPngImage(JKstat jkstat, Kstat ks, String statistic,
					File f) {
	this(jkstat, ks, statistic, f, true);
    }

    /**
     * Create an area Chart of the given statistic.
     *
     * @param jkstat a JKstat
     * @param ks the Kstat supplying the data
     * @param statistic the statistic to be charted
     * @param f the File to be written to
     * @param rates if true, show rates, else show absolute values
     */
    public KstatAreaPngImage(JKstat jkstat, Kstat ks, String statistic,
		File f, boolean rates) {
	saveImage(f, new KstatAreaChart(jkstat, ks, statistic, rates));
    }

    /**
     * Create an area Chart of the rate of change of the given aggregated
     * statistics.
     *
     * @param jkstat a JKstat
     * @param ksa the KstatAggregate supplying the data
     * @param statistics the statistics to be charted
     * @param f the File to be written to
     */
    public KstatAreaPngImage(JKstat jkstat, KstatAggregate ksa,
		List<String> statistics, File f) {
	this(jkstat, ksa, statistics, f, true);
    }

    /**
     * Create an area Chart of the given aggregated statistics.
     *
     * @param jkstat a JKstat
     * @param ksa the KstatAggregate supplying the data
     * @param statistics the statistics to be charted
     * @param f the File to be written to
     * @param rates if true, show rates, else show absolute values
     */
    public KstatAreaPngImage(JKstat jkstat, KstatAggregate ksa,
		List<String> statistics, File f, boolean rates) {
	saveImage(f, new KstatAggregateAreaChart(jkstat, ksa, statistics,
						rates));
    }

    /**
     * Create an area Chart of the rate of change of the given statistics.
     *
     * @param jkstat a JKstat
     * @param ks the Kstat supplying the data
     * @param statistics the statistics to be charted
     * @param f the File to be written to
     */
    public KstatAreaPngImage(JKstat jkstat, Kstat ks,
		List<String> statistics, File f) {
	this(jkstat, ks, statistics, f, true);
    }

    /**
     * Create an area Chart of the given statistics.
     *
     * @param jkstat a JKstat
     * @param ks the Kstat supplying the data
     * @param statistics the statistics to be charted
     * @param f the File to be written to
     * @param rates if true, show rates, else show absolute values
     */
    public KstatAreaPngImage(JKstat jkstat, Kstat ks,
		List<String> statistics, File f, boolean rates) {
	saveImage(f, new KstatAreaChart(jkstat, ks, statistics, rates));
    }

    private static void usage(String message) {
	System.err.println("ERROR: " + message);
	System.err.println("Usage: areapng -z source -o output_file "
				+ "kstat_spec");
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
	     * According to the API docs this should throw an IOException
	     * on error, but this doesn't seem to be the case. As a result
	     * it's necessary to catch exceptions more generally. Even this
	     * doesn't work properly, but at least we manage to convey the
	     * message to the user that the write failed, even if the
	     * error itself isn't handled.
	     */
	} catch (Exception ioe) {
	    System.err.println(ioe.toString());
	}
    }

    /**
     * Create a graph according to the command line arguments.
     *
     * @param args command line arguments
     * @param jkstat a JKstat object to supply the data
     * @param f the output file
     */
    public static void makeGraph(String[] args, JKstat jkstat, File f) {
	makeGraph(args, jkstat, f, true);
    }

    /**
     * Create a graph according to the command line arguments.
     *
     * @param args command line arguments
     * @param jkstat a JKstat object to supply the data
     * @param f the output file
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
		new KstatAreaPngImage(jkstat, ksa, ss[3], f, rates);
	    } else {
		Kstat ks = KstatUtil.makeKstat(ss[0], ss[1], ss[2]);
		if (ks == null) {
		    usage("Invalid instance - must be numeric.");
		} else {
		    new KstatAreaPngImage(jkstat, ks, ss[3], f, rates);
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
		new KstatAreaPngImage(jkstat, ksa, arglist, f, rates);
	    } else {
		Kstat ks = KstatUtil.makeKstat(ss[0], ss[1], ss[2]);
		if (ks == null) {
		    usage("Invalid instance - must be numeric.");
		} else {
		    new KstatAreaPngImage(jkstat, ks, arglist, f, rates);
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
	String[] nargs = new String[args.length-4];
	System.arraycopy(args, 4, nargs, 0, args.length-4);
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
