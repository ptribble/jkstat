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

import java.util.ResourceBundle;

/**
 * A class to manage the resource bundles used by JKstat.
 *
 * @author Peter Tribble
 */
public final class KstatResources {

    private static final ResourceBundle KSTATRES =
				ResourceBundle.getBundle("properties/jkstat");

    private KstatResources() {
    }

    /**
     * Returns the string from the resource bundle that corresponds to the
     * given key. If there is no matching key, returns null.
     *
     * @param key the key to be looked up
     *
     * @return the matching String from the resource bundle
     */
    public static String getString(String key) {
	return KSTATRES.getString(key);
    }
}
