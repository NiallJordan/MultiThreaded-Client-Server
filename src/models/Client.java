package models;

import java.awt.EventQueue;


import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JScrollPane;


public class Client {

	//==========FIELDS==========\\
	private JFrame frmClient;
	private JTextField stud_id_field;

	private JTextField radiusTextField;
	private JTextArea clientTextArea;
	private JButton submitButton;
	private JButton exitButton;
	private JButton sendButton;
	private InetAddress address;

	private String STUD_ID = "";
	//	private int SID = 0;
	//	private String FNAME = "";
	//	private  String SNAME = "";
	//	private  int TOT_REQ = 0;



	//==========IO STREAMS==========\\
	private DataOutputStream toServer;
	private DataInputStream fromServer;

	//	private Connection conn;


	//==========LAUNCH APPLICATION==========\\
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client window = new Client();
					window.frmClient.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	//==========CONSTRUCTOR==========\\
	/**
	 * Client Constructor that uses the initializes function to create the frame/GUI as 
	 * well as establishing the client socket to connect to the server.
	 */
	public Client() {
		initialize();

		try {
			// Create a socket to connect to the server
			@SuppressWarnings("resource")
			Socket socket = new Socket("localhost", 8000);
			clientTextArea.append("Connected at " + new Date() + '\n');

			// Create an input stream to receive data from the server
			fromServer = new DataInputStream(socket.getInputStream());

			// Create an output stream to send data to the server
			toServer = new DataOutputStream(socket.getOutputStream());

			//The IP address of the client
			address = InetAddress.getLocalHost();
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}


	//==========METHODS==========\\
	/**
	 * Initialize method for frame components
	 */
	private void initialize() {

		frmClient = new JFrame();
		frmClient.setTitle("Client");
		frmClient.setBounds(100, 100, 530, 430);
		frmClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmClient.getContentPane().setLayout(null);

		JPanel loginPanel = new JPanel();
		loginPanel.setBackground(Color.LIGHT_GRAY);
		loginPanel.setBounds(0, 0, 514, 137);
		frmClient.getContentPane().add(loginPanel);
		loginPanel.setLayout(null);

		JLabel headerLabel = new JLabel("Multithreaded Client/Server - User Authentication");
		headerLabel.setFont(new Font("Arial Black", Font.BOLD, 11));
		headerLabel.setBounds(101, 11, 331, 23);
		loginPanel.add(headerLabel);
		headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

		stud_id_field = new JTextField();
		stud_id_field.setBounds(212, 61, 194, 31);
		loginPanel.add(stud_id_field);
		stud_id_field.setHorizontalAlignment(SwingConstants.CENTER);
		stud_id_field.setColumns(10);

		submitButton = new JButton("Submit");
		submitButton.setBounds(222, 103, 76, 23);
		loginPanel.add(submitButton);

		JLabel STUD_IDLabel = new JLabel("Enter STUD_ID: ");
		STUD_IDLabel.setBounds(115, 65, 99, 23);
		loginPanel.add(STUD_IDLabel);

		JPanel aocPanel = new JPanel();
		aocPanel.setBounds(0, 136, 514, 255);
		frmClient.getContentPane().add(aocPanel);
		aocPanel.setLayout(null);

		JPanel inputPanel = new JPanel();
		inputPanel.setBounds(0, 0, 513, 23);
		aocPanel.add(inputPanel);
		inputPanel.setLayout(new GridLayout(0, 3, 0, 0));

		sendButton = new JButton("Send");
		sendButton.setEnabled(false);
		inputPanel.add(sendButton);

		radiusTextField = new JTextField();
		radiusTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		radiusTextField.setText("Enter Radius");
		radiusTextField.setEnabled(false);

		inputPanel.add(radiusTextField);
		radiusTextField.setColumns(10);

		exitButton = new JButton("Exit");
		inputPanel.add(exitButton);

		JScrollPane scroll = new JScrollPane();
		scroll.setBounds(10, 34, 494, 210);
		aocPanel.add(scroll);

		clientTextArea = new JTextArea();
		clientTextArea.setEditable(false);
		scroll.setViewportView(clientTextArea);

		submitButton.addActionListener(new loginListener());
		exitButton.addActionListener(new exitListener());
		sendButton.addActionListener(new aocListener());
		submitButton.setEnabled(false);
		stud_id_field.getDocument().addDocumentListener(new studListener());
	}

	//===========LISTENERS=============\\
	/**
	 * Login listener applied to the loginButton. This class notifies user of a login.
	 */
	public class loginListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			//Get text from field
			STUD_ID = stud_id_field.getText();
			try{
				//Send Login message to Server
				toServer.writeUTF("Login");

				//Send STUD_ID from field to server
				toServer.writeUTF(STUD_ID);
				toServer.flush();

				//Read message back from Server
				String loginSuccess = fromServer.readUTF();

				//If message contains sorry, notify client that is an incorrect login, else proceed.
				if(loginSuccess.contains("Success")){
					//Welcome Message to Student
					clientTextArea.append("Welcome User: "+ STUD_ID + ", you are now connected to the Server \n");
					clientTextArea.append("Please enter the Radius of the Circle \n \n");

					//Disable login fields
					stud_id_field.setEnabled(false);
					submitButton.setEnabled(false);

					//Enable radius fields
					radiusTextField.setEnabled(true);
					sendButton.setEnabled(true);
				}else if (loginSuccess.contains("Sorry")) {
					clientTextArea.append("Sorry " + STUD_ID + ". You are not a registered student. Try again or Exit \n");
				}
			}catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	/**
	 * Stud listener for text field, if text is added to field the button is enabled.
	 * 
	 * @author niall
	 *
	 */
	private class studListener implements DocumentListener{

		public void changedUpdate(DocumentEvent e) {
			studFieldChanged();
		}
		public void removeUpdate(DocumentEvent e) {
			studFieldChanged();
		}
		public void insertUpdate(DocumentEvent e) {
			studFieldChanged();
		}

		public void studFieldChanged() {
			if (stud_id_field.getText().equals("")){
				submitButton.setEnabled(false);
			}
			else {
				submitButton.setEnabled(true);
			}

		}
	}

/**
 * Listener for submit radius, radius is sent, area is received.
 */
private class aocListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		try {

			// Get the radius from the text field
			double radius = 0;

			try {
				//Read in radius from the field
				radius = Double.parseDouble(radiusTextField.getText().trim());

				// Send the radius to the server
				toServer.writeUTF("Radius");
				toServer.writeUTF(String.valueOf(radius));
				toServer.flush();

				// Get area from the server
				double area = fromServer.readDouble();

				//Send HostName and IP to server
				toServer.writeUTF(address.getHostName());
				toServer.flush();
				toServer.writeUTF(address.getHostAddress());

				// Display to the text area
				clientTextArea.append("Client "+STUD_ID+": Radius is " + radius + "\n");
				clientTextArea.append("Server: Area received from the server is " + area + "\n \n");
			}catch(NumberFormatException nfe) {
				//Show JOPTION pane of error
				JOptionPane.showMessageDialog(frmClient,"Please enter a number!", "Error", JOptionPane.ERROR_MESSAGE);
			}

		} catch (IOException ex) {
			System.err.println(ex);
		}
	}
}

/**
 * Listener for exiting system
 */
private class exitListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		int result = JOptionPane.showConfirmDialog(frmClient,
				"Exit the Client?",
				"Confirm Exit", JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION) System.exit(0);		
	}
}
}
