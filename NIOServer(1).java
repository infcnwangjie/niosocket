/**
 * 
 */
package com.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 版本: [1.0]
 * 功能说明: 
 *
 * 作者: WChao
 * 创建时间: 2017年5月17日 上午11:37:59
 */
public class NIOServer {
	
	private int blockingSize = 4096;
	private ByteBuffer sendBuffer = ByteBuffer.allocate(blockingSize);
	private ByteBuffer receiveBuffer = ByteBuffer.allocate(blockingSize);
	//通道管理器选择器
	private Selector selector = null;
	
	/**
	 * 获得一个ServerSocket通道，并对该通道做一些初始化的工作
	 * @param port
	 * @throws IOException
	 */
	public NIOServer(int port)throws IOException{
		// 获得打开一个ServerSocket通道
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		// 设置通道为非阻塞
		serverChannel.configureBlocking(false);
		//  通过服务端通道获取对应的serverSocket并绑定到port端口
		serverChannel.socket().bind(new InetSocketAddress(port));
		//	打开通道管理器选择器;
		selector = Selector.open();
		//将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，
		//当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		System.out.println("Server Start->"+port);
	}
	/**
	 * 
		 * 
		 * 功能描述：[监听:采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理]
		 *
		 * 创建者：WChao
		 * 创建时间: 2017年5月18日 上午10:42:44
		 * @throws Exception
	 */
	public void listen() throws Exception{
		while(true){
			//当注册的事件到达时，方法返回；否则,该方法会一直阻塞
			selector.select();
			// 获得selector中注册的事件迭代器
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while(it.hasNext()){
				SelectionKey key = it.next();
				// 删除已选的key,以防重复处理
				it.remove();
				//业务逻辑;
				handleKey(key);
			}
		}
	}
	/**
	 * 
		 * 
		 * 功能描述：[对Key事件进行业务处理]
		 *
		 * 创建者：WChao
		 * 创建时间: 2017年5月18日 上午10:47:30
		 * @param selectionKey
		 * @throws IOException
	 */
	public void handleKey(SelectionKey selectionKey) throws IOException{
		ServerSocketChannel server = null;
		SocketChannel client = null;
		String receiveText ;
		String sendText;
		int count = 0;
		// 客户端请求【连接】事件
		if(selectionKey.isAcceptable()){
			server = (ServerSocketChannel)selectionKey.channel();
			client = server.accept();
			client.configureBlocking(false);
			//在这里可以给客户端发送信息哦
			client.write(ByteBuffer.wrap(new String("向客户端发送了一条信息").getBytes()));
			//在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
			client.register(selector, SelectionKey.OP_READ);
		}else if(selectionKey.isReadable()){
			client = (SocketChannel)selectionKey.channel();
			count = client.read(receiveBuffer);
			receiveText = new String(receiveBuffer.array(),0,count);
			System.out.println("服务端接收到客户端的信息:"+receiveText);
			client.register(selector, SelectionKey.OP_WRITE);
		}else if(selectionKey.isWritable()){
			sendBuffer.clear();
			client = (SocketChannel)selectionKey.channel();
			sendText = "msg send to client :"+client.getRemoteAddress().toString();
			sendBuffer.put(sendText.getBytes());
			sendBuffer.flip();
			client.write(sendBuffer);
			System.out.println("发送数据给客户端:"+sendText);
		}
	}
	/**
		 * 
		 * 功能描述：[]
		 *
		 * 创建者：WChao
		 * 创建时间: 2017年5月17日 上午11:37:59
		 * @param args
		 */
	public static void main(String[] args) throws Exception{
		 int port = 7080;
		 NIOServer server = new NIOServer(port);
		 server.listen();
	}

}
