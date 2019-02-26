package whatschat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;

public class FriendManagement {
	
	private Network network;
	private GroupManagement gm;
	private UserManagement um;

	private DefaultListModel<String> friendsModel = new DefaultListModel<String>();
	private Map<String, String> IPMapping = new HashMap<String, String>();
	
	public FriendManagement(GroupManagement gm, UserManagement um, Network network) {
        this.network = network;
        this.gm = gm;
        this.um = um;
    }
	
	public DefaultListModel<String> getFriends() {
		return friendsModel;
	}
	
	public void inviteFriends(List<String> selectedUsers, String requester, String IP) {
		// Sends invite to all selected members
		for (int i = 0; i < selectedUsers.size(); i++) {
			if (requester.equals(selectedUsers.get(i)) || friendsModel.contains(selectedUsers.get(i))) {
				continue;
			}
			String bmsg = "FriendInvite|" + selectedUsers.get(i) + "|" + requester + "|" + IP;
			network.sendBroadcastMessage(bmsg);
		}
	}
	
	public void addFriend(String friendName, String friendIP) {
		IPMapping.put(friendName,friendIP);
		friendsModel.addElement(friendName); 
	}
	
	public void connectToFriend(int index) {
		String ip = IPMapping.get(friendsModel.getElementAt(index));
		gm.connectToGroup(ip, friendsModel.getElementAt(index));
	}
	
	public void removeFriend(String friend) {
		friendsModel.remove(friendsModel.indexOf(friend)); // Remove the friend from list
		IPMapping.remove(friend); // Remove from IP Mapping
	}
}
