/**
 * 
 */
package com.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 版本: [1.0]
 * 功能说明: 
 *
 * 作者: WChao
 * 创建时间: 2017年5月17日 下午3:09:35
 */
public class NIOClient {
	
	private int blockingSize = 4096;
	private ByteBuffer sendBuffer = ByteBuffer.allocate(blockingSize);
	private ByteBuffer receiveBuffer = ByteBuffer.allocate(blockingSize);
	private Selector selector = null;
	private int flag = 0;
	
	/**
	 * 获得一个Socket通道，并对该通道做一些初始化的工作
	 * @param ip
	 * @param port
	 * @throws IOException
	 */
	public NIOClient(String ip , int port)throws IOException{
		// 获得一个Socket通道
		SocketChannel socketChannel = SocketChannel.open();
		// 设置通道为非阻塞
		socketChannel.configureBlocking(false);
		// 获得一个通道管理器
		selector = Selector.open();
		// 客户端连接服务器,其实方法执行并没有实现连接，需要方法中调
		// 用channel.finishConnect();才能完成连接
		socketChannel.connect(new InetSocketAddress(ip,port));
		//将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
	}
	
	/**
	 * 
		 * 
		 * 功能描述：[采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理]
		 *
		 * 创建者：WChao
		 * 创建时间: 2017年5月18日 上午11:15:30
		 * @throws IOException
	 */
	public void listen()throws IOException{
		while(true){
			selector.select();
			// 获得selector中选中的项的迭代器
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while(it.hasNext()){
				SelectionKey key = it.next();
				// 删除已选的key,以防重复处理
				it.remove();
				//业务处理key;
				handleKey(key);
			}
		}
	}
	
	public void handleKey(SelectionKey selectionKey) throws IOException{
		SocketChannel client = null;
		int count = 0;
		String receiveText = null;
		String sendText = null;
		// 连接事件发生
		if(selectionKey.isConnectable()){
			System.out.println("client connet");
		    client = (SocketChannel)selectionKey.channel();
		    // 如果正在连接，则完成连接
			if(client.isConnectionPending()){
				client.finishConnect();
				System.out.println("客户端完成连接操作");
			}
			// 设置成非阻塞
			client.configureBlocking(false);
			sendBuffer.clear();
			sendBuffer.put("Hello,Server".getBytes());
			sendBuffer.flip();
			client.write(sendBuffer);
			//在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。
			client.register(selector, SelectionKey.OP_READ);
			// 获得了可读的事件
		}else if(selectionKey.isReadable()){
			client = (SocketChannel)selectionKey.channel();
			receiveBuffer.clear();
			count = client.read(receiveBuffer);
			if(count > 0){
				receiveText = new String(receiveBuffer.array(),0,count);
				client.configureBlocking(false);
				System.out.println("客户端接收到服务器端的数据:"+receiveText);
				client.register(selector, SelectionKey.OP_WRITE);
			}
		}else if(selectionKey.isWritable()){
			sendBuffer.clear();
			client = (SocketChannel)selectionKey.channel();
			client.configureBlocking(false);
			sendText = "Msg Send to Server ->"+flag++;
			sendBuffer.put(sendText.getBytes());
			sendBuffer.flip();
			client.write(sendBuffer);
			System.out.println("客户端发送数据给服务器端:"+sendText);
			client.register(selector, SelectionKey.OP_WRITE);
		}
	}
	/**
		 * 
		 * 功能描述：[]
		 *
		 * 创建者：WChao
		 * 创建时间: 2017年5月17日 下午3:09:36
		 * @param args
		 */
	public static void main(String[] args)throws Exception {
		NIOClient client = new NIOClient("127.0.0.1", 7080);
		client.listen();

	}

}
