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

import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.gui.KstatResources;
import uk.co.petertribble.jkstat.browser.KstatBrowser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.*;

/**
 * Creates a Menu, allowing access to a kstat browser and a number of
 * useful utilities from other applications.
 *
 * @author Peter Tribble
 */
public class KstatToolsMenu extends JMenu implements ActionListener {

    private JMenuItem kstatbrowserItem;
    private JMenuItem jcpustateItem;
    private JMenuItem jiostatItem;
    private JMenuItem jmpstatItem;
    private JMenuItem jnetloadItem;
    private JMenuItem jnfsstatItem;
    private JKstat jkstat;

    /**
     * Constructs a KstatToolsMenu object.
     *
     * @param jkstat a JKstat object
     */
    public KstatToolsMenu(JKstat jkstat) {
	this(jkstat, true);
    }

    /**
     * Constructs a KstatToolsMenu object.
     *
     * @param jkstat a JKstat object
     * @param showbrowser a boolean -- true if the kstat browser is to be shown
     */
    public KstatToolsMenu(JKstat jkstat, boolean showbrowser) {
	super(KstatResources.getString("DEMO.TEXT"));
	setMnemonic(KeyEvent.VK_D);

	this.jkstat = jkstat;

	if (showbrowser) {
	    kstatbrowserItem = new JMenuItem(
			KstatResources.getString("BROWSERUI.NAME.TEXT"));
	    add(kstatbrowserItem);
	    kstatbrowserItem.addActionListener(this);
	    addSeparator();
	}
	jcpustateItem = new JMenuItem("Cpustate");
	add(jcpustateItem);
	jcpustateItem.addActionListener(this);
	jiostatItem = new JMenuItem("iostat");
	add(jiostatItem);
	jiostatItem.addActionListener(this);
	jmpstatItem = new JMenuItem("mpstat");
	add(jmpstatItem);
	jmpstatItem.addActionListener(this);
	jnetloadItem = new JMenuItem("Network load");
	add(jnetloadItem);
	jnetloadItem.addActionListener(this);
	jnfsstatItem = new JMenuItem("nfsstat");
	add(jnfsstatItem);
	jnfsstatItem.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == kstatbrowserItem) {
	    new KstatBrowser();
	}
	if (e.getSource() == jcpustateItem) {
	    new JCpuState(jkstat, false);
	}
	if (e.getSource() == jiostatItem) {
	    new JIOstat(jkstat, false);
	}
	if (e.getSource() == jmpstatItem) {
	    new JMPstat(jkstat, false);
	}
	if (e.getSource() == jnetloadItem) {
	    new JNetLoad(jkstat, false);
	}
	if (e.getSource() == jnfsstatItem) {
	    new Jnfsstat(jkstat, false);
	}
    }
}
