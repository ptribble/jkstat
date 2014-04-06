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
