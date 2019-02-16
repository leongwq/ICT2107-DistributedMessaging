package whatschat;

import javax.swing.DefaultListModel;

public class UserManagement {
	
	private String UserName = "";
	private String status = "OFFLINE";
	private boolean UsernameTaken = false;
	private DefaultListModel<String> onlineUsersModel = new DefaultListModel<String>();

	public void setUser(String user) {
		UserName = user;
	}
	
	public String getUser() {
		return UserName;
	}
	
	public void setUsernameTaken(boolean taken) {
		UsernameTaken = taken;
	}
	
	public boolean getUsernameTaken() {
		return UsernameTaken;
	}
	
	public void addOnlineUser(String user) {
		if (!onlineUsersModel.contains(user)) { // Only add if user is not inside the list
			onlineUsersModel.addElement(user);
		}
	}
	
	public void removeOnlineUser(String user) {
		if (onlineUsersModel.contains(user)) { // Only remove if user is not inside the list
			onlineUsersModel.removeElement(user);
		}
	}
	
	public void changeName(String oldUser, String newUser) {
		if (onlineUsersModel.contains(oldUser)) { // 
			onlineUsersModel.removeElement(oldUser);
			onlineUsersModel.addElement(newUser);
		}
	}
	
	public DefaultListModel<String> getOnlineUsers() {
		return onlineUsersModel;
	}

}
