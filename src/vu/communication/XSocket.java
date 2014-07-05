package vu.communication;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.connection.IConnectionScoped;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.INonBlockingConnection;
import org.xsocket.connection.IServer;
import org.xsocket.connection.Server;

public class XSocket {

	private final Logger log = Logger.getLogger(this.getClass());
	private final int port = 5555;
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

	private void stop() throws Exception {
		log.info("Stopping ApiServer");
		ioServer.close();
		log.info("ApiServer stopped");
	}

	class RequestHandler implements IDataHandler, IConnectionScoped {
		private static final String DELIMITER = "\n";
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
	public void xsocketTcpServer() throws Exception {
		ExecutorService taskExecutor = Executors.newFixedThreadPool(20);
		start();
		for (int i = 0; i < 1000; i++) {
			taskExecutor.submit(new Callable<Void>() {
				@Override public Void call() throws Exception {
					IBlockingConnection clientConnection =  new BlockingConnection("localhost", APP_CONTEXT.apiTcpPort());
					clientConnection.write(request + "\n");
					String response = clientConnection.readStringByDelimiter("\n");
					return null;
				}
			});
		}
	}
	@After
	public void stopServer() throws Exception {
		stop();
	}
}
