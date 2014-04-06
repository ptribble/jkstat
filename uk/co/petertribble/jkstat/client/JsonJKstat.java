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

package uk.co.petertribble.jkstat.client;

import java.util.Date;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.parse.JSONParser;

/**
 * An implementation of the JKstat class that retrieves data from a remote
 * JKstat server via a RESTful API.
 *
 * @author Peter Tribble
 */
public class JsonJKstat extends JKstat {

    private JKhttpClient client;

    /**
     * Constructs a JsonJKstat object.
     *
     * @param kcc the configuration specifying how to contact the server
     */
    public JsonJKstat(KClientConfig kcc) {
	super();
	client = new JKhttpClient(kcc);
    }

    @Override
    public Kstat getKstatObject(String module, int inst, String name) {
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
	return new Date().getTime();
    }
}
