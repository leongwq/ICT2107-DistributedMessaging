package whatschat;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import java.util.List;
import java.util.Random;

import javax.swing.JList;
import javax.swing.SwingConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WhatsChat extends JFrame implements Performable {
	
	Network network = new Network();
	UserManagement um = new UserManagement();
	GroupManagement gm = new GroupManagement(WhatsChat.this,network);
	String groupName;

	List<String> selectedUsers;
	
	String prevUsername = "";
	boolean registered = false;
	
	private JPanel contentPane;
	private JTextField txtUserName;
	private JTextField textField;
	JTextArea textArea = new JTextArea();
	JLabel currentGroupLabel = new JLabel("Current Group: -");
	
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
		
		network.connectToBroadcast();
		MulticastSocket multicastBroadcastSocket = network.getBroadcastSocket();
				
		txtUserName = new JTextField();
		txtUserName.setBounds(380, 6, 179, 26);
		contentPane.add(txtUserName);
		txtUserName.setColumns(10);
		
		textField = new JTextField();
		
		JLabel lblNewLabel = new JLabel("Current Username:");
		JLabel lblCurrentUsername = new JLabel("NotRegistered");
		JButton btnRegisterUser = new JButton("Register User");
		JList<String> listOnlineUsers = new JList<String>(um.getOnlineUsers());

		// Get all current online user
		String command = "KnockKnock";
		network.sendBroadcastMessage(command);
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent e) {
				// App closing. Time to say goodbye.
				String command = "Bye|" + um.getUser();
				network.sendBroadcastMessage(command);
		    }
		});


		btnRegisterUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(txtUserName.getText().trim().equals("")){
					JOptionPane.showMessageDialog(new JFrame(), "Username cannot be blank", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					String command = "UsernameCheck|" + txtUserName.getText() + "|" + lblCurrentUsername.getText();
					network.sendBroadcastMessage(command); // Checks if the user name is taken by other user

					try { // Sleep for 1 second
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					} 
					
					if (!um.getUsernameTaken()) {
						prevUsername = um.getUser(); // Store previous user name
						um.setUser(txtUserName.getText()); // Set name in UM
						lblCurrentUsername.setText(txtUserName.getText()); // Display it
						JOptionPane.showMessageDialog(null,
								txtUserName.getText() + ", you have been successfully registered!");
						// Announce name change
						String nccommand = "NameChange|" + prevUsername + "|" + um.getUser();
						network.sendBroadcastMessage(nccommand); 
					}
					else {
						JOptionPane.showMessageDialog(new JFrame(), "User name has been taken", "Error", JOptionPane.ERROR_MESSAGE); // Show error message
					}
					um.setUsernameTaken(false); // Reset flag
				}
			}
		});
		btnRegisterUser.setBounds(562, 6, 117, 29);
		contentPane.add(btnRegisterUser);
		
		lblNewLabel.setBounds(6, 11, 133, 16);
		contentPane.add(lblNewLabel);
		
		lblCurrentUsername.setBounds(131, 11, 237, 16);
		contentPane.add(lblCurrentUsername);
		
		textArea.setBounds(330, 121, 349, 242);
		contentPane.add(textArea);
		
		Random rand = new Random();
		String user = "Eva" + rand.nextInt(2000);
		um.setUser(user);
		lblCurrentUsername.setText(user);
		
		listOnlineUsers.setBounds(6, 121, 117, 242);
		contentPane.add(listOnlineUsers);
		
		JLabel lblNewLabel_1 = new JLabel("Group Management");
		lblNewLabel_1.setBounds(6, 40, 133, 16);
		contentPane.add(lblNewLabel_1);
		
		JButton btnCreateGroup = new JButton("Create");
		btnCreateGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedUsers = listOnlineUsers.getSelectedValuesList(); // Stores selected users into variable
				groupName = JOptionPane.showInputDialog("Enter a group name");
				String command = "GroupnameCheck|" + groupName + "|" + um.getUser();
				network.sendBroadcastMessage(command); // Sends a request to check if group name is taken
				
				try { // Sleep for 1 second
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} 
				
				if (!gm.getGroupnameTaken()) {
					String IP = network.getRandomIP();
					gm.addGroup(groupName, IP);
					JOptionPane.showMessageDialog(null,
							groupName + ", have been successfully created!");
					// Sends invite to all selected members
					for (int i = 0; i < selectedUsers.size(); i++) {
						String bmsg = "GroupInvite|" + selectedUsers.get(i) + "|" + groupName + "|" + IP;
						System.out.println(IP);
						network.sendBroadcastMessage(bmsg);
					}
				}
				else {
					JOptionPane.showMessageDialog(new JFrame(), "Group name has been taken", "Error", JOptionPane.ERROR_MESSAGE); // Show error message
				}
				gm.setGroupnameTaken(false); // Reset flag
			}
		});
		
		btnCreateGroup.setBounds(6, 62, 117, 29);
		contentPane.add(btnCreateGroup);
		
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
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JList list = (JList)e.getSource();
				if (e.getClickCount() == 2) { // Double-click detected. Behaviour for group selection
		            int index = list.locationToIndex(e.getPoint());
		            gm.connectToGroup(index);
				}
			}
		});
		list.setBounds(135, 121, 183, 242);
		contentPane.add(list);
		
		JLabel lblConversation = new JLabel("Conversation");
		lblConversation.setBounds(330, 97, 91, 16);
		contentPane.add(lblConversation);
		
		JButton btnNewButton_2 = new JButton("Send");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String chatMsg = um.getUser() + ": " + textField.getText();
				network.sendChatMessage(chatMsg);
			}
		});
		btnNewButton_2.setBounds(562, 375, 117, 29);
		contentPane.add(btnNewButton_2);
		
		textField.setBounds(6, 375, 553, 26);
		contentPane.add(textField);
		textField.setColumns(10);
		
		currentGroupLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		currentGroupLabel.setBounds(456, 97, 223, 16);
		contentPane.add(currentGroupLabel);
		
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
			            String[] command = msg.split("\\|"); // Split command by |
						if (command[0].equals("UsernameCheck")) { //UsernameCheck newUsername requester 
							if (um.getUser().equals(command[1])) { 
								String bmsg = "UsernameTaken|" + command[2]; // Sends taken command + requester
								network.sendBroadcastMessage(bmsg);
							}
						}
						if (command[0].equals("UsernameTaken")) {
							if (command[1].equals(um.getUser())) { // User is requester
								um.setUsernameTaken(true); // Set user name taken flag
							}
						}
						if (command[0].equals("NameChange")) {
							um.changeName(command[1], command[2]);
						}
						if (command[0].equals("KnockKnock")) {
							String bmsg = "Hello|" + um.getUser(); // Sends hello response with user name
							network.sendBroadcastMessage(bmsg);
						}
						if (command[0].equals("Hello")) {
							um.addOnlineUser(command[1]); // Add user to online user model
						}
						if (command[0].equals("Bye")) { // Going offline
							um.removeOnlineUser(command[1]); // Remove offline user from user model
						}
						if (command[0].equals("GroupnameCheck")) { // Check if group name is taken
							if (gm.isGroupNameTaken(command[1])) {
								String bmsg = "GroupnameTaken|" + command[1]; // Sends taken command + requested group name + requester
								network.sendBroadcastMessage(bmsg);
							}
						}
						if (command[0].equals("GroupnameTaken")) {
							if (command[1].equals(groupName)) { // User is requester
								gm.setGroupnameTaken(true); // Set group name taken flag
							}
						}
						if (command[0].equals("GroupInvite")) { // Group Invite command. GroupInvite invites groupname ip
							if (command[1].equals(um.getUser())) { // If this command is for the user
								// Add the group to own data
								gm.addGroup(command[2], command[3]);
							}
						}
						
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();	}

	@Override
	public void appendToChat(String str) {
		textArea.append(str);
	}
	
	@Override
	public void updateCurrentGroup(String str) {
		currentGroupLabel.setText("Current Group: " + str);
	}
}
