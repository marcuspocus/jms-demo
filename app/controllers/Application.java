package controllers;

import static play.libs.F.Matcher.ClassOf;
import static play.libs.F.Matcher.Equals;
import static play.mvc.Http.WebSocketEvent.SocketClosed;
import static play.mvc.Http.WebSocketEvent.TextFrame;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

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

public class Application extends Controller {

	public static void index() {
		render();
	}

	public void onMessage(final String msg) {
		Logger.info("Message received in JMS...");
		try {
			WebSocket.stream.publish("Via JMS => " + msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class WebSocket extends WebSocketController {

		public static final EventStream<String> stream = new EventStream<String>(100);

		@Inject
		public static JmsTemplate jms;

		public static void echo() {

			while (inbound.isOpen()) {
				Either<WebSocketEvent, String> e = await(Promise.waitEither(inbound.nextEvent(), stream.nextEvent()));

				for (String quit : TextFrame.and(Equals("quit")).match(e._1)) {
					outbound.send("bye:%s", quit);
					disconnect();
				}

				for (final String msg : TextFrame.match(e._1)) {
					Logger.info("msg: %s", msg);
					jms.setPubSubDomain(true);
					jms.setDefaultDestinationName("chat");
					jms.send(new MessageCreator() {
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(msg);
						}
					});
				}

				for (String msg : ClassOf(String.class).match(e._2)) {
					outbound.send("%s", msg);
				}

				for (WebSocketClose closed : SocketClosed.match(e._1)) {
					Logger.info("Socket Closed: %s", closed.toString());
				}

			}

		}

	}

}