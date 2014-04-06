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

import java.io.File;
import java.util.List;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import uk.co.petertribble.jkstat.api.ChartableKstat;
import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.api.SequencedJKstat;

import org.jfree.chart.ChartPanel;

/**
 * A graphical chart of a Kstat, in its own window.
 *
 * @author Peter Tribble
 */
public class KstatBaseChartFrame extends JFrame implements ActionListener {

    /**
     * A KstatBaseChart object for subclasses to use.
     */
    protected KstatBaseChart kbc;

    /**
     * A Menu item to exit.
     */
    protected JMenuItem exitItem;

    /**
     * A Menu item to save the chart.
     */
    protected JMenuItem saveItem;

    /**
     * A Menu item to set the update interval to 1s.
     */
    protected JRadioButtonMenuItem sleepItem1;

    /**
     * A Menu item to set the update interval to 2s.
     */
    protected JRadioButtonMenuItem sleepItem2;

    /**
     * A Menu item to set the update interval to 5s.
     */

    protected JRadioButtonMenuItem sleepItem5;
    /**
     * A Menu item to set the update interval to 10s.
     */
    protected JRadioButtonMenuItem sleepItem10;

    /**
     * A reference to a Jkstat object.
     */
    protected JKstat jkstat;

    /**
     * A flag to determine whether we show rates (if true) or raw values.
     */
    protected boolean showdelta;

    /**
     * Initialize the Frame.
     *
     * @param title the window title
     */
    protected void init(String title) {
	init(title, (JMenu) null);
    }

    /**
     * Initialize the Frame.
     *
     * @param title the window title
     * @param statsMenu an optional menu listing available statistics
     */
    protected void init(String title, JMenu statsMenu) {
	setTitle(title);

	setContentPane(new ChartPanel(kbc.getChart()));

	addWindowListener(new winExit());

	JMenuBar jm = new JMenuBar();
	jm.add(fileMenu());
	if (!(jkstat instanceof SequencedJKstat)) {
	    jm.add(sleepMenu());
	}
	if (statsMenu != null) {
	    jm.add(statsMenu);
	}
	setJMenuBar(jm);

	pack();
	setVisible(true);
    }

    /**
     * Create the File... Menu.
     *
     * @return the File Menu
     */
    protected JMenu fileMenu() {
	JMenu jme = new JMenu(KstatResources.getString("FILE.TEXT"));
	jme.setMnemonic(KeyEvent.VK_F);

	saveItem = new JMenuItem(KstatResources.getString("FILE.SAVEAS.TEXT"),
				KeyEvent.VK_S);
	saveItem.addActionListener(this);
	jme.add(saveItem);
	jme.addSeparator();
	exitItem = new JMenuItem(KstatResources.getString("FILE.CLOSE.TEXT"),
				KeyEvent.VK_C);
	exitItem.addActionListener(this);
	jme.add(exitItem);
	return jme;
    }

    /**
     * Create the Sleep for... Menu.
     *
     * @return the Sleep Menu
     */
    protected JMenu sleepMenu() {
	JMenu jms = new JMenu(KstatResources.getString("SLEEP.TEXT"));
	jms.setMnemonic(KeyEvent.VK_U);
	sleepItem1 = new JRadioButtonMenuItem(
				KstatResources.getString("SLEEP.1"));
	sleepItem1.addActionListener(this);
	sleepItem2 = new JRadioButtonMenuItem(
				KstatResources.getString("SLEEP.2"));
	sleepItem2.addActionListener(this);
	sleepItem5 = new JRadioButtonMenuItem(
				KstatResources.getString("SLEEP.5"), true);
	sleepItem5.addActionListener(this);
	sleepItem10 = new JRadioButtonMenuItem(
				KstatResources.getString("SLEEP.10"));
	sleepItem10.addActionListener(this);
	jms.add(sleepItem1);
	jms.add(sleepItem2);
	jms.add(sleepItem5);
	jms.add(sleepItem10);

	ButtonGroup sleepGroup = new ButtonGroup();
	sleepGroup.add(sleepItem1);
	sleepGroup.add(sleepItem2);
	sleepGroup.add(sleepItem5);
	sleepGroup.add(sleepItem10);

	return jms;
    }

    /**
     * Create the Statistics Menu, showing all available statistics for the
     * chosen ChartableKstat.
     *
     * @param cks the Kstat to show the statistics of in the menu
     * @param statistic the initial statistic, which will be checked
     *
     * @return the Statistics Menu
     */
    protected JMenu statisticsMenu(ChartableKstat cks, String statistic) {
	JMenu jmstat = new JMenu(KstatResources.getString("CHART.SHOW"));
	jmstat.setMnemonic(KeyEvent.VK_S);
	for (String stat : cks.getStatistics()) {
	    JCheckBoxMenuItem jmi =
		    new JCheckBoxMenuItem(stat, stat.equals(statistic));
	    jmi.addActionListener(this);
	    jmstat.add(jmi);
	}
	return jmstat;
    }

    /**
     * Create the Statistics Menu, showing all available statistics for the
     * chosen ChartableKstat.
     *
     * @param cks the ChartableKstat to show the statistics of in the menu
     * @param statistics the initial statistics, which will be checked
     *
     * @return the Statistics Menu
     */
    protected JMenu statisticsMenu(ChartableKstat cks,
				List <String> statistics) {
	JMenu jmstat = new JMenu(KstatResources.getString("CHART.SHOW"));
	jmstat.setMnemonic(KeyEvent.VK_S);
	for (String stat : cks.getStatistics()) {
	    JCheckBoxMenuItem jmi =
		    new JCheckBoxMenuItem(stat, statistics.contains(stat));
	    jmi.addActionListener(this);
	    jmstat.add(jmi);
	}
	return jmstat;
    }

    class winExit extends WindowAdapter {
	public void windowClosing(WindowEvent we) {
	    kbc.stopLoop();
	    dispose();
	}
    }

    /**
     * Saves the current chart as an image in png format. The user can select
     * the filename, and is asked to confirm the overwrite of an existing file.
     */
    public void saveImage() {
	JFileChooser fc = new JFileChooser();
	if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
	    File f = fc.getSelectedFile();
	    if (f.exists()) {
		int ok = JOptionPane.showConfirmDialog(this,
		    KstatResources.getString("SAVEAS.OVERWRITE.TEXT") + " "
				+ f.toString(),
		    KstatResources.getString("SAVEAS.CONFIRM.TEXT"),
		    JOptionPane.YES_NO_OPTION);
		if (ok != JOptionPane.YES_OPTION) {
		    return;
		}
	    }
	    BufferedImage bi = kbc.getChart().createBufferedImage(500, 300);
	    try {
		ImageIO.write(bi, "png", f);
		/*
		 * According to the API docs this should throw an IOException
		 * on error, but this doesn't seem to be the case. As a result
		 * it's necessary to catch exceptions more generally. Even this
		 * doesn't work properly, but at least we manage to convey the
		 * message to the user that the write failed, even if the
		 * error itself isn't handled.
		 */
	    } catch (Exception ioe) {
		JOptionPane.showMessageDialog(this, ioe.toString(),
			KstatResources.getString("SAVEAS.ERROR.TEXT"),
			JOptionPane.ERROR_MESSAGE);
	    }
	}
    }

    /**
     * Set the update delay of the chart.
     *
     * @param delay the desired update delay, in seconds
     */
    protected void setDelay(int delay) {
	kbc.setDelay(delay);
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == exitItem) {
	    kbc.stopLoop();
	    setVisible(false);
	    dispose();
	} else if (e.getSource() == saveItem) {
	    saveImage();
	} else if (e.getSource() == sleepItem1) {
	    setDelay(1);
	} else if (e.getSource() == sleepItem2) {
	    setDelay(2);
	} else if (e.getSource() == sleepItem5) {
	    setDelay(5);
	} else if (e.getSource() == sleepItem10) {
	    setDelay(10);
	} else if (e.getSource() instanceof JCheckBoxMenuItem) {
	    JCheckBoxMenuItem jmi = (JCheckBoxMenuItem) e.getSource();
	    String stat = jmi.getText();
	    if (jmi.isSelected()) {
		kbc.addStatistic(stat);
	    } else {
		kbc.removeStatistic(stat);
	    }
	}
    }
}
