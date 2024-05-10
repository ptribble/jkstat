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
import java.io.IOException;
import java.io.File;
import java.net.InetAddress;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * A simple kstat server using the xml-rpc WebServer class. Taken straight
 * from the Apache XML-RPC examples.
 *
 * @author Peter Tribble
 */
public class KServer1 {

    // global so can be called at shutdown
    private JmDNS jmdns;

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
	    if (ksc.getRegister()) {
		registerService(ksc);
	    }
	} catch (Exception e) {
	    System.err.println("Server failed to start!");
	}
    }

    /*
     * Register this server in mdns, with the type "_jkstat._tcp"
     */
    private void registerService(KServerConfig ksc) {
	try {
	    jmdns = JmDNS.create(ksc.getInetAddress());
	    ServiceInfo serviceInfo = ServiceInfo.create("_jkstat._tcp.local.",
		    "JKstat/"+ksc.getHostname(),
		    ksc.getPort(),
		    "path=/");
            jmdns.registerService(serviceInfo);
	    Thread exitHook = new Thread(() -> this.unRegisterService());
	    Runtime.getRuntime().addShutdownHook(exitHook);
	    System.out.println("Service registered on "+ksc.getInetAddress());
	} catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /*
     * Called as a shutdown hook.
     */
    private void unRegisterService() {
	jmdns.unregisterAllServices();
    }

    private static void usage() {
	System.err.println("Usage: server [-m ] [-p port | -f config_file]");
	System.exit(1);
    }

    /**
     * Start the server. A -p argument specifies a listener port, default
     * 8080. A -f argument specifies a configuration file. A -m argument
     * causes the server to be registered in mdns.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
	KServerConfig ksc = new KServerConfig();
	int i = 0;
	while (i < args.length) {
	    if ("-m".equals(args[i])) {
		ksc.setRegister(true);
	    } else if ("-p".equals(args[i])) {
		if (i+1 < args.length) {
		    i++;
		    try {
			ksc.setPort(Integer.parseInt(args[i]));
		    } catch (NumberFormatException nfe) {
			usage();
		    }
		} else {
		    usage();
		}
	    } else if ("-f".equals(args[i])) {
		if (i+1 < args.length) {
		    i++;
		    File f = new File(args[i]);
		    if (f.exists()) {
			ksc.parseConfig(f);
		    } else {
			usage();
		    }
		} else {
		    usage();
		}
	    } else {
		usage();
	    }
	    i++;
	}
	new KServer1(ksc);
    }
}
