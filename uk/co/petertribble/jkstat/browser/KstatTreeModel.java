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

package uk.co.petertribble.jkstat.browser;

import javax.swing.tree.DefaultTreeModel;
import java.util.Map;
import uk.co.petertribble.jkstat.api.Kstat;

/**
 * A TreeModel for Kstats.
 *
 * @author Peter Tribble
 */
public class KstatTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = 1L;

    /**
     * The root node of this tree.
     */
    private KstatTreeNode rootNode;

    /**
     * Construct a new KstatTreeModel.
     *
     * @param m the Map of Kstats
     */
    @SuppressWarnings("rawtypes")
    public KstatTreeModel(final Map m) {
	this(new KstatTreeNode("Kstats", m));
    }

    /*
     * Construct a new KstatTreeModel.
     */
    private KstatTreeModel(final KstatTreeNode rootNode) {
	super(rootNode);
	this.rootNode = rootNode;
    }

    /**
     * Add a Kstat to the right place in the tree.
     *
     * @param s the name of the node
     * @param ks the Kstat to add to the tree
     */
    public void addKstat(final String s, final Kstat ks) {
	KstatTreeNode kn = rootNode.addKstat(s, ks);
	if (kn != null) {
	    nodeStructureChanged(kn);
	}
    }

    /**
     * Remove a Kstat from the tree.
     *
     * @param s the name of the node
     * @param ks the Kstat to remove
     */
    public void removeKstat(final String s, final Kstat ks) {
	KstatTreeNode kn = rootNode.removeKstat(s, ks);
	if (kn != null) {
	    nodeStructureChanged(kn);
	}
    }
}
