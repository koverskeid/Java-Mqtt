package org.overskeid.mqtt.packets;

/**
 * Created by Kristian on 20.11.2015.
 */
public class MqttConnAck extends MqttMessage{

    public enum ReturnCode {
        CONNECTION_ACCEPTED, UACCEPTABLE_PROTOCOL_VERSION, IDENTIFIER_REJECTED, SERVER_UNAVAILABLE, BAD_USER_NAME_OR_PASSWORD, NOT_AUTHORIZED
    }

    private ReturnCode returnCode;
    private boolean sessionPreset;

    public MqttConnAck(byte[] message) {
        super(message);
        formatMessage();
    }
    
	@Override
	void formatMessage() {
		sessionPreset = (message[1] & 0xFF) > 0;
		setReturnCode();
	}

    private void setReturnCode() {
        switch (message[2]) {
            case 0: returnCode = ReturnCode.CONNECTION_ACCEPTED;
                break;
            case 1: returnCode = ReturnCode.UACCEPTABLE_PROTOCOL_VERSION;
                break;
            case 2: returnCode = ReturnCode.IDENTIFIER_REJECTED;
                break;
            case 3: returnCode = ReturnCode.SERVER_UNAVAILABLE;
                break;
            case 4: returnCode = ReturnCode.BAD_USER_NAME_OR_PASSWORD;
                break;
            case 5: returnCode = ReturnCode.NOT_AUTHORIZED;
                break;
        }
    }

    public boolean isSessionPreset() {return sessionPreset;}

    public ReturnCode getReturnCode() {return returnCode;}

	@Override
	protected void createMessage() {
		// only incoming
		
	}

}
