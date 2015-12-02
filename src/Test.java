import org.overskeid.mqtt.service.MqttCommonucationHandler;

public class Test {

	public static void main(String[] args) {
		MqttCommonucationHandler communicationHandler = new MqttCommonucationHandler("test.mosquitto.org", 1883, "Kristianfffffffffffffffffffffffaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaafffffffffffffffffffffffffffffff");
		new Thread(communicationHandler).start();
	}
}
