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
	KstatInstanceMap h_1 = hm.get(ks.getModule());
	if (h_1 == null) {
	    return;
	}
	KstatNameMap h_2 = h_1.get(ks.getInstance());
	if (h_2 != null) {
	    h_2.remove(ks.getName());
	}
    }
}
