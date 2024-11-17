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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import uk.co.petertribble.jkstat.api.Kstat;

/**
 * Describes a cpu core and its constituent threads.
 */
public class ProcessorCore {

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
}
