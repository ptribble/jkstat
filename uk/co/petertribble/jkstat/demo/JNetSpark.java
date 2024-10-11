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
import java.awt.Dimension;
import java.awt.event.*;
import java.util.*;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.gui.*;

/**
 * A graphical display of network traffic.
 *
 * @author Peter Tribble
 */
public class JNetSpark extends JKdemo implements ActionListener {

    private static final long serialVersionUID = 1L;

    private JKstat jkstat;
    private KstatAccessorySet kas;

    // to identify menu popups
    private Map <JMenuItem, Kstat> itemMap;

    /**
     * Construct a new JNetSpark application.
     */
    public JNetSpark() {
	this(new NativeJKstat(), true);
    }

    /**
     * Construct a new JNetSpark application.
     *
     * @param jkstat a JKstat object
     * @param standalone a boolean, true if the demo is a standalone
     * application.
     */
    public JNetSpark(JKstat jkstat, boolean standalone) {
	super("JNetSpark", 1, standalone);

	this.jkstat = jkstat;

	itemMap = new HashMap <JMenuItem, Kstat> ();

	List <KstatAccessoryPanel> kaplist =
	    new ArrayList <KstatAccessoryPanel> ();

	// create a main panel
	JPanel mainPanel = new JPanel();
	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

	setContentPane(mainPanel);

	String iflabel = KstatResources.getString("NETLOAD.IF.TEXT") + " ";
	String grlabel = KstatResources.getString("NETLOAD.GRAPH.TEXT") + " ";

	// filter the kstats we need
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.setFilterClass("net");
	ksf.addFilter(":::rbytes64");
	ksf.addNegativeFilter("::mac");

	// add the kstats to the panel
	for (Kstat ks : new TreeSet <Kstat> (ksf.getKstats())) {
	    KstatAccessoryPanel kap =
		new SparkRateAccessory(ks, -1, jkstat, "rbytes64");
	    kap.setMinimumSize(new Dimension(300, 50));
	    kap.setPreferredSize(new Dimension(300, 80));
	    kap.setBorder(BorderFactory.createTitledBorder
				  (iflabel + ks.getName()));
	    mainPanel.add(kap);
	    kaplist.add(kap);

	    // popup menu for graph
	    JPopupMenu jpm = new JPopupMenu();
	    JMenuItem jmi = new JMenuItem(grlabel + ks.getName());
	    jmi.addActionListener(this);
	    jpm.add(jmi);
	    kap.addMouseListener((MouseListener) new PopupListener(jpm));
	    itemMap.put(jmi, ks);
	}

	pack();
	setVisible(true);
	kas = new KstatAccessorySet(kaplist, 1);
    }

    @Override
    public void stopLoop() {
	kas.stopLoop();
    }

    @Override
    public void setDelay(int i) {
	kas.setDelay(i);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	super.actionPerformed(e);
	Kstat ks = itemMap.get((JMenuItem) e.getSource());
	if (ks != null) {
	    new KstatChartFrame(jkstat, ks,
				Arrays.asList("rbytes64", "obytes64"));
	}
    }

    /*
     * Inner class to handle mouse popups.
     */
    static class PopupListener extends MouseAdapter {
	private JPopupMenu popup;

	public PopupListener(JPopupMenu popup) {
	    this.popup = popup;
	}

	@Override
	public void mousePressed(MouseEvent e) {
	    showPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    showPopup(e);
	}

	private void showPopup(MouseEvent e) {
	    if (e.isPopupTrigger()) {
		popup.show(e.getComponent(), e.getX(), e.getY());
	    }
	}
    }

    /**
     * Create a JNetSpark application from the command line.
     *
     * @param args Command line arguments, ignored
     */
    public static void main(String[] args) {
	new JNetSpark();
    }
}
