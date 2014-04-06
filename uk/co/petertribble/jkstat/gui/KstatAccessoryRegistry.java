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

package uk.co.petertribble.jkstat.gui;

import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.KstatType;

/**
 * A registry of known accessories.
 *
 * @author Peter Tribble
 */
public class KstatAccessoryRegistry {

    protected KstatAccessoryRegistry() {
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
	if (ks.hasStatistic("rbytes64") &&
	    ("mac".equals(ks.getKstatClass())
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
	if ("kmem_cache".equals(ks.getKstatClass()) &&
		ks.getName().startsWith("kmem_alloc_") &&
		ks.hasStatistic("alloc")) {
	    return new AccessoryKmemAlloc(ks, interval, jkstat);
	}
	return null;
    }
}
