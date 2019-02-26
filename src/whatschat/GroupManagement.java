package whatschat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;

public class GroupManagement{
	
	private Performable perf;
	private Network network;
	private UserManagement um;

	private DefaultListModel<String> groupsModel = new DefaultListModel<String>();
	private DefaultListModel<String> groupMembers = new DefaultListModel<String>();
	private Map<String, String> IPMapping = new HashMap<String, String>();
	private boolean GroupnameTaken = false;
	private volatile boolean groupChanged = false;
	private String currentGroup;
	Thread t;	
	
	JedisConnection jedis = new JedisConnection(); // Create Jedis object
	
	public GroupManagement(Performable perf, Network network, UserManagement um) {
        this.perf = perf;
        this.network = network;
        this.um = um;
    }
	
	public void setCurrentGroup(String group) {
		currentGroup = group;
	}
	
	public String getCurrentGroup() {
		return currentGroup;
	}
	
	public void setGroupnameTaken(boolean taken) {
		GroupnameTaken = taken;
	}
	
	public boolean getGroupnameTaken() {
		return GroupnameTaken;
	}

	public void addGroup(String groupName, String groupIP) {
		if (!groupsModel.contains(groupName)) { // Group name is not taken
			currentGroup = groupName;
			perf.updateCurrentGroup(); // Update UI
			network.connectToChat(groupIP); // Connect to chat IP
			t = receiveChat(); // Receives thread object
			jedis.pushGroupMembers(groupIP, um.getUser()); // Updates group member list in redis
			IPMapping.put(groupName,groupIP);
			groupsModel.addElement(groupName); 
			setGroupMembers(); // Populate the group member list
			
			// Notify group members that a new member joined
			String command = "NewMember|" + groupIP;
			network.sendBroadcastMessage(command);
		}
	}
	
	public void addOnlineUser(String user) {
		if (!groupsModel.contains(user)) { // Only add if user is not inside the list
			groupsModel.addElement(user);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void leaveGroup() { // Leave the current group
		jedis.removeGroupMember(IPMapping.get(currentGroup),um.getUser());
		groupsModel.remove(groupsModel.indexOf(currentGroup)); // Remove the group from list
		IPMapping.remove(currentGroup); // Remove from IP Mapping
		t.stop();
		
		//Let's wait for the thread to die
        try {
			TimeUnit.MILLISECONDS.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
		disconnectChat();
		currentGroup = "-"; // Remove current group
		perf.updateCurrentGroup(); // Update UI
		perf.clearChat();
		groupMembers.clear(); // Clear group members list
		
		// Notify that i've left
		String command = "ByeByeGroup";
		network.sendBroadcastMessage(command);
	}
	
	@SuppressWarnings("deprecation")
	public void leaveGroup(String group) { // Leave the current group
		jedis.removeGroupMember(IPMapping.get(group),um.getUser());
		groupsModel.remove(groupsModel.indexOf(group)); // Remove the group from list
		IPMapping.remove(group); // Remove from IP Mapping
		t.stop();
		
		//Let's wait for the thread to die
        try {
			TimeUnit.MILLISECONDS.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
		disconnectChat();
		currentGroup = "-"; // Remove current group
		perf.updateCurrentGroup(); // Update UI
		perf.clearChat();
		groupMembers.clear(); // Clear group members list
		
		// Notify that i've left
		String command = "ByeByeGroup";
		network.sendBroadcastMessage(command);
	}
	
	public void changeGroupName(String oldGroupName, String newGroupName) {
		if (groupsModel.contains(oldGroupName)) { // 
			String ip = IPMapping.get(oldGroupName);
			groupsModel.removeElement(oldGroupName);
			groupsModel.addElement(newGroupName);
			currentGroup = newGroupName;
			perf.updateCurrentGroup();
			IPMapping.put(newGroupName,ip);
		}
		
	}
	
	public DefaultListModel<String> getGroups() {
		return groupsModel;
	}
	
	public boolean isGroupNameTaken(String groupName) {
		if (!groupsModel.contains(groupName)) { // Group name is not taken
			return false;
		} else {
			return true;
		}
	}
	
	public String getMember(int index) {
		return groupsModel.getElementAt(index);
	}
	
	@SuppressWarnings("deprecation")
	public void connectToGroup(int index) {
		String ip = IPMapping.get(groupsModel.getElementAt(index));
		network.connectToChat(ip); // Connect to chat IP
		
		if (t != null) {
			t.stop(); // DIE NOW!
		}
		
		//Let's wait for the thread to die
        try {
			TimeUnit.MILLISECONDS.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
                
		t = receiveChat();
		currentGroup = groupsModel.getElementAt(index);
		setGroupMembers();
		perf.updateCurrentGroup(); // Update UI
		perf.clearChat();
		List<String> conversations = jedis.getChatContent(ip);
		perf.updateChatWithHistory(conversations);
	}
	
	@SuppressWarnings("deprecation")
	public void connectToGroup(String ip, String friendName) {
		network.connectToChat(ip); // Connect to chat IP
		
		if (t != null) {
			t.stop(); // DIE NOW!
		}
		
		//Let's wait for the thread to die
        try {
			TimeUnit.MILLISECONDS.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
                
		t = receiveChat();
		currentGroup = "Sliding into " + friendName + "'s DM";
		perf.updateCurrentGroup(); // Update UI
		perf.clearChat();
		List<String> conversations = jedis.getChatContent(ip);
		perf.updateChatWithHistory(conversations);
	}
	
	public void setGroupMembers() { // Sets the data of group members in defaultlistmodel
		groupMembers.clear();
		String ip = IPMapping.get(currentGroup);
		if (ip == null) { return; }
		List<String> members = jedis.getGroupMembers(ip);
		for(int i = 0; i < members.size(); i++) {
			groupMembers.addElement(members.get(i));
        }
	}
	
	public DefaultListModel<String> getGroupMembers() { // Returns group members list
		return groupMembers;
	}
	
	public boolean addMembers(List<String> selectedUsers) {
		// Lets find the current group the user is on
		if (getCurrentGroup() == null) {
			return false;
		}
		inviteMembers(selectedUsers,getCurrentGroup(), IPMapping.get(getCurrentGroup()));
		return true;
	}
	
	public void inviteMembers(List<String> selectedUsers, String groupName, String IP) {
		// Sends invite to all selected members
		for (int i = 0; i < selectedUsers.size(); i++) {
			String bmsg = "GroupInvite|" + selectedUsers.get(i) + "|" + groupName + "|" + IP;
			network.sendBroadcastMessage(bmsg);
		}
	}
	
	public void kickMembers(List<String> selectedUsers) {
		// Sends invite to all selected members
		for (int i = 0; i < selectedUsers.size(); i++) {
			String bmsg = "Kick!|" + selectedUsers.get(i) + "|" + currentGroup;
			network.sendBroadcastMessage(bmsg);
		}
	}
	
	public void disconnectChat() {
		network.disconnectChat();
	}
	
	public Thread receiveChat() {
		MulticastSocket multicastChatSocket = network.getChatSocket();
		    
		Thread t = new Thread(new Runnable() {			
			@Override
			public void run() {
				byte buf1[] = new byte[1000];
				DatagramPacket dgpReceived = new DatagramPacket(buf1, buf1.length);
				while (groupChanged == false) {
					try {
						multicastChatSocket.receive(dgpReceived);
						byte[] receivedData = dgpReceived.getData();
						int length = dgpReceived.getLength();
						// Assumed we received string
						String msg = new String(receivedData, 0, length);
						perf.appendToChat(msg + "\n");
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		
		t.start();
		return t;
	}

}

