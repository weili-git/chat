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
				if(isstart) {//未启动则不需要关闭对象
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
						numLabel.setText("消息条目："+clients.get(i).j);
						IPBox.setText("IP地址:"+clients.get(i).ip);
						fileNum.setText("文件数目："+clients.get(i).k);
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
					JOptionPane.showMessageDialog(frame,"服务器未启动！","提示",JOptionPane.WARNING_MESSAGE);
					return;
				}
				for(int i=clients.size()-1;i>=0;i--) {
					if(list.isSelectionEmpty()) {
						JOptionPane.showMessageDialog(frame,"未选中用户！","提示",JOptionPane.WARNING_MESSAGE);
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
					JOptionPane.showMessageDialog(frame,"服务端已经启动，不要重复启动！","提示",JOptionPane.WARNING_MESSAGE);
					return;
				}
				try {
					server = new ServerSocket(Integer.parseInt(portBox.getText()));//设置端口
					clients = new ArrayList<ClientThread>();//用户线程
					//new ThreadHandler(server,Integer.parseInt(numBox.getText())).start();
					threadHandler = new ThreadHandler(server,Integer.parseInt(numBox.getText()),context);
					threadHandler.start();
					listModel = new DefaultListModel();
					list.setModel(listModel);
					isstart = true;
					JOptionPane.showMessageDialog(frame,"启动成功！","提示",JOptionPane.WARNING_MESSAGE);
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
					JOptionPane.showMessageDialog(frame,"服务端已经断开，无需重复断开！","提示",JOptionPane.WARNING_MESSAGE);
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
					listModel.removeAllElements();// 清空用户列表  
				}catch(IOException e2) {
					System.out.println(e2);
				}
				isstart = false;
				JOptionPane.showMessageDialog(frame,"停止成功！","提示",JOptionPane.WARNING_MESSAGE);
			}
		});
		stop.setBounds(346, 6, 93, 23);
		frame.getContentPane().add(stop);
		
		JButton disconnect = new JButton("\u65AD\u5F00");
		disconnect.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				if(list.isSelectionEmpty()) {
					JOptionPane.showMessageDialog(frame,"未选中用户！","提示",JOptionPane.WARNING_MESSAGE);
					return;
				}else {
					try {
						for(int i = clients.size()-1;i>=0;i--) {
							if(clients.get(i).name == list.getSelectedValue()) {
								clients.get(i).out.writeUTF("CLOSE");
								clients.get(i).in.close();
								clients.get(i).out.close();
								clients.get(i).socket.close();
								
								listModel.removeElement(clients.get(i).name);//从列表中删除用户名
								ClientThread temp = clients.get(i);  
								clients.remove(i);// 删除此用户的服务线程  
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
						JOptionPane.showMessageDialog(frame,"人数已满！","提示",JOptionPane.WARNING_MESSAGE);
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
		
		private DataInputStream dis;//文件操作=====================类结束自动销毁？？？？
		private FileOutputStream fos;
		private int k;

		public ClientThread(Socket socket,JTextArea context) {//构造方法接受客户端消息
			try { 
				isstart = true;
				this.socket = socket;
				this.context = context;
				in = new DataInputStream(this.socket.getInputStream());
				out = new DataOutputStream(this.socket.getOutputStream());
				this.name= in.readUTF();//第一条消息默认为用户名      代码顺序不能变
				for(int i = clients.size()-1;i>=0;i--) {//构造完成以前clients.size()还没有改变
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
			j = 0;//消息条目数
			k = 0;
			String message = null;
			if(!isstart) {
				try {
					in.close();
					out.close();
					socket.close();
					JOptionPane.showMessageDialog(frame,"名字重复！","提示",JOptionPane.WARNING_MESSAGE);//测试代码
					return;
				}catch(IOException e2) {
					System.out.println(e2);
				}
				
			}
			while(true) {
				try {
					message = in.readUTF();
					if(message.equals("CLOSE")) {//清除用户信息,不能用 message == "CLOSE"
						in.close();
						out.close();
						socket.close();
						listModel.removeElement(name);//从列表中删除用户名
						for(int i = clients.size()-1;i>=0;i--) {
							if(clients.get(i).name == name) {
								ClientThread temp = clients.get(i);  
								clients.remove(i);// 删除此用户的服务线程  
								temp.stop();
								return;  
							}
						}
					}else if(message.equals("FILE")) {
						try {
							dis =new DataInputStream(socket.getInputStream());
							String fileName = dis.readUTF();
							long fileLength = dis.readLong();
							fos =new FileOutputStream(new File("d:/" + fileName));//通知存储位置
							byte[] sendBytes =new byte[1024];
							int transLen =0;
							System.out.println("----开始接收文件<" + fileName +">,文件大小为<" + fileLength +">----");
							while(true){
			                    int read =0;
			                    read = dis.read(sendBytes);
			                    if(read == -1)
			                        break;
			                    transLen += read;
			                    System.out.println("接收文件进度" +100 * transLen/fileLength +"%...");
			                    fos.write(sendBytes,0, read);
			                    fos.flush();
			                    if(transLen/fileLength == 1) {
			                    	break;
			                    }
			                }
							System.out.println("----接收文件<" + fileName +">成功-------");
							k++;
							fileNum.setText("文件数目："+k);
							context.append(name+"发送文件：d:/"+fileName+"\r\n");
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
								numLabel.setText("消息条目："+clients.get(i).j);
								IPBox.setText("IP地址:"+clients.get(i).ip);
								fileNum.setText("文件数目："+clients.get(i).k);
							}
						}
						
					}
				}catch(IOException e1) {
					System.out.println(e1);
				}/*finally {
					try {
						if(dis!=null) dis.close();//正确的关闭方式//暂时先不管把
						if(fos!=null) fos.close();
					}catch (IOException e) {
						e.printStackTrace();
					}
				}*/
			}
		}
		
	}
}
