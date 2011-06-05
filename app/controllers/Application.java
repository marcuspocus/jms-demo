package controllers;

import static play.libs.F.Matcher.ClassOf;
import static play.libs.F.Matcher.Equals;
import static play.mvc.Http.WebSocketEvent.SocketClosed;
import static play.mvc.Http.WebSocketEvent.TextFrame;

import java.util.List;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import models.ChatMessage;
import models.EchoEvents;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import play.Logger;
import play.libs.F.Either;
import play.libs.F.EventStream;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Http.WebSocketClose;
import play.mvc.Http.WebSocketEvent;
import play.mvc.WebSocketController;
import play.mvc.With;

import com.google.gson.Gson;

@With(Secure.class)
public class Application extends Controller {

	@Inject
	private static CamelContext camel;

	public static void index() {
		render();
	}

	public static void history() {
		List<ChatMessage> msgs = EchoEvents.getHistory();
		render(msgs);
	}

	public static void sendDirect() throws Exception {
		String msg = params.get("msg");
		Endpoint e = camel.getEndpoint("direct:chat");
		Exchange exchange = e.createExchange();
		ChatMessage m = new ChatMessage(Secure.Security.connected(), msg);
		exchange.getIn().setBody(m, ChatMessage.class);
		e.createProducer().process(exchange);
	}

	public void onMessage(final ChatMessage msg) {
		Logger.info("Message received in JMS...");
		try {
			EchoEvents.publish(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class WebSocket extends WebSocketController {

		@Inject
		public static JmsTemplate jms;

		public static Gson json = new Gson();

		public static void echo() {

			EventStream<ChatMessage> stream = EchoEvents.getEventStream();

			while (inbound.isOpen()) {
				Either<WebSocketEvent, ChatMessage> e = await(Promise.waitEither(inbound.nextEvent(), stream.nextEvent()));

				for (String quit : TextFrame.and(Equals("quit")).match(e._1)) {
					disconnect();
					Logger.info("%s: %s disconnected...", quit, Secure.Security.connected());
				}

				for (final String msg : TextFrame.match(e._1)) {
					jms.setPubSubDomain(true);
					jms.send("chat", new MessageCreator() {
						public Message createMessage(Session session) throws JMSException {
							return session.createObjectMessage(new ChatMessage(Secure.Security.connected(), msg));
						}
					});
				}

				for (ChatMessage msg : ClassOf(ChatMessage.class).match(e._2)) {
					outbound.send(json.toJson(msg));
				}

				for (WebSocketClose closed : SocketClosed.match(e._1)) {
					EchoEvents.publish(new ChatMessage(Secure.Security.connected(), "Disconnected..."));
					Logger.info("Socket Closed: %s", closed.toString());
				}

			}

		}

	}

}