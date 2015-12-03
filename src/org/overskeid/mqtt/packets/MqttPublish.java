package org.overskeid.mqtt.packets;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Kristian on 09.11.2015.
 */
public class MqttPublish extends MqttMessage{
    private static final int messageType = 3;
    private boolean duplicate;
    private boolean retain;
    private int qos;
    private String topic;
    private String payload;

    public MqttPublish(String topic, String payload, int qos) { //outgoing
        super(qos>0);
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        duplicate = false;
        retain = false;
    }

    public MqttPublish(byte[] message) { //incoming
        super(message);
    }


    @Override
    protected void createMessage() {
        ArrayList<Byte> message = new ArrayList<Byte>();
        //Fixed header
        message.add(createByte1());
        //Variable header
        message.add((byte) ((topic.length() >> 8) & 0xFF)); //topic length msb
        message.add((byte) (topic.length() & 0xFF)); //topic length lsb
        for(char c :this.topic.toCharArray()) // topic
            message.add((byte) c);
        if(qos>0) {
            message.add((byte) ((this.packetIdentifier >> 8) & 0xFF)); //packet identifier msb
            message.add((byte) (this.packetIdentifier & 0xFF)); //packet identifier lsb
        }
        for(char c :this.payload.toCharArray()) // payload
            message.add((byte) c);
        //Fixed header - remaining length field;
        int remainingLength = message.size()-1;
        addRemainingLengthField(remainingLength,message);
        String.format("%8s", Integer.toBinaryString(this.message[0] & 0xFF)).replace(' ', '0');
    }

    private byte createByte1() {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.insert(0, (String.format("%4s", Integer.toBinaryString(messageType & 0xFF)).replace(' ', '0')));
        stringBuilder.insert(4, (String.format("%1s", Integer.toBinaryString((duplicate) ? 1:0 & 0xFF)).replace(' ', '0')));
        stringBuilder.insert(5, (String.format("%2s", Integer.toBinaryString(qos & 0xFF)).replace(' ', '0')));
        stringBuilder.insert(7, (String.format("%1s", Integer.toBinaryString((retain) ? 1 : 0 & 0xFF)).replace(' ', '0')));
        return (byte) (Integer.parseInt(stringBuilder.toString(), 2) & 0xff);
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public String getTopic() {
        return this.topic;
    }

    public String getPayload() {
        return payload;
    }

    public int getQos() {
        return qos;
    }

    public boolean isRetain() {
        return this.retain;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

	@Override
	void formatMessage() {
		String binary = String.format("%8s", Integer.toBinaryString(message[0] & 0xFF)).replace(' ', '0');
        this.duplicate = binary.substring(4,5).equals("1");
        qos = Integer.parseInt(binary.substring(5, 7),2);
        retain = binary.substring(7).equals("1");
        int index = 0;
        try {
            int remainingLength = remainingLength(index+=1);
            topic = new String(Arrays.copyOfRange(message, index+=2, index+=remainingLength), "UTF-8");
            if(qos==0)
                payload = new String(Arrays.copyOfRange(message, index,message.length), "UTF-8");
            else {
            	packetIdentifier = getPacketId(index);
            	payload = new String(Arrays.copyOfRange(message, index+=2,message.length), "UTF-8");
                
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
	}
}