package widget;


import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.*;
import java.io.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
public class Client {

	private JFrame frame;
	private JTextField addressBox;
	private JTextField portBox;
	private JTextField nameBox;
	
	private Socket mysocket;
	private DataInputStream in=null;
	private DataOutputStream out=null;
	private int times = 0;
	private boolean isconnect;
	
	private FileInputStream fis;
	private DataOutputStream dos;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client window = new Client();
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
	public Client() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				if(mysocket!=null) {
					try {
						out.writeUTF("CLOSE");//发送关闭消息
						mysocket.close();
						////////////////////////////////////////////////////////关闭
						in.close();
						out.close();
						//
					}catch(IOException e2){
						System.out.println("Error:"+e2);
					}
				}
			}
		});
		frame.setTitle("\u5BA2\u6237\u7AEF");
		frame.setResizable(false);
		frame.setBounds(100, 100, 582, 529);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblIp = new JLabel("IP\u5730\u5740\uFF1A");
		lblIp.setBounds(10, 10, 54, 15);
		frame.getContentPane().add(lblIp);
		
		addressBox = new JTextField();
		addressBox.setText("localhost");
		addressBox.setBounds(60, 7, 66, 21);
		frame.getContentPane().add(addressBox);
		addressBox.setColumns(10);
		
		JLabel label = new JLabel("\u7AEF\u53E3\uFF1A");
		label.setBounds(136, 10, 54, 15);
		frame.getContentPane().add(label);
		
		portBox = new JTextField();
		portBox.setText("8000");
		portBox.setBounds(174, 7, 66, 21);
		frame.getContentPane().add(portBox);
		portBox.setColumns(10);
		
		JLabel label_1 = new JLabel("\u7528\u6237\u540D\uFF1A");
		label_1.setBounds(250, 10, 54, 15);
		frame.getContentPane().add(label_1);
		
		nameBox = new JTextField();
		nameBox.setText("test1");
		nameBox.setBounds(297, 7, 66, 21);
		frame.getContentPane().add(nameBox);
		nameBox.setColumns(10);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 35, 559, 328);
		frame.getContentPane().add(scrollPane);
		
		JTextArea context = new JTextArea();
		context.setLineWrap(true);
		scrollPane.setViewportView(context);
		context.setEditable(false);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 371, 557, 86);
		frame.getContentPane().add(scrollPane_1);
		
		JTextArea text = new JTextArea();
		text.setLineWrap(true);
		scrollPane_1.setViewportView(text);
		
		JLabel numLabel = new JLabel("\u6D88\u606F\u6761\u76EE\uFF1A0");
		numLabel.setBounds(10, 464, 153, 15);
		frame.getContentPane().add(numLabel);
		
		JButton connect = new JButton("\u8FDE\u63A5");
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isconnect) {
					JOptionPane.showMessageDialog(frame,"已连接到服务器，不要重复链接！","提示",JOptionPane.WARNING_MESSAGE);
					return;
				}
				new ClientThread().start();
				if(isconnect) {
					JOptionPane.showMessageDialog(frame,"连接成功！","提示",JOptionPane.WARNING_MESSAGE);
				}
			}
			class ClientThread extends Thread{
				private String s;
				public ClientThread() {
					try {
						isconnect = true;
						mysocket = new Socket(addressBox.getText(),Integer.parseInt(portBox.getText()));
						in = new DataInputStream(mysocket.getInputStream());
						out = new DataOutputStream(mysocket.getOutputStream());
						out.writeUTF(nameBox.getText());//第一条消息发送用户名信息
						s = in.readUTF();
						if(s.equals("RENAME")) {//最先检测用户名是否重复
							mysocket.close();
							JOptionPane.showMessageDialog(frame,"用户名已存在！","提示",JOptionPane.WARNING_MESSAGE);
							isconnect = false;
						}
					}/*catch(IOException e1) {
						System.out.println("Error:"+e1);
					}*/catch(Exception e2) {
						isconnect = false;
						JOptionPane.showMessageDialog(frame,"与服务器连接失败！","提示",JOptionPane.WARNING_MESSAGE);
					}
				}
				public void run() {
					try {
						if(!isconnect) {
							return;
						}
						boolean done = false;
						while(!done) {
							s = in.readUTF();
							if(s.equals("CLOSE")) {
								mysocket.close();
								////////////////////////////////////////////////////////关闭
								in.close();
								out.close();
								//
								JOptionPane.showMessageDialog(frame,"连接已断开！","提示",JOptionPane.WARNING_MESSAGE);
								isconnect = false;
								times = 0;
								numLabel.setText("消息条目"+times);
								return;
							}else if(s!=null) {
								context.append("Server:"+s+"\r\n");
								times++;
								numLabel.setText("消息条目"+times);
							}
						}
					}catch(IOException e1) {
						System.out.println("Error:"+e1);
					}
				}
			}
		});
		
		connect.setBounds(373, 6, 93, 23);
		frame.getContentPane().add(connect);
		
		JButton disconnect = new JButton("\u65AD\u5F00");
		disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(mysocket!=null) {
					try {
						out.writeUTF("CLOSE");//发送关闭消息
						mysocket.close();
						////////////////////////////////////////////////////////关闭
						in.close();
						out.close();
						//
						isconnect = false;
						times = 0;
						numLabel.setText("消息条目"+times);
						JOptionPane.showMessageDialog(frame,"连接已断开！","提示",JOptionPane.WARNING_MESSAGE);
					}catch(IOException e2){
						System.out.println("Error:"+e2);
					}
				}
			}
		});
		disconnect.setBounds(476, 6, 93, 23);
		frame.getContentPane().add(disconnect);
		
		JButton pass = new JButton("\u53D1\u9001");
		pass.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if(!isconnect) {
						JOptionPane.showMessageDialog(frame,"未连接到服务器！","提示",JOptionPane.WARNING_MESSAGE);
						return;
					}else {
					out.writeUTF(text.getText());
					context.append("Client:"+text.getText()+"\r\n");
					text.setText(null);
					}
				}catch(IOException e1){
					System.out.println("Error:"+e1);
				}
			}
		});
		pass.setBounds(476, 460, 93, 23);
		frame.getContentPane().add(pass);
		
		JButton button = new JButton("\u53D1\u9001\u6587\u4EF6");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();//定义变量
				File file = null;
				fis = null;
				dos = null;
				try {
					if(!isconnect) {
						JOptionPane.showMessageDialog(frame,"未连接到服务器！","提示",JOptionPane.WARNING_MESSAGE);
						return;
					}else {
						fc.setDialogTitle("打开文件");//设置标题
						fc.showOpenDialog(frame);//显示打开文件对话框
						file = fc.getSelectedFile();//获得文件路径
						if(file != null) {
							out.writeUTF("FILE");//判断打开了才能 发送文件消息模式
							fis = new FileInputStream(file);
							dos = new DataOutputStream(mysocket.getOutputStream());
							dos .writeUTF(file.getName());
							dos.flush();
							dos.writeLong(file.length());
							dos.flush();
						
							byte[] bytes = new byte[1024];//1024字节
							int length = 0;
							while((length = fis.read(bytes,0,bytes.length)) > 0) {
								dos.write(bytes,0,length);
								dos.flush();
							}
						}
					}
				}catch(Exception e2) {
					e2.printStackTrace();
				}
			}
		});
		button.setBounds(373, 460, 93, 23);
		frame.getContentPane().add(button);
	}
}
