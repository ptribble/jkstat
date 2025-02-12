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

package uk.co.petertribble.jkstat.client;

import java.util.Date;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.parse.JSONParser;
import org.apache.xmlrpc.XmlRpcException;

/**
 * An implementation of the JKstat class that retrieves data from a remote
 * JKstat server via XML-RPC.
 *
 * @author Peter Tribble
 */
public class XmlRpcJKstat extends JKstat {

    private JKstatClient client;

    /**
     * Constructs a XmlRpcJKstat object.
     *
     * @param kcc the configuration specifying how to contact the server
     */
    public XmlRpcJKstat(KClientConfig kcc) {
	super();
	client = new JKstatClient(kcc);
    }

    @Override
    public Kstat getKstatObject(String module, int inst, String name) {
	try {
	    return JSONParser.getKstat((String) client.execute("kstat",
				new Object[] {module, inst, name}));
	} catch (XmlRpcException e) {
	    throw new KstatException("XmlRpcJKstat getKstatObject failed", e);
	}
    }

    @Override
    public int getKCID() {
	try {
	    chainid = (Integer) client.execute("getKCID");
	} catch (XmlRpcException e) {
	    throw new KstatException("XmlRpcJKstat getKCID failed", e);
	}
	return chainid;
    }

    @Override
    public int enumerate() {
	try {
	    kstats = JSONParser.getKstats(
					(String) client.execute("listKstats"));
	} catch (XmlRpcException e) {
	    throw new KstatException("XmlRpcJKstat enumerate failed", e);
	}
	return chainid;
    }

    /**
     * Gets the time, as the number of milliseconds since January 1, 1970,
     * 00:00:00 GMT, associated with this JKstat object. For a XmlRpcJKstat,
     * always returns the current time.
     */
    @Override
    public long getTime() {
	return new Date().getTime();
    }
}
