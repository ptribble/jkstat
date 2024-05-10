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

import java.io.IOException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;

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
    public JKhttpClient(KClientConfig kcc) {
	baseURL = kcc.remoteURL();
	if (!baseURL.endsWith("/")) {
	    baseURL = baseURL + "/";
	}
	httpclient = HttpClients.createDefault();
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
    public String execute(String method)  throws IOException {
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
    public String execute(String method, String[] args) throws IOException {
	StringBuilder sb = new StringBuilder();
	sb.append(method);
	for (String s : args) {
	    sb.append("/").append(s);
	}
	return doGet(sb.toString());
    }

    private String doGet(String request) throws IOException {
	return httpclient.execute(new HttpGet(baseURL + request),
						new BasicResponseHandler());
    }
}
