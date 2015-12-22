package org.overskeid.mqtt.service;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import javax.xml.stream.events.StartDocument;

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
public class MqttClient implements Runnable {
    private static final int standardKeepAlive = 1000;
	private int maxQueue = 10;
    private int expectedRTT = 1000;
    private boolean cleanSession = true;
    private int keepAlive = standardKeepAlive;
    private boolean willRetain = false;
    private Integer willQos = 0;
    private String willTopic;
    private String willMessage;
    private String userName;
    private String password;
    
    private Vector<Object> messages = new Vector<Object>();
    private Vector<MqttPublish> publishMessages = new Vector<MqttPublish>();
    private String address;
    private int port;
    private MqttOutStream outStream;
    private MqttInStream inStream;
    private Hashtable<Integer,ResendTimer> unacknowledgedMessages = new Hashtable<Integer, ResendTimer>();
    private boolean connectionEstablished = false;
    private Hashtable<String, Integer> subscribedTopics = new Hashtable<String, Integer>();
    private Vector<Integer> receivedQos2Messages = new Vector<Integer>();

    public MqttClient(String address, int port) {
    	this.port = port;
        this.address = address;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            Socket socket = new Socket(inetAddress, port);
            inStream = new MqttInStream(socket, this);
            outStream = new MqttOutStream(socket);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
		}
        setReadyToSend();
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
    
    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setWill(String topic, String message, int qos) {
        this.willTopic = topic;
        this.willMessage = message;
        this.willQos = qos;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public void setMaxQueue(int maxQueue) {
    	this.maxQueue = maxQueue;
    }
    
    public void setExpectedRTT(int rtt) {
    	this.expectedRTT = rtt;
    }
    
    public void connect(String clientIdentifier) {
    	try {
			send(new MqttConnect(clientIdentifier,keepAlive,userName,password,willRetain,cleanSession,willTopic,willMessage,willQos));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public synchronized void waitForConnection() throws InterruptedException {
    	while(!connectionEstablished)
    		wait();
    }
    
    public MqttPublish receiveMessage() throws InterruptedException {
        synchronized (publishMessages) {
            while (publishMessages.size() == 0)
                publishMessages.wait();
            MqttPublish message = publishMessages.firstElement();
            publishMessages.removeElement(message);
            publishMessages.notify();
            return message;	
		}
    }
    
    public void sendMessage(String topic, String payload, int qos) {
    	try {
			send(new MqttPublish(topic, payload, qos));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    public void subscribeTopic(String topic, int maxQos) {
    	String[] topics = {topic};
    	try {
			send(new MqttSubscribe(topics, maxQos));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void subscribeTopics(String[] topics, int maxQos) {
    	try {
			send(new MqttSubscribe(topics, maxQos));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
  
    public void unsubscribeTopic(String topic, int maxQos) {
    	String[] topics = {topic};
    	try {
			send(new MqttUnsubsribe(topics));
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
    }
    
    public void unsubscribeTopics(String[] topics, int maxQos) {
    	try {
			send(new MqttUnsubsribe(topics));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public String[] getSubscribedTopics() {
    	return subscribedTopics.keySet().toArray(new String[subscribedTopics.size()]);		
    }
    
    public int getTopicMaxQos(String topic) {
    	return subscribedTopics.get(topic);
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
        //System.out.println("received subAck: "+subAck.getPacketIdentifier());
        MqttSubscribe mqttSubscribe = (MqttSubscribe) registerAck(subAck.getPacketIdentifier());
        Subscription[] subscriptions = mqttSubscribe.getSubscriptions();
        Integer[] returnCodes = subAck.getReturnCodes();
        for(int i = 0; i < returnCodes.length ; i++) {
        	if(returnCodes[i]!=128) {
        		subscriptions[i].setMaxQos(returnCodes[i]);
        		subscribedTopics.put(subscriptions[i].getTopic(),returnCodes[i]);
        	}
        }
        
    }

    private void processPublish(Object message) {
        MqttPublish publish = (MqttPublish) message;
			try {
				if(publish.getQos()==0) {
					putPublishMessage(publish);
				}
				else if(publish.getQos()==1) {
					send(new MqttPubAck(publish.getPacketIdentifier()));
					putPublishMessage(publish);
				}
				else if(publish.getQos()==2) {
					int packetId = publish.getPacketIdentifier();
					if(!receivedQos2Messages.contains(packetId)) {
						receivedQos2Messages.add(packetId);
						putPublishMessage(publish);
						send(new MqttPubRec(packetId));
					}
				}
					
			} catch (InterruptedException e1) {
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
    	try {
			send(new MqttPubRel(packetId));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    private void processPubRel(Object message) {
    	MqttPubRel mqttPubRel = (MqttPubRel) message;
    	int packetId = mqttPubRel.getPacketIdentifier();
    	registerAck(packetId);
    	for(Integer i :receivedQos2Messages) {
    		if(i.equals(packetId)) {
    			receivedQos2Messages.remove(i);
    			break;
    		}
    		
    	}
    		
    	try {
			send(new MqttPubComp(packetId));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    private void processPubComp(Object message) {
    	MqttPubComp mqttPubComp = (MqttPubComp) message;
    	int packetId = mqttPubComp.getPacketIdentifier();
    	registerAck(packetId);
    }

    private Object registerAck(int identifier) {
    	ResendTimer timer = unacknowledgedMessages.get(identifier);
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

    private synchronized void send(Object message) throws InterruptedException {
        while(outStream==null)
        	wait();
    	MqttMessage mqttMessage = (MqttMessage) message;
        if(mqttMessage.isAckRequired()) {
        	Integer packetId = mqttMessage.getPacketIdentifier();;
        	if(mqttMessage instanceof MqttConnect)
        		packetId = 0;
        	else if(packetId == null) { // if packetId is set, the message is retransmitted
        		packetId = generatePacketIdentifier();
        		mqttMessage.setPacketIdentifier(packetId);
        	}
        	ResendTimer ackTimer = new ResendTimer(message,expectedRTT,this);
        	unacknowledgedMessages.put(packetId, ackTimer);
        }
        if(mqttMessage instanceof MqttDisconnect) {
        	inStream.stop();
        	outStream.stop();
        	putMessage(null);
        }
        outStream.send(mqttMessage.getBytes());
    }
    
    private synchronized void setReadyToSend() {
    	notify();
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

    protected void resendMessage(Object message) throws InterruptedException {
    	MqttMessage mqttMessage = (MqttMessage) message;
    	int packetId;
    	if(mqttMessage instanceof MqttConnect)
    		packetId = 0;
    	else
    		packetId = mqttMessage.getPacketIdentifier();
    	unacknowledgedMessages.remove(packetId);
    	if(mqttMessage instanceof MqttPublish) {
    		MqttPublish mqttPublish = (MqttPublish) mqttMessage;
    		mqttPublish.setDuplicate(true);
    		message = mqttPublish;
    	}
    	send(message);
    }

    protected void putMessage(Object message) throws InterruptedException {
        synchronized (messages) {
        	while (messages.size() >= maxQueue)
                messages.wait();
            messages.addElement(message);
            messages.notify();
		}
    }

    private Object getMessage() throws InterruptedException {
    	synchronized (messages) {
    		 while (messages.size() == 0)
    	        	messages.wait();
    	        Object message = messages.firstElement();
    	        messages.removeElement(message);
    	        messages.notify();
    	        return message;
		}  
    }
    
    private void putPublishMessage(MqttPublish message) throws InterruptedException {
        synchronized (publishMessages) {
        	while (publishMessages.size() >= maxQueue)
                publishMessages.wait();
            publishMessages.addElement(message);
            publishMessages.notify();
		}
    }
   
    private synchronized void setConnectionEstablished() throws InterruptedException {
    	connectionEstablished = true;
    	notify();
    }
}
