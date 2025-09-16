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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * A class providing access to a remote JKstat server via REST.
 *
 * @author Peter Tribble
 */
public class JKhttpClient {

    private String baseURL;
    private HttpClient httpclient;

    /**
     * Create a JKstat client that uses REST to communicate with a HTTP
     * server.
     *
     * @param kcc holds the configuration with details of how to contact the
     * server
     */
    public JKhttpClient(final KClientConfig kcc) {
	baseURL = kcc.remoteURL();
	if (!baseURL.endsWith("/")) {
	    baseURL = baseURL + "/";
	}
	httpclient = HttpClient.newHttpClient();
    }

    /**
     * Execute the given method on a remote JKstat server.
     *
     * @param method the name of the method to execute
     *
     * @return the result of the remote method execution
     *
     * @throws IOException if there was a problem communicating with the server
     */
    public String execute(final String method) throws IOException {
	return doGet(method);
    }

    /**
     * Execute the given method on a remote JKstat server.
     *
     * @param method the name of the method to execute
     * @param args an array of parameters to pass as arguments to the
     * method call
     *
     * @return the result of the remote method execution
     *
     * @throws IOException if there was a problem communicating with the server
     */
    public String execute(final String method, final String[] args)
	    throws IOException {
	StringBuilder sb = new StringBuilder();
	sb.append(method);
	for (String s : args) {
	    sb.append('/').append(s);
	}
	return doGet(sb.toString());
    }

    private String doGet(final String request) throws IOException {
	HttpRequest hrequest = HttpRequest.newBuilder()
	    .uri(URI.create(baseURL + request))
	    .build();
	try {
	    HttpResponse<String> response
		= httpclient.send(hrequest, BodyHandlers.ofString());
	    return response.body();
	} catch (InterruptedException ie) {
	    return "";
	}
    }
}
