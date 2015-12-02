package org.overskeid.mqtt.service;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import org.overskeid.mqtt.packets.MqttConnAck;
import org.overskeid.mqtt.packets.MqttConnect;
import org.overskeid.mqtt.packets.MqttDisconnect;
import org.overskeid.mqtt.packets.MqttMessage;
import org.overskeid.mqtt.packets.MqttPublish;
import org.overskeid.mqtt.packets.MqttSubAck;
import org.overskeid.mqtt.packets.MqttSubscribe;
import org.overskeid.mqtt.packets.MqttSubAck.ReturnCode;
import org.overskeid.mqtt.streams.MqttInStream;
import org.overskeid.mqtt.streams.MqttOutStream;

/**
 * Created by Kristian on 07.11.2015.
 */
public class MqttCommonucationHandler implements Runnable {
    private static final int MAXQUEUE = 10;
    private static final long expectedRTT = 1000;
    private Vector<Object> messages = new Vector();
    private String address;
    private int port;
    private String clientId;
    private MqttOutStream outStream;
    private MqttInStream inStream;
    private String button;
    private String command;
    private Hashtable<Integer,CheckAckTimer> unacknowledgedMessages;

    public MqttCommonucationHandler(String address, int port, String clientId) {
    	this.port = port;
        this.address = address;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        System.out.println("InitiateSOcket started");
        this.unacknowledgedMessages = new Hashtable<Integer, CheckAckTimer>();
        try {
            System.out.println("Verifying internet address");
            InetAddress inetAddress = InetAddress.getByName(address);
            System.out.println("creating socket");
            Socket socket = new Socket(inetAddress, port);
            inStream = new MqttInStream(socket, this);
            outStream = new MqttOutStream(socket);
            new Thread(inStream).start();
            new Thread(outStream).start();
            sendMessage(new MqttConnect(clientId));
        } catch (UnknownHostException e) {
            System.out.println("unknown host");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        while (true) {
            Object message = null;
            try {
                message = getMessage();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            processMessage(message);
        }
    }

    private void processMessage(Object message) {
        if(message instanceof MqttConnAck)
            processConnAck(message);
        else if(message instanceof MqttSubAck)
            processSubAck(message);
        else if(message instanceof MqttPublish)
            processPublish(message);

    }

    private void processConnAck(Object message) {
        MqttConnAck connAck = (MqttConnAck) message;
        cancelTimer(0);
        if(connAck.getReturnCode().equals(MqttConnAck.ReturnCode.CONNECTION_ACCEPTED)) {
            System.out.println("connection accepted, sending subribe message");
            try {
                sendMessage(new MqttSubscribe("kristian/#",0));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else
            System.out.println("Connection failed: " + connAck.getReturnCode());
    }

    private void processSubAck(Object message) {
        MqttSubAck subAck = (MqttSubAck) message;
        cancelTimer(subAck.getPacketIdentifier());
        System.out.println("received suback with return codes");
        for(ReturnCode rc : subAck.getReturnCodes())
        	System.out.println(rc.toString());
        System.out.println("sending message to Kristian");
        try {
			outStream.send(new MqttPublish("kristian/test","Testing",0).getBytes());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void processPublish(Object message) {
        MqttPublish publish = (MqttPublish) message;
        System.out.println("Received message: " +publish.getTopic()+": "+publish.getPayload());
        try {
			sendMessage(new MqttDisconnect());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void cancelTimer(int identifier) {
    	CheckAckTimer timer = unacknowledgedMessages.get(identifier);
    	if(timer==null)
    		System.out.println("Couldn't find any reference");
    	else {
    		timer.cancel();
    		unacknowledgedMessages.remove(identifier);
    	}
    }

    public void resendMessage(Object message) throws InterruptedException {
    	System.out.println("Resending message");
    	MqttMessage mqttMessage = (MqttMessage) message;
    	unacknowledgedMessages.remove(mqttMessage.getPacketIdentifier());
    	if(mqttMessage instanceof MqttPublish) {
    		MqttPublish mqttPublish = (MqttPublish) mqttMessage;
    		mqttPublish.setDuplicate(true);
    		message = mqttPublish;
    	}
    	sendMessage(message);
    }

    public synchronized void putMessage(Object message) throws InterruptedException {
        while (messages.size() >= MAXQUEUE)
            wait();
        messages.addElement(message);
        notify();
    }

    private synchronized Object getMessage() throws InterruptedException {
        notify();
        while (messages.size() == 0)
            wait();
        Object message = messages.firstElement();
        messages.removeElement(message);
        return message;
    }

    public void sendMessage(Object message) throws InterruptedException {
        MqttMessage mqttMessage = (MqttMessage) message;
        if(mqttMessage.isAckRequired()) {
        	int packetId;
        	if(mqttMessage instanceof MqttConnect)
        		packetId = 0;
        	else {
        		packetId = generatePacketIdentifier();
        		mqttMessage.setPacketIdentifier(packetId);
        	}
        	CheckAckTimer ackTimer = new CheckAckTimer(message,expectedRTT,this);
        	unacknowledgedMessages.put(packetId, ackTimer);
        }
        outStream.send(mqttMessage.getBytes());
    }
    
    private int generatePacketIdentifier() {
    	Random random = new Random();
    	int packetId;
    	while(true) {
    		packetId = random.nextInt(MqttMessage.maxPacketIdNb+1);
    		if(!unacknowledgedMessages.contains(packetId))
    			break;
    	}
    	return packetId;
    }

}
