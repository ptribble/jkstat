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

package uk.co.petertribble.jkstat.server;

import uk.co.petertribble.jkstat.api.Kstat;
import java.util.Set;

/**
 * An MBean, exposing some JKstat data via JMX.
 *
 * @author Peter Tribble
 */
public interface JKstatMXMBean {

    /**
     * Return a Set of all the kstats.
     *
     * @return a Set of all the kstats
     */
    Set<Kstat> getKstats();

    /**
     * Return a given kstat.
     *
     * @param module the module of the desired kstat
     * @param inst the instance of the desired kstat
     * @param name the name of the desired kstat
     *
     * @return the desired kstat
     */
    Kstat getKstat(String module, int inst, String name);

    /**
     * Return the value of a given statistic.
     *
     * @param module the module of the desired kstat
     * @param inst the instance of the desired kstat
     * @param name the name of the desired kstat
     * @param statistic the desired statistic
     *
     * @return the value of the desired statistic for the desired kstat
     */
     Object getKstatData(String module, int inst, String name,
			String statistic);
}
