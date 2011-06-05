package models;

import java.util.List;

import play.libs.F.ArchivedEventStream;
import play.libs.F.EventStream;

public class EchoEvents {
	
	public static ArchivedEventStream<ChatMessage> stream = new ArchivedEventStream<ChatMessage>(100);
	
	public static EventStream<ChatMessage> getEventStream(){
		return stream.eventStream();
	}
	
	public static void publish(ChatMessage event){
		stream.publish(event);
	}
	
	public static List<ChatMessage> getHistory(){
		return stream.archive();
	}

}
