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

package uk.co.petertribble.jkstat.browser;

import java.util.SortedMap;
import java.util.TreeMap;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.KstatSet;
import uk.co.petertribble.jkstat.util.NumericStringComparator;

/**
 * A class that holds kstats in a hierarchical set of Maps.
 *
 * @author Peter Tribble
 */
public class KstatTreeMap {

    private SortedMap <String, SortedMap> kstatMap;
    private SortedMap <String, SortedMap> kstatClassMap;
    private SortedMap <String, SortedMap> kstatTypeMap;

    /**
     * Constructs a KstatTreeMap.
     *
     * @param kss a KstatSet object
     */
    public KstatTreeMap(KstatSet kss) {
	/*
	 * Create SortedMaps to store the Kstats in a hierarchical structure
	 * and populate them with the contents of the list.
	 */
	kstatMap = new TreeMap <String, SortedMap> (
		NumericStringComparator.getInstance());
	kstatTypeMap = new TreeMap <String, SortedMap> (
		NumericStringComparator.getInstance());
	kstatClassMap = new TreeMap <String, SortedMap> (
		NumericStringComparator.getInstance());
	for (Kstat ks : kss.getKstats()) {
	    addKstat(ks);
	}
    }

    /**
     * Return the kstat Map.
     *
     * @return The kstat Map
     */
    public SortedMap <String, SortedMap> getKstatMap() {
	return kstatMap;
    }

    /**
     * Return the kstat Map, including the class hierarchy.
     *
     * @return The kstat Map, including the class hierarchy
     */
    public SortedMap <String, SortedMap> getKstatClassMap() {
	return kstatClassMap;
    }

    /**
     * Return the kstat Map, including the type hierarchy.
     *
     * @return The kstat Map, including the type hierarchy
     */
    public SortedMap <String, SortedMap> getKstatTypeMap() {
	return kstatTypeMap;
    }

    /**
     * Add a new Kstat to the Maps.
     *
     * @param ks the Kstat to add
     */
    public void addKstat(Kstat ks) {
	addToModuleMap(kstatMap, ks);
	addToTypeMap(kstatTypeMap, ks);
	addToClassMap(kstatClassMap, ks);
    }

    /**
     * Removes a Kstat from the Maps.
     *
     * @param ks the Kstat to remove
     */
    public void removeKstat(Kstat ks) {
	removeFromModuleMap(kstatMap, ks);
	removeFromTypeMap(kstatTypeMap, ks);
	removeFromClassMap(kstatClassMap, ks);
    }

    /*
     * Add a Kstat to the right place in the Map.
     */
    private void addToTypeMap(SortedMap <String, SortedMap> hc, Kstat ks) {
	String ktype = ks.getTypeAsString();
	if (!hc.containsKey(ktype)) {
	    hc.put(ktype, new TreeMap <String, SortedMap> (
		NumericStringComparator.getInstance()));
	}
	@SuppressWarnings("unchecked")
	SortedMap <String, SortedMap> hm = hc.get(ktype);
	addToModuleMap(hm, ks);
    }

    /*
     * Add a Kstat to the right place in the Map.
     */
    private void addToClassMap(SortedMap <String, SortedMap> hc, Kstat ks) {
	String kc = ks.getKstatClass();
	if (!hc.containsKey(kc)) {
	    hc.put(kc, new TreeMap <String, SortedMap> (
		NumericStringComparator.getInstance()));
	}
	@SuppressWarnings("unchecked")
	SortedMap <String, SortedMap> hm = hc.get(kc);
	addToModuleMap(hm, ks);
    }

    /*
     * Add a Kstat to the right place in the Map.
     */
    private void addToModuleMap(SortedMap <String, SortedMap> hm, Kstat ks) {
	String km = ks.getModule();
	SortedMap <String, SortedMap> hi;
	if (hm.containsKey(km)) {
	    @SuppressWarnings("unchecked")
	    SortedMap <String, SortedMap> tmp = hm.get(km);
	    hi = tmp;
	} else {
	    hi = new TreeMap <String, SortedMap> (
		NumericStringComparator.getInstance());
	    hm.put(km, hi);
	}
	addToInstanceMap(hi, ks);
    }

    /*
     * Add a Kstat to the right place in the Map.
     */
    private void addToInstanceMap(SortedMap <String, SortedMap> hi, Kstat ks) {
	String ki = ks.getInstance();
	SortedMap <String, Kstat> hn;
	if (hi.containsKey(ki)) {
	    @SuppressWarnings("unchecked")
	    SortedMap <String, Kstat> tmp = hi.get(ki);
	    hn = tmp;
	} else {
	    hn = new TreeMap <String, Kstat> (
		NumericStringComparator.getInstance());
	    hi.put(ki, hn);
	}
	hn.put(ks.getName(), ks);
    }

    private void removeFromTypeMap(SortedMap <String, SortedMap> ht, Kstat ks) {
	@SuppressWarnings("unchecked")
	SortedMap <String, SortedMap> hm = ht.get(ks.getTypeAsString());
	removeFromModuleMap(hm, ks);
    }

    private void removeFromClassMap(SortedMap <String, SortedMap> hc,
		Kstat ks) {
	@SuppressWarnings("unchecked")
	SortedMap <String, SortedMap> hm = hc.get(ks.getKstatClass());
	removeFromModuleMap(hm, ks);
    }

    /*
     * Delete the kstat from the Maps.
     */
    private void removeFromModuleMap(SortedMap <String, SortedMap> hm,
		Kstat ks) {
	@SuppressWarnings("unchecked")
	SortedMap <String, SortedMap> h_1 = hm.get(ks.getModule());
	if (h_1 == null) {
	    return;
	}
	@SuppressWarnings("unchecked")
	SortedMap <String, Kstat> h_2 = h_1.get(ks.getInstance());
	if (h_2 != null) {
	    h_2.remove(ks.getName());
	}
    }
}
