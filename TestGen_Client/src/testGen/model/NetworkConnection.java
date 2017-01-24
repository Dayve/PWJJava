package testGen.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkConnection {

	public static Timer serverCommunicationTimer = null;

	private static AtomicInteger incomingEventCheckCounter = new AtomicInteger(0);
	public static AtomicInteger numberOfClientJobs = new AtomicInteger(0);

	private static ObjectInputStream objIn = null;
	private static ObjectOutputStream objOut = null;
	private static Socket s = null;

	public static LinkedBlockingQueue<SocketEvent> eventsFromServer = new LinkedBlockingQueue<SocketEvent>();
	public static LinkedBlockingQueue<SocketEvent> eventsToServer = new LinkedBlockingQueue<SocketEvent>();

	public static void sendSocketEvent(SocketEvent se) {
		eventsToServer.add(se);
		numberOfClientJobs.incrementAndGet();
	}

	public static SocketEvent rcvSocketEvent(String... desiredSignatures) {
		return rcvSocketEvent(15, desiredSignatures);
	}

	public static SocketEvent rcvSocketEvent(int howManyPeriodsToWait, String... desiredSignatures) {
		NetworkConnection.incomingEventCheckCounter.set(0);

		while (incomingEventCheckCounter.get() <= howManyPeriodsToWait) {
			for (SocketEvent receivedEvent : eventsFromServer) {
				for (String desiredSignature : desiredSignatures) {
					if (receivedEvent.getName().equals(desiredSignature)) {
						// System.out.println("RETURNING: event returned");

						SocketEvent temp = receivedEvent;
						eventsFromServer.remove(receivedEvent);

						return temp;
					}
				}
			}
		}
		return null;
	}

	public static void connect(String address, int port) {
		try {
			s = new Socket(address, port);
			objOut = new ObjectOutputStream(s.getOutputStream());
			objIn = new ObjectInputStream(s.getInputStream());
		} catch (ConnectException e) {
			System.out.println("Server is not responding!");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			NetworkConnection.connect(address, port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (serverCommunicationTimer == null) {
			serverCommunicationTimer = new Timer();

			System.out.println("creating timer" + serverCommunicationTimer);

			serverCommunicationTimer.scheduleAtFixedRate(new TimerTask() {
				@Override public void run() {
					try {
						if (!eventsToServer.isEmpty()) {
							// System.out.println(" ->
							// objOut.writeObject(eventsToServer.poll());");
							objOut.writeObject(eventsToServer.poll());
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						if (NetworkConnection.numberOfClientJobs.get() > 0) {
							eventsFromServer.add((SocketEvent) objIn.readObject());
							NetworkConnection.numberOfClientJobs.decrementAndGet();
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					NetworkConnection.incomingEventCheckCounter.incrementAndGet();
				}
			}, 0, 100);
		}
	}

	public static void disconnect() {
		serverCommunicationTimer.cancel();
		serverCommunicationTimer.purge();

		try {
			objIn.close();
			objOut.close();
			s.close();
			// objIn and objOut are closed
			objOut = null;
			objIn = null;
			s = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
