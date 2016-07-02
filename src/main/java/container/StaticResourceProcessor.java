package container;

import java.io.IOException;

import server.Request;
import server.Response;

public class StaticResourceProcessor {
	
	public void process(Request request,Response response) {
		try {
			response.sendStaticResource();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
