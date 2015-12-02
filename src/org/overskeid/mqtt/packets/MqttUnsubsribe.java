package org.overskeid.mqtt.packets;

import java.util.ArrayList;


public class MqttUnsubsribe extends MqttMessage {
	private static final byte byte1 = (byte) (162 & 0xFF);
	private static final boolean ackRequired = true;
	private String[] topics;
	
	public MqttUnsubsribe(String topic) {
		super(ackRequired);
		this.topics[0] = topic;
	}
	
	public MqttUnsubsribe(String[] topics) {
		super(ackRequired);
		this.topics = topics;
	}
	
	@Override
	protected void createMessage() {
		 ArrayList<Byte> message = new ArrayList<Byte>();
	     message.add(byte1);
	     //Variable header
	     message.add((byte) ((this.packetIdentifier >> 8) & 0xFF)); //topic length msb
	     message.add((byte) (this.packetIdentifier & 0xFF)); //topic length lsb
	     //payload
	     for(String t : topics)
	            message = addDataField(t, message);
	     int remainingLength = message.size()-1;
	     addRemainingLengthField(remainingLength,message);
	}
	
	public String[] getTopics() {return topics;}

}
