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

/**
 * The JKstat API allows programmatic access to Solaris kstats from java
 * code.
 * <p>
 * To start, instantiate a JKstat. If working locally, this would be a
 * NativeJKstat.
 * <pre>
 * JKstat jkstat = new NativeJKstat();
 * </pre>
 * Once you have a JKstat, you can either read a single Kstat or
 * enumerate the Kstats. To read a whole kstat, you need to know the
 * module, instance, and name. Given a kstat, you can then read all its
 * statistics, or an individual statistic.
 * <pre>
 * Kstat ks = jkstat.getKstat("unix", 0, "system_misc");
 * Set &lt;String&gt; stats = ks.statistics();
 * Object data = ks.getData("statistic");
 * long l1 = ls.longData("avenrun_1min");
 * </pre>
 * The Set of statistics is sorted, being backed by a TreeSet. The data
 * object will be either a String, or a Long for all the numeric values.
 */

package uk.co.petertribble.jkstat.api;
