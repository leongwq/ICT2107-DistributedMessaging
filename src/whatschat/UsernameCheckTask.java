package whatschat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class UsernameCheckTask implements Callable<String>{
	
	private Network network;
	private UserManagement um;
	
	public UsernameCheckTask(Network network, UserManagement um) {
        this.network = network;
        this.um = um;
    }
	
	public String call() throws Exception { 
		
		MulticastSocket multicastBroadcastSocket = network.getBroadcastSocket();
		while (true && Thread.currentThread().isInterrupted() == false) {
			System.out.println("Called from thread");
		}
//		try {
//			byte receiveBuf[] = new byte[1000];
//			DatagramPacket dgpReceived = new DatagramPacket(receiveBuf, receiveBuf.length);
//			multicastBroadcastSocket.setSoTimeout(2000); 
//			try {
//				multicastBroadcastSocket.receive(dgpReceived);
//				multicastBroadcastSocket.setSoTimeout(0); // Clear timeout
//				byte[] receivedData = dgpReceived.getData();
//				int length = dgpReceived.getLength();
//				String receivedMessage = new String(receivedData, 0, length);
//	            String[] response = receivedMessage.split("\\|"); // Split command by |
//	            if (response[0].equals("UsernameTaken") && response[1].equals(lblCurrentUsername.getText())) { // Username is taken and is from the requester
//					JOptionPane.showMessageDialog(new JFrame(), "Username has been taken", "Error", JOptionPane.ERROR_MESSAGE);
//	            }
//
//			} catch (SocketTimeoutException ex) {
//				multicastBroadcastSocket.setSoTimeout(0);
//				prevUsername = um.getUser(); // Store previous user name
//				um.setUser(txtUserName.getText()); // Set name in UM
//				lblCurrentUsername.setText(txtUserName.getText()); // Display it
//				JOptionPane.showMessageDialog(null,
//						txtUserName.getText() + ", you have been successfully registered!");
//
//			}
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
		return null;
    }
}
