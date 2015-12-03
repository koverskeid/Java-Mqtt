package org.overskeid.mqtt.packets;

/**
 * Created by Kristian on 20.11.2015.
 */
public class MqttSubAck extends MqttMessage{
    public enum ReturnCode {
        SUCCESS_MAXIMUM_QOS_0, SUCCESS_MAXIMUM_QOS_1, SUCCESS_MAXIMUM_QOS_2, FAILURE
    }

    private Integer[] returnCodes;

    public MqttSubAck(byte[] message) {
        super(message);
    }

    public Integer[] getReturnCodes() {return this.returnCodes;}
    

	@Override
	void formatMessage() {
		this.packetIdentifier = message[2] & 0xFF | (message[1] & 0xFF) << 8;
		this.returnCodes = new Integer[message.length-3];
        for(int i=0;i<this.message.length-3;i++) {
            returnCodes[i] = this.message[i+3] & 0xFF;
            System.out.println("ReturnCode: "+returnCodes[i]);
        }
		
	}

	@Override
	protected void createMessage() {
		// Only incoming
		
	}
}
