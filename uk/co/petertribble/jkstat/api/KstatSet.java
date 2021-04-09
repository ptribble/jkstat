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
 * Represents a Set of Kstats, capable of being updated as the kernel's
 * kstat chain changes. Multiple threads may be accessing kstats concurrently
 * and this class provides a mechanism whereby each thread can keep its
 * kstat chain synchronized.
 *
 * @author Peter Tribble
 */
public class KstatSet {

    private Set <Kstat> addedKstats;
    private Set <Kstat> deletedKstats;
    private Set <Kstat> newKstats;
    private Set <Kstat> currentKstats;

    private JKstat jkstat;
    private KstatFilter ksf;
    private String title;

    private int chainid;

    /**
     * Allocates a KstatSet to manage the kstats in the kstat chain.
     *
     * @param jkstat a {@code JKstat}
     */
    public KstatSet(JKstat jkstat) {
	this(jkstat, "all kstats");
    }

    /**
     * Allocates a KstatSet to manage the kstats in the kstat chain.
     *
     * @param jkstat a {@code JKstat}
     * @param title a String that can be used for presentation
     */
    public KstatSet(JKstat jkstat, String title) {
	this.jkstat = jkstat;
	this.title = title;
	chainid = jkstat.getKCID();
	ksf = null;
	currentKstats = jkstat.getKstats();
	addedKstats = new HashSet <Kstat> ();
	deletedKstats = new HashSet <Kstat> ();
    }

    /**
     * Allocates a KstatSet to manage the kstats in the kstat chain that
     * match the given filter.
     *
     * @param jkstat a {@code JKstat}
     * @param ksf a {@code KstatFilter}
     */
    public KstatSet(JKstat jkstat, KstatFilter ksf) {
	// if KstatFilter had a toString method we could use that
	this(jkstat, ksf, (String) null);
    }

    /**
     * Allocates a KstatSet to manage the kstats in the kstat chain that
     * match the given filter.
     *
     * @param jkstat a {@code JKstat}
     * @param ksf a {@code KstatFilter}
     * @param title a String that can be used for presentation
     */
    public KstatSet(JKstat jkstat, KstatFilter ksf, String title) {
	this.jkstat = jkstat;
	chainid = jkstat.getKCID();
	this.ksf = ksf;
	this.title = title;
	currentKstats = ksf.getKstats();
	addedKstats = new HashSet <Kstat> ();
	deletedKstats = new HashSet <Kstat> ();
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
	Set <Kstat> oldKstats = currentKstats;
	if (ksf == null) {
	    newKstats = jkstat.getKstats();
	} else {
	    newKstats = ksf.getKstats();
	}
	addedKstats = new HashSet <Kstat> (newKstats);
	addedKstats.removeAll(oldKstats);
	deletedKstats = new HashSet <Kstat> (oldKstats);
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
     * @param jkstat a {@code JKstat}
     */
    public void setJKstat(JKstat jkstat) {
	this.jkstat = jkstat;
    }

    /**
     * Returns the kstats added in the last update.
     *
     * @return the {@code Set} of {@code Kstat}s added in the last update
     */
    public Set <Kstat> getAddedKstats() {
	return addedKstats;
    }

    /**
     * Returns the kstats deleted in the last update.
     *
     * @return the {@code Set} of {@code Kstat}s deleted in the last update
     */
    public Set <Kstat> getDeletedKstats() {
	return deletedKstats;
    }

    /**
     * Returns the current kstats.
     *
     * @return the {@code Set} of {@code Kstat}s managed by this KstatSet
     */
    public Set <Kstat> getKstats() {
	return currentKstats;
    }

    /**
     * Returns all valid modules in this KstatSet.
     *
     * @return a Set (of Strings) containing all the modules in the Kstats
     * in this KstatSet
     */
    public Set <String> getModuleSet() {
	Set <String> ss = new HashSet <String> ();
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
    public Set <String> getInstanceSet() {
	Set <String> ss = new HashSet <String> ();
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
    public Set <String> getNameSet() {
	Set <String> ss = new HashSet <String> ();
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
    public Set <String> getClassSet() {
	Set <String> ss = new HashSet <String> ();
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
	    sb.append("{\"class\":\"").append(ks.getKstatClass());
	    sb.append("\",\"type\":").append(ks.getType());
	    sb.append(",\"module\":\"").append(ks.getModule());
	    sb.append("\",\"name\":\"").append(ks.getName());
	    sb.append("\",\"instance\":").append(ks.getInst());
	    sb.append("}\n");
	}
	// end the array
	sb.append("]\n");
	return sb.toString();
    }
}
