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

import javax.swing.tree.*;
import java.util.Map;
import java.util.HashMap;
import uk.co.petertribble.jkstat.api.Kstat;

/**
 * Represent a node in the Kstat tree.
 *
 * @author Peter Tribble
 */
public final class KstatTreeNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;

    private boolean nodeisleaf;
    private Map<String, KstatTreeNode> nodeMap;

    /**
     * Constructs a KstatTreeNode object. Because it's passed a Map, which
     * contains the Kstat hierarchy, this creates a top-level node.
     *
     * @param s the name of the node
     * @param m a Map containing a Kstat hierarchy
     */
    @SuppressWarnings("rawtypes")
    public KstatTreeNode(String s, Map m) {
	setUserObject(s);
	nodeMap = new HashMap<>();
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
	nodeMap = new HashMap<>();
    }

    /**
     * Constructs a KstatTreeNode object. Because it's passed a Kstat
     * directly, this represents a leaf node.
     *
     * @param ks a Kstat object
     */
    public KstatTreeNode(Kstat ks) {
	nodeisleaf = true;
	setUserObject(ks);
    }

    @Override
    public boolean getAllowsChildren() {
	return !nodeisleaf;
    }

    @Override
    public boolean isLeaf() {
	return nodeisleaf;
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

    /*
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
