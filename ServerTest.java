package javanetpractise.Socket;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerTest {

	public static void serverStart() throws UnknownHostException {
		SocketAddress sa=new InetSocketAddress(InetAddress.getByName("localhost"), 80);

		try (ServerSocket ss = new ServerSocket()) {

			System.out.println(ss.getLocalSocketAddress()+"已经开启");
			ss.bind(sa);
			while (true) {
				Socket connection = ss.accept();

				InputStreamReader isr = new InputStreamReader(connection.getInputStream());
				int c = 0;
				StringBuilder sb=new StringBuilder();
				while ((c = isr.read()) != -1) {

					sb.append((char)c);
				}
				System.out.println("客户端请求："+sb.toString());

				OutputStreamWriter ow = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
				ow.write("server response:"+sb.toString());
				ow.flush();
				connection.shutdownOutput();
				connection.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws UnknownHostException {
		serverStart();

	}
}
