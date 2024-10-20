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

package uk.co.petertribble.jkstat.demo;

import uk.co.petertribble.jkstat.api.*;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.text.DateFormat;

/**
 * Emulates psrinfo(8).
 */
public class PsrInfo {

    private JKstat jkstat;

    // for arguments
    private static boolean flag_c;
    private static boolean flag_p;
    private static boolean flag_t;
    private static boolean flag_v;

    private static final DateFormat df = DateFormat.getInstance();

    /**
     * Emulate psrinfo(8) output.
     */
    public PsrInfo() {
	jkstat = new NativeJKstat();
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.addFilter("cpu_info:::");

	if (flag_t) {
	    displayT(new TreeSet<>(ksf.getKstats()));
	} else if (flag_p && flag_v) {
	    displayVP();
	} else if (flag_v) {
	    displayV(new TreeSet<>(ksf.getKstats()));
	} else if (flag_p) {
	    displayP();
	} else {
	    displayPlain(new TreeSet<>(ksf.getKstats()));
	}
    }

    private void displayPlain(Set <Kstat> kstats) {
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
					    DateFormat.SHORT);
	for (Kstat ks : kstats) {
	    Kstat nks = jkstat.getKstat(ks);
	    System.out.println(nks.getInstance() + "\t" + nks.getData("state")
		+ "   since "
		+ df.format(new Date(1000*nks.longData("state_begin"))));
	}
    }

    private void displayP() {
	System.out.println(new ProcessorTree(jkstat).numChips());
    }

    private void displayT(Set <Kstat> kstats) {
	if (flag_p) {
	    System.out.println(new ProcessorTree(jkstat).numChips());
	} else if (flag_c) {
	    System.out.println(new ProcessorTree(jkstat).numCores());
	} else {
	    System.out.println(kstats.size());
	}
    }

    private void displayV(Set <Kstat> kstats) {
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
		.append(" as of: ").append(df.format(new Date()))
		.append("\n  ").append(ks.getData("state")).append(" since ")
		.append(df.format(new Date(1000*ks.longData("state_begin"))))
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
	for (Long l : proctree.getChips()) {
	    System.out.print(proctree.chipDetails(l));
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
		flag_t = true;
	    }
	    if ("-c".equals(arg)) {
		flag_c = true;
	    }
	    if ("-p".equals(arg)) {
		flag_p = true;
	    }
	    if ("-v".equals(arg)) {
		flag_v = true;
	    }
	    if ("-vp".equals(arg) || "-pv".equals(arg)) {
		flag_p = true;
		flag_v = true;
	    }
	}
	new PsrInfo();
    }
}
