package org.overskeid.mqtt.packets;

/**
 * Created by Kristian on 09.11.2015.
 */
public class CreateMqttMessage {


    public static Object getMessage(byte[] message) {
        String binary = String.format("%8s", Integer.toBinaryString(message[0] & 0xFF)).replace(' ', '0');
        int packetType = Integer.parseInt(binary.substring(0, 4), 2);
        switch (packetType) {
            case 1: return null; // connect - only outgoing
            case 2: return new MqttConnAck(message);
            case 3: return new MqttPublish(message);
            case 4: return new MqttPubAck(message);
            case 5: return null; //pubrec
            case 6: return null; //pubrel
            case 7: return null; //pubcomp
            case 8: return null; //subscribe - only outgoing
            case 9: return new MqttSubAck(message);
            case 10: return null; //unsubscribe - only outgoing
            case 11: return new MqttUnsubAck(message);
            case 12: return null; //pingReq - only outgoing
            case 13: return new MqttPingResp();
            case 14: return null;// disconnect - only outgoing
        }
        return null;
    }
}
