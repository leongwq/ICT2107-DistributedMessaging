package whatschat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

public class Network {
	
	private String BROADCAST_ADDRESS = "230.1.1.1";
	private int PORT = 6789;

	// Broadcast Socket
	MulticastSocket multicastBroadcastSocket = null;
	InetAddress multicastBroadcastGroup = null;

	// Chat Socket
	MulticastSocket multicastChatSocket = null;
	InetAddress multicastChatGroup = null;
	
	public void connectToBroadcast() {
		try {
			multicastBroadcastGroup = InetAddress.getByName(BROADCAST_ADDRESS);
			multicastBroadcastSocket = new MulticastSocket(PORT);
			multicastBroadcastSocket.joinGroup(multicastBroadcastGroup);
			System.out.println("Connected to Broadcast Group: " + BROADCAST_ADDRESS);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public InetAddress getBroadcastGroup() {
		return multicastBroadcastGroup;
	}
	
	public MulticastSocket getBroadcastSocket() {
		return multicastBroadcastSocket;
	}
	
	public void sendBroadcastMessage(String msg) {
		try {
			byte[] buf = msg.getBytes();
			DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastBroadcastGroup,
					PORT);
			multicastBroadcastSocket.send(dgpSend);
			System.out.println("Sent to broadcast: " + msg);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public int getBroadcastPort() {
		return PORT;
	}
	
	public String getRandomIP() {
		Random r = new Random(); // Random IP address
		String ip = "230.1." + r.nextInt(256) + "." + r.nextInt(256);
		return ip;
	}

}

