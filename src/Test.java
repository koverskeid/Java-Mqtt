import org.overskeid.mqtt.packets.MqttDisconnect;
import org.overskeid.mqtt.packets.MqttPublish;
import org.overskeid.mqtt.packets.MqttSubscribe;
import org.overskeid.mqtt.packets.MqttUnsubsribe;
import org.overskeid.mqtt.service.MqttClient;

public class Test {

	public static void main(String[] args) {
		MqttClient mqttClient = new MqttClient("test.mosquitto.org", 1883);
		new Thread(mqttClient).start();
		try {
			mqttClient.connect("KristianStasjo");
			mqttClient.waitForConnection();
			System.out.println("Connection Established");
			//mqttClient.sendMessage(new MqttSubscribe("kristian/#", 0));
			mqttClient.subscribeTopic("kristian/test", 2);
			mqttClient.sendMessage("kristian/test", "test1", 2);
			while(true)
				System.out.println(mqttClient.receiveMessage().getPayload());
			//System.out.println("topics:");
			//for(String s : mqttClient.getSubscribedTopics())
				//System.out.println(s);
			//mqttClient.sendMessage(new MqttUnsubsribe("kristian/test"));
			//mqttClient.sendMessage(new MqttPublish("kristian/test", "test2", 0));
			//System.out.println(mqttClient.receiveMessage().getPayload());
			//System.out.println("topics:");
			//for(String s : mqttClient.getSubscribedTopics())
				//System.out.println(s);
			//mqttClient.sendMessage(new MqttDisconnect());
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
