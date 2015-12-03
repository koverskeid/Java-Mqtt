package org.overskeid.mqtt.packets;

public class MqttPubRel extends MqttQosMessage{
	private static final boolean ackRequired = true;
	private static final byte byte1 = (byte) 98 & 0xFF;
	
	public MqttPubRel(int packetIdentifier) {
		super(ackRequired, packetIdentifier, byte1);
	}
	
	public MqttPubRel(byte[] message) {
		super(message);
	}

	
}
