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

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.XmlRpcException;
import java.net.MalformedURLException;

/**
 * A class providing access to a remote JKstat server over XML-RPC.
 *
 * @author Peter Tribble
 */
public class JKstatClient {

    private XmlRpcClient client;

    /**
     * Create a JKstat client that communicates with a server using the XML-RPC
     * protocol.
     *
     * @param kcc holds the configuration with details of how to contact the
     * server
     */
    public JKstatClient(KClientConfig kcc) {
	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	try {
	    config.setServerURL(kcc.getServerURL());
	} catch (MalformedURLException mue) {
	    throw new KstatException("Malformed URL.", mue);
	}
	initClient(config);
    }

    /**
     * Create a JKstat client that communicates with a server using the XML-RPC
     * protocol.
     *
     * @param config holds the configuration with details of how to contact
     * the server
     */
    public JKstatClient(XmlRpcClientConfigImpl config) {
	initClient(config);
    }

    private void initClient(XmlRpcClientConfigImpl config) {
	config.setEnabledForExtensions(true);
	client = new XmlRpcClient();
	client.setConfig(config);
    }

    /**
     * Execute the given method on a remote JKstat server.
     *
     * @param method the name of the method to execute
     *
     * @return the result of the remote method execution. The type depends
     * on the method called, and is defined by the server, although the XML-RPC
     * layer changes the return types
     *
     * @throws XmlRpcException an exception, passed up from XML-RPC if the
     * remote procedure call failed
     */
    public Object execute(String method) throws XmlRpcException {
	return client.execute("JKstatServer." + method, new Object[0]);
    }

    /**
     * Execute the given method on a remote JKstat server.
     *
     * @param method the name of the method to execute
     * @param args an array of parameters to pass as arguments to the
     * method call
     *
     * @return the result of the remote method execution. The type depends
     * on the method called, and is defined by the server, although the XML-RPC
     * layer changes the return types
     *
     * @throws XmlRpcException an exception, passed up from XML-RPC if the
     * remote procedure call failed
     */
    public Object execute(String method, Object[] args) throws XmlRpcException {
	return client.execute("JKstatServer." + method, args);
    }
}
