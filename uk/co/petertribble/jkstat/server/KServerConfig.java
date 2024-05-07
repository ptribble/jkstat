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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.File;
import java.util.Map;

/**
 * Hold the configuration of a JKstat server.
 *
 * @author Peter Tribble
 */
public class KServerConfig {

    private int port = 8080;
    private boolean registermdns;
    private String listenAddress;

    /**
     * Construct a default KServerConfig. The default port is set to 8080
     */
    public KServerConfig() {
    }

    /**
     * Configure a KServerConfig from a configuration file.
     *
     * @param f the file to be read containing the configuration
     */
    public void parseConfig(File f) {
	if (f.exists()) {
	    Map <String, String> m = JumbleUtils.stringToPropMap(
					JumbleFile.getStringContents(f), "\n");
	    try {
		port = Integer.parseInt(m.get("Port"));
	    } catch (NumberFormatException nfe) {
		port = 0;
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
     * Returns the hostname of the system this server is running on.
     *
     * @return the hostname of the system this server is running on
     */
    public String getHostname() {
	try {
	    return InetAddress.getLocalHost().getHostName();
	} catch (UnknownHostException uhe) {
	    return "unknown";
	}
    }

    /**
     * Returns the IP address this server should listen on. By default,
     * listen on 0.0.0.0 (all addresses).
     *
     * @return the InetAddress this server should listen on
     */
    public InetAddress getInetAddress() {
	/*
	 * Shenanigans because this method is declared to throw an exception
	 * even though it really can't for this usage. If we don't assign it
	 * to null initially then we get a compile error that it might not
	 * have been initialized.
	 */
	InetAddress ia = null;
	try {
	    /*
	     * we can't use getLocalHost() because it might (and often does)
	     * really resolve to localhost.
	     */
	    ia = InetAddress.getByAddress(new byte[]{0,0,0,0});
	} catch (UnknownHostException uhe) {}
	return ia;
    }

    /**
     * Returns true if the server should register in mdns, so it's
     * discoverable on a network.
     *
     * @return true if the server should register itself in mdns
     */
    public boolean getRegister() {
	return registermdns;
    }

    /**
     * Specify whether the server should register in mdns, so it's
     * discoverable on a network.
     *
     * @param registermdns a boolean specifying if the server should
     * register itself in mdns
     */
    void setRegister(boolean registermdns) {
	this.registermdns = registermdns;
    }

    /**
     * Get the port to listen on.
     *
     * @return the port number
     */
    public int getPort() {
	return port;
    }

    /**
     * Set the port to listen on.
     *
     * @param port the port number
     */
    void setPort(int port) {
	this.port = port;
    }
}
