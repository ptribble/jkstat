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
import uk.co.petertribble.jkstat.util.NumericStringComparator;

/**
 * Describes a map of kstats keyed by instance.
 */
public final class KstatInstanceMap extends TreeMap<String, KstatNameMap> {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new KstatInstanceMap, to store a Map of named Kstats.
     */
    public KstatInstanceMap() {
	super(NumericStringComparator.getInstance());
    }
}
