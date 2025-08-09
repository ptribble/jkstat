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

import java.awt.event.*;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.*;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import uk.co.petertribble.jkstat.gui.KstatResources;

/**
 * A graphical panel that prompts the user for details of a remote JKstat
 * server.
 *
 * @author Peter Tribble
 */
public final class KBrowseDialog implements ActionListener {

    private KClientConfig kcc;

    private JComboBox<String> protobox;
    private JComboBox<String> servicebox;
    private JTextField ufield;
    private JPasswordField pfield;
    private JCheckBox authbox;
    private Map<String, String> serviceMap;
    private int dialogStatus;

    static class BrowseListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
            System.out.println("Service added: " + event.getInfo());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            System.out.println("Service removed: " + event.getInfo());
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            System.out.println("Service resolved: " + event.getInfo());
        }
    }

    /**
     * Create a dialog for the user to choose an available server.
     */
    public KBrowseDialog() {
	serviceMap = new HashMap<>();
	try (JmDNS jmdns =
	     JmDNS.create(InetAddress.getByAddress(new byte[]{0, 0, 0, 0}))) {

	    jmdns.addServiceListener("_jkstat._tcp.local.",
				     new BrowseListener());
	    ServiceInfo[] serviceInfos = jmdns.list("_jkstat._tcp.local.");
	    /*
	     * We'll present the user with a list of names to select from,
	     * which is the key in this map, and the value is the URL that
	     * we'll need to connect to for that name.
	     */
	    for (ServiceInfo info : serviceInfos) {
		for (String srvurl : info.getURLs()) {
		    serviceMap.put(info.getName(), srvurl);
		}
	    }

	    kcc = new KClientConfig();
	    /*
	     * From the top, we have:
	     *  - dropdown from list of services
	     *  - dropdown menu for protocol
	     *  - tickbox for authentication
	     *  - if authenticated prompt for username
	     *  - if authenticated prompt for password
	     *  - Connect and Cancel buttons
	     */
	    servicebox = new JComboBox<>(new Vector<>(serviceMap.keySet()));
	    protobox = new JComboBox<>(KClientConfig.PROTOCOLS);
	    protobox.setSelectedIndex(KClientConfig.CLIENT_XMLRPC);
	    ufield = new JTextField();
	    pfield = new JPasswordField();
	    authbox = new JCheckBox(KstatResources.getString("CLIENT.AUTHQ"));
	    authbox.setSelected(false);
	    authbox.addActionListener(this);
	    ufield.setEditable(false);
	    pfield.setEditable(false);

	    JPanel qpan = new JPanel();
	    qpan.setLayout(new BoxLayout(qpan, BoxLayout.PAGE_AXIS));

	    JPanel qpan0 = new JPanel(new GridLayout(0, 2));
	    qpan0.add(new JLabel(KstatResources.getString("CLIENT.SERVICE")));
	    qpan0.add(servicebox);
	    qpan.add(qpan0);

	    JPanel qpan2 = new JPanel(new GridLayout(0, 2));
	    qpan2.add(new JLabel(KstatResources.getString("CLIENT.PROTOCOL")));
	    qpan2.add(protobox);
	    qpan.add(qpan2);

	    qpan.add(authbox);

	    JPanel qpan3 = new JPanel(new GridLayout(0, 2));
	    qpan3.add(new JLabel(KstatResources.getString("CLIENT.USERNAME")));
	    qpan3.add(ufield);
	    qpan3.add(new JLabel(KstatResources.getString("CLIENT.PASSWORD")));
	    qpan3.add(pfield);
	    qpan.add(qpan3);

	    String[] options =  {KstatResources.getString("CLIENT.CONNECT"),
			KstatResources.getString("CLIENT.CANCEL")};
	    dialogStatus = JOptionPane.showOptionDialog(null, qpan,
				KstatResources.getString("CLIENT.DETAIL"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null, options, options[0]);
	} catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Return the actual client configuration data based on what the user has
     * entered.
     *
     * @return a KClientConfig populated based on user input
     */
    @SuppressWarnings("deprecation")
    public KClientConfig getConfig() {
	if (dialogStatus == JOptionPane.OK_OPTION) {
	    kcc.setServerURL(serviceMap.get(servicebox.getSelectedItem()));
	}
	kcc.setProtocol(protobox.getSelectedIndex());
	if (authbox.isSelected()) {
	    kcc.setUser(ufield.getText());
	    kcc.setPass(pfield.getText());
	}
	return kcc;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == authbox) {
	    ufield.setEditable(authbox.isSelected());
	    pfield.setEditable(authbox.isSelected());
	}
    }
}
