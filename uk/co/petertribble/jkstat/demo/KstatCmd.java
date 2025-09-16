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

package uk.co.petertribble.jkstat.demo;

import uk.co.petertribble.jkstat.api.*;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TimeZone;

/**
 * An emulation of the kstat cli.
 *
 * @author Peter Tribble
 */
public final class KstatCmd {

    private JKstat jkstat;
    private KstatFilter ksf;

    // argument handling
    private boolean pflag;
    private boolean lflag;
    private String showclass;
    private boolean tflag;
    private String ttype;

    // usage 1 is [-m module] [-i instance] [-n name] [-s statistic]
    private String showmodule;
    private String showinstance;
    private String showname;
    private boolean sflag;
    private String showstatistic;

    // optional count
    int count;

    private static final DateTimeFormatter DTF =
	DateTimeFormatter.ofPattern("EEE MMM d H:mm:ss yyyy");
    private static final ZoneId ZID = TimeZone.getDefault().toZoneId();

    /**
     * Create a new KstatCmd application, and produce output according to the
     * supplied arguments.
     *
     * @param args Command line arguments
     */
    public KstatCmd(final String[] args) {
	jkstat = new NativeJKstat();
	Set<String> statspecs = new HashSet<>();

	boolean cflag = false;
	boolean iflag = false;
	boolean mflag = false;
	boolean nflag = false;
	int interval = 0;
	/*
	 * A subset of the kstat(1) command line arguments are supported.
	 */
	int i = 0;
	while (i < args.length) {
	    if ("-p".equals(args[i])) {
		pflag = true;
	    } else if ("-l".equals(args[i])) {
		lflag = true;
	    } else if ("-c".equals(args[i])) {
		if (i + 1 < args.length) {
		    cflag = true;
		    i++;
		    showclass = args[i];
		} else {
		    usage("missing argument to -c flag");
		}
	    } else if ("-m".equals(args[i])) {
		if (i + 1 < args.length) {
		    mflag = true;
		    i++;
		    showmodule = args[i];
		} else {
		    usage("missing argument to -m flag");
		}
	    } else if ("-i".equals(args[i])) {
		if (i + 1 < args.length) {
		    iflag = true;
		    i++;
		    showinstance = args[i];
		} else {
		    usage("missing argument to -i flag");
		}
	    } else if ("-n".equals(args[i])) {
		if (i + 1 < args.length) {
		    nflag = true;
		    i++;
		    showname = args[i];
		} else {
		    usage("missing argument to -n flag");
		}
	    } else if ("-s".equals(args[i])) {
		if (i + 1 < args.length) {
		    sflag = true;
		    i++;
		    showstatistic = args[i];
		} else {
		    usage("missing argument to -s flag");
		}
	    } else if ("-T".equals(args[i])) {
		if (i + 1 < args.length) {
		    tflag = true;
		    i++;
		    ttype = args[i];
		    if (!"u".equals(ttype) && !"d".equals(ttype)) {
			usage("invalid argument to -T flag");
		    }
		} else {
		    usage("missing argument to -T flag");
		}
	    } else {
		if (args[i].indexOf(':') > -1) {
		    // module:instance:name:statistic
		    statspecs.add(args[i]);
		} else {
		    // interval or count
		    try {
			int ii = Integer.parseInt(args[i]);
			if (ii < 1) {
			    usage();
			}
			if (count > 0) {
			    // both already specified
			    usage();
			}
			if (interval > 0) {
			    count = ii;
			} else {
			    interval = ii;
			}
		    } catch (NumberFormatException e) {
			usage();
		    }
		}
	    }
	    i++;
	}

	ksf = new KstatFilter(jkstat);
	// filter by class if requested
	if (cflag) {
	    ksf.setFilterClass(showclass);
	}
	// if any of module, name, or instance are requested, add a filter
	if (mflag || iflag || nflag || sflag) {
	    StringBuilder sb = new StringBuilder();
	    if (mflag) {
		sb.append(showmodule);
	    }
	    sb.append(':');
	    if (iflag) {
		sb.append(showinstance);
	    }
	    sb.append(':');
	    if (nflag) {
		sb.append(showname);
	    }
	    sb.append(':');
	    if (sflag) {
		sb.append(showstatistic);
	    }
	    ksf.addFilter(sb.toString());
	} else if (!statspecs.isEmpty()) {
	    for (String s : statspecs) {
		ksf.addFilter(s);
	    }
	}
	doDisplay();

	if (interval > 0) {
	    // already displayed once
	    count--;
	    Timer timer = new Timer();
	    timer.schedule(new DisplayTask(), interval * 1000, interval * 1000);
	}
    }

    void doDisplay() {
	displayTimeHeader();

	for (Kstat ks : ksf.getKstats(true)) {
	    Kstat ks2 = jkstat.getKstat(ks);
	    if (ks2 != null) {
		if (lflag) {
		    printlist(ks2);
		} else {
		    printkstat(ks2);
		}
	    }
	}
    }

    /*
     * Time header, from -T flag
     */
    private void displayTimeHeader() {
	if (tflag) {
	    Instant now = Instant.now();
	    if ("u".equals(ttype)) {
		System.out.println(now.toEpochMilli() / 1000);
	    } else if ("d".equals(ttype)) {
		System.out.println(
		    DTF.format(LocalDateTime.ofInstant(now, ZID)));
	    }
	}
    }

    /*
     * Inner class to implement the Task in the Timer loop
     */
    class DisplayTask extends TimerTask {
	@Override
	public void run() {
	    doDisplay();
	    if (count > 0) {
		count--;
	    }
	    if (count == 0) {
		System.exit(0);
	    }
	}
    }

    /*
     * Print a list of module:instance:name:statistic for a Kstat.
     * Differs from kstat(1) output in that class, crtime, and snaptime
     * are not listed.
     */
    private void printlist(final Kstat ks) {
	String triplet = ks.getTriplet();
	if (showstatistic == null) {
	    for (String s : ksf.filteredStatistics(ks)) {
		System.out.println(triplet + ":" + s);
	    }
	} else {
	    System.out.println(triplet + ":" + showstatistic);
	}
    }

    /*
     * Print out a Kstat and its values
     * Differs from kstat(1) output in that crtime, snaptime and
     * class are missing from the parseable output.
     */
    private void printkstat(final Kstat ks) {
	if (pflag) {
	    String triplet = ks.getTriplet();
	    if (showstatistic == null) {
		for (String s : ksf.filteredStatistics(ks)) {
		    System.out.println(triplet + ":" + s + "\t"
				+ ks.getData(s));
		}
	    } else {
		System.out.println(triplet + ":" + showstatistic + "\t"
		    + ks.getData(showstatistic));
	    }
	} else {
	    printinfo(ks);
	    if (showstatistic == null) {
		for (String s : ksf.filteredStatistics(ks)) {
		    System.out.println("\t" + s + "\t" + ks.getData(s));
		}
	    } else {
		System.out.println("\t" + showstatistic + "\t"
				+ ks.getData(showstatistic));
	    }
	    System.out.println();
	}
    }

    /*
     * Print out a header module, instance, name, class
     */
    private void printinfo(final Kstat ks) {
	System.out.println("module:\t" + ks.getModule() + "\tinstance:\t"
		+ ks.getInstance());
	System.out.println("name:\t" + ks.getName() + "\tclass:\t"
		+ ks.getKstatClass());
	if (!sflag) {
	    System.out.println("\tcrtime\t"
			+ (ks.getCrtime() / 1000000000.0));
	    System.out.println("\tsnaptime\t"
			+ (ks.getSnaptime() / 1000000000.0));
	}
    }

    private void usage(final String s) {
	System.err.println("Error: " + s);
	usage();
    }

    private void usage() {
	System.err.println("Usage:");
	System.err.println("kstat [-l] [-p] [ -T d|u ] [-c class]");
	System.err.println(
	    "      [-m module] [-i instance] [-n name] [-s statistic]");
	System.err.println("kstat [-l] [-p] [ -T d|u ] [-c class]");
	System.err.println("      [ interval [ count ] ]");
	System.err.println(
	    "      [ module:instance:name:statistic ... ]");
	System.err.println("      [ interval [ count ] ]");
	System.exit(1);
    }

    /**
     * Run the application from the command line.
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
	new KstatCmd(args);
    }
}
