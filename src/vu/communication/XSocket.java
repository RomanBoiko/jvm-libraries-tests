package vu.communication;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.connection.BlockingConnection;
import org.xsocket.connection.IBlockingConnection;
import org.xsocket.connection.IConnectionScoped;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.INonBlockingConnection;
import org.xsocket.connection.IServer;
import org.xsocket.connection.Server;

public class XSocket {

	private static final String DELIMITER = "\n";
	private static final Logger log = Logger.getLogger(XSocket.class);
	private static final int port = 5555;
	private final int workersNumber = 6;
	
	private final AtomicInteger requestsCount = new AtomicInteger(0);
	private final AtomicInteger handlersCount = new AtomicInteger(0);

	private IServer ioServer;

	private XSocket start() throws Exception {
		log.info("Starting ApiServer");
		ioServer = new Server(port, new RequestHandler(), 2, workersNumber);
		ioServer.start();
		log.info("ApiServer started");
		return this;
	}

	public void stop() throws Exception {
		log.info("Stopping ApiServer");
		ioServer.close();
		log.info("ApiServer stopped");
	}

	class RequestHandler implements IDataHandler, IConnectionScoped {
		private final Logger log = Logger.getLogger(this.getClass());
		private final int handlerNumber;
		
		public RequestHandler() {
			handlerNumber = handlersCount.incrementAndGet();
			log.info("Handler created, number " + handlerNumber);
		}

		@Override
		public boolean onData(INonBlockingConnection connection)
				throws IOException, BufferUnderflowException,
				ClosedChannelException, MaxReadSizeExceededException {
			String request = connection.readStringByDelimiter(DELIMITER);
			log.debug(String.format("Responding (request: %s, requestNumber: %s, handler:  %s", request, requestsCount.incrementAndGet(), handlerNumber));
			connection.write("response" + DELIMITER);
			return true;
		}

		public Object clone() throws java.lang.CloneNotSupportedException {
			return new RequestHandler();
		}
	}

	@Test
	public void run() throws Exception {
		main(new String[0]);
	}

	public static void main(String[] args) throws Exception {
		XSocket socketServer = new XSocket();
		socketServer.start();
		executeNumberOfRequestsToServer(1000);
		socketServer.stop();
	}
	

	private static void executeNumberOfRequestsToServer(int requestsNumber) throws Exception {
		ExecutorService taskExecutor = Executors.newFixedThreadPool(20);
		List<Future<Void>> submittedTasks = new ArrayList<Future<Void>>();
		for (int i = 0; i < requestsNumber; i++) {
			final int requestId = i;
			submittedTasks.add(taskExecutor.submit(new Callable<Void>() {
				@Override public Void call() throws Exception {
					IBlockingConnection clientConnection =  new BlockingConnection("localhost", port);
					clientConnection.write("request" + DELIMITER);
					log.debug("Response " + requestId + ": " + clientConnection.readStringByDelimiter(DELIMITER));
					return null;
				}
			}));
		}
		for (int i = 0; i < submittedTasks.size(); i++) {
			submittedTasks.get(i).get();
		}
	}
}
