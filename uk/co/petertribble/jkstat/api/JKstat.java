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
    public Kstat getKstat(final String module, final int inst,
			  final String name) {
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
    public Kstat getKstat(final Kstat ks) {
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
     * @param kstatClass a String specifying the kstat class
     * @param type an int specifying the kstat type
     * @param crtime a long specifying the kstat creation time
     */
    protected void addKstat(final String module, final int inst,
			    final String name, final String kstatClass,
			    final int type, final long crtime) {
	Kstat ks = new Kstat(module, inst, name);
	ks.setStandardInfo(kstatClass, type, crtime, 0L);
	kstats.add(ks);
    }
}
