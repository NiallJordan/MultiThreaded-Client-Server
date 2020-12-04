package models;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import utils.Database;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;

public class Server {

	//==========FIELDS==========\\
	private JFrame frmServer;
	private JTextArea serverTextArea;
	private Database db = new Database();
	private JButton exitButton;

	private int STUD_ID = 0;
	private int SID = 0;
	@SuppressWarnings("unused")
	private String FNAME = "";
	@SuppressWarnings("unused")
	private String SNAME = "";
	private int TOT_REQ = 0;


	//==========UTILS==========\\
	private Connection conn;
	@SuppressWarnings("unused")
	private InetAddress serverAddress;


	//==========LAUNCH APPLICATION==========\\
	public static void main(String[] args) {
		new Server();
	}


	//==========CONSTRUCTOR==========\\
	/**
	 * Server Constructor that uses the initializes function to create the frame/GUI as 
	 * well as establishing the server socket. This constructor listens for a connection and
	 * creates a new thread when it receives a request.
	 */
	public Server() {
		initialize();
		try {
			// Create a server socket
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(8000);
			serverTextArea.append("Server started on "+ new Date() + '\n');
			serverTextArea.append("Connected to the database");
			/*  Listen for a connection request
            Create new threaded Client
			 */
			while (true) {
				Socket socket = serverSocket.accept();
				myClient c = new myClient(socket);
				c.start();
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}




	//==========METHODS==========\\
	/**
	 * Initialize method for frame components
	 */
	private void initialize() {

		frmServer = new JFrame();
		frmServer.setTitle("Server");
		frmServer.setBounds(100, 100, 450, 300);
		frmServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmServer.getContentPane().setLayout(null);

		exitButton = new JButton("Exit");
		exitButton.setBounds(368, 0, 66, 261);
		frmServer.getContentPane().add(exitButton);
		exitButton.addActionListener(new exitListener());

		JScrollPane scroll = new JScrollPane();
		scroll.setBounds(10, 11, 348, 239);

		serverTextArea = new JTextArea();
		serverTextArea.setEditable(false);
		scroll.setViewportView(serverTextArea);

		frmServer.getContentPane().add(scroll);

		frmServer.setVisible(true);

	}



	//===========THREAD=============\\
	/**
	 * My Client thread, creates a thread used to connect a client to the server.
	 * 
	 * 
	 * @author Niall
	 *
	 */
	private class myClient extends Thread {
		//The socket the client is connected through
		private Socket socket;

		//The input and output streams to the client
		private DataInputStream inputFromClient;
		private DataOutputStream outputToClient;

		// The Constructor for the client
		public myClient(Socket socket) throws IOException {
			// Create data input and output streams
			inputFromClient = new DataInputStream(socket.getInputStream());
			outputToClient = new DataOutputStream(socket.getOutputStream());
		}

		/**
		 * The method that runs when the thread starts
		 */
		public void run() {
			try {
				//The IP address of the client
				serverAddress = InetAddress.getLocalHost();
				conn = db.getConnection();
				while(true) {
					//Receive message from server
					String clientMessage = inputFromClient.readUTF();
					try {

						//If message received from
						if(clientMessage.contains("Login")){
							//Receive the new message (STUD_ID) from Client and parse as INT
							clientMessage = inputFromClient.readUTF();
							STUD_ID = Integer.parseInt(clientMessage);

							//SQL Query to select student from database and set to variables.
							try {
								String stmt = "SELECT * FROM students WHERE STUD_ID = ?";
								PreparedStatement ps = conn.prepareStatement(stmt);
								ps.setInt(1, STUD_ID);

								ResultSet rs = ps.executeQuery();
								int count = 0;

								while (rs.next()) {
									count++;
									SID = rs.getInt("SID");
									FNAME = rs.getString("FNAME");
									SNAME = rs.getString("SNAME");
									TOT_REQ = rs.getInt("TOT_REQ");
								}
								rs.close();

								/**
								 * If student record has been received, send success message to client and 
								 * update TOT_REQ.
								 * Else send message back to client saying unknown user/user does not exist.
								 */
								if (count == 1) {
									serverTextArea.append("Server: Client "+STUD_ID +" is connected \n" );
									outputToClient.writeUTF("Success");

									try {
										int updateTOT_REQ = TOT_REQ + 1;
										String updatestmt = "UPDATE students SET TOT_REQ =? WHERE STUD_ID = ?";
										PreparedStatement updatePS = conn.prepareStatement(updatestmt);
										updatePS.setInt(1, updateTOT_REQ);
										updatePS.setInt(2, STUD_ID);
										int countUpdate = updatePS.executeUpdate();
										updatePS.close();
										System.out.println(countUpdate + " row updated");
									}catch (SQLException sqlException){
										sqlException.printStackTrace();
									}

								}else{
									outputToClient.writeUTF("Sorry, unknown user.");
								}
							}catch (SQLException ex){
								ex.printStackTrace();
							}
						}

						/**
						 * If message is radius, read in the next value (radius number) and parse as double
						 */
						if(clientMessage.contains("Radius")) {
							//Read in radius sent from server
							clientMessage = inputFromClient.readUTF();
							double radius = Double.parseDouble(clientMessage);

							// Compute area
							double area = radius * radius * Math.PI;

							// Send area back to the client
							outputToClient.writeDouble(area);

							//Read in client HostName and IP
							String clientHostname = inputFromClient.readUTF();
							String clientIp = inputFromClient.readUTF();
							serverTextArea.append("Incoming message from "+clientHostname + "  "+ clientIp +"\n");
							serverTextArea.append("Server: Radius received from Client-"+ SID + " " + radius + '\n');
							serverTextArea.append("Server: Area found = " + area + "\n \n");
						}
					} catch (IOException exception) {
						exception.printStackTrace();
					}
				}
			} catch (Exception e) {
				System.err.println(e + " on " + socket);
			}
			finally {
				if(conn!=null){
					try {
						conn.close();
					} catch (SQLException sqlException) {
						sqlException.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Listener for exiting system, launches JOptionPane to confirm exit.
	 */
	private class exitListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int result = JOptionPane.showConfirmDialog(frmServer,
					"Exit the Server?",
					"Confirm Exit", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) System.exit(0);
		}
	}
}
