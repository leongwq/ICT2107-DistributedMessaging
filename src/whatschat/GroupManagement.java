package whatschat;

import javax.swing.DefaultListModel;

public class GroupManagement {
	private DefaultListModel<String> groupsModel = new DefaultListModel<String>();


	public void addOnlineUser(String user) {
		if (!groupsModel.contains(user)) { // Only add if user is not inside the list
			groupsModel.addElement(user);
		}
	}
	
	public DefaultListModel<String> getGroups() {
		return groupsModel;
	}

}
