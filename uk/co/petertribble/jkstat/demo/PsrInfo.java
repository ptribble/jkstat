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
 * Emulates psrinfo(1M).
 */
public class PsrInfo {

    private JKstat jkstat;

    // for arguments
    private static boolean flag_v;
    private static boolean flag_p;

    /**
     * Emulate psrinfo(1M) output.
     */
    public PsrInfo() {
	jkstat = new NativeJKstat();
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.addFilter("cpu_info:::");

	if (flag_p && flag_v) {
	    display_vp();
	} else if (flag_v) {
	    display_v(new TreeSet <Kstat> (ksf.getKstats()));
	} else if (flag_p) {
	    display_p();
	} else {
	    display_plain(new TreeSet <Kstat> (ksf.getKstats()));
	}
    }

    private void display_plain(Set <Kstat> kstats) {
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
					    DateFormat.SHORT);
	for (Kstat ks : kstats) {
	    ks = jkstat.getKstat(ks);
	    System.out.println(ks.getInstance() + "\t" + ks.getData("state")
		+ "   since "
		+ df.format(new Date(1000*ks.longData("state_begin"))));
	}
    }

    private void display_p() {
	System.out.println(new ProcessorTree(jkstat).numChips());
    }

    private void display_v(Set <Kstat> kstats) {
	for (Kstat ks : kstats) {
	    System.out.println(ProcessorTree.details(jkstat.getKstat(ks)));
	}
    }

    /*
     * A slightly different version of psrinfo -vp
     */
    private void display_vp() {
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
