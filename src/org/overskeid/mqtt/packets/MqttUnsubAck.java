package org.overskeid.mqtt.packets;

public class MqttUnsubAck extends MqttMessage{

	public MqttUnsubAck(byte[] message) {
		super(message);
	}

	@Override
	void formatMessage() {
		this.packetIdentifier = message[2] & 0xFF | (message[1] & 0xFF) << 8;
	}

	@Override
	protected void createMessage() {
		//Only incoming
	}

}
