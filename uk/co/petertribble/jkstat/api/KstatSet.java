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
import java.util.TreeSet;

/**
 * Represents a Set of Kstats, capable of being updated as the kernel's
 * kstat chain changes. Multiple threads may be accessing kstats concurrently
 * and this class provides a mechanism whereby each thread can keep its
 * kstat chain synchronized.
 *
 * @author Peter Tribble
 */
public class KstatSet {

    private Set<Kstat> addedKstats;
    private Set<Kstat> deletedKstats;
    private Set<Kstat> currentKstats;

    private JKstat jkstat;
    private KstatFilter ksf;
    private String title;

    private int chainid;

    /**
     * Allocates a KstatSet to manage the kstats in the kstat chain.
     *
     * @param njkstat a {@code JKstat}
     */
    public KstatSet(final JKstat njkstat) {
	this(njkstat, "all kstats");
    }

    /**
     * Allocates a KstatSet to manage the kstats in the kstat chain.
     *
     * @param njkstat a {@code JKstat}
     * @param ntitle a String that can be used for presentation
     */
    public KstatSet(final JKstat njkstat, final String ntitle) {
	jkstat = njkstat;
	title = ntitle;
	chainid = jkstat.getKCID();
	ksf = null;
	currentKstats = jkstat.getKstats();
	addedKstats = new HashSet<>();
	deletedKstats = new HashSet<>();
    }

    /**
     * Allocates a KstatSet to manage the kstats in the kstat chain that
     * match the given filter.
     *
     * @param njkstat a {@code JKstat}
     * @param nksf a {@code KstatFilter}
     */
    public KstatSet(final JKstat njkstat, final KstatFilter nksf) {
	// if KstatFilter had a toString method we could use that
	this(njkstat, nksf, (String) null);
    }

    /**
     * Allocates a KstatSet to manage the kstats in the kstat chain that
     * match the given filter.
     *
     * @param njkstat a {@code JKstat}
     * @param nksf a {@code KstatFilter}
     * @param ntitle a String that can be used for presentation
     */
    public KstatSet(final JKstat njkstat, final KstatFilter nksf,
		    final String ntitle) {
	jkstat = njkstat;
	chainid = jkstat.getKCID();
	ksf = nksf;
	title = ntitle;
	currentKstats = ksf.getKstats();
	addedKstats = new HashSet<>();
	deletedKstats = new HashSet<>();
    }

    /**
     * Checks for updates to the kstat chain.
     *
     * @return the new chainid if the chain has been updated, zero if no
     * changes were made
     */
    public int chainupdate() {
	/*
	 * We cannot simply trust chainupdate, as it may have been called by
	 * other callers. So we just compare the current chain ID to our own.
	 */
	int newchainid = jkstat.getKCID();
	/*
	 * If the id hasn't changed then clear the added and deleted sets
	 * and return zero to the caller.
	 */
	if (newchainid == chainid) {
	    addedKstats.clear();
	    deletedKstats.clear();
	    return 0;
	}
	/*
	 * Something has changed, so we must now work through the kstat chain
	 * to get ourselves a new Set of kstats and then use that to find out
	 * what kstats have been added and removed.
	 */
	Set<Kstat> oldKstats = currentKstats;
	Set<Kstat> newKstats =
	    (ksf == null) ? jkstat.getKstats() : ksf.getKstats();
	addedKstats = new HashSet<>(newKstats);
	addedKstats.removeAll(oldKstats);
	deletedKstats = new HashSet<>(oldKstats);
	deletedKstats.removeAll(newKstats);
	/*
	 * Save current state.
	 */
	chainid = newchainid;
	currentKstats = newKstats;
	/*
	 * If the added and deleted lists are both empty, then the current
	 * list is valid. Presumably, the kstat chain has been updated multiple
	 * times so that all the changes cancel out. Or, if we are filtering
	 * the kstats, the kstats that have changed are filtered out. So if
	 * there are no changes, we lie to the caller and say that the chain
	 * hasn't changed. In reality this isn't a lie so much as the contract
	 * of this method.
	 */
	if (addedKstats.isEmpty() && deletedKstats.isEmpty()) {
	    return 0;
	}
	return newchainid;
    }

    /**
     * Connect to a different {@code JKstat} object. This is probably a bug,
     * but the charts create a new instance of a {@code SequencedJKstat}. This
     * allows them to resync jkstat with the one in the chart.
     *
     * @param njkstat a {@code JKstat}
     */
    public void setJKstat(final JKstat njkstat) {
	jkstat = njkstat;
    }

    /**
     * Returns the kstats added in the last update.
     *
     * @return the {@code Set} of {@code Kstat}s added in the last update
     */
    public Set<Kstat> getAddedKstats() {
	return addedKstats;
    }

    /**
     * Returns the kstats deleted in the last update.
     *
     * @return the {@code Set} of {@code Kstat}s deleted in the last update
     */
    public Set<Kstat> getDeletedKstats() {
	return deletedKstats;
    }

    /**
     * Returns the current kstats held in this {@code KstatSet}.
     * The returned {@code Kstat}s will not be sorted.
     *
     * @return the {@code Set} of {@code Kstat}s managed by this KstatSet
     */
    public Set<Kstat> getKstats() {
	return getKstats(false);
    }

    /**
     * Returns the current kstats. If requested, the {@code Kstat}s will
     * be sorted, backed by a {@code TreeSet}.
     *
     * @param sorted whether the returned {@code Kstat}s should be sorted
     *
     * @return the {@code Set} of {@code Kstat}s managed by this KstatSet
     */
    public Set<Kstat> getKstats(final boolean sorted) {
	return sorted ? new TreeSet<>(currentKstats) : currentKstats;
    }

    /**
     * Returns all valid modules in this KstatSet.
     *
     * @return a Set (of Strings) containing all the modules in the Kstats
     * in this KstatSet
     */
    public Set<String> getModuleSet() {
	Set<String> ss = new HashSet<>();
	for (Kstat ks : currentKstats) {
	    ss.add(ks.getModule());
	}
	return ss;
    }

    /**
     * Returns all valid instances in this KstatSet.
     *
     * @return a Set (of Strings) containing all the instances in the Kstats
     * in this KstatSet
     */
    public Set<String> getInstanceSet() {
	Set<String> ss = new HashSet<>();
	for (Kstat ks : currentKstats) {
	    ss.add(ks.getInstance());
	}
	return ss;
    }

    /**
     * Returns all valid names in this KstatSet.
     *
     * @return a Set (of Strings) containing all the names in the Kstats
     * in this KstatSet
     */
    public Set<String> getNameSet() {
	Set<String> ss = new HashSet<>();
	for (Kstat ks : currentKstats) {
	    ss.add(ks.getName());
	}
	return ss;
    }

    /**
     * Returns all valid classes in this KstatSet.
     *
     * @return a Set (of Strings) containing all the classes in the Kstats
     * in this KstatSet
     */
    public Set<String> getClassSet() {
	Set<String> ss = new HashSet<>();
	for (Kstat ks : currentKstats) {
	    ss.add(ks.getKstatClass());
	}
	return ss;
    }

    /**
     * Returns a String representation of this KstatSet. If a title has been
     * supplied in the constructor, then use that, else a standard String is
     * used.
     *
     * @return a String representation of this KstatSet
     */
    @Override
    public String toString() {
	return title;
    }

    /**
     * Returns a JSON representation of the Kstats in this KstatSet. Contains
     * only the metadata (class, type, module, name, instance) and no data.
     *
     * @return A String containing a JSON representation of this
     * {@code KstatSet}
     */
    public String toJSON() {
	/*
	 * This is constructed by hand. This minimizes dependencies and
	 * guarantees the representation stays fixed. Besides, generating
	 * JSON isn't hard.
	 */
	boolean firstdata = true;
	StringBuilder sb = new StringBuilder(120);
	// start the array
	sb.append("[\n");
	// loop over all the kstats
	for (Kstat ks : currentKstats) {
	    if (firstdata) {
		firstdata = false;
	    } else {
		sb.append(",\n");
	    }
	    sb.append("{\"class\":\"").append(ks.getKstatClass())
		.append("\",\"type\":").append(ks.getType())
		.append(",\"module\":\"").append(ks.getModule())
		.append("\",\"name\":\"").append(ks.getName())
		.append("\",\"instance\":").append(ks.getInst())
		.append("}\n");
	}
	// end the array
	sb.append("]\n");
	return sb.toString();
    }
}
