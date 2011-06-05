package models;

import java.io.Serializable;
import java.util.Date;

import play.templates.JavaExtensions;

public class ChatMessage implements Serializable{

	public String timestamp;
	
	public String user;
	
	public String msg;

	public ChatMessage(){}
	
	public ChatMessage(String user, String msg){
		this.timestamp = JavaExtensions.asdate(System.currentTimeMillis(), "HH:mm:ss");
		this.user = user;
		this.msg = msg;
	}
	
}
