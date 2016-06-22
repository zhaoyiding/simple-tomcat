package startup;

import container.Container;
import server.Connector;

public class Bootstrap {

	public static void main(String[] args) {
		Connector connector = new Connector();
		Container container = new Container();
		connector.setContainer(container);

		connector.initialise();
		connector.start();
	}

}
