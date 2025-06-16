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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * A class to hold the configuration details a JKstat client needs to
 * connect to a JKstat server.
 *
 * @author Peter Tribble
 */
public class KClientConfig {

    /**
     * Represents an XML-RPC client/server configuration.
     */
    public static final int CLIENT_XMLRPC = 0;

    /**
     * Represents a RESTful client/server configuration.
     */
    public static final int CLIENT_REST = 1;

    /*
     * The entries in the array below MUST match the available protocols above,
     * as KClientDialog uses it as an array, and the index then refers back
     * to the selected protocol.
     */

    /**
     * An array of the names of available client-server protocols.
     */
    public static final String[] PROTOCOLS = {"XML-RPC", "REST"};

    private String urlString;
    private String username;
    private String userpass;
    private int protocol;

    /**
     * Create an empty KClientConfig.
     */
    public KClientConfig() {
    }

    /**
     * Create a KClientConfig that is configured to connect to a given url.
     *
     * @param s the textual url of the server to connect to
     */
    public KClientConfig(String s) {
	this(s, CLIENT_XMLRPC);
    }

    /**
     * Create a KClientConfig that is configured to connect to a given url.
     *
     * @param s the textual url of the server to connect to
     * @param protocol specifies the communication protocol
     */
    public KClientConfig(String s, int protocol) {
	urlString = s;
	this.protocol = protocol;
    }

    /**
     * Returns whether this KClientConfig has enough configuration to be
     * useful.
     *
     * @return whether this KClientConfig has enough configuration to be
     * useful
     */
    public boolean isConfigured() {
	return (urlString != null) && (!"".equals(urlString));
    }

    /**
     * Return the url to connect to, as a String. If the url has been
     * explicitly passed, use that.
     *
     * @return the url to connect to, as a String
     */
    public String remoteURL() {
	return urlString;
    }

    /**
     * Set the user to connect as.
     *
     * @see #getUser
     *
     * @param username the username to connect as
     */
    public void setUser(String username) {
	this.username = username;
    }

    /**
     * Get the user to connect as.
     *
     * @see #setUser
     *
     * @return the username to connect as
     */
    public String getUser() {
	return username;
    }

    /**
     * Set the password to use for authentication.
     *
     * @see #getPass
     *
     * @param userpass the password to use for authentication
     */
    public void setPass(String userpass) {
	this.userpass = userpass;
    }

    /**
     * Get the password to use for authentication.
     *
     * @see #setPass
     *
     * @return the password to use for authentication
     */
    public String getPass() {
	return userpass;
    }

    /**
     * Set the server to connect to, expressed as a String.
     *
     * @see #getServerURL
     *
     * @param s the server to connect to
     */
    public void setServerURL(String s) {
	urlString = s;
    }

    /**
     * Set the communication protocol.
     *
     * @see #getProtocol
     *
     * @param protocol an integer representing the communication protocol
     */
    public void setProtocol(int protocol) {
	this.protocol = protocol;
    }

    /**
     * Get the communication protocol.
     *
     * @see #setProtocol
     *
     * @return an integer representing the communication protocol
     */
    public int getProtocol() {
	return protocol;
    }

    /**
     * Get the server to connect to.
     *
     * @see #setServerURL
     *
     * @throws MalformedURLException if the URL is invalid
     *
     * @return the URL of the server to connect to
     */
    public URL getServerURL() throws MalformedURLException {
	try {
	    return new URI(remoteURL()).toURL();
	} catch (URISyntaxException baduri) {
	    throw new MalformedURLException(baduri.getMessage());
	}
    }
}
