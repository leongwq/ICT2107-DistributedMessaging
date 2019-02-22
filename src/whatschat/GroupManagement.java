package whatschat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import redis.clients.jedis.Jedis;

public class GroupManagement{
	
	private Performable perf;
	private Network network;
		
	private DefaultListModel<String> groupsModel = new DefaultListModel<String>();
	private Map<String, String> IPMapping = new HashMap<String, String>();
	private boolean GroupnameTaken = false;
	private volatile boolean groupChanged = false;
	private String currentGroup;
	Thread t;	
	JedisConnection jedis = new JedisConnection(); // Create Jedis object
	
	public GroupManagement(Performable perf, Network network) {
        this.perf = perf;
        this.network = network;
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
			if (groupsModel.isEmpty()) { // When user has no group, auto joins the first group
				currentGroup = groupName;
				perf.updateCurrentGroup(); // Update UI
				network.connectToChat(groupIP); // Connect to chat IP
				t = receiveChat(); // Receives thread object
			}
			IPMapping.put(groupName,groupIP);
			groupsModel.addElement(groupName); 
		}
	}
	
	public void addOnlineUser(String user) {
		if (!groupsModel.contains(user)) { // Only add if user is not inside the list
			groupsModel.addElement(user);
		}
	}
	
	public void changeGroupName(String oldGroupName, String newGroupName) {
		if (groupsModel.contains(oldGroupName)) { // 
//			String ipAddress = IPMapping.get(groupsModel.getElementAt(index));
			String ip = IPMapping.get(oldGroupName);
			groupsModel.removeElement(oldGroupName);
			groupsModel.addElement(newGroupName);
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
		
		t.stop(); // DIE NOW!
		
		//Let's wait for the thread to die
        try {
			TimeUnit.MILLISECONDS.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
                
		t = receiveChat();
		currentGroup = groupsModel.getElementAt(index);
		perf.updateCurrentGroup(); // Update UI
		perf.clearChat();
		List<String> conversations = jedis.getChatContent(ip);
		perf.updateChatWithHistory(conversations);
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
			System.out.println(IP);
			network.sendBroadcastMessage(bmsg);
		}
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

