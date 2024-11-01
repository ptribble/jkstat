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

import java.util.Set;
import java.util.HashSet;

/**
 * An access class for Solaris kstats. Allows the available kstats to be
 * enumerated, and kstats and individual statistics to be retrieved.
 *
 * @author Peter Tribble
 */
public abstract class JKstat {

    /**
     * The {@code Set} of all available kstats.
     */
    protected Set<Kstat> kstats;

    /**
     * A flag to mark whether the kstats have been enumerated.
     */
    protected boolean enumerated;

    /**
     * The id of the kstat chain.
     */
    protected int chainid;

    /**
     * Creates a new {@code JKstat} object.
     */
    public JKstat() {
	kstats = new HashSet<>();
    }

    /**
     * Returns all available kstats. If the kstats haven't been enumerated,
     * they will be enumerated first. If the kstat chain has changed, we
     * re-enumerate.
     *
     * @return a {@code Set} of the currently available {@code Kstat}s
     */
    public synchronized Set<Kstat> getKstats() {
	if (enumerated) {
	    if (getKCID() != chainid) {
		kstats.clear();
		chainid = enumerate();
	    }
	} else {
	    chainid = enumerate();
	    enumerated = true;
	}
	return new HashSet<>(kstats);
    }

    /**
     * Retrieves a {@code Kstat} and its statistics.
     *
     * @param module the kstat module
     * @param inst the kstat instance
     * @param name the name of the kstat
     *
     * @return a new {@code Kstat} populated with current data
     */
    public abstract Kstat getKstatObject(String module, int inst, String name);

    /**
     * Retrieves a {@code Kstat} and its statistics. Also updates the internal
     * list of {@code Kstat}s.
     *
     * @param module The kstat module
     * @param inst The kstat instance
     * @param name The name of the kstat
     *
     * @return a new {@code Kstat}, or {@code null} if no matching
     * {@code Kstat} is found
     * @throws NullPointerException if the given module or kstat name is
     * {@code null}
     */
    public Kstat getKstat(String module, int inst, String name) {
	if (module == null) {
	    throw new NullPointerException("module is null");
	}
	if (name == null) {
	    throw new NullPointerException("name is null");
	}

	Kstat ks = getKstatObject(module, inst, name);
	if (ks == null) {
	    // it feels wrong to create a new object to remove the old
	    kstats.remove(new Kstat(module, inst, name));
	} else {
	    kstats.add(ks);
	}
	return ks;
    }

    /**
     * Retrieves a {@code Kstat} and its statistics. Also updates the internal
     * list of {@code Kstat}s.
     *
     * @param ks A {@code Kstat} that defines the module, instance, and name
     *
     * @return A new {@code Kstat}, or {@code null} if no matching
     * {@code Kstat} is found
     */
    public Kstat getKstat(Kstat ks) {
	if (ks == null) {
	    return (Kstat) null;
	}
	return getKstat(ks.getModule(), ks.getInst(), ks.getName());
    }

    /**
     * Gets the current kstat chain ID.
     *
     * @return the id of the current kstat chain
     */
    public abstract int getKCID();

    /**
     * Enumerates the available {@code Kstat}s. None of the kstat data is read
     * by this operation.
     *
     * @return the id of the current kstat chain
     */
    public abstract int enumerate();

    /**
     * Gets the time, as the number of milliseconds since January 1, 1970,
     * 00:00:00 GMT, associated with this JKstat object.
     *
     * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * associated with this JKstat object
     */
    public abstract long getTime();

    /**
     * Adds a {@code Kstat} to the internal list.
     *
     * @param module a String specifying the kstat module
     * @param inst an int specifying the kstat instance
     * @param name a String specifying the kstat name
     * @param kstat_class a String specifying the kstat class
     * @param type an int specifying the kstat type
     * @param crtime a long specifying the kstat creation time
     */
    protected void addKstat(String module, int inst, String name,
			 String kstat_class, int type, long crtime) {
	Kstat ks = new Kstat(module, inst, name);
	ks.setStandardInfo(kstat_class, type, crtime, 0L);
	kstats.add(ks);
    }
}
