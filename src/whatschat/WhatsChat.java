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
import javax.swing.BorderFactory;
import java.awt.Font;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;

public class WhatsChat extends JFrame implements Performable {
	
	Network network = new Network();
	UserManagement um = new UserManagement();
	GroupManagement gm = new GroupManagement(WhatsChat.this,network,um);
	FriendManagement fm = new FriendManagement(gm,um,network);
	String groupName;
	JedisConnection jedis = new JedisConnection(); // Create Jedis object

	List<String> selectedUsers;
	List<String> selectedGroup;
	
	Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
	
	String prevUsername = "";
	String prevGroupName = "";
	String name = "";
	boolean registered = false;
	
	private JPanel contentPane;
	private JTextField textField;
	JTextArea textArea = new JTextArea();
	
	JLabel currentGroupLabel = new JLabel("");
	
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
		setBounds(100, 100, 944, 549);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnUser = new JMenu("User");
		menuBar.add(mnUser);
		
		JMenuItem RegisterUsername = new JMenuItem("Change Name");
		mnUser.add(RegisterUsername);
		
		JMenu mnGroupManagement = new JMenu("Group Management");
		menuBar.add(mnGroupManagement);
		
		JMenuItem btnCreateGroup = new JMenuItem("Create Group");
		mnGroupManagement.add(btnCreateGroup);
		
		JMenuItem btnChangeName = new JMenuItem("Edit Group Name");
		mnGroupManagement.add(btnChangeName);
		
		JMenuItem btnLeaveGroup1 = new JMenuItem("Leave Group");
		btnLeaveGroup1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gm.leaveGroup();
			}
		});
		mnGroupManagement.add(btnLeaveGroup1);
		
		JSeparator separator = new JSeparator();
		mnGroupManagement.add(separator);
		
		JMenuItem btnNewMember = new JMenuItem("Add Member");
		mnGroupManagement.add(btnNewMember);
		
		JMenu mnFriends = new JMenu("Friend Management");
		menuBar.add(mnFriends);
		
		JMenuItem btn_AddFriend = new JMenuItem("Add Friend");
		mnFriends.add(btn_AddFriend);
		
		JMenuItem btn_removeFriend = new JMenuItem("Unfriend");
		mnFriends.add(btn_removeFriend);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		network.connectToBroadcast();
		MulticastSocket multicastBroadcastSocket = network.getBroadcastSocket();
		
		// Get random user name
		Random rand = new Random();
		String user = "Eva" + rand.nextInt(9000);
		um.setUser(user);

		// Get all current online user
		String command = "KnockKnock";
		network.sendBroadcastMessage(command);
		
		// User Panel. Side menu
		JPanel User = new JPanel();
		User.setBackground(Color.WHITE);
		User.setBounds(10, 11, 240, 477);
		contentPane.add(User);
		User.setLayout(null);
				
		// Tab Pane
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(6, 184, 227, 277);
		User.add(tabbedPane);
		
		// Online Tab
		JPanel Online = new JPanel();
		Online.setBackground(new Color(248, 248, 255));
		tabbedPane.addTab("Online", null, Online, null);
		Online.setLayout(null);
		
		JList<String> listOnlineUsers = new JList<String>(um.getOnlineUsers());
		listOnlineUsers.setBackground(new Color(248, 248, 255));
		listOnlineUsers.setBounds(0, 28, 200, 205);
		Online.add(listOnlineUsers);
		
		JButton btnClearOnlineUsers = new JButton("Clear Selection"); // Clear online selection
		btnClearOnlineUsers.setBounds(0, 0, 200, 29);
		Online.add(btnClearOnlineUsers);
		
		// Group Tab
		JPanel group = new JPanel();
		group.setBackground(new Color(248, 248, 255));
		tabbedPane.addTab("Groups", null, group, null);
		group.setLayout(null);
		
		JList<String> listGroup = new JList<String>(gm.getGroups());
		listGroup.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listGroup.setBounds(0, 6, 200, 227);
		group.add(listGroup);
		listGroup.setBackground(new Color(248, 248, 255));
		
		// Friends Tab
		JPanel friends = new JPanel();
		friends.setBackground(new Color(248, 248, 255));
		tabbedPane.addTab("Friends", null, friends, null);
		friends.setLayout(null);
		
		JList<String> listFriends = new JList<String>(fm.getFriends());
		listFriends.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listFriends.setBackground(new Color(248, 248, 255));
		listFriends.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JList list = (JList)e.getSource();
				if (e.getClickCount() == 2) { // Double-click detected. 
		            int index = list.locationToIndex(e.getPoint());
		            fm.connectToFriend(index);
		            listGroup.clearSelection();
		        }
			}
		});
		listFriends.setBounds(6, 6, 194, 219);
		friends.add(listFriends);
		JButton btnNewButton_2 = new JButton("Send");
		JButton btnChnageGroupName = new JButton("");
		
		// Labels Declaration 
		JLabel lblCurrentUsername = new JLabel("NotRegistered");
		JLabel image = new JLabel("");
		
		// Probably the only client. Reset redis database
		if (um.getOnlineUsers().getSize() == 1 && gm.getGroups().isEmpty()) {
			jedis.flush();
		}
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent e) {
				// App closing. Time to say goodbye.
				String command = "Bye|" + um.getUser();
				network.sendBroadcastMessage(command);
		    }
		});

		
		image.setIcon(new ImageIcon("img/profile.png"));

		image.setBounds(67, 18, 104, 99);
		User.add(image);
		
		lblCurrentUsername.setHorizontalAlignment(SwingConstants.CENTER);
		lblCurrentUsername.setBounds(74, 129, 87, 20);

		User.add(lblCurrentUsername);
		lblCurrentUsername.setText(user);
		
		JList list_2 = new JList();
		list_2.setBounds(30, 123, 147, 90);
		Online.add(list_2);
		
		btnClearOnlineUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listOnlineUsers.clearSelection();
			}
		});
		
		//Create Group
		btnCreateGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedUsers = listOnlineUsers.getSelectedValuesList(); // Stores selected users into variable
				groupName = JOptionPane.showInputDialog("Enter a group name");
				
				if (groupName == null) { return; } // If there is no input, exit the method
				
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
					
					listOnlineUsers.clearSelection(); // Clears selection for online users
				}
				else {
					JOptionPane.showMessageDialog(new JFrame(), "Group name has been taken", "Error", JOptionPane.ERROR_MESSAGE); // Show error message
				}
				gm.setGroupnameTaken(false); // Reset flag
			}
		});
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);

		panel.setBounds(262, 11, 505, 477);

		contentPane.add(panel);
		panel.setLayout(null);
		
		textField = new JTextField();
		textField.setBackground(new Color(248, 248, 255));

		textField.setBounds(15, 437, 355, 29);

		panel.add(textField);
		textField.setColumns(10);
		
		btnNewButton_2.setBounds(372, 437, 117, 29);

		panel.add(btnNewButton_2);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
		textArea.setEditable(false);
		textArea.setBackground(new Color(248, 248, 255));
		textArea.setBorder(border);

		textArea.setBounds(15, 42, 474, 384);

		panel.add(textArea);
		currentGroupLabel.setBounds(15, 16, 416, 20);
		currentGroupLabel.setText("Current Group: -");
		panel.add(currentGroupLabel);
		
		currentGroupLabel.setHorizontalAlignment(SwingConstants.LEFT);
		
		btnChnageGroupName.setBackground(Color.WHITE);
		btnChnageGroupName.setIcon(new ImageIcon("img/setting.png"));
		
		btnChnageGroupName.setBounds(464, 7, 26, 29);
		panel.add(btnChnageGroupName);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.WHITE);
		panel_1.setBounds(779, 11, 152, 477);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		JList<String> listGroupMembers = new JList<String>(gm.getGroupMembers());
		listGroupMembers.setBounds(6, 28, 140, 443);
		panel_1.add(listGroupMembers);
		
		JLabel lblGroupMembers = new JLabel("Group Members");
		lblGroupMembers.setBounds(6, 6, 99, 16);
		panel_1.add(lblGroupMembers);
		
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String chatMsg = um.getUser() + ": " + textField.getText();
				network.sendChatMessage(chatMsg);
				textField.setText("");
			}
		});
		
		//Change group name
		btnChangeName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				groupName = JOptionPane.showInputDialog("New Group Name");
				
				if(groupName.equals("")){
					JOptionPane.showMessageDialog(new JFrame(), "Group name cannot be blank", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					String command = "GroupnameCheck|" + groupName + "|" + um.getUser();
					network.sendBroadcastMessage(command); // Sends a request to check if group name is taken

					try { // Sleep for 1 second
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					} 
					
					if (!gm.getGroupnameTaken()) {
						prevGroupName = gm.getCurrentGroup(); // Store previous group name
						gm.setCurrentGroup(groupName); // Set name in UM
						JOptionPane.showMessageDialog(null,
								groupName+ ", you have been successfully changed!");
						// Announce name change
						String nccommand = "GroupNameChanged|" + prevGroupName + "|" + gm.getCurrentGroup();
						network.sendBroadcastMessage(nccommand); 
						currentGroupLabel.setText("Current Group: "+gm.getCurrentGroup()); // Display it
					}
					else {
						JOptionPane.showMessageDialog(new JFrame(), "Group name has been taken", "Error", JOptionPane.ERROR_MESSAGE); // Show error message
					}
					gm.setGroupnameTaken(false); // Reset flag
				}
			}
		});
		
		//button near the label - to change group name
		btnChnageGroupName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				groupName = JOptionPane.showInputDialog("New Group Name");
				
				if(groupName.equals("")){
					JOptionPane.showMessageDialog(new JFrame(), "Group name cannot be blank", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					String command = "GroupnameCheck|" + groupName;
					network.sendBroadcastMessage(command); // Sends a request to check if group name is taken

					try { // Sleep for 1 second
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					} 
					
					if (!gm.getGroupnameTaken()) {
						prevGroupName = gm.getCurrentGroup(); // Store previous group name
						gm.setCurrentGroup(groupName); // Set name in UM
						currentGroupLabel.setText("Current Group: -"); // Display it
						JOptionPane.showMessageDialog(null,
								groupName+ ", you have been successfully changed!");
						// Announce name change
						String nccommand = "GroupNameChanged|" + prevGroupName + "|" + gm.getCurrentGroup();
						network.sendBroadcastMessage(nccommand); 
					}
					else {
						JOptionPane.showMessageDialog(new JFrame(), "Group name has been taken", "Error", JOptionPane.ERROR_MESSAGE); // Show error message
					}
					gm.setGroupnameTaken(false); // Reset flag
				}
				
			}
		});
		
		
		listGroup.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JList list = (JList)e.getSource();
				if (e.getClickCount() == 2) { // Double-click detected. Behavior for group selection
		            int index = list.locationToIndex(e.getPoint());
		            gm.connectToGroup(index);
		            listGroup.clearSelection();
		        }
			}
		});
		
		JMenuItem btn_RemoveMember = new JMenuItem("Remove Member");
		btn_RemoveMember.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedUsers = listGroupMembers.getSelectedValuesList(); // Stores selected users into variable
				if (selectedUsers.isEmpty()) {
					JOptionPane.showMessageDialog(new JFrame(), "Please select a user from the group members list", "Remove Member", JOptionPane.INFORMATION_MESSAGE);
				}
				gm.kickMembers(selectedUsers);
			}
		});
		mnGroupManagement.add(btn_RemoveMember);
		

		//Getting inputs from user to create user name
		RegisterUsername.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				name = JOptionPane.showInputDialog("Name");
				
				if(name.equals("") || name.substring(0, 1).matches("[0-9]") || name.length() > 8){
					JOptionPane.showMessageDialog(new JFrame(), "Username invalid", "Error", JOptionPane.ERROR_MESSAGE);
					name = JOptionPane.showInputDialog("Name");
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
				
		//Add member to existing group
		btnNewMember.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Sends invite to all selected members
				selectedUsers = listOnlineUsers.getSelectedValuesList(); // Stores selected users into variable
				boolean success = gm.addMembers(selectedUsers);
				if (success) {
					JOptionPane.showMessageDialog(new JFrame(), "Invited selected user(s)", "Success", JOptionPane.INFORMATION_MESSAGE); // Show success message
				}
				else {
					JOptionPane.showMessageDialog(new JFrame(), "Unable to invite. Make sure you are in a group", "Error", JOptionPane.ERROR_MESSAGE); // Show error message
				}
			}
		});
		
		//Friends Function
		
		//Add Friend
		btn_AddFriend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedUsers = listOnlineUsers.getSelectedValuesList(); // Stores selected users into variable
				if (selectedUsers.isEmpty()) {
					JOptionPane.showMessageDialog(new JFrame(), "Please select a friend", "Error", JOptionPane.ERROR_MESSAGE); // Show error message
					return;
				}
				String IP = network.getRandomIP(); // Generates IP
				fm.inviteFriends(selectedUsers, um.getUser(), IP);
				listOnlineUsers.clearSelection(); // Clears selection for online users

				JOptionPane.showMessageDialog(new JFrame(), "Friend request sent", "Success", JOptionPane.INFORMATION_MESSAGE); // Show error message
			}
		});
		
		btn_removeFriend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedUsers = listFriends.getSelectedValuesList(); // Stores selected friend into variable
				if (selectedUsers.isEmpty()) {
					JOptionPane.showMessageDialog(new JFrame(), "Please select a friend", "Error", JOptionPane.ERROR_MESSAGE); // Show error message
					return;
				}
				int dialogButton = JOptionPane.YES_NO_OPTION;
				int dialogResult = JOptionPane.showConfirmDialog (null, "Would you like to unfriend " + selectedUsers.get(0) ,"Unfriend",dialogButton);
				if(dialogResult == JOptionPane.YES_OPTION){ // I want to unfriend
					fm.removeFriend(selectedUsers.get(0));
					String bmsg = "Unfriend|" + selectedUsers.get(0) + "|" + um.getUser();
					network.sendBroadcastMessage(bmsg);
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
			            
			            //commands
			            
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
							fm.changeName(command[1], command[2]);
						}
						
						if (command[0].equals("KnockKnock")) {
							String bmsg = "Hello|" + um.getUser(); // Sends hello response with user name
							network.sendBroadcastMessage(bmsg);
						}
						
						if (command[0].equals("Hello")) {
							if (!command[1].equals(um.getUser())) {
								um.addOnlineUser(command[1]); // Add user to online user model
							}
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
						
						if (command[0].equals("NewMember")) { // Update group member list
							gm.setGroupMembers();
						}
						
						if (command[0].equals("ByeByeGroup")) { // Someone left the group
							gm.setGroupMembers();
						}
						
						if (command[0].equals("GroupNameChanged")) {
							gm.changeGroupName(command[1], command[2]);
						}
						if (command[0].equals("FriendInvite")) { // FriendInvite,TargetUser,Requester,IP
							if (command[1].equals(um.getUser())) { // If this command is for the user
								int dialogButton = JOptionPane.YES_NO_OPTION;
								int dialogResult = JOptionPane.showConfirmDialog (null, "Would you like to be friends with " + command[2],"Friend Request",dialogButton);
								if(dialogResult == JOptionPane.YES_OPTION){ // I want to be friend
									fm.addFriend(command[2], command[3]); // Add to friend list
									String bmsg = "FriendAccepted|" + command[2] + "|" + command[1] + "|" + command[3];
									network.sendBroadcastMessage(bmsg);
								}
							}
						}
						if (command[0].equals("FriendAccepted")) { // FriendAccepted,TargetUser,Requester,IP
							if (command[1].equals(um.getUser())) { // If this command is for the user
								fm.addFriend(command[2], command[3]); // Add to friend list
								JOptionPane.showMessageDialog(new JFrame(), command[2] + " has accepted your friend request", "Friend Request", JOptionPane.INFORMATION_MESSAGE);
							}
						}
						if (command[0].equals("Unfriend")) { // FriendAccepted,TargetUser,Requester,IP
							if (command[1].equals(um.getUser())) { // If this command is for the user
								fm.removeFriend(command[2]);
							}
						}
						if (command[0].equals("Kick!")) { // Kick you out of the group muahaha
							if (command[1].equals(um.getUser())) { // If this command is for the user
								gm.leaveGroup(command[2]);
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
	public void updateCurrentGroup() {
		currentGroupLabel.setText("Current Group: " + gm.getCurrentGroup());
	}
	
	@Override
	public void clearChat() {
		textArea.setText("");
	}
	
	@Override
	public void updateChatWithHistory(List<String> conversations) {
		for(int i = 0; i < conversations.size(); i++) {
			textArea.append(conversations.get(i) + '\n');
        }
	
	}
}
