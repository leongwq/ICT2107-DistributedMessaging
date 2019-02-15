package whatschat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;

public class GroupManagement{
	
	private Performable perf;
		
	private DefaultListModel<String> groupsModel = new DefaultListModel<String>();
	private Map<String, String> IPMapping = new HashMap<String, String>();
	
	public GroupManagement(Performable perf) {
        this.perf = perf;
    }

	public void addGroup(String groupName, String groupIP) {
		if (!groupsModel.contains(groupName)) { // Group name is not taken
			IPMapping.put(groupName,groupIP);
			groupsModel.addElement(groupName); 
		}
	}
	
	public void addOnlineUser(String user) {
		if (!groupsModel.contains(user)) { // Only add if user is not inside the list
			groupsModel.addElement(user);
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
	
	public void receiveChat(Network network) {
		MulticastSocket multicastChatSocket = network.getChatSocket();

		new Thread(new Runnable() {
			@Override
			public void run() {
				byte buf1[] = new byte[1000];
				DatagramPacket dgpReceived = new DatagramPacket(buf1, buf1.length);
				while (true) {
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
			
		}).start();
	}

}
