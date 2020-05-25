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

    private KstatTreeNode rootNode;

    /**
     * Construct a new KstatTreeModel.
     *
     * @param m the Map of Kstats
     */
    @SuppressWarnings("rawtypes")
    public KstatTreeModel(Map m) {
	this(new KstatTreeNode("Kstats", m));
    }

    /*
     * Construct a new KstatTreeModel.
     */
    private KstatTreeModel(KstatTreeNode rootNode) {
	super(rootNode);
	this.rootNode = rootNode;
    }

    /**
     * Add a Kstat to the right place in the tree.
     *
     * @param s the name of the node
     * @param ks the Kstat to add to the tree
     */
    public void addKstat(String s, Kstat ks) {
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
    public void removeKstat(String s, Kstat ks) {
	KstatTreeNode kn = rootNode.removeKstat(s, ks);
	if (kn != null) {
	    nodeStructureChanged(kn);
	}
    }
}
