package org.overskeid.mqtt.packets;

public class Subscription {
	private String topic;
	private int maxQos;
	public Subscription(String topic, int maxQos) {
		this.topic = topic;
		this.maxQos = maxQos;
	}
	public Subscription(String topic) {
		this.topic = topic;
		this.maxQos = -1;
	}
	public String getTopic() {return topic;}
	public int getMaxQos() {return maxQos;}
	public void setMaxQos(int maxQos) {this.maxQos = maxQos;}
}
