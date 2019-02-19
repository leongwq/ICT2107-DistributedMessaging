package whatschat;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
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
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Color;
import javax.swing.JTabbedPane;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;

public class WhatsChat extends JFrame implements Performable {
	
	Network network = new Network();
	UserManagement um = new UserManagement();
	GroupManagement gm = new GroupManagement(WhatsChat.this,network);
	String groupName;

	List<String> selectedUsers;
	
	Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
	
	String prevUsername = "";
	String name = "";
	boolean registered = false;
	
	private JPanel contentPane;
//	private JTextField txtUserName;
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
		setBounds(100, 100, 789, 564);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnUser = new JMenu("User");
		menuBar.add(mnUser);
		
		JMenuItem RegisterUsername = new JMenuItem("Register");
		mnUser.add(RegisterUsername);
		
		JMenu mnGroupManagement = new JMenu("Group Management");
		menuBar.add(mnGroupManagement);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		network.connectToBroadcast();
		MulticastSocket multicastBroadcastSocket = network.getBroadcastSocket();
		JButton btnRegisterUser = new JButton("Register User");

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
		
		JButton btnCreateGroup = new JButton("Create");
		
		btnCreateGroup.setBounds(15, 0, 117, 29);
		contentPane.add(btnCreateGroup);
		
		JButton btnNewButton = new JButton("Edit");
		btnNewButton.setBounds(259, 0, 117, 29);
		contentPane.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Delete");
		btnNewButton_1.setBounds(363, 0, 117, 29);
		contentPane.add(btnNewButton_1);
		
		JPanel User = new JPanel();
		User.setBackground(Color.WHITE);
		User.setBounds(15, 26, 207, 439);
		contentPane.add(User);
		User.setLayout(null);
		
		JLabel image = new JLabel("");
		image.setIcon(new ImageIcon("img/profile.png"));
		image.setBounds(54, 17, 104, 99);
		User.add(image);
		
		Random rand = new Random();
		String user = "Eva" + rand.nextInt(2000);
		um.setUser(user);
		
		JLabel lblCurrentUsername = new JLabel("NotRegistered");
		lblCurrentUsername.setBounds(70, 132, 87, 20);
		User.add(lblCurrentUsername);
		lblCurrentUsername.setText(user);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(15, 184, 181, 244);
		User.add(tabbedPane);
		
		JPanel Online = new JPanel();
		Online.setBackground(Color.WHITE);
		tabbedPane.addTab("Online", null, Online, null);
		Online.setLayout(null);
		JList<String> listOnlineUsers = new JList<String>(um.getOnlineUsers());
		listOnlineUsers.setBounds(0, 0, 176, 290);
		Online.add(listOnlineUsers);
		
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
					gm.inviteMembers(selectedUsers, groupName,IP);
				}
				else {
					JOptionPane.showMessageDialog(new JFrame(), "Group name has been taken", "Error", JOptionPane.ERROR_MESSAGE); // Show error message
				}
				gm.setGroupnameTaken(false); // Reset flag
			}
		});
		
		JPanel group = new JPanel();
		group.setBackground(Color.WHITE);
		tabbedPane.addTab("Group", null, group, null);
		group.setLayout(null);
		
		JList<String> listGroup = new JList<String>(gm.getGroups());
		listGroup.setBounds(0, 0, 225, 322);
		group.add(listGroup);
		listGroup.setBackground(Color.WHITE);
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBounds(237, 29, 517, 436);
		contentPane.add(panel);
		panel.setLayout(null);
		
//		txtUserName = new JTextField();
//		txtUserName.setBounds(409, 108, 179, 26);
//		contentPane.add(txtUserName);
//		txtUserName.setColumns(10);
		
		textField = new JTextField();
		textField.setBackground(new Color(248, 248, 255));
		textField.setBounds(15, 398, 353, 26);
		panel.add(textField);
		textField.setColumns(10);
		
		JButton btnNewButton_2 = new JButton("Send");
		btnNewButton_2.setBounds(380, 397, 117, 29);
		panel.add(btnNewButton_2);
		textArea.setEditable(false);
		textArea.setBackground(new Color(248, 248, 255));
		textArea.setBorder(border);
		textArea.setBounds(15, 52, 482, 315);
		panel.add(textArea);
		currentGroupLabel.setBounds(15, 16, 129, 20);
		panel.add(currentGroupLabel);
		
		currentGroupLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		
		JButton btnNewMember = new JButton("Add Member");
		btnNewMember.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		btnNewMember.setBounds(130, 0, 117, 29);
		contentPane.add(btnNewMember);
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String chatMsg = um.getUser() + ": " + textField.getText();
				network.sendChatMessage(chatMsg);
			}
		});
		listGroup.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JList list = (JList)e.getSource();
				if (e.getClickCount() == 2) { // Double-click detected. Behaviour for group selection
		            int index = list.locationToIndex(e.getPoint());
		            gm.connectToGroup(index);
				}
			}
		});
		//

		//Getting inputs from user to create username
				RegisterUsername.addActionListener (new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						name = JOptionPane.showInputDialog("Name");
//						String msg = name+" have been successfully registered!";
//						JOptionPane.showMessageDialog(null, msg);
//						lblCurrentUsername.setText(name);
						
						if(name.equals("")){
							JOptionPane.showMessageDialog(new JFrame(), "Username cannot be blank", "Error", JOptionPane.ERROR_MESSAGE);
						} else {
							String command = "UsernameCheck|" + name + "|" + lblCurrentUsername.getText();;
							network.sendBroadcastMessage(command); // Checks if the user name is taken by other user

							try { // Sleep for 1 second
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							} 
							
							if (!um.getUsernameTaken()) {
								prevUsername = um.getUser(); // Store previous user name
								um.setUser(name); // Set name in UM
								lblCurrentUsername.setText(name); // Display it
								JOptionPane.showMessageDialog(null,
										name+ ", you have been successfully registered!");
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
