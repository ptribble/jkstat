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
