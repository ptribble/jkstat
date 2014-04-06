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

package uk.co.petertribble.jkstat.api;

/**
 * A class to get IO rates from a Kstat (designed to be used in charts).
 *
 * @author Peter Tribble
 */
public class ChartableIOKstat extends ChartableKstat {

    /**
     * Create a ChartableIOKstat using the given Kstat as the data source.
     *
     * @param jkstat a {@code JKstat}
     * @param ks the {@code Kstat} supplying the data
     */
    public ChartableIOKstat(JKstat jkstat, Kstat ks) {
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

	// get the new values
	long nr = ks.longData("reads");
	long nw = ks.longData("writes");
	long nkr = ks.longData("nread");
	long nkw = ks.longData("nwritten");
	long nrtime = ks.longData("rtime");
	long nwtime = ks.longData("wtime");
	long nrlentime = ks.longData("rlentime");
	long nwlentime = ks.longData("wlentime");

	// get the old values
	long r = getValue("reads");
	long w = getValue("writes");
	long kr = getValue("nread");
	long kw = getValue("nwritten");

	// operations per second
	rateMap.put("r/s", (double) (nr-r)*1000000000.0/dt);
	rateMap.put("w/s", (double) (nw-w)*1000000000.0/dt);

	// data transferred per second
	rateMap.put("kr/s", (double) (nkr-kr)*1000000000.0/(dt*1024.0));
	rateMap.put("kw/s", (double) (nkw-kw)*1000000000.0/(dt*1024.0));

	// wait (wait queue length)
	double di = (nwlentime-getValue("wlentime"))/dt;
	rateMap.put("wait", di);

	// actv (run queue length)
	double dj = (nrlentime-getValue("rlentime"))/dt;
	rateMap.put("actv", dj);

	// service time
	double ds = (nr-r)+(nw-w);
	if (((int) ds) == 0) {
	    rateMap.put("svc_t", 0.0);
	} else {
	    rateMap.put("svc_t", (di+dj)*1000.0/ds);
	}
	ds = nw-w;
	if (((int) ds) == 0) {
	    rateMap.put("wsvc_t", 0.0);
	} else {
	    rateMap.put("wsvc_t", di*1000.0/ds);
	}
	ds = nr-r;
	if (((int) ds) == 0) {
	    rateMap.put("asvc_t", 0.0);
	} else {
	    rateMap.put("asvc_t", dj*1000.0/ds);
	}

	// wait percentage
	rateMap.put("%w", 100.0*(nwtime-getValue("wtime"))/dt);

	// busy percentage
	rateMap.put("%b", 100.0*(nrtime-getValue("rtime"))/dt);

	// save the new values
	valueMap.put("reads", nr);
	valueMap.put("writes", nw);
	valueMap.put("nread", nkr);
	valueMap.put("nwritten", nkw);
	valueMap.put("rtime", nrtime);
	valueMap.put("wtime", nwtime);
	valueMap.put("rlentime", nrlentime);
	valueMap.put("wlentime", nwlentime);
	return true;
    }

    @Override
    public String toString() {
	return ks.getName();
    }
}
