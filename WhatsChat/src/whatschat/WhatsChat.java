package whatschat;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class WhatsChat extends JFrame {
	
	UserManagement um = new UserManagement();
	String tempUsername = "";
	
	private JPanel contentPane;
	private JTextField txtUserName;

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
		setBounds(100, 100, 685, 467);
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
		JLabel lblCurrentUsername = new JLabel("-");
		JButton btnRegisterUser = new JButton("Register User");
		
		btnRegisterUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(txtUserName.getText().trim().equals("")){
					JOptionPane.showMessageDialog(new JFrame(), "Username cannot be blank", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					tempUsername = lblCurrentUsername.getText(); // Save current username into temp for revert if taken
					um.checkUsername(lblCurrentUsername.getText(), txtUserName.getText()); // Checks if the username is taken by other user
					lblCurrentUsername.setText(txtUserName.getText()); // Set current username to new username
				}
			}
		});
		btnRegisterUser.setBounds(562, 6, 117, 29);
		contentPane.add(btnRegisterUser);
		
		lblNewLabel.setBounds(6, 11, 133, 16);
		contentPane.add(lblNewLabel);
		
		lblCurrentUsername.setBounds(131, 11, 237, 16);
		contentPane.add(lblCurrentUsername);
		
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
						if (command[0].equals("UsernameCheck")) { //UsernameCheck currentUsername newUsername
							System.out.println("UsernameCheck command called");
							if (lblCurrentUsername.getText().equals(command[2]) && !tempUsername.equals(command[1]) ) { // Name matches. Excludes checking ownself
								String bmsg = "UsernameCheckReply Taken " + command[2] + " " + command[1];
								network.sendBroadcastMessage(bmsg);
							} 
						}
						if (command[0].equals("UsernameCheckReply")) { //UsernameCheckReply Taken newUsername requestingUser
							System.out.println("Requesting user: " + command[3]);
							System.out.println("Temp username : " + tempUsername);
							if (command[3].equals(tempUsername)) { // Check if this user is the one requesting
								if (command[1].equals("Taken")) { // Username is taken
									lblCurrentUsername.setText(tempUsername); // Revert back to old username;
									txtUserName.setText(""); // Clear the textfield
									JOptionPane.showMessageDialog(new JFrame(), "Username has been taken", "Error", JOptionPane.ERROR_MESSAGE);
								}
							}
						}
						if (command[0].equals("Status")) {
						}
						


			            
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();	}
	
}
