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
 * An access class for Solaris kstats. Allows the available kstats to be
 * enumerated, and kstats and individual statistics to be retrieved. Extends
 * the base JKstat by allowing the consumer to step backwards and forwards
 * in time.
 *
 * @author Peter Tribble
 */
public abstract class SequencedJKstat extends JKstat {

    /**
     * The timestamp of the current JKstat.
     */
    protected long timestamp;

    /*
     * newInstance(), next(), previous(), and size() are the core
     * distinguishing features of a SequencedJKstat
     */

    /**
     * Creates a new copy of this SequencedJKstat.
     *
     * @return a new copy of this SequencedJKstat
     */
    public abstract SequencedJKstat newInstance();

    /**
     * Rewind to the beginning.
     */
    public abstract void begin();

    /**
     * Step forward to the next point in time. When we run out of data points,
     * returns false, but leaves the kstats as they are.
     *
     * @return false if there is no more data
     */
    public abstract boolean next();

    /**
     * Step back to the previous point in time. When we run out of data points,
     * returns false, but leaves the kstats as they are.
     *
     * @return false if there is no more data
     */
    public abstract boolean previous();

    /**
     * Return the number of data points available. If unknown, return zero.
     *
     * @return the number of data points available if known, else zero
     */
    public abstract int size();

    /*
     * The following are concrete implementations that are shared between all
     * (or most) SequencedJKstat implementations. Normally, these are
     * retrieved from a fixed store, we just increment the chainid each time,
     * enumeration isn't relevant as we do that anyway, and getting a kstat
     * just involves searching the list.
     */

    @Override
    public int getKCID() {
	return chainid;
    }

    @Override
    public int enumerate() {
	return chainid;
    }

    @Override
    public Kstat getKstatObject(String module, int inst, String name) {
	for (Kstat ks : kstats) {
	    if (ks.getModule().equals(module) && (inst == ks.getInst())
			&& ks.getName().equals(name)) {
		return ks;
	    }
	}
	return null;
    }

    @Override
    public long getTime() {
	return timestamp;
    }
}
