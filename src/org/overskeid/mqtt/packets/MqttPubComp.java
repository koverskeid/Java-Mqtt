package org.overskeid.mqtt.packets;

public class MqttPubComp extends MqttQosMessage{

	private static final boolean ackRequired = false;
	private static final byte byte1 = (byte) 112 & 0xFF;
	
	public MqttPubComp(int packetIdentifier) {
		super(ackRequired, packetIdentifier, byte1);
	}
	
	public MqttPubComp(byte[] message) {
		super(message);
	}

}
