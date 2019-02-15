package whatschat;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;

public class GroupManagement {
	
	private DefaultListModel<String> groupsModel = new DefaultListModel<String>();
	private Map<String, String> IPMapping = new HashMap<String, String>();

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

}
