package net.optifine.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Map;

public class HttpPipelineSender extends Thread {
   private HttpPipelineConnection httpPipelineConnection = null;
   private static final String CRLF = "\r\n";
   private static Charset ASCII = Charset.forName("ASCII");

   public HttpPipelineSender(HttpPipelineConnection httpPipelineConnection) {
      super("HttpPipelineSender");
      this.httpPipelineConnection = httpPipelineConnection;
   }

   @Override
   public void run() {
      HttpPipelineRequest hpr = null;

      try {
         this.connect();

         while (!Thread.interrupted()) {
            hpr = this.httpPipelineConnection.getNextRequestSend();
            HttpRequest req = hpr.getHttpRequest();
            OutputStream out = this.httpPipelineConnection.getOutputStream();
            this.writeRequest(req, out);
            this.httpPipelineConnection.onRequestSent(hpr);
         }
      } catch (InterruptedException e) {
         return;
      } catch (Exception e) {
         this.httpPipelineConnection.onExceptionSend(hpr, e);
      }
   }

   private void connect() throws IOException {
      String host = this.httpPipelineConnection.getHost();
      int port = this.httpPipelineConnection.getPort();
      Proxy proxy = this.httpPipelineConnection.getProxy();
      Socket socket = new Socket(proxy);
      socket.connect(new InetSocketAddress(host, port), 5000);
      this.httpPipelineConnection.setSocket(socket);
   }

   private void writeRequest(HttpRequest req, OutputStream out) throws IOException {
      this.write(out, req.getMethod() + " " + req.getFile() + " " + req.getHttp() + "\r\n");
      Map<String, String> headers = req.getHeaders();

      for (String key : headers.keySet()) {
         String val = req.getHeaders().get(key);
         this.write(out, key + ": " + val + "\r\n");
      }

      this.write(out, "\r\n");
   }

   private void write(OutputStream out, String str) throws IOException {
      byte[] bytes = str.getBytes(ASCII);
      out.write(bytes);
   }
}
