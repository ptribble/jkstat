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
 * Describes a cpu core and its constituent threads.
 */
public class ProcessorCore implements Comparable<ProcessorCore> {

    private static final long serialVersionUID = 1L;

    private final Map<Long, Kstat> threadmap = new TreeMap<>();

    private long chipid;
    private long coreid;

    /**
     * Create a new ProcessorCore to store details of a processor
     * core and its constituent threads.
     *
     * @param chipid the chip id of this ProcessorCore
     * @param coreid the core id of this ProcessorCore
     */
    public ProcessorCore(long chipid, long coreid) {
	this.chipid = chipid;
	this.coreid = coreid;
    }

    /**
     * Get the id of this ProcessorCore.
     *
     * @return this ProcessorCore's numerical id
     */
    public long getCoreid() {
	return coreid;
    }

    /**
     * Add a new thread to this ProcessorCore.
     *
     * @param threadid the id of the thread to add
     * @param kstat the cpu_info Kstat for this thread
     */
    public void addThread(Long threadid, Kstat kstat) {
	threadmap.put(threadid, kstat);
    }

    /**
     * Get the given thread from this ProcessorCore.
     *
     * @param threadid the thread to retrieve
     *
     * @return the Kstat matching the requested id
     */
    public Kstat getThread(Long threadid) {
	return threadmap.get(threadid);
    }

    /**
     * Get the Set of threads in this ProcessorCore.
     *
     * @return the Set of Kstats covering all threads
     */
    public Set<Kstat> getThreads() {
	return new TreeSet<>(threadmap.values());
    }

    /**
     * Get the number of threads on this core.
     *
     * @return the number of distinct threads on this core
     */
    public int numThreads() {
	return threadmap.size();
    }

    /**
     * Get whether this core contains multiple threads.
     *
     * @return true if this core contains multiple threads, false if it's
     * single threaded
     */
     public boolean isMultithreaded() {
	 return threadmap.size() > 1;
    }

    /**
     * Get the cpu_info kstats for all threads in this core.
     *
     * @return a KstatSet containing all the cpu_info Kstats associated
     * with this core
     */
    public Set<Kstat> infoStats() {
	return new TreeSet<>(threadmap.values());
    }

    /**
     * Compare with another ProcessorCore.
     *
     * Note: this class has a natural ordering that is inconsistent with
     * equals. The comparison is only valid for Cores within a given
     * ProcessorChip, as cores in different chips may have the same core id.
     *
     * @param core the ProcessorCore to be compared
     *
     * @return the signed comparison of the id of the given ProcessorCore
     * with the id of this ProcessorCore
     */
    @Override
    public int compareTo(ProcessorCore core) {
	if (this == core) {
	    return 0;
	}
	return (int) (coreid - core.getCoreid());
    }
}
