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

	GroupLayout layout = new GroupLayout(this);
	setLayout(layout);
	// the label text is vertically centered
	GroupLayout.Alignment gac = GroupLayout.Alignment.CENTER;
	// because we have to add components in a loop, create the groups
	// so we can refer to them
	GroupLayout.SequentialGroup vgroup = layout.createSequentialGroup();
	GroupLayout.ParallelGroup leftGroup = layout.createParallelGroup();
	GroupLayout.ParallelGroup rightGroup = layout.createParallelGroup();
	// horizontally, we have a sequential group containing a parallel
	// group of all the labels and a parallel group of the accessories
	// vertically, we have a sequential group containing parallel
	// groups each with a label and its accessory
	// there's a 6 pixel gap on the left, 2 pixels other sides and between
	// elements

	for (Kstat ks : kms) {
	    AccessoryKmemAlloc aka = new AccessoryKmemAlloc(ks, -1, jkstat);
	    vkstat.add(aka);
	    JLabel jl = new JLabel(ks.getName());
	    leftGroup.addComponent(jl);
	    rightGroup.addComponent(aka);
	    vgroup.addGap(2).addGroup(layout.createParallelGroup(gac)
			  .addComponent(jl)
			  .addComponent(aka)
				      );
	}
	vgroup.addGap(2);
	// now add the groups to the main layout
	layout.setHorizontalGroup(
		layout.createSequentialGroup()
		.addGap(6)
		.addGroup(leftGroup)
		.addGap(2)
		.addGroup(rightGroup)
		.addGap(2)
	);
	layout.setVerticalGroup(vgroup);

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
