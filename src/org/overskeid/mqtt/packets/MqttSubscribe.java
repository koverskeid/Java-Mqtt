package org.overskeid.mqtt.packets;

import java.util.ArrayList;

/**
 * Created by Kristian on 20.11.2015.
 */

public class MqttSubscribe extends MqttMessage{
	
	public class Subscription {
		private String topic;
		private int maxQos;
		public Subscription(String topic, int maxQos) {
			this.topic = topic;
			this.maxQos = maxQos;
		}
		public Subscription(String topic) {
			this.topic = topic;
			this.maxQos = -1;
		}
		public String getTopic() {return topic;}
		public int getMaxQos() {return maxQos;}
	}
	
	private static final boolean ackRequired = true;
    private static final byte byte1 = (byte) (130 & 0xFF);
    private ArrayList<Subscription> subscriptions;

    public MqttSubscribe(String topic, int maxQos) {
        super(ackRequired);
        this.subscriptions = new ArrayList<Subscription>();
        this.subscriptions.add(new Subscription(topic, maxQos));
    }

    public MqttSubscribe(Subscription[] subscriptions) {
        super(ackRequired);
        this.subscriptions = new ArrayList<Subscription>();
        for(Subscription s : subscriptions)
            this.subscriptions.add(s);
    }

    @Override
    protected void createMessage() {
        ArrayList<Byte> message = new ArrayList<Byte>();
        message.add(byte1);

        //Variable header
        message.add((byte) ((this.packetIdentifier >> 8) & 0xFF)); //topic length msb
        message.add((byte) (this.packetIdentifier & 0xFF)); //topic length lsb
        //payload
        for(Subscription s : subscriptions) {
            message = addDataField(s.getTopic(), message);
        	message.add((byte)(s.getMaxQos() & 0xFF));
        }
        int remainingLength = message.size()-1;
       addRemainingLengthField(remainingLength,message);
    }

    public String[] getSubscriptions() {
        String[] topics = new String[this.subscriptions.size()];
        for(int i=0;i < this.subscriptions.size();i++)
            topics[i] = this.subscriptions.get(i).getTopic();
        return topics;
    }
}
