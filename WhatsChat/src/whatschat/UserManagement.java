package whatschat;

import java.io.IOException;
import java.net.DatagramPacket;

public class UserManagement {
	
	private String UserName = "";
	private String status = "OFFLINE";
	
	Network network = new Network();
	
	public UserManagement() {
		network.connectToBroadcast();
	}

	// Sends command to broadcast group to check user name availability
	public void checkUsername(String username, String newUsername) {
		String command = "UsernameCheck " + username + " " + newUsername;
		network.sendBroadcastMessage(command); 
	}

}
