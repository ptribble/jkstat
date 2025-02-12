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

/**
 * An implementation of the JKstat class that retrieves data from a remote
 * JKstat server.
 *
 * @author Peter Tribble
 */
public class RemoteJKstat extends JKstat {

    private JKstat childJKstat;

    /**
     * Constructs a RemoteJKstat object.
     *
     * @param kcc the configuration specifying how to contact the server
     */
    public RemoteJKstat(KClientConfig kcc) {
	super();
	if (kcc.getProtocol() == KClientConfig.CLIENT_XMLRPC) {
	    childJKstat = new XmlRpcJKstat(kcc);
	} else if (kcc.getProtocol() == KClientConfig.CLIENT_REST) {
	    childJKstat = new JsonJKstat(kcc);
	} else {
	    throw new KstatException("Invalid remote protocol");
	}
    }

    @Override
    public Kstat getKstatObject(String module, int inst, String name) {
	return childJKstat.getKstatObject(module, inst, name);
    }

    @Override
    public int getKCID() {
	return childJKstat.getKCID();
    }

    @Override
    public int enumerate() {
	int i = childJKstat.enumerate();
	kstats = childJKstat.getKstats();
	return i;
    }

    /**
     * Gets the time, as the number of milliseconds since January 1, 1970,
     * 00:00:00 GMT, associated with this JKstat object. For a RemoteJKstat,
     * always returns the current time.
     */
    @Override
    public long getTime() {
	return new Date().getTime();
    }
}
