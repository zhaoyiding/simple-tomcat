package server;

import java.io.*;
import java.net.Socket;

import javax.servlet.ServletException;

import container.StaticResourceProcessor;
import server.Request;
import server.Response;

public class Processor implements Runnable {

	private Socket socket;
	private Connector connector;
	private Request request;
	private Response response;

	public Processor(Connector connector) {
		super();
		this.connector = connector;
	}

	@Override
	public void run() {
		try (InputStream input = socket.getInputStream();
				OutputStream output = socket.getOutputStream()) {

			while (true) {
				request = connector.createRequest(input);
				response = connector.createResponse(output);

				request.setSocket(socket);
				
				parse(input);
				
				response.setProtocol(request.getProtocol());
				response.setUri(request.getRequestURI());
				response.setHeader("Server", "tomcat server");
				
				RequestFacade requestFacade = new RequestFacade(request);
				ResponseFacade responseFacade = new ResponseFacade(response);

				if (request.getRequestURI().startsWith("/servlet/")) {
					connector.getContainer().invoke(requestFacade, responseFacade);
				} else {
					StaticResourceProcessor processor=
							new StaticResourceProcessor();
					processor.process(request, response);
				}

				if (!request.getProtocol().equals("HTTP/1.1")
						|| !request.getHeader(DefaultHeaders.CONNECTION_NAME).equals("keep-Alive")) {
					break;
				}
			}

			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (request != null) {
				request = null;
			}
			if (response != null) {
				response = null;
			}
			if (socket != null) {
				socket = null;
			}
		}
	}

	private void parse(InputStream input) {
		StringBuffer buffer = null;
		int i;

		try {
			buffer = new StringBuffer(Constants.BUFFER_SIZE);
			while (((char) (i = input.read())) != '\r') {
				buffer.append((char) i);
			}
			input.read();
			String requestLine = buffer.toString();
			parseRequestLine(requestLine);

		} catch (IOException | ServletException e) {
			e.printStackTrace();
			System.out.println("error request line");
		}

		/*--------------------------------------------------------*/
		try {
			while (true) {
				buffer = new StringBuffer(Constants.BUFFER_SIZE);
				while (((char) (i = input.read())) != '\r') {
					buffer.append((char) i);
				}
				input.read();

				String headerLine = buffer.toString();
				if (headerLine == null || headerLine.length() == 0) {
					break;
				}
				parseHeaderLine(headerLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("error headers");
		}

		/*--------------------------------------------------------*/
		String contentType = request.getContentType();
		if (contentType == null) {
			contentType = "";
		}

		int semincolon = contentType.indexOf(';');
		if (semincolon >= 0) {
			contentType = contentType.substring(0, semincolon).trim();
		} else {
			contentType = contentType.trim();
		}

		if (request.getMethod().equals("POST") && request.getContentLength() > 0
				&& contentType.equals("application/x-www-form-urlencoded")) {
			try {
				int max = request.getContentLength();
				int len = 0;
				byte[] bytes = new byte[max];
				while (len < max) {
					int next = input.read(bytes, len, max - len);
					if (next < 0) {
						break;
					}
					len += next;
				}
				if (len < max) {
					throw new RuntimeException("content length mismatch");
				}

				String requestBody = bytes.toString();
				parseRequestBody(requestBody);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("content read fail");
			}
		}
	}

	private void parseRequestLine(String requestLine) throws ServletException {
		String[] requestString = requestLine.split(" ");

		String method = requestString[0];
		if (method == null || method.length() == 0) {
			throw new ServletException("missing request method");
		}
		request.setMethod(method);

		/*--------------------------------------------------------*/
		String uri = requestString[1];
		if (uri == null || uri.length() == 0) {
			throw new ServletException("missing request uri");
		}

		int pos = uri.indexOf('?');
		if (pos >= 0) {
			String queryString = uri.substring(pos + 1);
			request.setQueryString(queryString);
			uri = uri.substring(0, pos);//
		}

		String match = ";jsessionid=";
		pos = uri.indexOf(match);
		if (pos >= 0) {
			String rest = uri.substring(pos + match.length());
			int pos2 = rest.indexOf(';');
			if (pos2 >= 0) {
				String requestedSessionId = rest.substring(0, pos2);
				request.setRequestedSessionId(requestedSessionId);
				rest = rest.substring(pos2);
			} else {
				request.setRequestedSessionId(rest);
				rest = "";
			}
			request.setRequestedSessionURL(true);
			uri = uri.substring(0, pos) + rest;
		} else {
			request.setRequestedSessionURL(false);
			request.setRequestedSessionId(null);
		}

		String normalizedUri = nomalizeUri(uri);
		if (normalizedUri == null || normalizedUri.length() == 0) {
			throw new ServletException("invalid uri");
		}
		request.setRequestURI(normalizedUri);

		/*--------------------------------------------------------*/
		String protocol = requestString[2];
		if (protocol == null || protocol.length() == 0) {
			throw new ServletException("missing request protocol");
		}
		request.setProtocol(protocol);
		String scheme = protocol.substring(0, protocol.indexOf('/'));
		request.setScheme(scheme);
	}

	private String nomalizeUri(String uri) {
		if (uri == null) {
			return null;
		}
		String normalized = uri;

		if (!normalized.startsWith("/"))
			normalized = "/" + normalized;

		int index = 0;
		while ((index = normalized.indexOf("//")) >= 0) {
			normalized = normalized.substring(0, index) + normalized.substring(index + 1);
		}

		while ((index = normalized.indexOf("\\")) >= 0) {
			normalized = normalized.replace('\\', '/');
		}

		return normalized;
	}

	private void parseHeaderLine(String headerLine) {
		int pos = headerLine.indexOf(':');

		String key = headerLine.substring(0, pos);
		key = key.toLowerCase();
		String value = headerLine.substring(pos + 1);
		value = value.trim();

		request.addHeader(key, value);

		if (key.equals(DefaultHeaders.CONTENT_TYPE_NAME)) {
			request.setContentType(value);

		} else if (key.equals(DefaultHeaders.CONTENT_LENGTH_NAME)) {
			int contentLength = Integer.parseInt(value);
			request.setContentLength(contentLength);

		} else if (key.equals(DefaultHeaders.CONNECTION_NAME)) {
			request.setCookieString(value);
		}
	}

	private void parseRequestBody(String requestBody) {
		request.setRequestBody(requestBody);
	}

	public void assign(Socket socket) {
		this.socket = socket;
	}

}
