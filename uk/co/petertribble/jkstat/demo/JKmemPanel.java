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
public final class JKmemPanel extends JingleVPanel {

    private static final long serialVersionUID = 1L;

    private transient KstatAccessorySet kas;

    /**
     * Create a panel to display kernel memory allocation statistics.
     *
     * @param jkstat A JKstat object
     * @param interval the update interval in seconds
     */
    public JKmemPanel(JKstat jkstat, int interval) {

	List<KstatAccessoryPanel> vkstat = new ArrayList<>();

	setLayout(new SpringLayout());

	/*
	 * Filter on all kmem cache kstats
	 */
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.setFilterClass("kmem_cache");
	ksf.addFilter("unix:0::");

	Set<Kstat> kms = new TreeSet<>();
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
