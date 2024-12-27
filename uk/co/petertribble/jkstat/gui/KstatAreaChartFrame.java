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
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.parse.ParseableJSONZipJKstat;

/**
 * A graphical chart of a kstat, showing one or multiple statistics, in
 * its own window. The statistic is charted by area, and multiple statistics
 * are stacked.
 *
 * @author Peter Tribble
 */
public class KstatAreaChartFrame extends KstatBaseChartFrame {

    private static final long serialVersionUID = 1L;

    /**
     * Create a JFrame containing an area Chart of the rate of change of the
     * given statistic.
     *
     * @param jkstat a JKstat
     * @param kss the KstatSet supplying the data
     * @param statistic the statistic to be charted
     */
    public KstatAreaChartFrame(JKstat jkstat, KstatSet kss,
				String statistic) {
	this(jkstat, kss, statistic, true);
    }

    /**
     * Create a JFrame containing an area Chart of the given aggregated
     * statistic.
     *
     * @param jkstat a JKstat
     * @param kss the KstatSet supplying the data
     * @param statistic the statistic to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatAreaChartFrame(JKstat jkstat, KstatSet kss,
				String statistic, boolean showdelta) {
	super();
	this.jkstat = jkstat;
	this.showdelta = showdelta;
	kbc = new KstatSetAreaChart(jkstat, kss, statistic, showdelta);
	init(kss.toString());
    }

    /**
     * Create a JFrame containing an area Chart of the rate of change of the
     * given aggregated statistic.
     *
     * @param jkstat a JKstat
     * @param ksa the KstatAggregate supplying the data
     * @param statistic the statistic to be charted
     */
    public KstatAreaChartFrame(JKstat jkstat, KstatAggregate ksa,
				String statistic) {
	this(jkstat, ksa, statistic, true);
    }

    /**
     * Create a JFrame containing an area Chart of the given aggregated
     * statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ksa the KstatAggregate supplying the data
     * @param statistic the statistic to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatAreaChartFrame(JKstat jkstat, KstatAggregate ksa,
				String statistic, boolean showdelta) {
	super();
	this.jkstat = jkstat;
	this.showdelta = showdelta;
	kbc = new KstatAggregateAreaChart(jkstat, ksa, statistic, showdelta);
	init(ksa.toString());
    }

    /**
     * Create a JFrame containing an area Chart of the rate of change of the
     * given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ks the Kstat supplying the data
     * @param statistic the statistic to be charted
     */
    public KstatAreaChartFrame(JKstat jkstat, Kstat ks, String statistic) {
	this(jkstat, ks, statistic, true);
    }

    /**
     * Create a JFrame containing an area Chart of the rate of change
     * of the given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ks a Kstat to be charted
     * @param cks the {@code ChartableKstat} generating rates from the data
     * @param statistic the statistic to be charted
     */
    public KstatAreaChartFrame(JKstat jkstat, Kstat ks, ChartableKstat cks,
		String statistic) {
	this(jkstat, ks, cks, statistic, true);
    }

    /**
     * Create a JFrame containing an area Chart of the given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ks a Kstat to be charted
     * @param statistic the statistic to be charted
     * @param showdelta if true, show rates rather than absolute values
     */
    public KstatAreaChartFrame(JKstat jkstat, Kstat ks, String statistic,
		boolean showdelta) {
	this(jkstat, ks, new ChartableKstat(jkstat, ks), statistic, showdelta);
    }

    /**
     * Create a JFrame containing an area Chart of the given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ks the Kstat supplying the data
     * @param cks the {@code ChartableKstat} generating rates from the data
     * @param statistic the statistic to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatAreaChartFrame(JKstat jkstat, Kstat ks, ChartableKstat cks,
		String statistic, boolean showdelta) {
	super();
	this.jkstat = jkstat;
	this.showdelta = showdelta;
	kbc = new KstatAreaChart(jkstat, ks, cks, statistic, showdelta);
	init(ks.getTriplet(), statisticsMenu(cks, statistic));
    }

    /**
     * Create a JFrame containing an area Chart of the rate of change of the
     * given aggregated statistics.
     *
     * @param jkstat a {@code JKstat}
     * @param ksa the KstatAggregate supplying the data
     * @param statistics the statistics to be charted
     */
    public KstatAreaChartFrame(JKstat jkstat, KstatAggregate ksa,
		List<String> statistics) {
	this(jkstat, ksa, statistics, true);
    }

    /**
     * Create a JFrame containing an area Chart of the given aggregated
     * statistics.
     *
     * @param jkstat a {@code JKstat}
     * @param ksa the KstatAggregate supplying the data
     * @param statistics the statistics to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatAreaChartFrame(JKstat jkstat, KstatAggregate ksa,
		List<String> statistics, boolean showdelta) {
	super();
	this.jkstat = jkstat;
	this.showdelta = showdelta;
	kbc = new KstatAggregateAreaChart(jkstat, ksa, statistics, showdelta);
	init(ksa.toString());
    }

    /**
     * Create a JFrame containing an area Chart of the rate of change of the
     * given statistics.
     *
     * @param jkstat a JKstat
     * @param ks the Kstat supplying the data
     * @param statistics the statistics to be charted
     */
    public KstatAreaChartFrame(JKstat jkstat, Kstat ks,
		List<String> statistics) {
	this(jkstat, ks, statistics, true);
    }

    /**
     * Create a JFrame containing an area Chart of the given statistics.
     *
     * @param jkstat a JKstat
     * @param ks the Kstat supplying the data
     * @param statistics the statistics to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatAreaChartFrame(JKstat jkstat, Kstat ks,
		List<String> statistics, boolean showdelta) {
	super();
	this.jkstat = jkstat;
	this.showdelta = showdelta;
	kbc = new KstatAreaChart(jkstat, ks, statistics, showdelta);
	init(ks.getTriplet(), statisticsMenu(new ChartableKstat(jkstat, ks),
					statistics));
    }

    private static void usage(String message) {
	System.err.println("ERROR: " + message);
	System.err.println("Usage: areachart module:instance:name:statistic");
	System.err.println("       areachart module:instance:name statistic "
			+ "[statistic...]");
	System.exit(1);
    }

    private static void doMain(String[] args, JKstat jkstat) {
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
		new KstatAreaChartFrame(jkstat, ksa, ss[3]);
	    } else {
		Kstat ks = KstatUtil.makeKstat(ss[0], ss[1], ss[2]);
		if (ks == null) {
		    usage("Invalid instance - must be numeric.");
		} else {
		    new KstatAreaChartFrame(jkstat, ks, ss[3]);
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
		new KstatAreaChartFrame(jkstat, ksa, arglist);
	    } else {
		Kstat ks = KstatUtil.makeKstat(ss[0], ss[1], ss[2]);
		if (ks == null) {
		    usage("Invalid instance - must be numeric.");
		} else {
		    new KstatAreaChartFrame(jkstat, ks, arglist);
		}
	    }
	} else {
	    usage("Invalid kstat.");
	}
    }

    /**
     * Show a chart according to the command line arguments.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
	if (args.length == 0) {
	    usage("Missing arguments.");
	}
	if ("-z".equals(args[0])) {
	    String[] nargs = new String[args.length - 2];
	    System.arraycopy(args, 2, nargs, 0, args.length - 2);
	    try {
		doMain(nargs, new ParseableJSONZipJKstat(args[1]));
	    } catch (IOException ioe) {
		usage("Invalid zip file");
	    }
	} else {
	    doMain(args, new NativeJKstat());
	}
    }
}
