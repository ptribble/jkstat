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

package uk.co.petertribble.jkstat.gui;

import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.KstatType;

/**
 * A registry of known accessories.
 *
 * @author Peter Tribble
 */
public final class KstatAccessoryRegistry {

    private KstatAccessoryRegistry() {
    }

    /**
     * Return a suitable KstatAccessoryPanel for a given Kstat. If no
     * accessory is known for the given kstat, return null.
     *
     * @param ks a kstat
     * @param interval the update interval in seconds
     * @param jkstat a JKstat object
     *
     * @return a KstatAccessoryPanel object suitable for the given kstat
     */
    public static KstatAccessoryPanel getAccessoryPanel(Kstat ks, int interval,
							JKstat jkstat) {
	ks = jkstat.getKstat(ks);
	if (ks == null) {
	    return null;
	}
	if (ks.hasStatistic("rbytes64")
	    && ("mac".equals(ks.getKstatClass())
		|| "net".equals(ks.getKstatClass()))) {
			return new AccessoryNetPanel(ks, interval, jkstat);
	}
	if (ks.getType() == KstatType.KSTAT_TYPE_IO) {
	    return new AccessoryIOPanel(ks, interval, jkstat);
	}
	if ("unix".equals(ks.getModule()) && ks.getInst() == 0
			&& "system_misc".equals(ks.getName())) {
	    return new AccessoryLoadPanel(ks, interval, jkstat);
	}
	if ("cpu_stat".equals(ks.getModule())) {
	    return new AccessoryCpuPanel(ks, interval, jkstat);
	}
	if ("bge".equals(ks.getModule()) && "statistics".equals(ks.getName())) {
	    return new AccessoryBgePanel(ks, interval, jkstat);
	}
	if ("kmem_cache".equals(ks.getKstatClass())
		&& ks.getName().startsWith("kmem_alloc_")
		&& ks.hasStatistic("alloc")) {
	    return new AccessoryKmemAlloc(ks, interval, jkstat);
	}
	return null;
    }
}
