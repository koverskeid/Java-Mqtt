package org.overskeid.mqtt.packets;

public class MqttUnsubAck extends MqttAck{

	public MqttUnsubAck(byte[] message) {
		super(message);
	}

	@Override
	void formatMessage() {
		setPacketIdentifier();
	}

}
