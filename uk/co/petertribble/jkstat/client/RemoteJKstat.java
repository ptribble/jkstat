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
