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

package uk.co.petertribble.jkstat.api;

/**
 * A class to hold the data fields required to implement mpstat.
 *
 * @author Peter Tribble
 */
public class ChartableMPstat extends ChartableKstat {

    private long flastsnap;

    /**
     * Initialise an mpstat data structure.
     *
     * @param jkstat a {@code JKstat}
     * @param ks the {@code Kstat} holding the data
     */
    public ChartableMPstat(JKstat jkstat, Kstat ks) {
	super(jkstat, ks, false);
    }

    @Override
    public boolean update() {
	ks = jkstat.getKstat(ks);
	if (ks == null) {
	    return false;
	}
        double dt = ks.getSnaptime() - lastsnap;
	lastsnap = ks.getSnaptime();

	Kstat ksf = jkstat.getKstat("cpu", ks.getInst(), "vm");

	long fdt = ksf.getSnaptime() - flastsnap;
	flastsnap = ksf.getSnaptime();

	// get the new values
	long nminf = ksf.longData("hat_fault") + ksf.longData("as_fault");
	long nmjf = ksf.longData("maj_fault");
	long nxcal = ks.longData("xcalls");
	long nintr = ks.longData("intr");
	long nithr = ks.longData("intrthread");
	long ncsw = ks.longData("pswitch");
	long nicsw = ks.longData("inv_swtch");
	long nmigr = ks.longData("cpumigrate");
	long nsmtx = ks.longData("mutex_adenters");
	long nsrw = ks.longData("rw_rdfails") + ks.longData("rw_wrfails");
	long nsyscl = ks.longData("syscall");
	long nusr = ks.longData("cpu_nsec_user");
	long nsys = ks.longData("cpu_nsec_kernel");
	long nidl = ks.longData("cpu_nsec_idle");

	// major and minor faults
	rateMap.put("minf", (nminf - getValue("minf")) * 1000000000.0 / fdt);
	rateMap.put("mjf", (nmjf - getValue("mjf")) * 1000000000.0 / fdt);

	rateMap.put("xcal", (nxcal - getValue("xcal")) * 1000000000.0 / dt);
	rateMap.put("intr", (nintr - getValue("intr")) * 1000000000.0 / dt);
	rateMap.put("ithr", (nithr - getValue("ithr")) * 1000000000.0 / dt);
	rateMap.put("csw", (ncsw - getValue("csw")) * 1000000000.0 / dt);
	rateMap.put("icsw", (nicsw - getValue("icsw")) * 1000000000.0 / dt);
	rateMap.put("migr", (nmigr - getValue("migr")) * 1000000000.0 / dt);
	rateMap.put("smtx", (nsmtx - getValue("smtx")) * 1000000000.0 / dt);
	rateMap.put("srw", (nsrw - getValue("srw")) * 1000000000.0 / dt);
	rateMap.put("syscl", (nsyscl - getValue("syscl")) * 1000000000.0 / dt);

	// cpu percentages
	rateMap.put("usr", (nusr - getValue("usr")) * 100.0 / dt);
	rateMap.put("sys", (nsys - getValue("sys")) * 100.0 / dt);
	rateMap.put("idl", (nidl - getValue("idl")) * 100.0 / dt);

	// save the new values
	valueMap.put("minf", nminf);
	valueMap.put("mjf", nmjf);
	valueMap.put("xcal", nxcal);
	valueMap.put("intr", nintr);
	valueMap.put("ithr", nithr);
	valueMap.put("csw", ncsw);
	valueMap.put("icsw", nicsw);
	valueMap.put("migr", nmigr);
	valueMap.put("smtx", nsmtx);
	valueMap.put("srw", nsrw);
	valueMap.put("syscl", nsyscl);
	valueMap.put("usr", nusr);
	valueMap.put("sys", nsys);
	valueMap.put("idl", nidl);

	return true;
    }

    @Override
    public String toString() {
	return ks.getInstance();
    }
}
