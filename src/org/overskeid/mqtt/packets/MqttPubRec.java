package org.overskeid.mqtt.packets;

public class MqttPubRec extends MqttQosMessage{
	private static final boolean ackRequired = true;
	private static final byte byte1 = (byte) (80 & 0xFF);

	public MqttPubRec(byte[] message) {
		super(message);
	}
	
	public MqttPubRec(int packetId) {
		super(ackRequired, packetId, byte1);
	}
}
