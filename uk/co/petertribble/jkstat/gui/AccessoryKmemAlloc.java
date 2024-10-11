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

package uk.co.petertribble.jkstat.gui;

import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jingle.SpringUtilities;
import javax.swing.*;
import java.text.DecimalFormat;

/**
 * An accessory to display kernel memory allocation statistics.
 */
public class AccessoryKmemAlloc extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    private DecimalFormat df = new DecimalFormat("##0.0#");

    private JProgressBar jpAlloc;
    private JProgressBar jpFree;

    private long iomax = 100;
    private long numalloc;
    private long numfree;

    /**
     * Create a panel showing memory allocation statistics that updates every
     * interval seconds.
     *
     * @param ks a kmem_alloc kstat
     * @param interval the update interval in seconds
     * @param jkstat a JKstat
     */
    public AccessoryKmemAlloc(Kstat ks, int interval, JKstat jkstat) {
	super(ks, interval, jkstat);

	jpAlloc = new JProgressBar(0, (int) iomax);
	jpFree = new JProgressBar(0, (int) iomax);
	// these will be rescaled later if need be

	jpAlloc.setStringPainted(true);
	jpFree.setStringPainted(true);

	setLayout(new SpringLayout());

	add(jpAlloc);
	add(new JLabel("alloc/s "));
	add(jpFree);
	add(new JLabel("free/s"));
	SpringUtilities.makeCompactGrid(this, 1, 4, 6, 3, 2, 2);
	updateAccessory();
	startLoop();
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	long nnumalloc = ks.longData("alloc");
	long nnumfree = ks.longData("free");
	int i = (int) ((nnumalloc-numalloc)*1000000000/snapdelta);
	int j = (int) ((nnumfree-numfree)*1000000000/snapdelta);
	while ((i > iomax) || (j > iomax)) {
	    iomax <<= 1;
	    jpAlloc.setMaximum((int) iomax);
	    jpFree.setMaximum((int) iomax);
	}
	jpAlloc.setValue(i);
	jpAlloc.setString(df.format(i));
	jpFree.setValue(j);
	jpFree.setString(df.format(j));
	numalloc = nnumalloc;
	numfree = nnumfree;
    }
}
