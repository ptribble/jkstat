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

import uk.co.petertribble.jkstat.api.*;
import java.util.Set;

/**
 * The actual implementation of the MBean exposing JKstat data over JMX.
 *
 * @author Peter Tribble
 */
public final class JKstatMX implements JKstatMXMBean {

    private static final JKstat JKSTAT = new NativeJKstat();

    /**
     * Return the full Set of Kstats.
     *
     * @return The full set of kstats
     */
    @Override
    public Set<Kstat> getKstats() {
	return JKSTAT.getKstats();
    }

    @Override
    public Kstat getKstat(final String module, final int inst,
			  final String name) {
	return JKSTAT.getKstat(module, inst, name);
    }

    @Override
    public Object getKstatData(final String module, final int inst,
			       final String name, final String statistic) {
	return JKSTAT.getKstat(module, inst, name).getData(statistic);
    }
}
