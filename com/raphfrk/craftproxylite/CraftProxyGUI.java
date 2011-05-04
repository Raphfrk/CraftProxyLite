package com.raphfrk.craftproxylite;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

public class CraftProxyGUI extends JFrame implements WindowListener, ActionListener {
	private static final long serialVersionUID = 1L;

	JPanel topPanel = new JPanel();
	JPanel secondPanel = new JPanel();
	JPanel combinedTop = new JPanel();
	JTextField serverName;
	JTextField portNum;
	JLabel localServerName;
	JTextField localServerPortnum;
	JLabel info;
	JButton connect;

	final Object statusTextSync = new Object();
	String statusText = "";

	final Object buttonTextSync = new Object();
	String buttonText = "Start";

	Thread serverMainThread = null;

	public JFrame main;

	MyPropertiesFile pf;

	public CraftProxyGUI() {

		pf = new MyPropertiesFile("CraftProxyLiteGUI.txt");

		pf.load();
		
		String defaultHostname = pf.getString("connect_hostname", "localhost");
		int defaultPort = pf.getInt("connect_port", 20000);
		int listenPort = pf.getInt("listen_port", 25565);

		setTitle("CraftProxyLite Local Cache Mode");
		setSize(375,200);
		setLocation(40,150);


		topPanel.setLayout(new BorderLayout());
		secondPanel.setLayout(new BorderLayout());

		serverName = new JTextField(defaultHostname, 20);
		TitledBorder border = new TitledBorder("Server Name");
		serverName.setBorder(border);
		serverName.addActionListener(this);

		portNum = new JTextField(Integer.toString(defaultPort) , 6);
		border = new TitledBorder("Port");
		portNum.setBorder(border);
		portNum.addActionListener(this);

		localServerName = new JLabel("localhost");
		border = new TitledBorder("Local Server Name");
		localServerName.setBorder(border);

		localServerPortnum = new JTextField(Integer.toString(listenPort), 6);
		border = new TitledBorder("Port");
		localServerPortnum.setBorder(border);
		localServerPortnum.addActionListener(this);

		topPanel.add(serverName, BorderLayout.CENTER);
		topPanel.add(portNum, BorderLayout.LINE_END);

		secondPanel.setLayout(new BorderLayout());
		secondPanel.add(localServerName, BorderLayout.CENTER);
		secondPanel.add(localServerPortnum, BorderLayout.LINE_END);

		combinedTop.setLayout(new BorderLayout());
		combinedTop.add(topPanel, BorderLayout.CENTER);
		combinedTop.add(secondPanel, BorderLayout.SOUTH);

		info = new JLabel();
		border = new TitledBorder("Status");
		info.setBorder(border);

		//SwingUtilities.invokeLater(paramRunnable);

		connect = new JButton(buttonText);
		connect.addActionListener(this);

		setLayout(new BorderLayout());
		add(combinedTop, BorderLayout.PAGE_START);
		add(info, BorderLayout.CENTER);
		add(connect, BorderLayout.PAGE_END);

		this.setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.addWindowListener(this);

	}

	public void safeSetStatus(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				info.setText(text);
			}
		});
	}

	public void safeSetButton(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				connect.setText(text);
				connect.updateUI();
			}
		});
	}

	public void windowClosing(WindowEvent paramWindowEvent) {
		if(serverMainThread != null) {
			serverMainThread.interrupt();
		}
	}

	public void windowOpened(WindowEvent paramWindowEvent) {
	}

	public void windowClosed(WindowEvent paramWindowEvent) {
	}

	public void windowIconified(WindowEvent paramWindowEvent) {

	}

	public void windowDeiconified(WindowEvent paramWindowEvent) {
	}

	public void windowActivated(WindowEvent paramWindowEvent) {
	}

	public void windowDeactivated(WindowEvent paramWindowEvent) {
	}

	public void actionPerformed(ActionEvent action) {
		if(action.getSource().equals(connect)) {

			try {			
				pf.setString("connect_hostname", serverName.getText());
				pf.setInt("connect_port", Integer.parseInt(portNum.getText()));
				pf.setInt("listen_port", Integer.parseInt(localServerPortnum.getText()));
				pf.save();
			} catch (NumberFormatException nfe) {
			}

			if(serverMainThread == null || !serverMainThread.isAlive()) {

				safeSetButton("Stop");

				final String distantServer = serverName.getText() + ":" + portNum.getText();
				final String localServer = localServerPortnum.getText();

				serverMainThread = new Thread(new Runnable() {

					public void run() {

						String[] args = {
								localServer,
								distantServer,
								"local_cache",
								"quiet",
								"bridge_connection"
						};

						Main.main(args, false);
					}

				});

				serverMainThread.start();

			} else {
				safeSetButton("Stopping");
				serverMainThread.interrupt();
			}
		}
	}


}
