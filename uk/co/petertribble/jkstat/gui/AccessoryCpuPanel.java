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
import java.awt.*;
import java.awt.geom.Rectangle2D;
import javax.swing.SwingConstants;

/**
 * An accessory panel that graphically represents cpu activity in the
 * style of xcpustate.
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class AccessoryCpuPanel extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    private long luser;
    private long lsys;
    private long lwait;
    private long lidle;
    private double duser;
    private double dsys;
    private double dwait;
    private double didle;

    private int orientation;

    /**
     * Create a panel showing the cpu activity.
     *
     * @param ks a cpu_stat {@code Kstat}
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     */
    public AccessoryCpuPanel(Kstat ks, int interval, JKstat jkstat) {
	this(ks, interval, jkstat, SwingConstants.HORIZONTAL);
    }

    /**
     * Create a panel showing the cpu activity.
     *
     * @param ks a cpu_stat {@code Kstat}
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     * @param orientation the desired orientation of the accessory, which
     * should be either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL
     */
    public AccessoryCpuPanel(Kstat ks, int interval, JKstat jkstat,
			int orientation) {
	super(ks, interval, jkstat);
	this.orientation = orientation;

	if (orientation == SwingConstants.VERTICAL) {
	    setMinimumSize(new Dimension(12, 48));
	    setPreferredSize(new Dimension(12, 48));
	} else {
	    setMinimumSize(new Dimension(64, 32));
	    setPreferredSize(new Dimension(64, 32));
	}

	updateAccessory();

	startLoop();
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	long nuser = ks.longData("user");
	long nsys = ks.longData("kernel");
	long nwait = ks.longData("wait");
	long nidle = ks.longData("idle");

	duser = nuser - luser;
	dsys = nsys - lsys;
	dwait = nwait - lwait;
	didle = nidle - lidle;

	repaint();

	luser = nuser;
	lsys = nsys;
	lwait = nwait;
	lidle = nidle;
    }

    @Override
    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	Dimension d = getSize();

	double h = d.height;
	double w = d.width;
	double x = 0.0d;
	double dscale = ((orientation == SwingConstants.VERTICAL) ? h : w)
	    / (duser + dsys + dwait + didle);
	double dx = dscale * didle;
	g2.setPaint(Color.BLUE);
	if (orientation == SwingConstants.VERTICAL) {
	    g2.fill(new Rectangle2D.Double(0.0d, x, w, dx));
	} else {
	    g2.fill(new Rectangle2D.Double(x, 0.0d, dx, h));
	}
	x += dx;
	dx = dscale * dwait;
	g2.setPaint(Color.RED);
	if (orientation == SwingConstants.VERTICAL) {
	    g2.fill(new Rectangle2D.Double(0.0d, x, w, dx));
	} else {
	    g2.fill(new Rectangle2D.Double(x, 0.0d, dx, h));
	}
	x += dx;
	dx = dscale * duser;
	g2.setPaint(Color.GREEN);
	if (orientation == SwingConstants.VERTICAL) {
	    g2.fill(new Rectangle2D.Double(0.0d, x, w, dx));
	} else {
	    g2.fill(new Rectangle2D.Double(x, 0.0d, dx, h));
	}
	x += dx;
	dx = dscale * dsys;
	g2.setPaint(Color.YELLOW);
	if (orientation == SwingConstants.VERTICAL) {
	    g2.fill(new Rectangle2D.Double(0.0d, x, w, dx));
	} else {
	    g2.fill(new Rectangle2D.Double(x, 0.0d, dx, h));
	}
    }
}
