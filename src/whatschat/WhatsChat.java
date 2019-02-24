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
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Font;

public class WhatsChat extends JFrame implements Performable {
	
	Network network = new Network();
	UserManagement um = new UserManagement();
	GroupManagement gm = new GroupManagement(WhatsChat.this,network);
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
		setBounds(100, 100, 805, 590);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnUser = new JMenu("User");
		menuBar.add(mnUser);
		
		JMenuItem RegisterUsername = new JMenuItem("Register");
		mnUser.add(RegisterUsername);
		
		JMenu mnGroupManagement = new JMenu("Group Management");
		menuBar.add(mnGroupManagement);
		
		JMenuItem btnCreateGroup = new JMenuItem("Create Group");
		mnGroupManagement.add(btnCreateGroup);
		
		JMenuItem btnChangeName = new JMenuItem("Edit Group Name");
		mnGroupManagement.add(btnChangeName);
		
		JMenuItem btnNewMember = new JMenuItem("Add Member");
		mnGroupManagement.add(btnNewMember);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		network.connectToBroadcast();
		MulticastSocket multicastBroadcastSocket = network.getBroadcastSocket();
		
		// Get random user name
		Random rand = new Random();
		String user = "Eva" + rand.nextInt(2000);
		um.setUser(user);

		// Get all current online user
		String command = "KnockKnock";
		network.sendBroadcastMessage(command);
		
		// User Panel. Side menu
		JPanel User = new JPanel();
		User.setBackground(Color.WHITE);
		User.setBounds(15, 16, 267, 477);
		contentPane.add(User);
		User.setLayout(null);
				
		// Tab Pane
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(6, 184, 255, 277);
		User.add(tabbedPane);
		
		// Online Tab
		JPanel Online = new JPanel();
		Online.setBackground(new Color(248, 248, 255));
		tabbedPane.addTab("Online", null, Online, null);
		Online.setLayout(null);
		
		JList<String> listOnlineUsers = new JList<String>(um.getOnlineUsers()); // Online users list
		listOnlineUsers.setBackground(new Color(248, 248, 255));
		listOnlineUsers.setBounds(0, 28, 188, 205);
		Online.add(listOnlineUsers);
		
		JButton btnClearOnlineUsers = new JButton("Clear Selection"); // Clear online selection
		btnClearOnlineUsers.setBounds(0, 0, 234, 29);
		Online.add(btnClearOnlineUsers);
		
		// Group Tab
		JPanel group = new JPanel();
		group.setBackground(new Color(248, 248, 255));
		tabbedPane.addTab("Groups", null, group, null);
		group.setLayout(null);
		
		JList<String> listGroup = new JList<String>(gm.getGroups());
		listGroup.setBounds(0, 33, 188, 200);
		group.add(listGroup);
		listGroup.setBackground(new Color(248, 248, 255));
		
		// Friends Tab
		JPanel friends = new JPanel();
		friends.setBackground(new Color(248, 248, 255));
		tabbedPane.addTab("Friends", null, friends, null);
		friends.setLayout(null);
		
		JList<String> listFriends = new JList<String>(fm.getFriends());
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
		listFriends.setBounds(6, 6, 222, 219);
		friends.add(listFriends);
		
		// Buttons Declaration
		JButton btnNewButton_1 = new JButton("Delete");
		JButton btnClearGroupList = new JButton("Clear Selection");
		JButton btnNewButton_2 = new JButton("Send");
		JButton btnChnageGroupName = new JButton("");
		
		// Labels Declaration 
		JLabel lblCurrentUsername = new JLabel("NotRegistered");
		JLabel image = new JLabel("");
		
		// Buttons Position
		btnNewButton_1.setBounds(363, 0, 117, 29);
		contentPane.add(btnNewButton_1);
		btnClearGroupList.setBounds(0, 0, 234, 29);
		group.add(btnClearGroupList);
		
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
		
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});

		
		image.setIcon(new ImageIcon("img/profile.png"));

		image.setBounds(64, 16, 104, 99);
		User.add(image);
		
		lblCurrentUsername.setHorizontalAlignment(SwingConstants.CENTER);
		lblCurrentUsername.setBounds(64, 131, 87, 20);

		User.add(lblCurrentUsername);
		lblCurrentUsername.setText(user);
		
		
		
		JPopupMenu popupMenu_1 = new JPopupMenu();
		addPopup(listOnlineUsers, popupMenu_1);
		
		JMenuItem popupCreateGroup = new JMenuItem("Create Group");
		popupMenu_1.add(popupCreateGroup);
		
		JMenuItem popupAddMember = new JMenuItem("Add Member");
		popupMenu_1.add(popupAddMember);
		
		JMenuItem popupAddFriend = new JMenuItem("Add Friend");
		popupMenu_1.add(popupAddFriend);
		
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
		
		popupCreateGroup.addActionListener(new ActionListener() {
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
		
		popupAddFriend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedUsers = listOnlineUsers.getSelectedValuesList(); // Stores selected users into variable
												
				String IP = network.getRandomIP(); // Generates IP
				fm.inviteFriends(selectedUsers, um.getUser(), IP);
				listOnlineUsers.clearSelection(); // Clears selection for online users

				JOptionPane.showMessageDialog(new JFrame(), "Friend request sent", "Success", JOptionPane.INFORMATION_MESSAGE); // Show error message
			}
		});

		
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(listGroup, popupMenu);
		
		JMenuItem details = new JMenuItem("Details");
		popupMenu.add(details);
		
		JMenuItem mntmChangeName = new JMenuItem("Change Name");
		popupMenu.add(mntmChangeName);
		
		btnClearGroupList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listGroup.clearSelection();
			}
		});
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);

		panel.setBounds(294, 26, 505, 467);

		contentPane.add(panel);
		panel.setLayout(null);
		
		textField = new JTextField();
		textField.setBackground(new Color(248, 248, 255));

		textField.setBounds(15, 422, 342, 29);

		panel.add(textField);
		textField.setColumns(10);
		
		btnNewButton_2.setBounds(372, 422, 117, 29);

		panel.add(btnNewButton_2);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
		textArea.setEditable(false);
		textArea.setBackground(new Color(248, 248, 255));
		textArea.setBorder(border);

		textArea.setBounds(15, 42, 474, 364);

		panel.add(textArea);
		currentGroupLabel.setBounds(15, 16, 416, 20);
		currentGroupLabel.setText("Current Group: -");
		panel.add(currentGroupLabel);
		
		currentGroupLabel.setHorizontalAlignment(SwingConstants.LEFT);
		
		btnChnageGroupName.setBackground(Color.WHITE);
		btnChnageGroupName.setIcon(new ImageIcon("img/setting.png"));
		
		btnChnageGroupName.setBounds(464, 7, 26, 29);
		panel.add(btnChnageGroupName);
		
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
		
		//button near the label
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
		

		//Getting inputs from user to create user name
				RegisterUsername.addActionListener (new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						name = JOptionPane.showInputDialog("Name");
						
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
				
				popupAddMember.addActionListener(new ActionListener() {
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
				
				//Get group details
				details.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.out.println(gm.getGroups());
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
						
						if (command[0].equals("CheckMember")) { //Check Member in group
							um.getUser();
						}
						
						if (command[0].equals("DeleteMember")) { //Delete Member command.
							
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
										
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();	}
	
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

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
