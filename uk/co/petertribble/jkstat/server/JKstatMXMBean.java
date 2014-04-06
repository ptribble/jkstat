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

import uk.co.petertribble.jkstat.api.Kstat;
import java.util.Set;

/**
 * An MBean, exposing some JKstat data via JMX.
 *
 * @author Peter Tribble
 */
public interface JKstatMXMBean {

    /**
     * Return a Set of all the kstats.
     *
     * @return a Set of all the kstats
     */
    public Set <Kstat> getKstats();

    /**
     * Return a given kstat.
     *
     * @param module the module of the desired kstat
     * @param inst the instance of the desired kstat
     * @param name the name of the desired kstat
     *
     * @return the desired kstat
     */
    public Kstat getKstat(String module, int inst, String name);

    /**
     * Return the value of a given statistic.
     *
     * @param module the module of the desired kstat
     * @param inst the instance of the desired kstat
     * @param name the name of the desired kstat
     * @param statistic the desired statistic
     *
     * @return the value of the desired statistic for the desired kstat
     */
    public Object getKstatData(String module, int inst, String name,
			String statistic);
}
