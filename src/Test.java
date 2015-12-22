import org.overskeid.mqtt.packets.MqttPublish;
import org.overskeid.mqtt.service.MqttClient;

public class Test {
	
	public static void main(String[] args) {
		MqttClient mqttClient = new MqttClient("192.168.1.12", 8883);
		mqttClient.connect("testing");
		try {
			mqttClient.waitForConnection();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("connected");
		mqttClient.subscribeTopic("kristian", 2);
		mqttClient.sendMessage("kristian", "ops", 0);
		try {
			while(true) {
				MqttPublish message = mqttClient.receiveMessage();
				System.out.println("Received " +message.getTopic()+"("+message.getQos()+"): "+message.getPayload());
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
