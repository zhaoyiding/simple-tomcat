package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

public class Request implements HttpServletRequest {

	private InputStream input;
	private Socket socket;
	
	private String method;
	private String scheme;
	private String protocol;
	private String requestURI;
	private String queryString;
	private boolean requestedSessionCookie;
	private String requestedSessionId;
	private boolean requestedSessionURL;
	
	private String cookieString;
	private int contentLength;
	private String contentType;
	
	@SuppressWarnings("unused")
	private String requestBody;
	
	private Map<String, String> headers = new HashMap<String, String>();
	private List<Cookie> cookies=new ArrayList<Cookie>();
	private Map<String, Object> attributes=new HashMap<String,Object>();
	private Map<String, String[]> parameters=new HashMap<String,String[]>();
	
	private boolean cookieParsed=false;
	private boolean parameterParsed=false;
	
	public Request(InputStream input) {
		super();
		this.input = input;
	}
	
	public String addHeader(String key,String value) {
		synchronized (headers) {
			return headers.put(key, value);
		}
	}
	
	private void parseCookie(String cookieString) {
		if (cookieString==null||cookieString.length()==0) {
			return;
		}
		
		cookieParsed=true;
		
		while(cookieString.length()>0){
			int semicolon=cookieString.indexOf(';');
			if (semicolon<0) {
				semicolon=cookieString.length();
			}
			if (semicolon==0) {
				break;
			}
			String token=cookieString.substring(0, semicolon);
			
			if (semicolon<cookieString.length()) {
				cookieString=cookieString.substring(semicolon+1);
			} else {
				cookieString="";
			}
			
			int equal=token.indexOf('=');
			if (equal>0) {
				String name=token.substring(0, equal).trim();
				String value=token.substring(equal+1).trim();
				
				if (name.equals("jsessionid")) {
					setRequestedSessionId(value);
					
					setRequestedSessionCookie(true);
					setRequestedSessionURL(false);
				}
				cookies.add(new Cookie(name, value));
			}
		}
		
	}
	
	public void addCookie(Cookie cookie) {
		synchronized (cookies) {
			if (!cookieParsed) {
				parseCookie(cookieString);
			}
			cookies.add(cookie);
		}
	}
	
	@Override
	public Cookie[] getCookies() {
		synchronized (cookies) {
			if (!cookieParsed) {
				parseCookie(cookieString);
			}
			
			if (cookies.size()==0) {
				return null;
			}
			
			return (Cookie[]) cookies.toArray();
		}
	}
	
	private void parseParameter() {
		parameterParsed=true;
		String query=queryString;
		
		while(query.length()>0){
			int pos=query.indexOf('&');
			String token=null;
			
			if (pos>0) {
				token=query.substring(0, pos);
				query=query.substring(pos+1);
			} else if (pos<0) {
				token=query;
				query="";
			} else {
				throw new IllegalStateException("queryString.invalid");
			}
			
			int equal=token.indexOf('=');
			String name=token.substring(0, equal);
			String[] value=new String[1];
			value[0]=token.substring(equal+1);
			parameters.put(name, value);
			
		}
	}

	@Override
	public Object getAttribute(String name) {
		synchronized (attributes) {
			return attributes.get(name);
		}
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getContentLength() {
		return contentLength;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return (ServletInputStream) input;
	}

	@Override
	public String getParameter(String name) {
		synchronized (parameters) {
			if (!parameterParsed) {
				parseParameter();
			}
			String[] value=parameters.get(name);
			if (value!=null) {
				return value[0];
			} else {
				return null;
			}
		}
	}

	@Override
	public Enumeration<String> getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getParameterValues(String name) {
		synchronized (parameters) {
			if (!parameterParsed) {
				parseParameter();
			}
			return parameters.get(name);
		}
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		synchronized (parameters) {
			if (!parameterParsed) {
				parseParameter();
			}
			return parameters;
		}
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public String getServerName() {
		return socket.getLocalAddress().getHostName();
	}

	@Override
	public int getServerPort() {
		return socket.getLocalPort();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(input));
	}

	@Override
	public String getRemoteAddr() {
		return socket.getInetAddress().getHostAddress();
	}

	@Override
	public String getRemoteHost() {
		return socket.getInetAddress().getHostName();
	}

	@Override
	public void setAttribute(String name, Object o) {
		synchronized (attributes) {
			attributes.put(name, o);
		}
	}

	@Override
	public void removeAttribute(String name) {
		synchronized (attributes) {
			attributes.remove(name);
		}
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemotePort() {
		return socket.getPort();
	}

	@Override
	public String getLocalName() {
		return socket.getLocalAddress().getHostName();
	}

	@Override
	public String getLocalAddr() {
		return socket.getLocalAddress().getHostAddress();
	}

	@Override
	public int getLocalPort() {
		return socket.getLocalPort();
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDateHeader(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHeader(String name) {
		synchronized (headers) {
			return headers.get(name);
		}
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIntHeader(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return requestedSessionId;
	}

	@Override
	public String getRequestURI() {
		return requestURI;
	}

	@Override
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession(boolean create) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return requestedSessionCookie;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return requestedSessionURL;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return requestedSessionURL;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public void setRequestedSessionCookie(boolean requestedSessionCookie) {
		this.requestedSessionCookie = requestedSessionCookie;
	}

	public void setRequestedSessionId(String requestedSessionId) {
		this.requestedSessionId = requestedSessionId;
	}

	public void setRequestedSessionURL(boolean requestedSessionURL) {
		this.requestedSessionURL = requestedSessionURL;
	}

	public void setCookieString(String cookieString) {
		this.cookieString = cookieString;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public void setSocket(Socket socket) {
		this.socket=socket;
	}

}
