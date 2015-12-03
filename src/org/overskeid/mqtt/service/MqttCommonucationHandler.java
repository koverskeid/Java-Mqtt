package org.overskeid.mqtt.service;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;


import org.overskeid.mqtt.packets.MqttConnAck;
import org.overskeid.mqtt.packets.MqttConnect;
import org.overskeid.mqtt.packets.MqttDisconnect;
import org.overskeid.mqtt.packets.MqttMessage;
import org.overskeid.mqtt.packets.MqttPubAck;
import org.overskeid.mqtt.packets.MqttPubComp;
import org.overskeid.mqtt.packets.MqttPubRel;
import org.overskeid.mqtt.packets.MqttPubRec;
import org.overskeid.mqtt.packets.MqttPublish;
import org.overskeid.mqtt.packets.MqttSubAck;
import org.overskeid.mqtt.packets.MqttSubAck.ReturnCode;
import org.overskeid.mqtt.packets.MqttSubscribe;
import org.overskeid.mqtt.packets.MqttUnsubAck;
import org.overskeid.mqtt.packets.MqttUnsubsribe;
import org.overskeid.mqtt.packets.Subscription;

/**
 * Created by Kristian on 07.11.2015.
 */
public class MqttCommonucationHandler implements Runnable {
    private static final int MAXQUEUE = 10;
    private static final long expectedRTT = 5000;
    private Vector<Object> messages = new Vector<Object>();
    private Vector<MqttPublish> publishMessages = new Vector<MqttPublish>();
    private String address;
    private int port;
    private String clientId;
    private MqttOutStream outStream;
    private MqttInStream inStream;
    private Hashtable<Integer,CheckAckTimer> unacknowledgedMessages = new Hashtable<Integer, CheckAckTimer>();
    private boolean connectionEstablished = false;
    private Hashtable<String, Integer> subscribedTopics = new Hashtable<String, Integer>();
    private Vector<Integer> receivedQos2Messages = new Vector<Integer>();

    public MqttCommonucationHandler(String address, int port, String clientId) {
    	this.port = port;
        this.address = address;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try {
        	//Connect
            InetAddress inetAddress = InetAddress.getByName(address);
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
			e.printStackTrace();
		}

        while (true) {
            Object message = null;
            try {
                message = getMessage();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(message == null)
            	break;
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
        else if(message instanceof MqttUnsubAck)
        	processUnsubAck(message);
        else if(message instanceof MqttPubAck)
        	processPubAck(message);
        else if(message instanceof MqttPubRec)
        	processPubRec(message);
        else if(message instanceof MqttPubRel)
        	processPubRel(message);
        else if(message instanceof MqttPubComp)
        	processPubComp(message);
    }

    private void processConnAck(Object message) {
        MqttConnAck connAck = (MqttConnAck) message;
        registerAck(0);
        if(connAck.getReturnCode().equals(MqttConnAck.ReturnCode.CONNECTION_ACCEPTED)) {
            try {
              setConnectionEstablished();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else
            System.out.println("Connection failed: " + connAck.getReturnCode());
    }

    private void processSubAck(Object message) {
        MqttSubAck subAck = (MqttSubAck) message;
        MqttSubscribe mqttSubscribe = (MqttSubscribe) registerAck(subAck.getPacketIdentifier());
        Subscription[] subscriptions = mqttSubscribe.getSubscriptions();
        Integer[] returnCodes = subAck.getReturnCodes();
        for(int i = 0; i < returnCodes.length ; i++) {
        	if(returnCodes[i]!=128) {
        		subscriptions[i].setMaxQos(returnCodes[i]);
        		subscribedTopics.put(subscriptions[i].getTopic(),returnCodes[i]);
        	}
        }
        System.out.println("received subAck: "+subAck.getPacketIdentifier());
    }

    private void processPublish(Object message) {
        MqttPublish publish = (MqttPublish) message;
        System.out.println("Received message with id: "+publish.getPacketIdentifier());
			try {
				if(publish.getQos()==0) {
					System.out.println("qos: "+publish.getQos());
					putPublishMessage(publish);
				}
				else if(publish.getQos()==1) {
					System.out.println("qos: "+publish.getQos());
					sendMessage(new MqttPubAck(publish.getPacketIdentifier()));
					putPublishMessage(publish);
				}
				else if(publish.getQos()==2) {
					System.out.println("qos: "+publish.getQos());
					int packetId = publish.getPacketIdentifier();
					if(!receivedQos2Messages.contains(packetId)) {
						sendMessage(new MqttPubRec(packetId));
						putPublishMessage(publish);
						receivedQos2Messages.add(packetId);
						System.out.println("Adding to receivedMessages: "+packetId);
					}
					else
						System.out.println("Message already in receivedMessages: "+packetId);
				}
				else {
					System.out.println("no qos: "+publish.getQos());
				}
					
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    }
    
    private void processUnsubAck(Object message) {
        MqttUnsubAck mqttUnsubAck = (MqttUnsubAck) message;
        MqttUnsubsribe mqttUnsubscribe = (MqttUnsubsribe) registerAck(mqttUnsubAck.getPacketIdentifier());
        String[] subscriptions = mqttUnsubscribe.getTopics();
        for(String subscription : subscriptions)
        	subscribedTopics.remove(subscription);
    }
    
    private void processPubAck(Object message) {
    	MqttPubAck mqttPubAck = (MqttPubAck) message;
    	registerAck(mqttPubAck.getPacketIdentifier());
    }
    
    private void processPubRec(Object message) {
    	MqttPubRec mqttPubReq = (MqttPubRec) message;
    	int packetId = mqttPubReq.getPacketIdentifier();
    	registerAck(packetId);
    	System.out.println("Received pubRec: " +packetId);
    	try {
			sendMessage(new MqttPubRel(packetId));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void processPubRel(Object message) {
    	MqttPubRel mqttPubRel = (MqttPubRel) message;
    	int packetId = mqttPubRel.getPacketIdentifier();
    	registerAck(packetId);
    	System.out.println("Received pubRel: "+packetId);
    	for(Integer i :receivedQos2Messages) {
    		if(i.equals(packetId)) {
    			receivedQos2Messages.remove(i);
    			System.out.println("Removed from receivedMessages: "+packetId);
    		}
    		
    	}
    		
    	try {
			sendMessage(new MqttPubComp(packetId));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void processPubComp(Object message) {
    	MqttPubComp mqttPubComp = (MqttPubComp) message;
    	int packetId = mqttPubComp.getPacketIdentifier();
    	registerAck(packetId);
    	System.out.println("Pubcomp registered: "+packetId);
    }

    private Object registerAck(int identifier) {
    	CheckAckTimer timer = unacknowledgedMessages.get(identifier);
    	//System.out.println("cancelling "+identifier);
    	if(timer==null) {
    		System.out.println("Couldn't find id " + identifier);
    		return null;
    	}
    	else {
    		timer.cancel();
    		unacknowledgedMessages.remove(identifier);
    	}
    	return timer.getMessage();
    }

    protected void resendMessage(Object message) throws InterruptedException {
    	MqttMessage mqttMessage = (MqttMessage) message;
    	System.out.println("Resending message with id: "+ mqttMessage.getPacketIdentifier());
    	unacknowledgedMessages.remove(mqttMessage.getPacketIdentifier());
    	if(mqttMessage instanceof MqttPublish) {
    		MqttPublish mqttPublish = (MqttPublish) mqttMessage;
    		mqttPublish.setDuplicate(true);
    		message = mqttPublish;
    	}
    	sendMessage(message);
    }

    protected synchronized void putMessage(Object message) throws InterruptedException {
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
    
    private synchronized void putPublishMessage(MqttPublish message) throws InterruptedException {
        while (publishMessages.size() >= MAXQUEUE)
            wait();
        publishMessages.addElement(message);
        notify();
    }

    public synchronized MqttPublish receiveMessage() throws InterruptedException {
        notify();
        while (publishMessages.size() == 0)
            wait();
        MqttPublish message = publishMessages.firstElement();
        publishMessages.removeElement(message);
        return message;
    }
    
    public synchronized void waitForConnection() throws InterruptedException {
    	notify();
    	while(!connectionEstablished)
    		wait();
    }
    
    private synchronized void setConnectionEstablished() throws InterruptedException {
    	connectionEstablished = true;
    	notify();
    }

    public void sendMessage(Object message) throws InterruptedException {
        MqttMessage mqttMessage = (MqttMessage) message;
        if(mqttMessage.isAckRequired()) {
        	Integer packetId = mqttMessage.getPacketIdentifier();;
        	if(mqttMessage instanceof MqttConnect)
        		packetId = 0;
        	else if(packetId == null) { // if packetId is set, the message is retransmitted
        		packetId = generatePacketIdentifier();
        		mqttMessage.setPacketIdentifier(packetId);
        		System.out.println("Sending message: "+packetId);
        	}
        	CheckAckTimer ackTimer = new CheckAckTimer(message,expectedRTT,this);
        	unacknowledgedMessages.put(packetId, ackTimer);
        	//System.out.println("putting message in unacknowledgedMessages: "+packetId);
        }
        
        if(mqttMessage instanceof MqttDisconnect) {
        	inStream.stop();
        	outStream.stop();
        	putMessage(null);
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
    
    public String[] getSubscribedTopics() {
    	return subscribedTopics.keySet().toArray(new String[subscribedTopics.size()]);		
    }

}