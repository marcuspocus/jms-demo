package jobs;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

import controllers.Application;

import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Bootstrap extends Job<Void> {
	
	@Inject
	private static CamelContext camel;
	
	public void doJob(){
		try {
			RouteBuilder builder = new RouteBuilder() {
				
				@Override
				public void configure() throws Exception {
					from("activemq:topic:chat").id("chat").bean(Application.class, "onMessage");
				}
				
			};
			camel.addRoutes(builder);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
