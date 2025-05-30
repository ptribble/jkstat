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

package uk.co.petertribble.jkstat.browser;

import java.util.TreeMap;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.util.NumericStringComparator;

/**
 * Describes a map of kstats keyed by name.
 */
public final class KstatNameMap extends TreeMap<String, Kstat> {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new KstatNameMap, to store a map of Kstats by name.
     */
    public KstatNameMap() {
	super(NumericStringComparator.getInstance());
    }
}
