# simple-tomcat

项目介绍：

  仿照tomcat4的默认连接器，写了一个简单的tomcat连接器，带一个servlet容器。

  功能： 1、监听端口，解析http请求，填充request和response。 2、处理静态资源请求 3、使用类加载器，加载servlet类来处理对servlet的请求

  特点： 1、参考tomcat，使用了连接器、处理器、容器的结构。 2、采用多线程的方法，连接器线程接收请求，处理器线程解析http请求。 3、连接器维护了一个线程池和一个连接器池，可以减少创建连接器的时间，循环使用连接器。
  

使用方法：

  1、server包下的Constants类里，声明了需要设置的常量。这里必须修改WEB_ROOT常量，改为用于存放 被请求资源   的目录。其中，被请求资源可以是静态文件（pdf、txt等），也可以是单个servlet类（必须是.class字节码文件，不能是.java源文件，源文件必须先编译）。
  其他的常量，HOST表示IP地址，默认是127.0.0.1；PORT代表端口号，默认是8080。可以自行修改。
  
  2、运行。startup包下的Bootstrap类，右键run，即可运行。
  当需要请求WEB_ROOT目录下的静态资源时，直接输入文件全名，如：http://localhost:8080/Hadoop.pdf，就可以传输Hadoop.pdf文件。
  当需要请求WEB_ROOT目录下的servlet类时，需要输入/servlet前缀，如：http://localhost:8080/servlet/PrimitiveServlet，就可以运行PrimitiveServlet类了。
  
  注意：目前只支持单个servlet运行。
