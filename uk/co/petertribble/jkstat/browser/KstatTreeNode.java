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

import javax.swing.tree.*;
import java.util.Map;
import java.util.HashMap;
import uk.co.petertribble.jkstat.api.Kstat;

/**
 * Represent a node in the Kstat tree.
 *
 * @author Peter Tribble
 */
public class KstatTreeNode extends DefaultMutableTreeNode {

    private boolean isleaf;
    private Map <String, KstatTreeNode> nodeMap;

    /**
     * Constructs a KstatTreeNode object. Because it's passed a Map, which
     * contains the Kstat hierarchy, this creates a top-level node.
     *
     * @param s the name of the node
     * @param m a Map containing a Kstat hierarchy
     */
    public KstatTreeNode(String s, Map m) {
	setUserObject(s);
	nodeMap = new HashMap <String, KstatTreeNode> ();
	for (Object o : m.keySet()) {
	    String ss = (String) o;
	    Object oo = m.get(o);
	    if (oo instanceof Map) {
		addNode(ss, new KstatTreeNode(ss, (Map) oo));
	    } else if (oo instanceof Kstat) {
		addNode(ss, new KstatTreeNode((Kstat) oo));
	    }
	}
    }

    /**
     * Constructs a KstatTreeNode object. Because it's passed a String,
     * this represents an intermediate node.
     *
     * @param s the name of the node
     */
    public KstatTreeNode(String s) {
	setUserObject(s);
	nodeMap = new HashMap <String, KstatTreeNode> ();
    }

    /**
     * Constructs a KstatTreeNode object. Because it's passed a Kstat
     * directly, this represents a leaf node.
     *
     * @param ks a Kstat object
     */
    public KstatTreeNode(Kstat ks) {
	isleaf = true;
	setUserObject(ks);
    }

    public boolean getAllowsChildren() {
	return !isleaf;
    }

    public boolean isLeaf() {
	return isleaf;
    }

    /*
     * The add and remove methods return the highest-level node which
     * changed, so that the model can call nodeStructureChanged() on it.
     *
     * This code needs checking. I'm not sure that it handles the case where
     * a new type or class is added - in other words whether it correctly deals
     * with the addition of a node directly to the root node, in which case
     * it should return the root node.
     *
     * The code is also broken in that it doesn't maintain the sorting order.
     * New nodes just get added on the end.
     */

    /**
     * Add a Kstat to the model.
     *
     * @param s the name of the node
     * @param ks the Kstat to add to the model
     *
     * @return a KstatTreeNode object that refers to the highest node in the
     * tree that changed, or null if the tree didn't change
     */
    public KstatTreeNode addKstat(String s, Kstat ks) {
	if (ks == null) {
	    return null;
	}
	if (s == null) {
	    return addKstat(ks);
	} else {
	    KstatTreeNode kn = nodeMap.get(s);
	    if (kn == null) {
		kn = new KstatTreeNode(s);
		addNode(s, kn);
	    }
	    return kn.addKstat(ks);
	}
    }

    /**
     * Remove a Kstat from the model.
     *
     * @param s the name of the node
     * @param ks the Kstat to be removed from the model
     *
     * @return a KstatTreeNode object that refers to the highest node in the
     * tree that changed, or null if the tree didn't change
     */
    public KstatTreeNode removeKstat(String s, Kstat ks) {
	if (ks == null) {
	    return null;
	}
	if (s == null) {
	    return removeKstat(ks);
	} else {
	    KstatTreeNode kn = nodeMap.get(s);
	    if (kn == null) {
		return null;
	    }
	    KstatTreeNode knr = kn.removeKstat(ks);
	    if (kn.isEmpty()) {
		removeNode(s);
		return this;
	    }
	    return knr;
	}
    }

    /*
     * Add a kstat to the model. This assumes that any top-level layer of the
     * hierarchy has been stripped off. This only gets called from the public
     * addKstat() method, so no need to check if we're passed null.
     */
    private KstatTreeNode addKstat(Kstat ks) {
	KstatTreeNode kn = new KstatTreeNode(ks);
	/*
	 * Get the child node corresponding to the kstat module. If there isn't
	 * one, create a new one and add it to the tree.
	 */
	String module = ks.getModule();
	KstatTreeNode knm = getChild(module);
	if (knm == null) {
	    knm = new KstatTreeNode(module);
	    addNode(module, knm);
	}
	/*
	 * Get the child node corresponding to the kstat instance. If there
	 * isn't one, create a new one and add it to the tree.
	 */
	String instance = ks.getInstance();
	KstatTreeNode kni = knm.getChild(instance);
	if (kni == null) {
	    kni = new KstatTreeNode(instance);
	    knm.addNode(instance, kni);
	}
	/*
	 * Check whether this node already exists, If so, we take no
	 * action, returning null.
	 */
	String name = ks.getName();
	if (kni.getChild(name) != null) {
	    return null;
	}
	/*
	 * We really do have to add a new node.
	 */
	kni.addNode(name, kn);
	return knm;
    }

    /**
     * Remove a Kstat from the model. This assumes that any top-level layer of
     * the hierarchy has been stripped off. This only gets called from the
     * public removeKstat() method, so no need to check if we're passed null.
     */
    private KstatTreeNode removeKstat(Kstat ks) {
	/*
	 * Get the child node corresponding to the kstat module. If there isn't
	 * one, there's nothing to do.
	 */
	String module = ks.getModule();
	KstatTreeNode knm = getChild(module);
	if (knm == null) {
	    return null;
	}
	/*
	 * Get the child node corresponding to the kstat instance. If there
	 * isn't one, there's nothing to do.
	 */
	String instance = ks.getInstance();
	KstatTreeNode kni = knm.getChild(instance);
	if (kni == null) {
	    return null;
	}
	/*
	 * If there isn't a child node corresponding to the kstat name, then
	 * there's nothing to do.
	 */
	String name = ks.getName();
	if (kni.getChild(name) == null) {
	    return null;
	}
	/*
	 * OK, remove the kstat node.
	 */
	kni.removeNode(name);
	/*
	 * Now we walk back up the tree, removing empty nodes. We then walk
	 * back down the tree to find the highest node that changed, and
	 * return that node to the caller.
	 *
	 * If that was the last kstat, remove the instance as well
	 */
	if (kni.isEmpty()) {
	    knm.removeNode(instance);
	}
	/*
	 * If that was the last instance, remove the module as well.
	 */
	if (knm.isEmpty()) {
	    removeNode(module);
	    return this;
	}
	/*
	 * We didn't remove the module node. If the instance node is empty
	 * then it would have been removed above so we return the module node.
	 */
	if (kni.isEmpty()) {
	    return knm;
	}
	/*
	 * Nothing else changed, just return the instance node.
	 */
	return kni;
    }

    /*
     * Get a matching child node.
     */
    private KstatTreeNode getChild(String s) {
	return nodeMap.get(s);
    }

    /*
     * Remove a node from the tree and the node map.
     */
    private void removeNode(String s) {
	remove(getChild(s));
	nodeMap.remove(s);
    }

    /*
     * Add a node to the tree and the node map.
     */
    private void addNode(String s, KstatTreeNode kn) {
	add(kn);
	nodeMap.put(s, kn);
    }

    /*
     * Detect if we're empty.
     */
    private boolean isEmpty() {
	return nodeMap.isEmpty();
    }
}
