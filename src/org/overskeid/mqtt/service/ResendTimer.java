package org.overskeid.mqtt.service;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Kristian on 20.11.2015.
 */
public class ResendTimer{

    private Object message;
    private long delay;
    private Timer timer;
    private MqttClient communicationHandler;

    public ResendTimer(Object message, long delay, MqttClient communicationHandler) {
        this.message = message;
        this.delay = delay;
        this.communicationHandler = communicationHandler;
        startTimer();
    }

    private void startTimer() {
        timer = new Timer();
        TimerTask alert = new TimerTask() {
            @Override
            public void run() {
                try {
                    communicationHandler.resendMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(alert, delay);
    }

    public void cancel() {
        timer.cancel();
    }
    
    public Object getMessage() {
    	return message;
    }
}
