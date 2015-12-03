package org.overskeid.mqtt.packets;

import java.util.ArrayList;

/**
 * Created by Kristian on 09.11.2015.
 */
public class MqttConnect extends MqttMessage{
    private static final boolean ackRequired = true;
    private static final int standardKeepAlive = 180; //3 minutes
    private static final byte byte1 = (byte) (16 & 0xFF);
    private static final byte lengthMsb = (byte) (0 & 0xFF);
    private static final byte lengthLsb = (byte) (4 & 0xFF);
    private static final byte[] protocolName = "MQTT".getBytes();
    private static final byte protoolLevel = (byte) (4 & 0xff);
    private static final boolean reserved = false;
    private boolean userNameFlag;
    private boolean passwordFlag;
    private boolean willRetain;
    private int willQos;
    private boolean willFlag;
    private boolean cleanSession;
    private int keepAlive;
    private String clientIdentifier;
    private String willTopic;
    private String willMessage;
    private String userName;
    private String password;
    private byte[] message;

    public MqttConnect(String clientIdentifier) {
        super(ackRequired);
        this.clientIdentifier = clientIdentifier;
        this.keepAlive = standardKeepAlive;
        this.userNameFlag = false;
        this.passwordFlag = false;
        this.willRetain = false;
        this.willFlag = false;
        this.cleanSession = true;
    }

    protected void createMessage() {
        ArrayList<Byte> message = new ArrayList<Byte>();
        message.add(byte1);
        message.add(lengthMsb);
        message.add(lengthLsb);
        for(byte b : protocolName)
            message.add(b);
        message.add(protoolLevel);
        message.add(createConnectFlagsByte());
        message.add((byte) ((keepAlive >> 8) & 0xFF));
        message.add((byte) (keepAlive & 0xFF));
        message = addDataField(clientIdentifier,message);
        if(willFlag) {
            message = addDataField(willTopic, message);
            message = addDataField(willMessage,message);
        }
        if(userNameFlag)
            message = addDataField(userName, message);
        if(passwordFlag)
            message = addDataField(password, message);
        this.message = new byte[message.size()];
        for(int i=0;i<message.size();i++)
            this.message[i] = message.get(i);
        addRemainingLengthField(message.size()-1, message);
    }

    private byte createConnectFlagsByte() {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.insert(0, (String.format("%1s", Integer.toBinaryString((userNameFlag) ? 1:0 & 0xFF)).replace(' ', '0')));
        stringBuilder.insert(1, (String.format("%1s", Integer.toBinaryString((passwordFlag) ? 1:0 & 0xFF)).replace(' ', '0')));
        stringBuilder.insert(2, (String.format("%1s", Integer.toBinaryString((willRetain) ? 1:0 & 0xFF)).replace(' ', '0')));
        stringBuilder.insert(3, (String.format("%2s", Integer.toBinaryString(willQos & 0xFF)).replace(' ', '0')));
        stringBuilder.insert(5, (String.format("%1s", Integer.toBinaryString((willFlag) ? 1:0 & 0xFF)).replace(' ', '0')));
        stringBuilder.insert(6, (String.format("%1s", Integer.toBinaryString((cleanSession) ? 1:0 & 0xFF)).replace(' ', '0')));
        stringBuilder.insert(7, (String.format("%1s", Integer.toBinaryString((reserved) ? 1:0 & 0xFF)).replace(' ', '0')));
        return (byte) (Integer.parseInt(stringBuilder.toString(), 2) & 0xFF);
    }


    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setWill(String topic, String message, int qos) {
        this.willTopic = topic;
        this.willMessage = message;
        this.willQos = qos;
        this.willFlag = true;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        this.userNameFlag = userName!=null;
    }

    public void setPassword(String password) {
        this.password = password;
        this.passwordFlag = password!=null;
    }

    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

	@Override
	void formatMessage() {
		// TODO Auto-generated method stub
		
	}
}
