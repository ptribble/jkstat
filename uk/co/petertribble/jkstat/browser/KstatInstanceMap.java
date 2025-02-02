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

package uk.co.petertribble.jkstat.browser;

import java.util.TreeMap;
import uk.co.petertribble.jkstat.util.NumericStringComparator;

/**
 * Describes a map of kstats keyed by instance.
 */
public class KstatInstanceMap extends TreeMap<String, KstatNameMap> {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new KstatInstanceMap, to store a Map of named Kstats.
     */
    public KstatInstanceMap() {
	super(NumericStringComparator.getInstance());
    }
}
