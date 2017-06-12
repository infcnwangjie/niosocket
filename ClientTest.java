package javanetpractise.Socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientTest {

	public static void clientStart() {

		ExecutorService es = Executors.newFixedThreadPool(5);

		for (int i = 0; i < 100; i++) {
			es.submit(() -> {
				Socket socket = null;

				try {
					socket = new Socket("127.0.0.1", 80);
					BufferedOutputStream bo = new BufferedOutputStream(socket.getOutputStream());
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bo, "UTF-8"));
					bw.write(Thread.currentThread().getName()+"my dream is  playing");
					bw.flush();
					socket.shutdownOutput(); 

					BufferedInputStream bi = new BufferedInputStream(socket.getInputStream());
					InputStreamReader br = new InputStreamReader(bi);

					int c = 0;
					StringBuilder sb=new StringBuilder();
					while ((c = br.read()) != -1) {
						sb.append((char)c);
					}
					System.out.println("服务器返回:"+sb.toString());

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (socket != null) {
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			});

		}
		es.shutdown();

	}

	public static void main(String[] args) {
		clientStart();

	}
}
