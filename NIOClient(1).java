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
 * �汾: [1.0]
 * ����˵��: 
 *
 * ����: WChao
 * ����ʱ��: 2017��5��17�� ����3:09:35
 */
public class NIOClient {
	
	private int blockingSize = 4096;
	private ByteBuffer sendBuffer = ByteBuffer.allocate(blockingSize);
	private ByteBuffer receiveBuffer = ByteBuffer.allocate(blockingSize);
	private Selector selector = null;
	private int flag = 0;
	
	/**
	 * ���һ��Socketͨ�������Ը�ͨ����һЩ��ʼ���Ĺ���
	 * @param ip
	 * @param port
	 * @throws IOException
	 */
	public NIOClient(String ip , int port)throws IOException{
		// ���һ��Socketͨ��
		SocketChannel socketChannel = SocketChannel.open();
		// ����ͨ��Ϊ������
		socketChannel.configureBlocking(false);
		// ���һ��ͨ��������
		selector = Selector.open();
		// �ͻ������ӷ�����,��ʵ����ִ�в�û��ʵ�����ӣ���Ҫ�����е�
		// ��channel.finishConnect();�����������
		socketChannel.connect(new InetSocketAddress(ip,port));
		//��ͨ���������͸�ͨ���󶨣���Ϊ��ͨ��ע��SelectionKey.OP_CONNECT�¼���
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
	}
	
	/**
	 * 
		 * 
		 * ����������[������ѯ�ķ�ʽ����selector���Ƿ�����Ҫ������¼�������У�����д���]
		 *
		 * �����ߣ�WChao
		 * ����ʱ��: 2017��5��18�� ����11:15:30
		 * @throws IOException
	 */
	public void listen()throws IOException{
		while(true){
			selector.select();
			// ���selector��ѡ�е���ĵ�����
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while(it.hasNext()){
				SelectionKey key = it.next();
				// ɾ����ѡ��key,�Է��ظ�����
				it.remove();
				//ҵ����key;
				handleKey(key);
			}
		}
	}
	
	public void handleKey(SelectionKey selectionKey) throws IOException{
		SocketChannel client = null;
		int count = 0;
		String receiveText = null;
		String sendText = null;
		// �����¼�����
		if(selectionKey.isConnectable()){
			System.out.println("client connet");
		    client = (SocketChannel)selectionKey.channel();
		    // ����������ӣ����������
			if(client.isConnectionPending()){
				client.finishConnect();
				System.out.println("�ͻ���������Ӳ���");
			}
			// ���óɷ�����
			client.configureBlocking(false);
			sendBuffer.clear();
			sendBuffer.put("Hello,Server".getBytes());
			sendBuffer.flip();
			client.write(sendBuffer);
			//�ںͷ�������ӳɹ�֮��Ϊ�˿��Խ��յ�����˵���Ϣ����Ҫ��ͨ�����ö���Ȩ�ޡ�
			client.register(selector, SelectionKey.OP_READ);
			// ����˿ɶ����¼�
		}else if(selectionKey.isReadable()){
			client = (SocketChannel)selectionKey.channel();
			receiveBuffer.clear();
			count = client.read(receiveBuffer);
			if(count > 0){
				receiveText = new String(receiveBuffer.array(),0,count);
				client.configureBlocking(false);
				System.out.println("�ͻ��˽��յ��������˵�����:"+receiveText);
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
			System.out.println("�ͻ��˷������ݸ���������:"+sendText);
			client.register(selector, SelectionKey.OP_WRITE);
		}
	}
	/**
		 * 
		 * ����������[]
		 *
		 * �����ߣ�WChao
		 * ����ʱ��: 2017��5��17�� ����3:09:36
		 * @param args
		 */
	public static void main(String[] args)throws Exception {
		NIOClient client = new NIOClient("127.0.0.1", 7080);
		client.listen();

	}

}
