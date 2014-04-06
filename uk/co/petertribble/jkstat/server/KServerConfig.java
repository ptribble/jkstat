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

package uk.co.petertribble.jkstat.server;

import uk.co.petertribble.jumble.JumbleUtils;
import uk.co.petertribble.jumble.JumbleFile;
import java.io.File;
import java.util.Map;

/**
 * Hold the configuration of a JKstat server.
 *
 * @author Peter Tribble
 */
public class KServerConfig {

    private int port;

    /**
     * Construct a KServerConfig with a specific port.
     *
     * @param port the port number to be used
     */
    public KServerConfig(int port) {
	this.port = port;
    }

    /**
     * Construct a KServerConfig from a configuration file.
     *
     * @param f the file to be read containing the configuration
     */
    public KServerConfig(File f) {
	if (f.exists()) {
	    Map <String, String> m = JumbleUtils.stringToPropMap(
					JumbleFile.getStringContents(f), "\n");
	    try {
		port = Integer.parseInt(m.get("Port"));
	    } catch (NumberFormatException nfe) {
		System.err.println("Invalid config file");
	    }
	} else {
	    System.err.println("Missing config file");
	}
    }

    /**
     * Returns true if there is adequate configuration to continue.
     * Specifically, the port must be known.
     *
     * @return true if this object is configured
     */
    public boolean isConfigured() {
	return (port != 0);
    }

    /**
     * Get the port to listen on.
     *
     * @return the port number
     */
    public int getPort() {
	return port;
    }
}
