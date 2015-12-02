package org.overskeid.mqtt.packets;

import java.util.ArrayList;

/**
 * Created by Kristian on 20.11.2015.
 */
public abstract class MqttMessage {
    public enum MessageType {
        CONNECT, CONNACK, PUBLISH, PUBACK, PUBREC, PUBREL, PUBCOMP, SUBSCRIBE, SUBACK, UNSUBSCRIBE, UNSUBACK, PINREQ, PINGRESP, DISCONNECT
    }

    protected static final int maxRemainingLength = 268435455;
    public static final int maxPacketIdNb = 2^16;
    private boolean ackRequired;
    protected byte[] message;
    protected int packetIdentifier;

    public MqttMessage(boolean ackRequired) {
        this.ackRequired = ackRequired;
    }
    
    public MqttMessage(boolean ackRequired, int packetIdentifier) {
    	this.ackRequired = ackRequired;
    	this.packetIdentifier = packetIdentifier;
    }
    
    public MqttMessage(byte[] message) {
    	this.message = message;
    }

    protected byte[] createRemainingLengthField(int remainingLength) {
        if(remainingLength>maxRemainingLength) {
            System.out.println("Message is too long, removing "+(remainingLength - maxRemainingLength)+" last bytes from the payload");
            remainingLength = maxRemainingLength;
        }
    	byte[] temp = new byte[4];
        int i = 0;
        while(remainingLength > 0) {
            int encodedByte = remainingLength % 128;
            remainingLength = remainingLength / 128;
            if(remainingLength > 0)
                encodedByte = encodedByte | 128;
            temp[i++] = (byte)(encodedByte & 0xFF);
        }
        byte[] remainingLengthField = new byte[i];
        System.arraycopy(temp, 0, remainingLengthField, 0, i);
        return remainingLengthField;
    }

    protected int remainingLength(int startByte) {
        return getPacketId(startByte);
    }

    protected int getPacketId(int startByte) {
        return message[startByte+1] & 0xFF | (message[startByte] & 0xFF) << 8;
    }

    protected void addRemainingLengthField(int remainingLength, ArrayList<Byte> message) {
    	byte[] remainingLengthField = createRemainingLengthField(remainingLength);;
        for(int i = 0; i<remainingLengthField.length;i++)
            message.add(i+1,remainingLengthField[i]);
        this.message = new byte[message.size()];
        for(int i = 0;i < message.size();i++)
            this.message[i] = message.get(i);
    }

    protected ArrayList<Byte> addDataField(String data, ArrayList<Byte> message) {
        message.add((byte) ((data.length() >> 8) & 0xFF));;
        message.add((byte) (data.length() & 0xFF));
        for(char c : data.toCharArray())
            message.add((byte) c);
        return message;
    }

    public boolean isAckRequired() {
        return this.ackRequired;
    }

    protected void extractPacketIdentifier() {
    	this.packetIdentifier = message[2] & 0xFF | (message[1] & 0xFF) << 8;
    }
    
    protected abstract void createMessage();
    
    public void setPacketIdentifier(int packetId) {packetIdentifier = packetId;}
    public int getPacketIdentifier() {return packetIdentifier;}

    public byte[] getBytes() {
        createMessage();
        return this.message;
    }
}
