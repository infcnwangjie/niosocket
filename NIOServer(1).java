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
 * �汾: [1.0]
 * ����˵��: 
 *
 * ����: WChao
 * ����ʱ��: 2017��5��17�� ����11:37:59
 */
public class NIOServer {
	
	private int blockingSize = 4096;
	private ByteBuffer sendBuffer = ByteBuffer.allocate(blockingSize);
	private ByteBuffer receiveBuffer = ByteBuffer.allocate(blockingSize);
	//ͨ��������ѡ����
	private Selector selector = null;
	
	/**
	 * ���һ��ServerSocketͨ�������Ը�ͨ����һЩ��ʼ���Ĺ���
	 * @param port
	 * @throws IOException
	 */
	public NIOServer(int port)throws IOException{
		// ��ô�һ��ServerSocketͨ��
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		// ����ͨ��Ϊ������
		serverChannel.configureBlocking(false);
		//  ͨ�������ͨ����ȡ��Ӧ��serverSocket���󶨵�port�˿�
		serverChannel.socket().bind(new InetSocketAddress(port));
		//	��ͨ��������ѡ����;
		selector = Selector.open();
		//��ͨ���������͸�ͨ���󶨣���Ϊ��ͨ��ע��SelectionKey.OP_ACCEPT�¼�,ע����¼���
		//�����¼�����ʱ��selector.select()�᷵�أ�������¼�û����selector.select()��һֱ������
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		System.out.println("Server Start->"+port);
	}
	/**
	 * 
		 * 
		 * ����������[����:������ѯ�ķ�ʽ����selector���Ƿ�����Ҫ������¼�������У�����д���]
		 *
		 * �����ߣ�WChao
		 * ����ʱ��: 2017��5��18�� ����10:42:44
		 * @throws Exception
	 */
	public void listen() throws Exception{
		while(true){
			//��ע����¼�����ʱ���������أ�����,�÷�����һֱ����
			selector.select();
			// ���selector��ע����¼�������
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while(it.hasNext()){
				SelectionKey key = it.next();
				// ɾ����ѡ��key,�Է��ظ�����
				it.remove();
				//ҵ���߼�;
				handleKey(key);
			}
		}
	}
	/**
	 * 
		 * 
		 * ����������[��Key�¼�����ҵ����]
		 *
		 * �����ߣ�WChao
		 * ����ʱ��: 2017��5��18�� ����10:47:30
		 * @param selectionKey
		 * @throws IOException
	 */
	public void handleKey(SelectionKey selectionKey) throws IOException{
		ServerSocketChannel server = null;
		SocketChannel client = null;
		String receiveText ;
		String sendText;
		int count = 0;
		// �ͻ����������ӡ��¼�
		if(selectionKey.isAcceptable()){
			server = (ServerSocketChannel)selectionKey.channel();
			client = server.accept();
			client.configureBlocking(false);
			//��������Ը��ͻ��˷�����ϢŶ
			client.write(ByteBuffer.wrap(new String("��ͻ��˷�����һ����Ϣ").getBytes()));
			//�ںͿͻ������ӳɹ�֮��Ϊ�˿��Խ��յ��ͻ��˵���Ϣ����Ҫ��ͨ�����ö���Ȩ�ޡ�
			client.register(selector, SelectionKey.OP_READ);
		}else if(selectionKey.isReadable()){
			client = (SocketChannel)selectionKey.channel();
			count = client.read(receiveBuffer);
			receiveText = new String(receiveBuffer.array(),0,count);
			System.out.println("����˽��յ��ͻ��˵���Ϣ:"+receiveText);
			client.register(selector, SelectionKey.OP_WRITE);
		}else if(selectionKey.isWritable()){
			sendBuffer.clear();
			client = (SocketChannel)selectionKey.channel();
			sendText = "msg send to client :"+client.getRemoteAddress().toString();
			sendBuffer.put(sendText.getBytes());
			sendBuffer.flip();
			client.write(sendBuffer);
			System.out.println("�������ݸ��ͻ���:"+sendText);
		}
	}
	/**
		 * 
		 * ����������[]
		 *
		 * �����ߣ�WChao
		 * ����ʱ��: 2017��5��17�� ����11:37:59
		 * @param args
		 */
	public static void main(String[] args) throws Exception{
		 int port = 7080;
		 NIOServer server = new NIOServer(port);
		 server.listen();
	}

}
