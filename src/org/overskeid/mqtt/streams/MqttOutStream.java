package org.overskeid.mqtt.streams;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

import org.overskeid.mqtt.packets.MqttConnect;

/**
 * Created by Kristian on 07.11.2015.
 */
public class MqttOutStream implements Runnable {
    private static final int MAXQUEUE = 10;
    private Vector<byte[]> messages;

    private Socket socket;
    private String clientId;
    private OutputStream outStream;
    private DataOutputStream dataOutStream;

    public MqttOutStream(Socket socket) {
    	this.socket = socket;
    	this.messages = new Vector<byte[]>();
    	this.clientId = "100986";
    	
    }


    private byte[] createConnectMessage() {
        byte[] connectMessage = new byte[14+clientId.length()];
        byte [] connectMessageFixed = {16 //Message type = 16 (connect)
                , (byte) (12+clientId.length())
                , 0 // MSB = 0
                , 4 // LSB = 4
                , 77 //M
                , 81 //Q
                , 84 //T
                , 84 //T
                , 4 //Description = 4 (V3.1.1)
                , 2 //MqttConnect flags = 2 (clean session - 00000010)
                , 0 //Keep Alive MSB = 0
                , 60 //Keep alive LSB = 05 seconds
                , 0 //String length MSB = 0
                , (byte) clientId.length() // String length LSB
        };
        byte[] bAndroidId = clientId.getBytes();
        System.arraycopy(connectMessageFixed, 0, connectMessage, 0, connectMessageFixed.length);
        System.arraycopy(bAndroidId, 0, connectMessage, connectMessageFixed.length, bAndroidId.length);
    return connectMessage;
    }


    @Override
    public void run() {
        try {
        	System.out.println("static:");
            outStream = socket.getOutputStream();
            dataOutStream = new DataOutputStream(outStream);
            while(socket!=null) {
                dataOutStream.write(getMessage());
                dataOutStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(String topic, int qos) throws InterruptedException {
        while(this.messages.size() >= MAXQUEUE)
            wait();
        this.messages.addElement(createSubscribeMessage(topic, qos));
        notify();
    }

    public synchronized void send(byte[] message) throws InterruptedException {
        while(this.messages.size() >= MAXQUEUE)
            wait();
        this.messages.addElement(message);
        notify();
    }

    private synchronized byte[] getMessage() throws InterruptedException {
        notify();
        while(this.messages.size() == 0) {
            wait();
        }
        byte[] message = messages.firstElement();
        this.messages.removeElement(message);
        return message;
    }

    private byte[] createSubscribeMessage(String topic, int qos) {
        byte[] message = new byte[7 + topic.length()];

        //HEADER
        message[0] = (byte) 130;
        message[1] = (byte) (5 + topic.length());
        message[2] = (byte) 0; //Message ID MSB
        message[3] = (byte) 10; //Message ID LSB

        //PAYLOAD
        message[4] = (byte) 0; //Length MSB
        message[5] = (byte) topic.length(); //Length LSB
        byte[] bTopic = topic.getBytes();
        System.arraycopy(bTopic, 0, message, 6, bTopic.length);
        message[6 + bTopic.length] = (byte) qos;
        return message;
    }
}
