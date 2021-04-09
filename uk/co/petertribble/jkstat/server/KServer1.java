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

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;
import java.io.File;

/**
 * A simple kstat server using the xml-rpc WebServer class. Taken straight
 * from the Apache XML-RPC examples.
 *
 * @author Peter Tribble
 */
public class KServer1 {

    /**
     * Constructs a KServer1 object.
     *
     * @param port the port to listen on
     */
    public KServer1(int port) {
	this(new KServerConfig(port));
    }

    /**
     * Constructs a KServer1 object.
     *
     * @param ksc the configuration to be applied
     */
    public KServer1(KServerConfig ksc) {
	try {
	    WebServer webServer = new WebServer(ksc.getPort());
	    XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
	    PropertyHandlerMapping phm = new PropertyHandlerMapping();
	    phm.load(Thread.currentThread().getContextClassLoader(),
                   "properties/KServer1.properties");
	    xmlRpcServer.setHandlerMapping(phm);
	    XmlRpcServerConfigImpl serverConfig =
		(XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
	    serverConfig.setContentLengthOptional(false);

	    webServer.start();
	} catch (Exception e) {
	    System.err.println("Server failed to start!");
	}
    }

    private static void usage() {
	System.err.println("Usage: server [-p port | -f config_file]");
	System.exit(1);
    }

    /**
     * Start the server. A -p argument specifies a listener port, or
     * a -f argument specifies a configuration file. Without arguments,
     * listens on port 8080.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
	if (args.length == 0) {
	    new KServer1(8080);
	} else if (args.length == 2) {
	    if ("-p".equals(args[0])) {
		try {
		    new KServer1(new KServerConfig(Integer.parseInt(args[1])));
		} catch (NumberFormatException nfe) {
		    usage();
		}
	    } else if ("-f".equals(args[0])) {
		File f = new File(args[1]);
		if (f.exists()) {
		    new KServer1(new KServerConfig(f));
		} else {
		    usage();
		}
	    } else {
		usage();
	    }
	} else {
	    usage();
	}
    }
}
