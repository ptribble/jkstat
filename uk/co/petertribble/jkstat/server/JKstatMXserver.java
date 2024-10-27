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
    public static void main(String[] args) {
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
