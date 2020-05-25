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

package uk.co.petertribble.jkstat.demo;

import uk.co.petertribble.jkstat.api.*;
import java.util.*;
import java.text.DateFormat;

/**
 * Maintains a description of processor topology.
 */
public class ProcessorTree {

    private JKstat jkstat;
    private Map <Long, ChipMap> map;
    private boolean is_threaded;
    private boolean is_multicore;
    private static final DateFormat df = DateFormat.getInstance();

    /**
     * Create a processor tree.
     *
     * @param jkstat A JKstat object
     */
    public ProcessorTree(JKstat jkstat) {
	this.jkstat = jkstat;
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.setFilterClass("misc");
	ksf.addFilter("cpu_info:::");
	build_tree(ksf.getKstats());
    }

    /*
     * Walk through the kstats, building up the hierarchy.
     *
     * The top-level Map is a map of <chip-id, Map of cores>
     * The next level map is a map is of <core-id, Map of threads>.
     * And finally, the lowest map is of <thread-id, Kstat>
     *
     * We describe non-threaded cores as cores with a single thread.
     */
    @SuppressWarnings("unchecked")
    private void build_tree(Set <Kstat> kstats) {
	map = new TreeMap <Long, ChipMap> ();

	for (Kstat ks : kstats) {
	    ks = jkstat.getKstat(ks);
	    /*
	     * Build a tree chip_id/core_id/clog_id
	     * Not all chips give us clog_id
	     *
	     * it is assumed that either all chips support clog_id, or none do
	     * our T2 box reports device_ID instead
	     */
	    Long lchip = (Long) ks.getData("chip_id");
	    Long lcore = (Long) ks.getData("core_id");
	    Long lclog = (Long) ks.getData("clog_id");
	    if (lclog == null) {
		lclog = (Long) ks.getData("device_ID");
	    }

	    if (!map.containsKey(lchip)) {
		map.put(lchip, new ChipMap(lchip));
	    }
	    ChipMap chipmap = map.get(lchip);

	    /*
	     * If we don't have threads, make up a thread id.
	     *
	     * Note: On a T2, the device_ID matches the instance number, going
	     * from 0-63. On the Xeon, clog_id goes from 0-3 on each chip. In
	     * other words, clog_id is only unique within a physical device.
	     * This makes it useless for identifying a given kstat.
	     * Fortunately, we store the kstat at the bottom and can use that
	     * to get to the real deal. If we are going to use the clog_id as
	     * the key, we need to be careful to not use it as anything more
	     * than a textual identifier.
	     */
	    if (lclog == null) {
		lclog = Long.valueOf(0L);
	    }
	    if (chipmap.containsKey(lcore)) {
		// this must be an additional thread in the same core
		is_threaded = true;
	    } else {
		chipmap.put(lcore, new CoreMap(lchip, lcore));
	    }
	    CoreMap coremap = chipmap.get(lcore);
	    coremap.put(lclog, ks);

	    // now test for multicore
	    is_multicore = (chipmap.size() > 1);
	}
    }

    /**
     * Returns whether the processors are threaded.
     *
     * @return true if the processor has multiple threads per core
     */
    public boolean isThreaded() {
	return is_threaded;
    }

    /**
     * Returns whether the processors have multiple cores.
     *
     * @return true if the processor has multiple cores
     */
    public boolean isMulticore() {
	return is_multicore;
    }

    /**
     * Return the number of chips.
     *
     * @return the number of distinct chips
     */
    public int numChips() {
	/*
	 * There's one entry for each chip in the top map, so the number
	 * of chips is the size of that map.
	 */
	return map.size();
    }

    /**
     * Return the Set of chip ids.
     *
     * @return the Set of chip ids
     */
    public Set <Long> getChips() {
	return map.keySet();
    }

    /**
     * Return the number of cores in a given chip. If invalid, return zero.
     *
     * @param chipid the chip to query
     *
     * @return the number of cores in a given chip
     */
    public int numCores(long chipid) {
	ChipMap m = map.get(chipid);
	return (m == null) ? 0 : m.size();
    }

    /**
     * Return the Set of core ids for a given chip.
     *
     * @param chipid the chip to query
     *
     * @return the Set of core ids for a given chip
     */
    public Set <Long> getCores(long chipid) {
	ChipMap m = map.get(chipid);
	Set <Long> set = new TreeSet <Long> ();
	if (m != null) {
	    set.addAll(m.keySet());
	}
	return set;
    }

    /**
     * Return the number of threads in a given core, identified by both chipid
     * and coreid. If invalid, return zero.
     *
     * @param chipid the chip to query
     * @param coreid the core to query
     *
     * @return the number of threads in the given core
     */
    public int numThreads(long chipid, long coreid) {
	ChipMap m = map.get(chipid);
	if (m == null) {
	    return 0;
	}
	CoreMap mm = m.get(coreid);
	return (mm == null) ? 0 : mm.size();
    }

    /**
     * The ProcessorTree stores the cpu_info kstats. Performance statistics
     * use the corresponding cpu_stat kstats. This is a convenience to convert
     * to the other form.
     *
     * @param ks the Kstat to convert
     *
     * @return the cpu_stat Kstat corresponding to the given cpu_info Kstat
     */
    public Kstat makeCpuKstat(Kstat ks) {
	return new Kstat("cpu_stat", ks.getInst(),
				  "cpu_stat" + ks.getInstance());
    }

    /**
     * Return all the Kstats corresponding to a given chip.
     * If invalid, return the empty Set.
     *
     * @param chipid the chip to query
     *
     * @return all the cpu_info Kstats for the given chip
     */
    public Set <Kstat> chipInfoStats(long chipid) {
	Set <Kstat> kss = new TreeSet <Kstat> ();
	ChipMap m = map.get(chipid);
	if (m != null) {
	    for (CoreMap mm : m.values()) {
		kss.addAll(mm.values());
	    }
	}
	return kss;
    }

    /**
     * Return all the Kstats corresponding to a given chip.
     * If invalid, return the empty Set.
     *
     * @param chipid the chip to query
     *
     * @return all the cpu_stat Kstats for the given chip
     */
    public Set <Kstat> chipStats(long chipid) {
	Set <Kstat> kss = new TreeSet <Kstat> ();
	for (Kstat ks : chipInfoStats(chipid)) {
	    kss.add(makeCpuKstat(ks));
	}
	return kss;
    }

    /**
     * Return the Kstats for a given core, identified by both chipid and coreid.
     * If invalid, return the empty Set.
     *
     * @param chipid the chip to query
     * @param coreid the core to query
     *
     * @return all the cpu_info Kstats for the given core
     */
    public Set <Kstat> coreInfoStats(long chipid, long coreid) {
	Set <Kstat> kss = new TreeSet <Kstat> ();
	ChipMap m = map.get(chipid);
	if (m == null) {
	    return kss;
	}
	CoreMap mm = m.get(coreid);
	if (mm == null) {
	    return kss;
	}
	kss.addAll(mm.values());
	return kss;
    }

    /**
     * Return the Kstats for a given core, identified by both chipid and coreid.
     * If invalid, return the empty Set.
     *
     * @param chipid the chip to query
     * @param coreid the core to query
     *
     * @return all the cpu_stat Kstats for the given core
     */
    public Set <Kstat> coreStats(long chipid, long coreid) {
	Set <Kstat> kss = new TreeSet <Kstat> ();
	for (Kstat ks : coreInfoStats(chipid, coreid)) {
	    kss.add(makeCpuKstat(ks));
	}
	return kss;
    }

    /**
     * Return the brand of the given cpu.
     *
     * @param chipid the chip to query
     *
     * @return a String representing the brand of the given chip
     */
    public String getBrand(long chipid) {
	ChipMap m = map.get(chipid);
	if (m != null) {
	    for (CoreMap mm : m.values()) {
		for (Kstat oo : mm.values()) {
		    return (String) oo.getData("brand");
		}
	    }
	}
	return null;
    }

    /**
     * Return the brand of the given cpu.
     *
     * @param chipid the chip to query
     * @param coreid the core to query
     *
     * @return a String representing the brand of the given core
     */
    public String getBrand(long chipid, long coreid) {
	ChipMap m = map.get(chipid);
	if (m == null) {
	    return null;
	}
	CoreMap mm = m.get(coreid);
	if (mm == null) {
	    return null;
	}
	for (Kstat o : mm.values()) {
	    return (String) o.getData("brand");
	}
	return null;
    }

    /**
     * Print the details of a given processor. This is psrinfo -v
     *
     * @param ks a cpu_info Kstat
     *
     * @return a String similar to psrinfo -v output for the given Kstat
     */
    public static String details(Kstat ks) {
	StringBuilder sb = new StringBuilder();
	if (ks != null) {
	    sb.append("Status of virtual processor ").append(ks.getInstance());
	    sb.append(" as of: ").append(df.format(new Date()));
	    sb.append("\n  ").append(ks.getData("state")).append(" since ");
	    sb.append(df.format(new Date(1000*ks.longData("state_begin"))));
	    sb.append("\n  The ").append(ks.getData("cpu_type"));
	    sb.append(" processor operates at ");
	    sb.append(ks.getData("clock_MHz")).append(" MHz,\n");
	    sb.append("        and has an ").append(ks.getData("fpu_type"));
	    sb.append(" floating point processor.");
	}
	return sb.toString();
    }

    /**
     * Print the details of a chip, like psrinfo -vp.
     *
     * @param l the id of the desired chip
     *
     * @return a String similar to psrinfo -vp output for the given chip
     */
    public String chipDetails(Long l) {
	StringBuilder sb = new StringBuilder();
	sb.append("Physical processor ").append(l).append(" has ");
	if (numCores(l) == 1) {
	    sb.append("1 core\n");
	} else {
	    sb.append(numCores(l)).append(" cores\n");
	}
	if (isThreaded()) {
	    for (Long ll : getCores(l)) {
		sb.append("    Core ").append(ll).append(" has ");
		sb.append(numThreads(l, ll)).append(" threads\n");
	    }
	}
	sb.append("        ").append(getBrand(l)).append("\n");
	sb.append("        Clock speed: ");
	sb.append(chipInfoStats(l).iterator().next().getData("clock_MHz"));
	sb.append(" MHz\n");
	return sb.toString();
    }
}
