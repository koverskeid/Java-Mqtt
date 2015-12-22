package org.overskeid.mqtt.packets;

public class MqttPingReq extends MqttMessage{

	private static final boolean ackRequired = true;
	private static final byte byte1 = (byte) (192 & 0xFF);
	private static final byte byte2 = (byte) (0 & 0xFF);
	
	public MqttPingReq() {
		super(ackRequired);
	}
	
	@Override
	protected void createMessage() {
		this.message[0] = byte1;
		this.message[1] = byte2;
	}

	@Override
	void formatMessage() {
		//Only outgoing
	}

	
}
