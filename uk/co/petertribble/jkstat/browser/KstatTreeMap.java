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

    private KstatModuleMap kstatMap;
    private SortedMap<String, KstatModuleMap> kstatClassMap;
    private SortedMap<String, KstatModuleMap> kstatTypeMap;

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
	kstatMap = new KstatModuleMap();
	kstatTypeMap = new TreeMap<>(NumericStringComparator.getInstance());
	kstatClassMap = new TreeMap<>(NumericStringComparator.getInstance());
	for (Kstat ks : kss.getKstats()) {
	    addKstat(ks);
	}
    }

    /**
     * Return the kstat Map.
     *
     * @return The kstat Map
     */
    public KstatModuleMap getKstatMap() {
	return kstatMap;
    }

    /**
     * Return the kstat Map, including the class hierarchy.
     *
     * @return The kstat Map, including the class hierarchy
     */
    public SortedMap<String, KstatModuleMap> getKstatClassMap() {
	return kstatClassMap;
    }

    /**
     * Return the kstat Map, including the type hierarchy.
     *
     * @return The kstat Map, including the type hierarchy
     */
    public SortedMap<String, KstatModuleMap> getKstatTypeMap() {
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
    private void addToTypeMap(SortedMap<String, KstatModuleMap> hc, Kstat ks) {
	String ktype = ks.getTypeAsString();
	if (!hc.containsKey(ktype)) {
	    hc.put(ktype, new KstatModuleMap());
	}
	KstatModuleMap hm = hc.get(ktype);
	addToModuleMap(hm, ks);
    }

    /*
     * Add a Kstat to the right place in the Map.
     */
    private void addToClassMap(SortedMap<String, KstatModuleMap> hc,
		Kstat ks) {
	String kc = ks.getKstatClass();
	if (!hc.containsKey(kc)) {
	    hc.put(kc, new KstatModuleMap());
	}
	KstatModuleMap hm = hc.get(kc);
	addToModuleMap(hm, ks);
    }

    /*
     * Add a Kstat to the right place in the Map.
     */
    private void addToModuleMap(KstatModuleMap hm, Kstat ks) {
	String km = ks.getModule();
	KstatInstanceMap hi;
	if (hm.containsKey(km)) {
	    KstatInstanceMap tmp = hm.get(km);
	    hi = tmp;
	} else {
	    hi = new KstatInstanceMap();
	    hm.put(km, hi);
	}
	addToInstanceMap(hi, ks);
    }

    /*
     * Add a Kstat to the right place in the Map.
     */
    private void addToInstanceMap(KstatInstanceMap hi, Kstat ks) {
	String ki = ks.getInstance();
	KstatNameMap hn;
	if (hi.containsKey(ki)) {
	    KstatNameMap tmp = hi.get(ki);
	    hn = tmp;
	} else {
	    hn = new KstatNameMap();
	    hi.put(ki, hn);
	}
	hn.put(ks.getName(), ks);
    }

    private void removeFromTypeMap(SortedMap<String, KstatModuleMap> ht,
		Kstat ks) {
	KstatModuleMap hm = ht.get(ks.getTypeAsString());
	removeFromModuleMap(hm, ks);
    }

    private void removeFromClassMap(SortedMap<String, KstatModuleMap> hc,
		Kstat ks) {
	KstatModuleMap hm = hc.get(ks.getKstatClass());
	removeFromModuleMap(hm, ks);
    }

    /*
     * Delete the kstat from the Maps.
     */
    private void removeFromModuleMap(KstatModuleMap hm,
		Kstat ks) {
	KstatInstanceMap hmap1 = hm.get(ks.getModule());
	if (hmap1 == null) {
	    return;
	}
	KstatNameMap hmap2 = hmap1.get(ks.getInstance());
	if (hmap2 != null) {
	    hmap2.remove(ks.getName());
	}
    }
}
