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

/**
 * Maintains a description of processor topology.
 */
public class ProcessorTree {

    private JKstat jkstat;
    private Map <Long, ChipMap> map;
    private boolean is_threaded;
    private boolean is_multicore;

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
	buildTree(ksf.getKstats());
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
    private void buildTree(Set <Kstat> kstats) {
	map = new TreeMap<>();

	for (Kstat iks : kstats) {
	    Kstat ks = jkstat.getKstat(iks);
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
		lclog = 0L;
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
     * Return the number of cores.
     *
     * @return the total number of distinct cores
     */
    public int numCores() {
	int nc = 0;
	for (Long lc : getChips()) {
	    nc += numCores(lc);
	}
	return nc;
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
	Set <Long> set = new TreeSet<>();
	if (m != null) {
	    set.addAll(m.keySet());
	}
	return set;
    }

    /**
     * Return the total number of threads.
     * If invalid, return zero.
     *
     * @return the total number of threads in the system
     */
    public int numThreads() {
	int nthreads = 0;
	for (Long chipid : getChips()) {
	    nthreads += numThreads(chipid);
	}
	return nthreads;
    }

    /**
     * Return the number of threads in a given chip.
     * If invalid, return zero.
     *
     * @param chipid the chip to query
     *
     * @return the number of threads in the given chip
     */
    public int numThreads(long chipid) {
	int nthreads = 0;
	for (Long coreid : getCores(chipid)) {
	    nthreads += numThreads(chipid, coreid);
	}
	return nthreads;
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
     * Return all the informational Kstats.
     * If invalid, return the empty Set.
     *
     * @return all the cpu_info Kstats
     */
    public Set <Kstat> allInfoStats() {
	Set <Kstat> kss = new TreeSet<>();
	for (Long chipid : getChips()) {
	    kss.addAll(chipInfoStats(chipid));
	}
	return kss;
    }

    /**
     * Return all the Kstats.
     * If invalid, return the empty Set.
     *
     * @return all the cpu_stat Kstats
     */
    public Set <Kstat> allStats() {
	Set <Kstat> kss = new TreeSet<>();
	for (Kstat ks : allInfoStats()) {
	    kss.add(makeCpuKstat(ks));
	}
	return kss;
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
	Set <Kstat> kss = new TreeSet<>();
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
	Set <Kstat> kss = new TreeSet<>();
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
	Set <Kstat> kss = new TreeSet<>();
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
	Set <Kstat> kss = new TreeSet<>();
	for (Kstat ks : coreInfoStats(chipid, coreid)) {
	    kss.add(makeCpuKstat(ks));
	}
	return kss;
    }

    /**
     * Return the system's processor brand.
     *
     * @return a String representing the system's processor brand
     */
    public String getBrand() {
	return (String) allInfoStats().iterator().next().getData("brand");
    }

    /**
     * Return the brand of the given chip.
     *
     * @param chipid the chip to query
     *
     * @return a String representing the brand of the given chip
     */
    public String getBrand(long chipid) {
	return (String) chipInfoStats(chipid).iterator().next().getData("brand");
    }

    /**
     * Return the brand of the given core.
     *
     * @param chipid the chip to query
     * @param coreid the core to query
     *
     * @return a String representing the brand of the given core
     */
    public String getBrand(long chipid, long coreid) {
	return (String) coreInfoStats(chipid, coreid).iterator().next().getData("brand");
    }

    /**
     * Print the details of a chip, like psrinfo -vp.
     *
     * @param l the id of the desired chip
     *
     * @return a String similar to psrinfo -vp output for the given chip
     */
    public String chipDetails(Long l) {
	StringBuilder sb = new StringBuilder(256);
	sb.append("Physical processor ").append(l).append(" has ");
	if (numCores(l) == 1) {
	    sb.append("1 core");
	} else {
	    sb.append(numCores(l)).append(" cores");
	}
	if (isThreaded()) {
	    sb.append(" and ").append(numThreads(l)).append(" threads");
	}
	sb.append('\n');
	if (isThreaded()) {
	    for (Long ll : getCores(l)) {
		sb.append("    Core ").append(ll).append(" has ");
		int nt = numThreads(l, ll);
		sb.append(nt).append(" threads (");
		int ni = 0;
		for (Kstat kst : coreStats(l, ll)) {
		    sb.append(kst.getInst());
		    ni++;
		    if (ni < nt) {
			sb.append(' ');
		    }
		}
		sb.append(")\n");
	    }
	}
	sb.append("        ").append(getBrand(l))
	    .append("\n        Clock speed: ")
	    .append(chipInfoStats(l).iterator().next().getData("clock_MHz"))
	    .append(" MHz\n");
	return sb.toString();
    }
}
