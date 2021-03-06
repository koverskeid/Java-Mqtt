package org.overskeid.mqtt.service;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.overskeid.mqtt.packets.CreateMqttMessage;

/**
 * Created by Kristian on 07.11.2015.
 *
 * Receives MQTT v3.1.1 messages. Removes the remaining length field and puts the message, including byte1, in the communications handlers input queue.
 */

public class MqttInStream implements Runnable{
    private Socket socket = null;
    private InputStream inStream;
    private DataInputStream dataInStream = null;
    private MqttClient commonucationHandler;
    
    public MqttInStream(Socket socket, MqttClient communicationHandler) {
    	this.socket = socket;
    	this.commonucationHandler = communicationHandler;
    	new Thread(this).start();
	}

    @Override
    public void run() {
        try {
            inStream = socket.getInputStream();
            dataInStream = new DataInputStream(inStream);
            byte [] message;
            while (inStream!=null) {
                byte byte1 = dataInStream.readByte();
                int remainingLength = readRemainingLengthField();
                message = new byte[remainingLength+1];
                message[0] = byte1;
                byte[] messageBuffer = new byte[remainingLength];
                dataInStream.read(messageBuffer);
                System.arraycopy(messageBuffer, 0, message, 1, remainingLength);
                commonucationHandler.putMessage(CreateMqttMessage.getMessage(message));

            }
        } catch (IOException e) {
        	 e.printStackTrace();
        	 System.out.println("closing inStream");
        } catch (InterruptedException e) {
        	 e.printStackTrace();
        	 System.out.println("closing inStream");
        }
    }

    /**
     * Reads the remaining length field according to the standard described in
     * http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718023
     */
    private int readRemainingLengthField() throws java.io.IOException{
        StringBuilder stringBuilder = new StringBuilder("");
        for(int byteNb=0;byteNb<5;byteNb++) {
            byte remainingLengthByte = dataInStream.readByte();
            stringBuilder.insert(0, (String.format("%8s", Integer.toBinaryString(remainingLengthByte & 0xFF)).replace(' ', '0')).substring(1));
            if(remainingLengthByte>=0) //does not start with 1(java uses signed magnitude)
                break;
        }
        return Integer.parseInt(stringBuilder.toString(),2);
    }

    public void stop() {
    	try {
			socket.shutdownInput();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        inStream = null;
    }

    public void setSocket(Socket socket){
        this.socket = socket;
    }

    public void setCommonucationHandler(MqttClient commonucationHandler) {
        this.commonucationHandler = commonucationHandler;
    }
}