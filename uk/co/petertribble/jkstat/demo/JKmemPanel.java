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

package uk.co.petertribble.jkstat.demo;

import javax.swing.*;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.gui.KstatAccessoryPanel;
import uk.co.petertribble.jkstat.gui.KstatAccessorySet;
import uk.co.petertribble.jkstat.gui.AccessoryKmemAlloc;
import uk.co.petertribble.jingle.JingleVPanel;
import uk.co.petertribble.jingle.SpringUtilities;
import java.util.*;

/**
 * A panel to display kernel memory allocation statistics.
 */
public class JKmemPanel extends JingleVPanel {

    private static final long serialVersionUID = 1L;

    private KstatAccessorySet kas;

    /**
     * Create a panel to display kernel memory allocation statistics.
     *
     * @param jkstat A JKstat object
     * @param interval the update interval in seconds
     */
    public JKmemPanel(JKstat jkstat, int interval) {

	List <KstatAccessoryPanel> vkstat =
	    new ArrayList <KstatAccessoryPanel> ();

	setLayout(new SpringLayout());

	/*
	 * Filter on all kmem cache kstats
	 */
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.setFilterClass("kmem_cache");
	ksf.addFilter("unix:0::");

	Set <Kstat> kms = new TreeSet <Kstat> ();
	for (Kstat ks : ksf.getKstats()) {
	    if (ks.getName().startsWith("kmem_alloc_")) {
		kms.add(ks);
	    }
	}
	for (Kstat ks : kms) {
	    AccessoryKmemAlloc aka = new AccessoryKmemAlloc(ks, -1, jkstat);
	    vkstat.add(aka);
	    add(new JLabel(ks.getName()));
	    add(aka);
	}
	SpringUtilities.makeCompactGrid(this, kms.size(), 2, 6, 3, 2, 2);
	kas = new KstatAccessorySet(vkstat, interval);
    }

    /**
     * Change the update interval, and propagate the new delay down to the
     * child accessories.
     *
     * @param i the new update interval, in seconds
     */
    public void setDelay(int i) {
	kas.setDelay(i);
    }

    /**
     * Stop the display updating.
     */
    public void stopLoop() {
	kas.stopLoop();
    }
}
