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
    private Map<Long, ProcessorChip> procmap;
    private boolean threaded;
    private boolean multicore;

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
     * The top-level Map is a map of <chip-id, ProcessorChip>
     * A ProcessorChip is a map of <core-id, ProcessorCore>.
     * A ProcessorCore is a map of <thread-id, Kstat>
     *
     * We describe non-threaded cores as cores with a single thread.
     */
    @SuppressWarnings("unchecked")
    private void buildTree(Set<Kstat> kstats) {
	procmap = new TreeMap<>();

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

	    // if we haven't seen this chip before, create a new ProcessorChip
	    // and add it to the top map
	    if (!procmap.containsKey(lchip)) {
		procmap.put(lchip, new ProcessorChip(lchip));
	    }
	    // chip is the chip this kstat belongs to
	    ProcessorChip chip = procmap.get(lchip);

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

	    // if we haven't seen this core before, create a new ProcessorCore
	    // and add it to the chip
	    if (!chip.containsCore(lcore)) {
		chip.addCore(lcore, new ProcessorCore(lchip, lcore));
	    }
	    // core is the core this thread belongs to
	    ProcessorCore core = chip.getCore(lcore);
	    // add the thread to the current core
	    core.addThread(lclog, ks);
	}

	// now we've constructed the whole tree, establish global properties
	for (ProcessorChip chip : procmap.values()) {
	    if (chip.isMulticore()) {
		multicore = true;
	    }
	    if (chip.isMultithreaded()) {
		threaded = true;
	    }
	}
    }

    /**
     * Returns whether the processors are threaded.
     *
     * @return true if the processor has multiple threads per core
     */
    public boolean isThreaded() {
	return threaded;
    }

    /**
     * Returns whether the processors have multiple cores.
     *
     * @return true if the processor has multiple cores
     */
    public boolean isMulticore() {
	return multicore;
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
	return procmap.size();
    }

    /**
     * Return the Set of chip ids.
     *
     * @return the Set of chip ids
     */
    public Set<Long> getChips() {
	return procmap.keySet();
    }

    /**
     * Return the Set of ProcessorChips.
     *
     * @return the Set of ProcessChips
     */
    public Set<ProcessorChip> getProcessorChips() {
	return new TreeSet<>(procmap.values());
    }

    /**
     * Return the number of cores.
     *
     * @return the total number of distinct cores
     */
    public int numCores() {
	int nc = 0;
	for (ProcessorChip chip : procmap.values()) {
	    nc += chip.numCores();
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
	ProcessorChip chip = procmap.get(chipid);
	return (chip == null) ? 0 : chip.numCores();
    }

    /**
     * Return the Set of core ids for a given chip.
     *
     * @param chipid the chip to query
     *
     * @return the Set of core ids for a given chip
     */
    public Set<Long> getCores(long chipid) {
	ProcessorChip chip = procmap.get(chipid);
	if (chip != null) {
	    return chip.getCoreIDs();
	} else {
	    return new TreeSet<>();
	}
    }

    /**
     * Return the total number of threads.
     * If invalid, return zero.
     *
     * @return the total number of threads in the system
     */
    public int numThreads() {
	int nthreads = 0;
	for (ProcessorChip chip : procmap.values()) {
	    nthreads += chip.numThreads();
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
	ProcessorChip chip = procmap.get(chipid);
	return (chip == null) ? 0 : chip.numCores();
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
	ProcessorChip chip = procmap.get(chipid);
	if (chip == null) {
	    return 0;
	}
	ProcessorCore core = chip.getCore(coreid);
	return (core == null) ? 0 : core.numThreads();
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

    /*
     * The ProcessorTree stores the cpu_info kstats. Performance statistics
     * use the corresponding cpu_stat kstats. This is a convenience to convert
     * to the other form.
     *
     * @param kss the Set of Kstats to convert
     *
     * @return the Set of cpu_stat Kstats corresponding to the given
     * cpu_info Kstats
     */
    private Set<Kstat> makeCpuKstats(Set<Kstat> kss) {
	Set<Kstat> kout = new TreeSet<>();
	for (Kstat ks : kss) {
	    kout.add(makeCpuKstat(ks));
	}
	return kout;
    }

    /**
     * Return all the informational Kstats.
     * If invalid, return the empty Set.
     *
     * @return all the cpu_info Kstats
     */
    public Set<Kstat> allInfoStats() {
	Set<Kstat> kss = new TreeSet<>();
	for (ProcessorChip chip : procmap.values()) {
	    kss.addAll(chip.infoStats());
	}
	return kss;
    }

    /**
     * Return all the Kstats.
     * If invalid, return the empty Set.
     *
     * @return all the cpu_stat Kstats
     */
    public Set<Kstat> allStats() {
	return makeCpuKstats(allInfoStats());
    }

    /**
     * Return all the Kstats corresponding to a given chip.
     * If invalid, return the empty Set.
     *
     * @param chipid the chip to query
     *
     * @return all the cpu_info Kstats for the given chip
     */
    public Set<Kstat> chipInfoStats(long chipid) {
	ProcessorChip chip = procmap.get(chipid);
	if (chip != null) {
	    return chip.infoStats();
	} else {
	    return new TreeSet<>();
	}
    }

    /**
     * Return all the Kstats corresponding to a given chip.
     * If invalid, return the empty Set.
     *
     * @param chipid the chip to query
     *
     * @return all the cpu_stat Kstats for the given chip
     */
    public Set<Kstat> chipStats(long chipid) {
	return makeCpuKstats(chipInfoStats(chipid));
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
    public Set<Kstat> coreInfoStats(long chipid, long coreid) {
	ProcessorChip chip = procmap.get(chipid);
	if (chip == null) {
	    return new TreeSet<>();
	}
	ProcessorCore core = chip.getCore(coreid);
	if (core == null) {
	    return new TreeSet<>();
	}
	return core.infoStats();
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
    public Set<Kstat> coreStats(long chipid, long coreid) {
	return makeCpuKstats(coreInfoStats(chipid, coreid));
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
	return (String) chipInfoStats(chipid).iterator().next()
	    .getData("brand");
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
	return (String) coreInfoStats(chipid, coreid).iterator().next()
	    .getData("brand");
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
		for (Kstat kst : coreInfoStats(l, ll)) {
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

    /**
     * Print the details of a chip, like psrinfo -vp.
     *
     * @param chip the ProcessorChip whose details are to be shown
     *
     * @return a String similar to psrinfo -vp output for the given chip
     */
    public String chipDetails(ProcessorChip chip) {
	StringBuilder sb = new StringBuilder(256);
	sb.append("Physical processor ").append(chip.getChipid())
	    .append(" has ");
	if (chip.numCores() == 1) {
	    sb.append("1 core");
	} else {
	    sb.append(chip.numCores()).append(" cores");
	}
	if (chip.isMultithreaded()) {
	    sb.append(" and ").append(chip.numThreads()).append(" threads");
	}
	sb.append('\n');
	if (chip.isMultithreaded()) {
	    for (ProcessorCore core : chip.getCores()) {
		sb.append("    Core ").append(core.getCoreid()).append(" has ");
		int nt = core.numThreads();
		sb.append(nt).append(" threads (");
		int ni = 0;
		for (Kstat kst : core.getThreads()) {
		    sb.append(kst.getInst());
		    ni++;
		    if (ni < nt) {
			sb.append(' ');
		    }
		}
		sb.append(")\n");
	    }
	}
	sb.append("        ").append(chip.getBrand())
	    .append("\n        Clock speed: ")
	    .append(chip.infoStats().iterator().next().getData("clock_MHz"))
	    .append(" MHz\n");
	return sb.toString();
    }
}
