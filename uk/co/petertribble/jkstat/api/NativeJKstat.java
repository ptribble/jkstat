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

package uk.co.petertribble.jkstat.api;

import java.util.Date;

/**
 * An access class for Solaris kstats. Allows the available kstats to be
 * enumerated, and kstats and individual statistics to be retrieved.
 *
 * @author Peter Tribble
 */
public class NativeJKstat extends JKstat {

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
    private native static void cacheids();

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
