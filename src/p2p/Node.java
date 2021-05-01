package p2p;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentSkipListSet;

public class Node {
	FileHandler fileHandler;
	int port;
	String ip;
	String path = System.getProperty("user.dir");
	String folderPath = path + File.separator + "files";
	File directoryPath = new File(folderPath);

	public final int FILE_SIZE = 6022386; // file size temporary hard coded
	// should bigger than the file to be downloaded
	String FILE =  folderPath + File.separator + "temp1.txt";

	public Node(String type, int port, ConcurrentSkipListSet<String> networkIps) throws IOException {
		switch (type) {
		case "Server":
			this.port = port;
			this.ip = InetAddress.getLocalHost().getHostAddress().toString();
			ServerSocket serverSock = new ServerSocket(port);
			Socket server = serverSock.accept();
			System.out.println("Connected");

			// receive file
			receiveFile(server);
//			receiveFile(server, this.FILE_SIZE, this.FILE);

			server.close();
			break;
		case "Client":
			this.port = port;
			System.out.println("Waiting for connection ...");
			Socket client;
			for (String ip : networkIps) {
				try {
					client = new Socket(ip, port);
					System.out.println("Connected");

					// send file
					sendFile(client);
					
					client.close();
				} catch (ConnectException e) {
					// do nothing
				}
			}
			break;
		}

	}


	private void setDirectory() {
		if(!directoryPath.exists()) {
			directoryPath.mkdir();
		}
	}

	private String getPath() {
		return folderPath;
	}

	private File[] getListofFiles() {
		setDirectory();
		return directoryPath.listFiles();
	}
	
	private void sendFile(Socket socket) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		DataOutputStream dos = new DataOutputStream(bos);
		
		dos.writeInt(getListofFiles().length);

		for(File file : getListofFiles())
		{
		    long length = file.length();
		    dos.writeLong(length);

		    String name = file.getName();
		    dos.writeUTF(name);

		    FileInputStream fis = new FileInputStream(file);
		    BufferedInputStream bis = new BufferedInputStream(fis);

		    int theByte = 0;
		    while((theByte = bis.read()) != -1) bos.write(theByte);

		    bis.close();
		    System.out.println("Sent " + name);
		}

		dos.close();
	}
	
	private void receiveFile(Socket socket) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
		DataInputStream dis = new DataInputStream(bis);

		int filesCount = dis.readInt();
		File[] files = new File[filesCount];
		
		for(int i = 0; i < filesCount; i++) {
			long fileLength = dis.readLong();
		    String fileName = dis.readUTF();

		    files[i] = new File(getPath() + File.separator + fileName);

		    FileOutputStream fos = new FileOutputStream(files[i]);
		    BufferedOutputStream bos = new BufferedOutputStream(fos);

		    for(int j = 0; j < fileLength; j++) bos.write(bis.read());
		    
		    System.out.println("File " + fileName + " downloaded");

		    bos.close();
		}
	}
	
}
