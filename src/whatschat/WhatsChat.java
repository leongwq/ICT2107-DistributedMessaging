package whatschat;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.util.Random;
import javax.swing.JTextPane;
import javax.swing.JList;
import javax.swing.JLayeredPane;
import javax.swing.ListModel;

public class WhatsChat extends JFrame {
	
	UserManagement um = new UserManagement();
	GroupManagement gm = new GroupManagement();
	
	String tempUsername = "";
	boolean registered = false;
	
	private JPanel contentPane;
	private JTextField txtUserName;
	private JTextField textField;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WhatsChat frame = new WhatsChat();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public WhatsChat() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 685, 431);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		Network network = new Network();
		network.connectToBroadcast();
		MulticastSocket multicastBroadcastSocket = network.getBroadcastSocket();
		InetAddress multicastBroadcastGroup = network.getBroadcastGroup();
		
		txtUserName = new JTextField();
		txtUserName.setBounds(380, 6, 179, 26);
		contentPane.add(txtUserName);
		txtUserName.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Current Username:");
		JLabel lblCurrentUsername = new JLabel("NotRegistered");
		JButton btnRegisterUser = new JButton("Register User");
		JList<String> listOnlineUsers = new JList<String>(um.getOnlineUsers());

		// Get all current online user
		String command = "KnockKnock";
		network.sendBroadcastMessage(command);

		btnRegisterUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(txtUserName.getText().trim().equals("")){
					JOptionPane.showMessageDialog(new JFrame(), "Username cannot be blank", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					String command = "UsernameCheck " + txtUserName.getText() + " " + lblCurrentUsername.getText();
					network.sendBroadcastMessage(command); // Checks if the username is taken by other user
					try {
						byte receiveBuf[] = new byte[1000];
						DatagramPacket dgpReceived = new DatagramPacket(receiveBuf, receiveBuf.length);
						multicastBroadcastSocket.setSoTimeout(2000); 
						try {
							multicastBroadcastSocket.receive(dgpReceived);
							multicastBroadcastSocket.setSoTimeout(0); // Clear timeout
							byte[] receivedData = dgpReceived.getData();
							int length = dgpReceived.getLength();
							String receivedMessage = new String(receivedData, 0, length);
				            String[] response = receivedMessage.split("\\s+"); // Split command by space
				            if (response[0].equals("UsernameTaken") && response[1].equals(lblCurrentUsername.getText())) { // Username is taken and is from the requester
								JOptionPane.showMessageDialog(new JFrame(), "Username has been taken", "Error", JOptionPane.ERROR_MESSAGE);
				            }

						} catch (SocketTimeoutException ex) {
							multicastBroadcastSocket.setSoTimeout(0);
							lblCurrentUsername.setText(txtUserName.getText());
							JOptionPane.showMessageDialog(null,
									txtUserName.getText() + ", you have been successfully registered!");

						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}

				}
			}
		});
		btnRegisterUser.setBounds(562, 6, 117, 29);
		contentPane.add(btnRegisterUser);
		
		lblNewLabel.setBounds(6, 11, 133, 16);
		contentPane.add(lblNewLabel);
		
		lblCurrentUsername.setBounds(131, 11, 237, 16);
		contentPane.add(lblCurrentUsername);
		
		JTextArea textArea = new JTextArea();
		textArea.setBounds(330, 121, 349, 242);
		contentPane.add(textArea);
		
		Random rand = new Random();
		lblCurrentUsername.setText("Eva" + rand.nextInt(2000));
		
		listOnlineUsers.setBounds(6, 121, 117, 242);
		contentPane.add(listOnlineUsers);
		
		JLabel lblNewLabel_1 = new JLabel("Group Management");
		lblNewLabel_1.setBounds(6, 40, 133, 16);
		contentPane.add(lblNewLabel_1);
		
		JButton btnCreate = new JButton("Create");
		btnCreate.setBounds(6, 62, 117, 29);
		contentPane.add(btnCreate);
		
		JButton btnNewButton = new JButton("Edit");
		btnNewButton.setBounds(118, 62, 117, 29);
		contentPane.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Delete");
		btnNewButton_1.setBounds(228, 62, 117, 29);
		contentPane.add(btnNewButton_1);
		
		JLabel lblOnlineUsers = new JLabel("Online Users");
		lblOnlineUsers.setBounds(6, 97, 91, 16);
		contentPane.add(lblOnlineUsers);
		
		JLabel lblNewLabel_2 = new JLabel("Groups");
		lblNewLabel_2.setBounds(131, 97, 61, 16);
		contentPane.add(lblNewLabel_2);
		
		JList<String> list = new JList<String>(gm.getGroups());
		list.setBounds(135, 121, 183, 242);
		contentPane.add(list);
		
		JLabel lblConversation = new JLabel("Conversation");
		lblConversation.setBounds(330, 97, 91, 16);
		contentPane.add(lblConversation);
		
		JButton btnNewButton_2 = new JButton("Send");
		btnNewButton_2.setBounds(562, 375, 117, 29);
		contentPane.add(btnNewButton_2);
		
		textField = new JTextField();
		textField.setBounds(6, 375, 553, 26);
		contentPane.add(textField);
		textField.setColumns(10);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				byte buf[] = new byte[1000];
				DatagramPacket dgpReceived = new DatagramPacket(buf, buf.length);
				while (true) {
					try {
						multicastBroadcastSocket.receive(dgpReceived);
						byte[] receivedData = dgpReceived.getData();
						int length = dgpReceived.getLength();
						String msg = new String(receivedData,0,length);
						
			            String[] command = msg.split("\\s+"); // Split command by space
						if (command[0].equals("UsernameCheck")) { //UsernameCheck newUsername requester 
							System.out.println("UsernameCheck");
							if (lblCurrentUsername.getText().equals(command[1])) { 
								System.out.println("Taken");
								String bmsg = "UsernameTaken " + command[2]; // Sends taken command + requester
								network.sendBroadcastMessage(bmsg);
							}
						}
						if (command[0].equals("KnockKnock")) {
							String bmsg = "Hello " + lblCurrentUsername.getText(); // Sends hello response with user name
							network.sendBroadcastMessage(bmsg);
						}
						if (command[0].equals("Hello")) {
							um.addOnlineUser(command[1]); // Add user to online user model
						}
						if (command[0].equals("Bye")) { // Going offline
						}
						


			            
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();	}
}
