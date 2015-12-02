package org.overskeid.mqtt.packets;

/**
 * Created by Kristian on 20.11.2015.
 */
public class MqttSubAck extends MqttAck{
    public enum ReturnCode {
        SUCCESS_MAXIMUM_QOS_0, SUCCESS_MAXIMUM_QOS_1, SUCCESS_MAXIMUM_QOS_2, FAILURE
    }

    private ReturnCode[] returnCodes;
    private byte[] message;
    private int packetIdentifier;

    public MqttSubAck(byte[] message) {
        super(message);
    }

    public ReturnCode[] getReturnCodes() {return this.returnCodes;}
    
    public int getPacketIdentifier() {return packetIdentifier;}

	@Override
	void formatMessage() {
		setPacketIdentifier();
		this.returnCodes = new ReturnCode[message.length-3];
        for(int i=0;i<this.message.length-3;i++) {
            switch ((int) (this.message[i+3] & 0xFF)) {
                case 0: this.returnCodes[i] = ReturnCode.SUCCESS_MAXIMUM_QOS_0;
                    break;
                case 1: this.returnCodes[i] = ReturnCode.SUCCESS_MAXIMUM_QOS_1;
                    break;
                case 2: this.returnCodes[i] = ReturnCode.SUCCESS_MAXIMUM_QOS_2;
                    break;
                case 128: this.returnCodes[i] = ReturnCode.FAILURE;
            }
            
        }
		
	}
}
