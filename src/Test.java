import org.overskeid.mqtt.packets.MqttDisconnect;
import org.overskeid.mqtt.packets.MqttPublish;
import org.overskeid.mqtt.packets.MqttSubscribe;
import org.overskeid.mqtt.packets.MqttUnsubsribe;
import org.overskeid.mqtt.service.MqttCommonucationHandler;

public class Test {

	public static void main(String[] args) {
		MqttCommonucationHandler communicationHandler = new MqttCommonucationHandler("test.mosquitto.org", 1883, "KristianStationary");
		new Thread(communicationHandler).start();
		try {
			communicationHandler.waitForConnection();
			System.out.println("Connection Established");
			//communicationHandler.sendMessage(new MqttSubscribe("kristian/#", 0));
			communicationHandler.sendMessage(new MqttSubscribe("kristian/test", 2));
			//communicationHandler.sendMessage(new MqttPublish("kristian/test", "test1", 2));
			System.out.println(communicationHandler.receiveMessage().getPayload());
			//System.out.println("topics:");
			//for(String s : communicationHandler.getSubscribedTopics())
				//System.out.println(s);
			//communicationHandler.sendMessage(new MqttUnsubsribe("kristian/test"));
			//communicationHandler.sendMessage(new MqttPublish("kristian/test", "test2", 0));
			//System.out.println(communicationHandler.receiveMessage().getPayload());
			//System.out.println("topics:");
			//for(String s : communicationHandler.getSubscribedTopics())
				//System.out.println(s);
			//communicationHandler.sendMessage(new MqttDisconnect());
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
