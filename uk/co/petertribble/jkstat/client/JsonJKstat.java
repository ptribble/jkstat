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

import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.parse.JSONParser;

/**
 * An implementation of the JKstat class that retrieves data from a remote
 * JKstat server via a RESTful API.
 *
 * @author Peter Tribble
 */
public final class JsonJKstat extends JKstat {

    private JKhttpClient client;

    /**
     * Constructs a JsonJKstat object.
     *
     * @param kcc the configuration specifying how to contact the server
     */
    public JsonJKstat(final KClientConfig kcc) {
	super();
	client = new JKhttpClient(kcc);
    }

    @Override
    public Kstat getKstatObject(final String module, final int inst,
				final String name) {
	try {
	    return JSONParser.getKstat(client.execute("get",
			new String[] {module, Integer.toString(inst), name}));
	} catch (Exception e) {
	    throw new KstatException("JsonJKstat getKstatObject failed", e);
	}
    }

    @Override
    public int getKCID() {
	try {
	    chainid = Integer.parseInt(client.execute("getkcid"));
	} catch (Exception e) {
	    throw new KstatException("JsonJKstat getKCID failed", e);
	}
	return chainid;
    }

    @Override
    public int enumerate() {
	try {
	    kstats = JSONParser.getKstats(client.execute("list"));
	} catch (Exception e) {
	    throw new KstatException("JsonJKstat enumerate failed", e);
	}
	return chainid;
    }

    /**
     * Gets the time, as the number of milliseconds since January 1, 1970,
     * 00:00:00 GMT, associated with this JKstat object. For a JsonJKstat,
     * always returns the current time.
     */
    @Override
    public long getTime() {
	return System.currentTimeMillis();
    }
}
