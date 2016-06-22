package server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import container.Container;

public class Connector implements Runnable {

	private static final int POOL_SIZE = Constants.POOL_SIZE;

	private Container container;
	private ExecutorService service;
	private Deque<Processor> processors;

	public void start() {
		Thread thread = new Thread(this);
		thread.start();
	}

	public void initialise() {
		service = Executors.newFixedThreadPool(POOL_SIZE);
		processors = new LinkedList<Processor>();

		for (int i = 0; i < POOL_SIZE; i++) {
			Processor processor = new Processor(this);
			processors.addLast(processor);
		}
	}

	public void recycle(Processor processor) {
		synchronized (processors) {
			processors.addLast(processor);
		}
	}

	public Processor getProcessor() {
		synchronized (processors) {
			return processors.pollFirst();
		}
	}

	@Override
	public void run() {
		try (ServerSocket serverSocket = new ServerSocket(Constants.PORT, Constants.BACK_LOG,
				InetAddress.getByName(Constants.HOST))) {

			while (true) {
				Socket socket = serverSocket.accept();

				Processor processor = getProcessor();
				if (processor != null) {
					processor.assign(socket);
					service.submit(processor);
				} else {
					socket.close();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Request createRequest(InputStream input) {
		return new Request(input);
	}

	public Response createResponse(OutputStream output) {
		return new Response(output);
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public Container getContainer() {
		return container;
	}

}
