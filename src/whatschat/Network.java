package whatschat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

public class Network {
	
	private String BROADCAST_ADDRESS = "230.1.1.1";
	private String CHAT_ADDRESS;;
	private int PORT = 6789;
	
	JedisConnection jedis = new JedisConnection(); // Create Jedis object

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
	
	public void connectToChat(String ip) {
		try {
			multicastChatGroup = InetAddress.getByName(ip);
			multicastChatSocket = new MulticastSocket(PORT);
			multicastChatSocket.joinGroup(multicastChatGroup);
			CHAT_ADDRESS = ip;
			System.out.println("Connected to Chat Group: " + ip);
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
	
	public MulticastSocket getChatSocket() {
		return multicastChatSocket;
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
	
	public void sendChatMessage(String msg) {
		try {
			byte[] buf = msg.getBytes();
			DatagramPacket dgpSend = new DatagramPacket(buf, buf.length, multicastChatGroup,
					PORT);
			multicastChatSocket.send(dgpSend);
			jedis.pushChatContent(CHAT_ADDRESS, msg);
			System.out.println("Sent to chat: " + msg);
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

