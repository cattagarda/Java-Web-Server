import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class WebServerDemo extends Thread{
	private Socket server;
	static String connection = "Connection: keep-alive";
	static String serverName = "Server: Pseudo-Apache";
	static String acceptRanges = "Accept-Ranges: bytes";
	static String keepAlive = "Keep-Alive: timeout=5, max=100";
	static String responseHeader = null;
	static String mimetype = "";
	   
	   private static ServerSocket serverSocket;
	  
	   public WebServerDemo(Socket server) throws IOException{
	         this.server = server ;
	   }
	   
	   public static void main(String [] args) throws IOException{
	      try{
	    	  getConnection(80);
	      } catch(ArrayIndexOutOfBoundsException e) {
	    	  e.printStackTrace();
	      }
	   }
	   
	   public static void getConnection(int port){
		   System.out.println("The Web Server has started...");
		   
		   try{
			   while(true){
				   //Create a socket that listens on the given port. e.g. 8080
				   serverSocket = new ServerSocket(port);
				   
				   System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
				   
				   //Accept connection of the socket
				   Socket anotherServer = serverSocket.accept();
				   WebServerDemo aServer = new WebServerDemo(anotherServer);
				   //Create a thread for the server
				   Thread forAServer = new Thread(aServer);
				   forAServer.start();
				   
				   serverSocket.close();
			   }
			   
		   } catch(IOException e){ }
		   
	   }
	   
	   public void run(){
		    InputStreamReader inputBuffer;
		    String table = null;
		    
			try {
				inputBuffer = new InputStreamReader(this.server.getInputStream());
				BufferedReader inFromClient = new BufferedReader(inputBuffer);
				String clientSentence = inFromClient.readLine();
			       
				System.out.println(clientSentence);
		         
				if(clientSentence != null){
					String[] inputString = clientSentence.split(" ");
					FileReader inputFile = null;
		         
			         String iString1 = inputString[1];
			         String [] iString2  = iString1.split("\\?");
			         
			         if(iString2.length == 2){
			        	 table = "<table border=1><tr><th>Parameter</th><th>Value</th></tr>";
			        	String [] sElement = iString2[1].split("\\&");
			        	
			        	for(String kElem : sElement){
			        		String [] splitted = kElem.split("=");
			        		
			        		for(String parsed: splitted){
			        			System.out.println(parsed);
			        		}
			        		
			        		table = table + "<tr><td>"+splitted[0]+"</td><td>"+splitted[1]+"</td></tr>";
			        	}
			        	table = table+"</table>";
			         } else {
			        	 table = null;
			         }
			         
			         String fileName = iString2[0];
			    	 
			         if(fileName.equals("/")){
			        	   inputFile = new FileReader("index.html");
			        	   fileName = "index.html";
			         } else {
			        	 try{
			        	   inputFile = new FileReader(fileName.replace("/", ""));
			        	 }catch(Exception e){}
			         }
			         
			         String response;
			         
			         	if(inputFile != null){
				        	BufferedReader retrieveFile  = new BufferedReader(inputFile);
				        	if(table != null){
				        	 response =  createResponse(retrieveFile, fileName, table);
				        	} else {
				        		response =  createResponse(retrieveFile, fileName, null);
				        	}
				        	
				        	retrieveFile.close();
			         	
			         	} else {
			         		 response =  createResponse(null, fileName, null);
			         	}
			        	
			        	DataOutputStream outputStream;
						
			        	try {
			        		outputStream = new DataOutputStream(this.server.getOutputStream());
			        		outputStream.writeUTF(response);
				
						} catch (IOException e) {
							e.printStackTrace();
						}
			        	
						this.server.close();
		          }

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	   
	   public static String createResponse(BufferedReader file, String filename, String table){
			String buffer = "";
			String response = "";
			
			final Date currentTime = new Date();
			final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");
			String[] FileType = filename.split("\\.");
			
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			
			switch(FileType[1]){
				case "html": mimetype = "text/html";
							break;
				case "css": mimetype = "text/css";
							break;
				case "js": mimetype = "application/javascript";
							break;
			}
			
			responseHeader = "HTTP/1.1 ";
			if(file != null){
				responseHeader = responseHeader+"200 OK\n";
			} else {
				responseHeader = responseHeader+"404 Not Found\n";
			}
			
			if(file != null){
				try {
					while((buffer = file.readLine())!= null){
						if(Pattern.matches("\\s*<body>\\s*",buffer) && table != null){
							response = response + buffer +"\n";
							response = response + table + "\n";
						}else{
							response = response + buffer +"\n";
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				response="<h1>404: Page Not Found</h1><p>This file may have been moved or deleted.</p><p>Powered by "+serverName+"</p>";
			}
			
			
			responseHeader = responseHeader+""+connection+"\n"+
					"Content-Length: "+response.length()+"\n"
					+"Content-Type: "+mimetype+"\n"+acceptRanges
					+"\nDate: "+sdf.format(currentTime)
					+"\n"+keepAlive+"\n"+serverName+"\n\n";
			
			responseHeader = responseHeader+response;
			
			return responseHeader;
			
		}
	   
}
