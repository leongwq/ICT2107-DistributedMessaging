package whatschat;

import java.io.IOException;
import java.net.DatagramPacket;

import javax.swing.DefaultListModel;

public class UserManagement {
	
	private String UserName = "";
	private String status = "OFFLINE";
	private DefaultListModel<String> onlineUsersModel = new DefaultListModel<String>();

	public void addOnlineUser(String user) {
		if (!onlineUsersModel.contains(user)) { // Only add if user is not inside the list
			onlineUsersModel.addElement(user);
		}
	}
	
	public DefaultListModel<String> getOnlineUsers() {
		return onlineUsersModel;
	}

}
