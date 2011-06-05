package jobs;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

import play.jobs.Job;
import play.jobs.OnApplicationStart;
import controllers.Application;

@OnApplicationStart
public class Bootstrap extends Job<Void> {
	
	@Inject
	private static CamelContext camel;
	
	public void doJob(){
		try {
			RouteBuilder builder = new RouteBuilder() {
				
				@Override
				public void configure() throws Exception {

					from("direct:chat").id("direct_chat").to("activemq:topic:chat");
					
					from("activemq:topic:chat").id("chat").bean(Application.class, "onMessage");
					
				}
				
			};
			camel.addRoutes(builder);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
