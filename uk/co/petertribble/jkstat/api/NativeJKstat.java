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

package uk.co.petertribble.jkstat.api;

import java.util.Date;

/**
 * An access class for Solaris kstats. Allows the available kstats to be
 * enumerated, and kstats and individual statistics to be retrieved.
 *
 * @author Peter Tribble
 */
public final class NativeJKstat extends JKstat {

    static {
	System.loadLibrary("kstat_jni");
	cacheids(); // native JKstat ref prevents class from being unloaded
    }

    /**
     * Creates a new JKstat object.
     */
    public NativeJKstat() {
	super();
    }

    /**
     * Caches all the methodids once, for efficiency and guaranteed code
     * coverage.
     */
    private static native void cacheids();

    @Override
    public native Kstat getKstatObject(String module, int inst,
    		String name);

    @Override
    public native int getKCID();

    @Override
    public native int enumerate();

    /**
     * Gets the time, as the number of milliseconds since January 1, 1970,
     * 00:00:00 GMT, associated with this JKstat object. For a NativeJKstat,
     * always returns the current time.
     */
    @Override
    public long getTime() {
	return new Date().getTime();
    }
}
