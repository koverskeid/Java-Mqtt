package org.overskeid.mqtt.packets;

import java.util.ArrayList;

public class MqttQosMessage extends MqttMessage{
	private byte byte1;
	
	public MqttQosMessage(boolean ackRequired, int packetIdentifier, byte byte1) {
		super(ackRequired, packetIdentifier);
		this.byte1 = byte1;
		createMessage();
	}
	
	public MqttQosMessage(byte[] message) {
		super(message);
		formatMessage();
	}

	void formatMessage() {
		extractPacketIdentifier();
	}

	@Override
	protected void createMessage() {
		ArrayList<Byte> message = new ArrayList<Byte>();
		message.add(byte1);
        message.add((byte) ((this.packetIdentifier >> 8) & 0xFF)); //topic length msb
        message.add((byte) (this.packetIdentifier & 0xFF)); //topic length lsb
        addRemainingLengthField(message.size()-1, message);
	}

}