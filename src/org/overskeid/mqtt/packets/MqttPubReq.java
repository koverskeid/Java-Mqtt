package org.overskeid.mqtt.packets;

public class MqttPubReq extends MqttAck{

	public MqttPubReq(byte[] message) {
		super(message);
	}

	@Override
	void formatMessage() {
		setPacketIdentifier();
	}

}
