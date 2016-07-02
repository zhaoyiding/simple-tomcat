package startup;

import container.ServletContainer;
import server.Connector;

public class Bootstrap {

	public static void main(String[] args) {
		Connector connector = new Connector();
		ServletContainer container = new ServletContainer();
		connector.setContainer(container);

		connector.initialise();
		connector.start();
	}

}
