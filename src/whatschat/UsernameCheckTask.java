package whatschat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.Callable;

public class UsernameCheckTask implements Callable<String>{
	
	private Network network;
	private UserManagement um;
	
	public UsernameCheckTask(Network network, UserManagement um) {
        this.network = network;
        this.um = um;
    }
	
	public String call() throws Exception { 
		
		MulticastSocket multicastBroadcastSocket = network.getBroadcastSocket();
		
		byte receiveBuf[] = new byte[1000];
		DatagramPacket dgpReceived = new DatagramPacket(receiveBuf, receiveBuf.length);
		
		while (true && Thread.currentThread().isInterrupted() == false) {
			try {
				multicastBroadcastSocket.receive(dgpReceived);
				byte[] receivedData = dgpReceived.getData();
				int length = dgpReceived.getLength();
				String receivedMessage = new String(receivedData, 0, length);
	            String[] response = receivedMessage.split("\\|"); // Split command by |
	            System.out.println(response[1]);
				if (response[0].equals("UsernameTaken") && response[1].equals(um.getUser())) { // User name is taken and is from the requester
					System.out.println("Setting username flag to true");
					um.setUsernameTaken(true); // Sets the flag taken to true
	            }
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return null;
    }
}
