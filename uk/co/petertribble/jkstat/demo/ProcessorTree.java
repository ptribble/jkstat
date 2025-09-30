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
     * @param njkstat A JKstat object
     */
    public ProcessorTree(final JKstat njkstat) {
	jkstat = njkstat;
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
    private void buildTree(final Set<Kstat> kstats) {
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
		chip.addCore(lcore, new ProcessorCore(chip, lcore));
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
     * Return the Set of ProcessorChips.
     *
     * @return the Set of ProcessorChips
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
     * The ProcessorTree stores the cpu_info kstats. Performance statistics
     * use the corresponding cpu_stat kstats. This is a convenience to convert
     * to the other form.
     *
     * @param ks the Kstat to convert
     *
     * @return the cpu_stat Kstat corresponding to the given cpu_info Kstat
     */
    public static Kstat makeCpuKstat(final Kstat ks) {
	return new Kstat("cpu_stat", ks.getInst(),
				  "cpu_stat" + ks.getInstance());
    }

    /**
     * The ProcessorTree stores the cpu_info kstats. Performance statistics
     * use the corresponding cpu_stat kstats. This is a convenience to convert
     * to the other form.
     *
     * @param kss the Set of Kstats to convert
     *
     * @return the Set of cpu_stat Kstats corresponding to the given Set of
     * cpu_info Kstats
     */
    public static Set<Kstat> makeCpuKstats(final Set<Kstat> kss) {
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
    private Set<Kstat> allInfoStats() {
	Set<Kstat> kss = new TreeSet<>();
	for (ProcessorChip chip : procmap.values()) {
	    kss.addAll(chip.infoStats());
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
     * Print the details of a chip, like psrinfo -vp.
     *
     * @param chip the ProcessorChip whose details are to be shown
     *
     * @return a String similar to psrinfo -vp output for the given chip
     */
    public String chipDetails(final ProcessorChip chip) {
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
