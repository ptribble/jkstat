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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import uk.co.petertribble.jkstat.api.Kstat;

/**
 * Describes a cpu chip and its constituent cores.
 */
public class ProcessorChip implements Comparable<ProcessorChip> {

    private static final long serialVersionUID = 1L;

    private final Map<Long, ProcessorCore> coremap = new TreeMap<>();

    private long chipid;

    /**
     * Create a new ProcessorChip to store details of a processor
     * chip and its constituent cores.
     *
     * @param chipid the id of this ProcessorChip
     */
    public ProcessorChip(long chipid) {
	this.chipid = chipid;
    }

    /**
     * Get the id of this ProcessorChip.
     *
     * @return this ProcessorChip's numerical id
     */
    public long getChipid() {
	return chipid;
    }

    /**
     * Add a new core to this ProcessorChip.
     *
     * @param coreid the id of the core to add
     * @param pcore the ProcessorCore to add
     */
    public void addCore(Long coreid, ProcessorCore pcore) {
	coremap.put(coreid, pcore);
    }

    /**
     * Get the given core from this ProcessorChip.
     *
     * @param coreid the core to retrieve
     *
     * @return the ProcessorCore matching the requested id
     */
    public ProcessorCore getCore(Long coreid) {
	return coremap.get(coreid);
    }

    /**
     * Get a Set of core ids from this ProcessorChip.
     *
     * @return a Set of Long values describing the IDs of the cores in this chip
     */
    public Set<Long> getCoreIDs() {
	return coremap.keySet();
    }

    /**
     * Get the Set of ProcessorCores contained in this ProcessorChip.
     *
     * @return the Set of ProcessorCores in this chip
     */
    public Set<ProcessorCore> getCores() {
	return new TreeSet<>(coremap.values());
    }

    /**
     * Get whether this ProcessorChip contains the given core.
     *
     * @param coreid the core to check
     *
     * @return true if the requested core is present
     */
    public boolean containsCore(Long coreid) {
	return coremap.containsKey(coreid);
    }

    /**
     * Get the number of cores on this chip.
     *
     * @return the number of distinct cores on this chip
     */
    public int numCores() {
	return coremap.size();
    }

    /**
     * Get whether this chip contains multiple cores.
     *
     * @return true if this chip contains multiple cores, false if it's
     * single core
     */
    public boolean isMulticore() {
	return coremap.size() > 1;
    }

    /**
     * Get whether this chip contains multiple threads, specifically
     * if any of this chip's cores contain multiple threads.
     *
     * @return true if this chip contains multiple threads, false if it's
     * a single thread per core
     */
     public boolean isMultithreaded() {
	 boolean bthread = false;
	 for (ProcessorCore icore : coremap.values()) {
	     if (icore.isMultithreaded()) {
		 bthread = true;
	     }
	 }
	 return bthread;
    }

    /**
     * Get the number of threads on this chip, which is the sum of the number
     * of threads on each core.
     *
     * @return the number of distinct threads on this chip
     */
    public int numThreads() {
	int nthreads = 0;
	for (ProcessorCore icore : coremap.values()) {
	    nthreads += icore.numThreads();
	}
	return nthreads;
    }

    /**
     * Get the cpu_info kstats for all threads in all cores of this chip.
     *
     * @return a Set containing all the cpu_info Kstats associated
     * with this chip
     */
    public Set<Kstat> infoStats() {
	Set<Kstat> kss = new TreeSet<>();
	for (ProcessorCore icore : coremap.values()) {
	    kss.addAll(icore.infoStats());
	}
	return kss;
    }

    /**
     * Return the brand of this chip.
     *
     * @return a String representing the brand of this chip
     */
    public String getBrand() {
	return (String) infoStats().iterator().next().getData("brand");
    }

    /**
     * Compare with another ProcessorChip.
     *
     * @param chip the ProcessorChip to be compared
     *
     * @return the signed comparison of the id of the given ProcessorChip
     * with the id of this ProcessorChip
     */
    @Override
    public int compareTo(ProcessorChip chip) {
	if (this == chip) {
	    return 0;
	}
	return (int) (chipid - chip.getChipid());
    }
}
