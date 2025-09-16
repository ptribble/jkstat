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

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;

/**
 * A standalone JMX server.
 *
 * @author Peter Tribble
 */
public final class JKstatMXserver {

    private JKstatMXserver() {
    }

    /**
     * Start a standalone JMX server.
     *
     * @param args command line arguments, ignored
     */
    public static void main(final String[] args) {
	try {
	    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	    ObjectName name =
	    new ObjectName("uk.co.petertribble.jkstat.server:type=JKstatMX");
	    JKstatMX mbean = new JKstatMX();
	    mbs.registerMBean(mbean, name);
	    System.out.println("JKstatMX server ready.");
	    Thread.sleep(Long.MAX_VALUE);
	} catch (JMException jme) {
	    System.err.println("JKstatMX server failed.");
	} catch (InterruptedException ie) {
	    System.err.println("JKstatMX server interrupted.");
	}
    }
}
