package org.overskeid.mqtt.packets;

public abstract class MqttAck {

	protected byte[] message;
	protected int packetIdentifier;
	protected boolean ackRequired;
	

	public MqttAck(byte[] message) {
		this.message = message;
		formatMessage();
	}
	
	abstract void formatMessage();
	
	protected void setPacketIdentifier() {
		this.packetIdentifier = message[2] & 0xFF | (message[1] & 0xFF) << 8;
	}
	
	public int getPackeIdentifier() {return packetIdentifier;}
}
