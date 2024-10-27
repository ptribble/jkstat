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

import javax.swing.*;
import java.awt.event.*;
import uk.co.petertribble.jingle.SpringUtilities;
import uk.co.petertribble.jkstat.gui.KstatResources;

/**
 * A graphical panel that prompts the user for details of a remote JKstat
 * server.
 *
 * @author Peter Tribble
 */
public class KClientDialog implements ActionListener {

    private KClientConfig kcc;

    private JTextField sfield;
    private JTextField portfield;
    private JComboBox <String> protobox;
    private JTextField ufield;
    private JPasswordField pfield;
    private JCheckBox authbox;
    private int dialogStatus;

    /**
     * Create a dialog for the user to choose the server settings.
     *
     * @param defproto the default server protocol
     */
    public KClientDialog(int defproto) {
	kcc = new KClientConfig();
	/*
	 * From the top, we have:
	 *  - Prompt for server name and port number
	 *  - dropdown menu for protocol
	 *  - tickbox for authentication
	 *  - if authenticated prompt for username
	 *  - if authenticated prompt for password
	 *  - Connect and Cancel buttons
	 */
	sfield = new JTextField();
	portfield = new JTextField("8080");
	protobox = new JComboBox<>(KClientConfig.PROTOCOLS);
	protobox.setSelectedIndex(defproto);
	ufield = new JTextField();
	pfield = new JPasswordField();
	authbox = new JCheckBox(KstatResources.getString("CLIENT.AUTHQ"));
	authbox.setSelected(false);
	authbox.addActionListener(this);
	ufield.setEditable(false);
	pfield.setEditable(false);

	JPanel qpanel = new JPanel();
	qpanel.setLayout(new BoxLayout(qpanel, BoxLayout.PAGE_AXIS));

	JPanel qpanel1 = new JPanel(new SpringLayout());
	qpanel1.add(new JLabel(KstatResources.getString("CLIENT.SERVER")));
	qpanel1.add(sfield);
	qpanel1.add(new JLabel(KstatResources.getString("CLIENT.PORT")));
	qpanel1.add(portfield);
	SpringUtilities.makeCompactGrid(qpanel1, 1, 4, 6, 3, 3, 3);
	qpanel.add(qpanel1);

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
    }

    /**
     * Return the actual client configuration data based on what the user has
     * entered.
     *
     * @return a KClientConfig populated based on user input
     */
    @SuppressWarnings("deprecation")
    public KClientConfig getConfig() {
	if ((dialogStatus == JOptionPane.OK_OPTION)
	        && !sfield.getText().isEmpty()) {
	    kcc.setServerURL(
		"http://" + sfield.getText() + ":" + portfield.getText());
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
