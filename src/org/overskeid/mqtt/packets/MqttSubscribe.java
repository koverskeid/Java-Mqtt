package org.overskeid.mqtt.packets;

import java.util.ArrayList;

/**
 * Created by Kristian on 20.11.2015.
 */

public class MqttSubscribe extends MqttMessage{
	
	
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

    public Subscription[] getSubscriptions() {
        Subscription[] subscriptions = new Subscription[this.subscriptions.size()];
    	return this.subscriptions.toArray(subscriptions);
    }

	@Override
	void formatMessage() {
		// TODO Auto-generated method stub
		
	}
}