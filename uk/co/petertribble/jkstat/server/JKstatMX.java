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

import uk.co.petertribble.jkstat.api.*;
import java.util.Set;

/**
 * The actual implementation of the MBean exposing JKstat data over JMX.
 *
 * @author Peter Tribble
 */
public class JKstatMX implements JKstatMXMBean {

    private static final JKstat jkstat = new NativeJKstat();

    /**
     * Return the full Set of Kstats.
     *
     * @return The full set of kstats
     */
    public Set <Kstat> getKstats() {
	return jkstat.getKstats();
    }

    public Kstat getKstat(String module, int inst, String name) {
	return jkstat.getKstat(module, inst, name);
    }

    public Object getKstatData(String module, int inst, String name,
				String statistic) {
	return jkstat.getKstat(module, inst, name).getData(statistic);
    }
}
