package ficoos.hellovdsm;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.ovirt.vdsm.VDSMClient;
import org.ovirt.vdsm.VDSMClientPool;
import org.ovirt.vdsm.VDSMResponse;
import org.ovirt.vdsm.VdsmCapabilities;
import org.ovirt.vdsm.reactors.NioReactor;
import org.ovirt.vdsm.reactors.Reactor;

public class HelloVdsm {

	private static HashMap<String, Reactor> _reactors;
	private static VDSMClientPool _cpool;

	public static void initializeReactors() throws IOException {
		_reactors = new HashMap<>();
		// Create an NIO reactor
		// NIO reactors are based around Javas own NIO framework
		final NioReactor tcpReactor = new NioReactor();

		// It's up to the user to run each reactors' serve thread
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				tcpReactor.serve();
			}
		}, "TCP Reactor");
		t.setDaemon(true);
		t.start();
		// The key is the scheme to be put in the URI to use this
		// reactor.
		_reactors.put("tcp", tcpReactor);
	}

	public static void initializeVDSMClientPool() {
		// All clients have to belond to a pool. The pool manages communication
		// with the transports.
		_cpool = new VDSMClientPool(_reactors);
		// It's, again, up to the user to start the client pool's
		// serving thread.
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				_cpool.serve();
			}
		}, "Client Pool");
		t.setDaemon(true);
		t.start();
	}

	public static void main(String args[]) throws IOException {
		final Logger logger = Logger.getLogger(HelloVdsm.class.getName());
		URI uri = URI.create(args[0]);

		initializeReactors();
		initializeVDSMClientPool();

		// Create a client, does not try and connect to the host and
		// will always work. This is because the host might be down but
		// the host still want's to try and send commands periodically.
		VDSMClient client = _cpool.createClient(uri);

		VDSMClient.Host host = client.new Host();
		Future<VDSMResponse<VdsmCapabilities>> call;
		try {
			call = host.getCapabilities(client.newID());
		} catch (TimeoutException ex) {
			// This happens if we can't even connect to the client.
			// I'm thinking of getting this part out and just
			// failing the call.
			logger.log(Level.SEVERE, null, ex);
			return;
		}
		VDSMResponse<VdsmCapabilities> resp;
		try {
			resp = call.get(30, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException ex) {
			// Anything can go wrong!
			logger.log(Level.SEVERE, null, ex);
			return;
		}

		// Cross fingers
		if (resp.isSuccess()) {
			logger.log(Level.INFO,
				String.format("SUCCESS: UUID is %s",
				resp.result().uuid()));
		} else {
			logger.log(Level.INFO, String.format("ERROR: %s (%d)",
				resp.error().message(),
				resp.error().code()));
		}
	}
}
