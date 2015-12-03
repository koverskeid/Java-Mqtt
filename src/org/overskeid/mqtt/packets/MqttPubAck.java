package org.overskeid.mqtt.packets;

import java.util.ArrayList;

public class MqttPubAck extends MqttQosMessage {
	private static final boolean ackRequired = false;
	private static final byte byte1 = (byte) 64 & 0xFF;
	
	public MqttPubAck(int packetIdentifier) {
		super(ackRequired, packetIdentifier,byte1);
	}
	
	public MqttPubAck(byte[] message) {
		super(message);
	}
}
