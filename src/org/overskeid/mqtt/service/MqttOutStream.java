package org.overskeid.mqtt.service;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

/**
 * Created by Kristian on 07.11.2015.
 */
public class MqttOutStream implements Runnable {
    private static final int MAXQUEUE = 10;
    private Vector<byte[]> messages;

    private Socket socket;
    private OutputStream outStream;
    private DataOutputStream dataOutStream;

    public MqttOutStream(Socket socket) {
    	this.socket = socket;
    	this.messages = new Vector<byte[]>();
    	
    }

    @Override
    public void run() {
        try {
            outStream = socket.getOutputStream();
            dataOutStream = new DataOutputStream(outStream);
            while(outStream!=null) {
            	byte[] message = getMessage();
                dataOutStream.write(message);
                dataOutStream.flush();
            }
            System.out.println("closing outStream");
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    
    public void stop()  {
    	outStream = null;
    }
}
