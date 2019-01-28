package com.company;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Network {
    // TODO: Move to constants file
    private static String BROADCAST_ADDRESS = "230.1.1.1";
    private static int PORT = 6789;

    // Multicast Broadcast Socket
    MulticastSocket multicastBroadcastSocket = null;
    InetAddress multicastBroadcastGroup = null;

    public void connectToBroadcast(){
        try {
            multicastBroadcastGroup = InetAddress.getByName(BROADCAST_ADDRESS);
            multicastBroadcastSocket = new MulticastSocket(PORT);
            multicastBroadcastSocket.joinGroup(multicastBroadcastGroup);
            System.out.println("Broadcast Group: " + BROADCAST_ADDRESS + " connected.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
