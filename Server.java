package widget;
import java.io.*;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.*;
import java.util.ArrayList;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
public class Server {

	private JFrame frame;
	private JTextField numBox;
	private JTextField portBox;
	
	private DefaultListModel listModel;
	//private String [][] names;
	private ArrayList<ClientThread> clients;
	private ThreadHandler threadHandler=null;
	private ServerSocket server=null;
	private JLabel numLabel;
	private JLabel IPBox;
	private JLabel fileNum;
	private boolean isstart = false;
	private JList list;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server window = new Server();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Server() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("\u670D\u52A1\u7AEF");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				if(isstart) {//δ��������Ҫ�رն���
					try {
						for(int i=clients.size()-1;i>=0;i--) {
							clients.get(i).out.writeUTF("CLOSE");
							clients.get(i).stop();
							clients.get(i).in.close();
							clients.get(i).out.close();
							clients.get(i).socket.close();
							
							clients.remove(i);
						}
					}catch(IOException e) {
						System.out.println(e);
					}
				}
			}
		});
		frame.setResizable(false);
		frame.setBounds(100, 100, 553, 485);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel label = new JLabel("\u4EBA\u6570\uFF1A");
		label.setBounds(10, 10, 54, 15);
		frame.getContentPane().add(label);
		
		numBox = new JTextField();
		numBox.setText("20");
		numBox.setBounds(55, 7, 66, 21);
		frame.getContentPane().add(numBox);
		numBox.setColumns(10);
		
		JLabel label_1 = new JLabel("\u7AEF\u53E3\uFF1A");
		label_1.setBounds(131, 10, 54, 15);
		frame.getContentPane().add(label_1);
		
		portBox = new JTextField();
		portBox.setText("8000");
		portBox.setBounds(168, 7, 66, 21);
		frame.getContentPane().add(portBox);
		portBox.setColumns(10);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 35, 429, 293);
		frame.getContentPane().add(scrollPane);
		
		JTextArea context = new JTextArea();
		context.setLineWrap(true);
		scrollPane.setViewportView(context);
		context.setEditable(false);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(449, 35, 89, 294);
		frame.getContentPane().add(scrollPane_1);
		
		list = new JList();
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				for(int i=clients.size()-1;i>=0;i--) {
					if(clients.get(i).name == list.getSelectedValue()) {
						numLabel.setText("��Ϣ��Ŀ��"+clients.get(i).j);
						IPBox.setText("IP��ַ:"+clients.get(i).ip);
						fileNum.setText("�ļ���Ŀ��"+clients.get(i).k);
					}
				}
			}
		});
		scrollPane_1.setViewportView(list);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(10, 336, 526, 75);
		frame.getContentPane().add(scrollPane_2);
		
		JTextArea text = new JTextArea();
		text.setLineWrap(true);
		scrollPane_2.setViewportView(text);
		
		numLabel = new JLabel("\u63A5\u6536\u6761\u76EE\uFF1A");
		numLabel.setBounds(10, 419, 111, 15);
		frame.getContentPane().add(numLabel);
		
		JButton pass = new JButton("\u53D1\u9001");
		pass.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!isstart) {
					JOptionPane.showMessageDialog(frame,"������δ������","��ʾ",JOptionPane.WARNING_MESSAGE);
					return;
				}
				for(int i=clients.size()-1;i>=0;i--) {
					if(list.isSelectionEmpty()) {
						JOptionPane.showMessageDialog(frame,"δѡ���û���","��ʾ",JOptionPane.WARNING_MESSAGE);
						return;
					}else if(clients.get(i).name == list.getSelectedValue()) {
						try {
							if(text.getText()!=null) {
								clients.get(i).out.writeUTF(text.getText());
								context.append("To "+clients.get(i).name+":"+text.getText()+"\r\n");
								text.setText(null);
							}
						}catch(IOException e4) {
							System.out.println(e4);
						}
					}
				}
			}
		});
		pass.setBounds(445, 415, 93, 23);
		frame.getContentPane().add(pass);
		
		JButton start = new JButton("\u542F\u52A8");
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isstart) {
					JOptionPane.showMessageDialog(frame,"������Ѿ���������Ҫ�ظ�������","��ʾ",JOptionPane.WARNING_MESSAGE);
					return;
				}
				try {
					server = new ServerSocket(Integer.parseInt(portBox.getText()));//���ö˿�
					clients = new ArrayList<ClientThread>();//�û��߳�
					//new ThreadHandler(server,Integer.parseInt(numBox.getText())).start();
					threadHandler = new ThreadHandler(server,Integer.parseInt(numBox.getText()),context);
					threadHandler.start();
					listModel = new DefaultListModel();
					list.setModel(listModel);
					isstart = true;
					JOptionPane.showMessageDialog(frame,"�����ɹ���","��ʾ",JOptionPane.WARNING_MESSAGE);
				}catch(Exception e1) {
					System.out.println("Error:"+e1);
				}
			}

		});
		start.setBounds(244, 6, 93, 23);
		frame.getContentPane().add(start);
		
		JButton stop = new JButton("\u505C\u6B62");
		stop.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				if(!isstart) {
					JOptionPane.showMessageDialog(frame,"������Ѿ��Ͽ��������ظ��Ͽ���","��ʾ",JOptionPane.WARNING_MESSAGE);
				}
				try {
					if(threadHandler!=null) {
						threadHandler.stop();
					}
					for(int i=clients.size()-1;i>=0;i--) {
						clients.get(i).out.writeUTF("CLOSE");
						clients.get(i).stop();
						clients.get(i).in.close();
						clients.get(i).out.close();
						clients.get(i).socket.close();
						clients.remove(i);
					}
					if(server != null) {
						server.close();
					}
					listModel.removeAllElements();// ����û��б�  
				}catch(IOException e2) {
					System.out.println(e2);
				}
				isstart = false;
				JOptionPane.showMessageDialog(frame,"ֹͣ�ɹ���","��ʾ",JOptionPane.WARNING_MESSAGE);
			}
		});
		stop.setBounds(346, 6, 93, 23);
		frame.getContentPane().add(stop);
		
		JButton disconnect = new JButton("\u65AD\u5F00");
		disconnect.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				if(list.isSelectionEmpty()) {
					JOptionPane.showMessageDialog(frame,"δѡ���û���","��ʾ",JOptionPane.WARNING_MESSAGE);
					return;
				}else {
					try {
						for(int i = clients.size()-1;i>=0;i--) {
							if(clients.get(i).name == list.getSelectedValue()) {
								clients.get(i).out.writeUTF("CLOSE");
								clients.get(i).in.close();
								clients.get(i).out.close();
								clients.get(i).socket.close();
								
								listModel.removeElement(clients.get(i).name);//���б���ɾ���û���
								ClientThread temp = clients.get(i);  
								clients.remove(i);// ɾ�����û��ķ����߳�  
								temp.stop();
								return; 
							}
						}
					}catch(IOException e3) {
						System.out.println(e3);
					}
				}
			}
		});
		disconnect.setBounds(445, 6, 93, 23);
		frame.getContentPane().add(disconnect);
		
		IPBox = new JLabel("IP\u5730\u5740\uFF1A");
		IPBox.setBounds(131, 421, 103, 15);
		frame.getContentPane().add(IPBox);
		
		fileNum = new JLabel("\u6587\u4EF6\u6570\u76EE\uFF1A");
		fileNum.setBounds(244, 421, 93, 15);
		frame.getContentPane().add(fileNum);
		
	}
	class ThreadHandler extends Thread{
		private ServerSocket server;
		private int max;
		private JTextArea context;
		public ThreadHandler(ServerSocket server, int max,JTextArea context) {
			this.server = server;  
			this.max = max; 
			this.context = context;
		}
		public void run() {
			while(true) {
				try {
					Socket socket = server.accept();
					if(clients.size() == max) {
						socket.close();
						JOptionPane.showMessageDialog(frame,"����������","��ʾ",JOptionPane.WARNING_MESSAGE);
						continue;
					}
					ClientThread client = new ClientThread(socket,context); 
					client.start();
					if(client.isstart) {
						clients.add(client);
						listModel.addElement(client.name);
						continue;
					}
				}catch(IOException e2) {
					System.out.println("Error:"+e2);
				}
			}
		}
	}
	
	class ClientThread extends Thread{
		private Socket socket;
		private String name;
		private DataInputStream in;
		private DataOutputStream out;
		private JTextArea context;
		private int j;
		private boolean isstart;
		private String ip;
		
		private DataInputStream dis;//�ļ�����=====================������Զ����٣�������
		private FileOutputStream fos;
		private int k;

		public ClientThread(Socket socket,JTextArea context) {//���췽�����ܿͻ�����Ϣ
			try { 
				isstart = true;
				this.socket = socket;
				this.context = context;
				in = new DataInputStream(this.socket.getInputStream());
				out = new DataOutputStream(this.socket.getOutputStream());
				this.name= in.readUTF();//��һ����ϢĬ��Ϊ�û���      ����˳���ܱ�
				for(int i = clients.size()-1;i>=0;i--) {//���������ǰclients.size()��û�иı�
					if(clients.get(i).name.equals(name)) {
						out.writeUTF("RENAME");
						isstart = false;
					}
				}
				out.writeUTF("Hello!");
				ip = socket.getInetAddress().getHostAddress();
				//new FileThread(socket).start();
			}catch(IOException e) {
				System.out.println("Error:"+e);
			}
		}
		@SuppressWarnings("deprecation")  
		public void run() {
			j = 0;//��Ϣ��Ŀ��
			k = 0;
			String message = null;
			if(!isstart) {
				try {
					in.close();
					out.close();
					socket.close();
					JOptionPane.showMessageDialog(frame,"�����ظ���","��ʾ",JOptionPane.WARNING_MESSAGE);//���Դ���
					return;
				}catch(IOException e2) {
					System.out.println(e2);
				}
				
			}
			while(true) {
				try {
					message = in.readUTF();
					if(message.equals("CLOSE")) {//����û���Ϣ,������ message == "CLOSE"
						in.close();
						out.close();
						socket.close();
						listModel.removeElement(name);//���б���ɾ���û���
						for(int i = clients.size()-1;i>=0;i--) {
							if(clients.get(i).name == name) {
								ClientThread temp = clients.get(i);  
								clients.remove(i);// ɾ�����û��ķ����߳�  
								temp.stop();
								return;  
							}
						}
					}else if(message.equals("FILE")) {
						try {
							dis =new DataInputStream(socket.getInputStream());
							String fileName = dis.readUTF();
							long fileLength = dis.readLong();
							fos =new FileOutputStream(new File("d:/" + fileName));//֪ͨ�洢λ��
							byte[] sendBytes =new byte[1024];
							int transLen =0;
							System.out.println("----��ʼ�����ļ�<" + fileName +">,�ļ���СΪ<" + fileLength +">----");
							while(true){
			                    int read =0;
			                    read = dis.read(sendBytes);
			                    if(read == -1)
			                        break;
			                    transLen += read;
			                    System.out.println("�����ļ�����" +100 * transLen/fileLength +"%...");
			                    fos.write(sendBytes,0, read);
			                    fos.flush();
			                    if(transLen/fileLength == 1) {
			                    	break;
			                    }
			                }
							System.out.println("----�����ļ�<" + fileName +">�ɹ�-------");
							k++;
							fileNum.setText("�ļ���Ŀ��"+k);
							context.append(name+"�����ļ���d:/"+fileName+"\r\n");
							//dis.close();
							//fos.close();
						}catch(Exception e) {
							e.printStackTrace();
						}
					}else if(message != null) {
						context.append(name+":"+message+"\r\n");
						j++;
						for(int i=clients.size()-1;i>=0;i--) {
							if(clients.get(i).name == name) {
								numLabel.setText("��Ϣ��Ŀ��"+clients.get(i).j);
								IPBox.setText("IP��ַ:"+clients.get(i).ip);
								fileNum.setText("�ļ���Ŀ��"+clients.get(i).k);
							}
						}
						
					}
				}catch(IOException e1) {
					System.out.println(e1);
				}/*finally {
					try {
						if(dis!=null) dis.close();//��ȷ�Ĺرշ�ʽ//��ʱ�Ȳ��ܰ�
						if(fos!=null) fos.close();
					}catch (IOException e) {
						e.printStackTrace();
					}
				}*/
			}
		}
		
	}
}
