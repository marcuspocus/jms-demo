package models;

import java.util.List;

import play.libs.F.ArchivedEventStream;
import play.libs.F.EventStream;

public class GraphEvents {
	
	public static ArchivedEventStream<String> stream = new ArchivedEventStream<String>(100);
	
	public static EventStream<String> getEventStream(){
		return stream.eventStream();
	}
	
	public static void publish(String event){
		stream.publish(event);
	}
	
	public List<String> getHistory(){
		return stream.archive();
	}

}
