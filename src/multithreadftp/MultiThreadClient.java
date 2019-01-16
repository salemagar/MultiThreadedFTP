package multithreadftp;

import java. util.*;
import java.io.*;
import java.net.Socket;

public class MultiThreadClient
{
	private static Scanner scan;
	private static Socket socket;
	private static PrintWriter out;
	private static BufferedReader in;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{	//ask and set the IP address
		System.out.println("Please enter the IP address running on port 9000:");
		scan = new Scanner(System.in);
		String serverAddress = scan.nextLine();
		socket = new Socket("localhost", 9000);
		
		//create IO stream
		out = new PrintWriter(socket.getOutputStream(),true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		System.out.println(in.readLine()); //show the "connection has been established
		String listOfchoice = in.readLine(); //show the list of options
		System.out.println("server says: " + listOfchoice);

		String client_choice0 = scan.nextLine(); 
		out.println(client_choice0);
		
		Boolean back = true;
		
		while(back)
		{
			switch(client_choice0)
			{	//new user
				case "1":
					back = true;
					new_user_client(in, out);
					break;
				//existed user
				case "2":
					back = false;
					existing_user_client(in, out);
					break;
				//disconnect
				case "3":
					back = false;
					out.println("Bye!");
					socket.close();
			} //switch
		} //while loop
	} // main method
	
	// create new user
	public static void new_user_client(BufferedReader in, PrintWriter out) throws IOException, ClassNotFoundException
	{
		boolean existed = true;
		
		while(!existed)
			
			System.out.println("READY*Please send new id and password!!!");
			String id = scan.next();
			String password = scan.next();
			
			//check if the id and password follow the rules
			if(id.matches("[A-Za-z0-9]+") && !password.isEmpty())
			{
				String new_user_info = String.join("*", id, password); //use delimiter to seperate id and password
				out.println(new_user_info);
				String msg = in.readLine(); //receive the msg from server
				
				if(msg.equals("CREATEFAILED"))
				{
					existed = false;
				}
				else
				{
					existed = true;
				}
			}
			else
			{
				System.out.println("You entered invalid userid or password. Please try again and follow the rules: id can include only alphanumeric characters and password can include alphanumeric characters and the special characters, ¡®!¡¯, ¡®#¡¯, ¡®$¡¯, ¡®%¡¯");
				existed = false;
			}
		existing_user_client(in, out);
	}
	
	public static void existing_user_client(BufferedReader in, PrintWriter out) throws IOException, ClassNotFoundException
	{
		System.out.println(in.readLine()); //read the ack "READY" from the server
		
		Boolean relogin = true;
		int trial_count = 0; //to count the times to try the password
		
		while(relogin)
		{
			if(trial_count < 3)
			{
				System.out.println("Please enter your id.");
				String user_id = scan.next();
				System.out.println("Please enter your password.");
				String user_password = scan.next();
				//combine id and password using delimiter*
				String loggedin_user_info = String.join("*", user_id, user_password);
				//pass the info to the server
				out.println(loggedin_user_info);
				String ack1 = in.readLine();
				System.out.println(ack1);
				
				if(ack1.equals("LOGINERROR"))
				{
					relogin = true;
					trial_count++;
				}
				else
				{
					relogin = false;
					//when log in succeed show the files owned by that client and the list of 4 options from the server
					String fileOfclient = in.readLine();
					System.out.println(fileOfclient);
					String listOfchoice2 = in.readLine();
					System.out.println(listOfchoice2);

					mainchoice(fileOfclient, loggedin_user_info);
				} //end else
			} //end if
			else
			{
				System.out.println("Sorry, you've tried 3 times. Please check your id and password");
				socket.close();
			}
		} //while loop
		
		
	}
	
	public static void mainchoice(String fileOfclient, String loggedin_user_info) throws IOException, ClassNotFoundException
	
	{
		Boolean repeat = true;
		while(repeat)
		{
			//scan what client chose
			String client_choice1 = scan.next();
			out.println(client_choice1);
			
			switch(client_choice1)
			{
				case "1":
					repeat = true;
					download(fileOfclient,loggedin_user_info);
					break;
				case "2":
					repeat = true;
					upload(fileOfclient, loggedin_user_info);
					break;
				case "3":
					repeat = true;
					System.out.println(fileOfclient);
					out.println("SUCCESSFUL"); //send the ack to the server
					System.out.println(in.readLine());//read list of choice again
					break;
				case "4":
					repeat = false;
					socket.close();
					break;
				default:
					repeat = true;
					break;
			} //switch
		} //while loop
	}
	
	public static void download(String fileOfclient, String loggedin_user_info) throws IOException, ClassNotFoundException
	{
		ObjectInputStream obj_i;
		Boolean exist = true;
		
		while(!exist)
		{
			System.out.println(in.readLine()); //read the msg of asking file neame client wants to download
			String name_of_file_d = scan.next();//scan the name of the file wanted to be downloaded
			
			out.println(name_of_file_d); //sent the name of file wanted to be downloaded to the server
			String ack = in.readLine(); //read the ack "FOUND" or "FILEDOWNLOADFAILED"
			if(ack.equals("FOUND"))
			{
				exist = true;
				
				System.out.println(ack);
				out.println("READY");
				
				obj_i = new ObjectInputStream(socket.getInputStream());
				File fileToDownload;
				fileToDownload = (File)obj_i.readObject(); //read everything from the server
				
				FileInputStream f_in = new FileInputStream(fileToDownload); //read the contents of that file
				FileOutputStream f_out = new FileOutputStream(name_of_file_d);
				
				//write those contents into a new file(downloading)
				int i;
				while((i = f_in.read()) != -1)
				{
					f_out.write(i);
				}
				f_out.close();
				f_in.close();
				out.println("DOWNLOADCOMPLETED");
				System.out.println(in.readLine()); //read the list of choice
				mainchoice(fileOfclient, loggedin_user_info);
			}
			
			if(ack.equals("FILEDOWNLOADFAILED"))
			{
				exist = false;
				System.out.println(in.readLine());
				mainchoice(fileOfclient,loggedin_user_info);
			}
		} //while loop
	}
	
	public static void upload(String fileOfclient, String loggedin_user_info) throws IOException, ClassNotFoundException
	{
		Boolean repeat = true;
		while(repeat)
		{
			//receives the ack "READY"
			System.out.println(in.readLine() + "Please enter the name of the file you want to upload");
			String name_of_file_u = scan.next();
			
			File fileToUpload = new File(name_of_file_u);
			
			int count = 0;
			while(count < 3)
			{
				if(fileToUpload.exists())
				{
					repeat = false;
					out.println(name_of_file_u); //send the name of the file to be uploaded to the server
				
					System.out.println(in.readLine()); //wait for ack "CONTINUE"
				
					/*
					//read the contents of the file
					BufferedReader br = new BufferedReader(new FileReader(fileToUpload));
					String contents = br.readLine();
					String contents_to_send = "";
					
					//keep reading the file need to be uploaded till it ends
					while(contents != null)
					{
						contents_to_send = contents_to_send + " " + contents;
					}
					*/

					String ack = in.readLine();
					if(ack.equals("CONTINUE"))
					{
						ObjectOutputStream obj_o = new ObjectOutputStream(socket.getOutputStream());
						obj_o.writeObject(name_of_file_u); //transfer the file wanted to be uploaded to the server
						//out.println(contents_to_send); //sent the contents to server
					}
					
					System.out.println(in.readLine()); //wait for the list of options
					//after upload successfully we need to send the that user info to the server to update account.txt
					out.println(loggedin_user_info); //it's a string with delimiter *
					mainchoice(fileOfclient, loggedin_user_info);
					
				}
				else
				{
					System.out.println("The file you want to upload does not exist, please try again");
					count++;
				}
			}//while loop
			
			out.println("ERROR");
			System.out.println(in.readLine()); //read the list of choices
			mainchoice(fileOfclient, loggedin_user_info);
			
		} //while loop
	} //upload
}
