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
import java.util.Date;
import java.util.Set;
import java.text.DateFormat;

/**
 * Emulates psrinfo(8).
 */
public class PsrInfo {

    private JKstat jkstat;

    // for arguments
    private static boolean flagC;
    private static boolean flagP;
    private static boolean flagT;
    private static boolean flagV;

    private static final DateFormat DF = DateFormat.getInstance();

    /**
     * Emulate psrinfo(8) output.
     */
    public PsrInfo() {
	jkstat = new NativeJKstat();
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.addFilter("cpu_info:::");

	if (flagT) {
	    displayT(ksf.getKstats(true));
	} else if (flagP && flagV) {
	    displayVP();
	} else if (flagV) {
	    displayV(ksf.getKstats(true));
	} else if (flagP) {
	    displayP();
	} else {
	    displayPlain(ksf.getKstats(true));
	}
    }

    private void displayPlain(Set<Kstat> kstats) {
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
					    DateFormat.SHORT);
	for (Kstat ks : kstats) {
	    Kstat nks = jkstat.getKstat(ks);
	    System.out.println(nks.getInstance() + "\t" + nks.getData("state")
		+ "   since "
		+ df.format(new Date(1000 * nks.longData("state_begin"))));
	}
    }

    private void displayP() {
	System.out.println(new ProcessorTree(jkstat).numChips());
    }

    private void displayT(Set<Kstat> kstats) {
	if (flagP) {
	    System.out.println(new ProcessorTree(jkstat).numChips());
	} else if (flagC) {
	    System.out.println(new ProcessorTree(jkstat).numCores());
	} else {
	    System.out.println(kstats.size());
	}
    }

    private void displayV(Set<Kstat> kstats) {
	for (Kstat ks : kstats) {
	    System.out.println(details(jkstat.getKstat(ks)));
	}
    }

    /*
     * Print the details of a given processor. This is psrinfo -v
     *
     * @param ks a cpu_info Kstat
     *
     * @return a String similar to psrinfo -v output for the given Kstat
     */
    private static String details(Kstat ks) {
	StringBuilder sb = new StringBuilder(160);
	if (ks != null) {
	    sb.append("Status of virtual processor ").append(ks.getInstance())
		.append(" as of: ").append(DF.format(new Date()))
		.append("\n  ").append(ks.getData("state")).append(" since ")
		.append(DF.format(new Date(1000 * ks.longData("state_begin"))))
		.append("\n  The ").append(ks.getData("cpu_type"))
		.append(" processor operates at ")
		.append(ks.getData("clock_MHz"))
		.append(" MHz,\n        and has an ")
		.append(ks.getData("fpu_type"))
		.append(" floating point processor.");
	}
	return sb.toString();
    }

    /*
     * A slightly different version of psrinfo -vp
     */
    private void displayVP() {
	ProcessorTree proctree = new ProcessorTree(jkstat);

	/*
	 * Now walk the tree printing out the data. The chipDetails method
	 * already includes the trailing newline.
	 */
	for (ProcessorChip chip : proctree.getProcessorChips()) {
	    System.out.print(proctree.chipDetails(chip));
	}
    }

    /**
     * Run the command line PsrInfo application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
	for (String arg : args) {
	    if ("-t".equals(arg)) {
		flagT = true;
	    }
	    if ("-c".equals(arg)) {
		flagC = true;
	    }
	    if ("-p".equals(arg)) {
		flagP = true;
	    }
	    if ("-v".equals(arg)) {
		flagV = true;
	    }
	    if ("-vp".equals(arg) || "-pv".equals(arg)) {
		flagP = true;
		flagV = true;
	    }
	}
	new PsrInfo();
    }
}
