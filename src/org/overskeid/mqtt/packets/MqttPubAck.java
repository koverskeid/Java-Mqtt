package org.overskeid.mqtt.packets;

public class MqttPubAck extends MqttMessage {
	private static final boolean ackRequired = false;

	public MqttPubAck(byte[] message) {
		super(message);
	}
	
	public MqttPubAck(int packetIdentifier) {
		super(ackRequired);
		this.packetIdentifier = packetIdentifier;
	}

	void formatMessage() {
		extractPacketIdentifier();
	}

	@Override
	protected void createMessage() {
		// TODO Auto-generated method stub
		
	}

}
