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

import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.*;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import uk.co.petertribble.jingle.SpringUtilities;
import uk.co.petertribble.jkstat.gui.KstatResources;

/**
 * A graphical panel that prompts the user for details of a remote JKstat
 * server.
 *
 * @author Peter Tribble
 */
public class KBrowseDialog implements ActionListener {

    private KClientConfig kcc;

    private JComboBox <String> protobox;
    private JComboBox <String> servicebox;
    private JTextField ufield;
    private JPasswordField pfield;
    private JCheckBox authbox;
    private Map <String, String> serviceMap;
    private int dialogStatus;

    private static class BrowseListener implements ServiceListener {
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
     * Create a dialog for the user to choose the server settings.
     *
     * @param defproto the default server protocol
     */
    public KBrowseDialog() {
	serviceMap = new HashMap <> ();
	try {
	    JmDNS jmdns = JmDNS.create(InetAddress.getByAddress(new byte[]{0,0,0,0}));
	    jmdns.addServiceListener("_jkstat._tcp.local.", new BrowseListener());
	    ServiceInfo[] serviceInfos = jmdns.list("_jkstat._tcp.local.");
	    for (ServiceInfo info : serviceInfos) {
		//System.out.println("## resolve service " + info.getName()  + " : " + info.getURL());
		//System.out.println("## property names " + info.getPropertyNames());
		serviceMap.put(info.getName(), info.getURL());
	    }

	    kcc = new KClientConfig();
	    /*
	     * From the top, we have:
	     *  - dropdown from list of services
	     *  - dropdown menu for protocol
	     *  - tickbox for authentication
	     *  - if authenticated prompt for username
	     *  - if authenticated prompt for password
	     *  - OK button, active if data filled in
	     */
	    servicebox = new JComboBox <String> (new Vector<String>(serviceMap.keySet()));
	    protobox = new JComboBox <String> (KClientConfig.PROTOCOLS);
	    protobox.setSelectedIndex(KClientConfig.CLIENT_XMLRPC);
	    ufield = new JTextField();
	    pfield = new JPasswordField();
	    authbox = new JCheckBox(KstatResources.getString("CLIENT.AUTHQ"));
	    authbox.setSelected(false);
	    authbox.addActionListener(this);
	    ufield.setEditable(false);
	    pfield.setEditable(false);
	    
	    JPanel qpanel = new JPanel();
	    qpanel.setLayout(new BoxLayout(qpanel, BoxLayout.PAGE_AXIS));

	    JPanel qpanel0 = new JPanel(new SpringLayout());
	    qpanel0.add(new JLabel(KstatResources.getString("CLIENT.SERVICE")));
	    qpanel0.add(servicebox);
	    SpringUtilities.makeCompactGrid(qpanel0, 1, 2, 6, 3, 3, 3);
	    qpanel.add(qpanel0);

	    JPanel qpanel2 = new JPanel(new SpringLayout());
	    qpanel2.add(new JLabel(KstatResources.getString("CLIENT.PROTOCOL")));
	    qpanel2.add(protobox);
	    SpringUtilities.makeCompactGrid(qpanel2, 1, 2, 6, 3, 3, 3);
	    qpanel.add(qpanel2);

	    qpanel.add(authbox);

	    JPanel qpanel3 = new JPanel(new SpringLayout());
	    qpanel3.add(new JLabel(KstatResources.getString("CLIENT.USERNAME")));
	    qpanel3.add(ufield);
	    qpanel3.add(new JLabel(KstatResources.getString("CLIENT.PASSWORD")));
	    qpanel3.add(pfield);
	    SpringUtilities.makeCompactGrid(qpanel3, 2, 2, 6, 3, 3, 3);
	    qpanel.add(qpanel3);

	    String[] options =  { KstatResources.getString("CLIENT.CONNECT"),
			KstatResources.getString("CLIENT.CANCEL") };
	    dialogStatus = JOptionPane.showOptionDialog(null, qpanel,
				KstatResources.getString("CLIENT.DETAIL"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null, options, options[0]);
	} catch (UnknownHostException e) {
            System.err.println(e.getMessage());
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
	if (dialogStatus==JOptionPane.OK_OPTION) {
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
