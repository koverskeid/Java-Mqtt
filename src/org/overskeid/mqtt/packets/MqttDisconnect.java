package org.overskeid.mqtt.packets;

public class MqttDisconnect extends MqttMessage {

	private final static boolean ackRequired = false;
	private final static byte byte1 = (byte) 0xE0;
	private final static byte byte2 = (byte) 0x00;
	
	public MqttDisconnect() {
		super(ackRequired);
	}

	@Override
	protected void createMessage() {
		this.message[0] = byte1;
		this.message[1] = byte2;
	}
}
