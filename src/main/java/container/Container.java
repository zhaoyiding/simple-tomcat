package container;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import server.Constants;


public class Container {
	public void invoke(HttpServletRequest request,HttpServletResponse response) {
		String uri=request.getRequestURI();
		String servletName=uri.substring(uri.lastIndexOf("/")+1);
		URLClassLoader loader=null;
		
		try {
			URL[] urls=new URL[1];
			URLStreamHandler streamHandler=null;
			File classPath=new File(Constants.WEB_ROOT);
			String repository=
				(new URL("file", null, classPath.getCanonicalPath()+File.separator)).toString();
		
			urls[0]=new URL(null, repository, streamHandler);
			loader=new URLClassLoader(urls);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		
		Servlet servlet=null;
		try {
			servlet=(Servlet) loader.loadClass(servletName).newInstance();
			servlet.service((ServletRequest)request, (ServletResponse)response);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
}
