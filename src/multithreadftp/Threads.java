package multithreadftp;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Threads implements Runnable
{
	
	private static Socket socket = null;
	private ArrayList<String> user_id;
    private ArrayList<String> user_info;
    private static File file;
	private static PrintWriter out;
    private static BufferedReader in;
    private static String loggedin_user_info;
	
	public Threads(Socket socket, ArrayList<String> user_id, ArrayList<String> user_info, File file) //pass the socket to the Threads class
	{
		this.socket = socket;
		this.user_id = user_id;
		this.user_info = user_info;	
		this.file = file;
	}
	
	public void run()
	{
		while(socket.isConnected()) //when the socket is connected do the following
		{
			try
			{	
				file = new File("account.txt");
				//create IO stream
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
				
				out.println("Please choose the number: 1. New User, 2. Existing User, 3. Disconnect");
				
				mainchoice(out, in);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			} 
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
		} //while loop
	} //run method
	
	
	public void mainchoice(PrintWriter out, BufferedReader in) throws IOException, ClassNotFoundException
	{
		System.out.println("ready to take the choice");
		String client_choice;
		client_choice = in.readLine();
		System.out.println("Client chose: " + client_choice);
		Boolean back = true;
		
		while(back)
		{
			//options
			switch(client_choice)
			{	//new user
				case "1":
					back = true;
					new_user_server(in, out, file);
					break;
				//existed user
				case "2":
					back = false;
					existing_user_server(in, out, file);
					break;
				//disconnect
				case "3":
					back = false;
					out.println("Bye!");
					socket.close();
			} //switch
		} //while loop
	}
	
	//create new user
	public void new_user_server(BufferedReader in, PrintWriter out, File file) throws IOException, ClassNotFoundException
	{		
		Boolean existed = true;
		
		while(existed)
		{				
			String ack0 = "READY";
			
			out.println(ack0 + "*Please enter a new id and a password.");
			
			String line = in.readLine(); //receive new_user_info which contains id and password splited by delimiter * from the client
			String[] id = line.split("\\*"); //split id and password using * and store them
			//out.println("Your username: " + id[0] + " Your password: " + id[1]);
			
			if(user_id.contains(id[0])) //when the id is existed go back and ask for entering new id
			{
				String ack1 = "CREATEFAILED";
				existed = true;
				out.println(ack1);
			}
			else //when there is no duplicated id, then write the new one into file
			{	
				existed = false;
				out.println("New user has been created!");
				BufferedWriter new_user_in = new BufferedWriter(new FileWriter(file,true));
				PrintWriter new_user_out = new PrintWriter(new_user_in, true);
				
				//write new user and its password to the file
				new_user_out.println("\n" + id[0] + " " + id[1]);
				new_user_out.close();
			}
		} //while loop
		
		choice_level_two(in, out, file);
	}
	
	public void choice_level_two(BufferedReader in, PrintWriter out, File file) throws IOException, ClassNotFoundException
	{
		//send the options to the client
		out.println("Please choose the number: 1. Download, 2. Upload, 3. File List, 4. Disconnect");
		
		Boolean repeat1 = true;
		
		while(repeat1)
		{
			String client_choice1 = in.readLine();
			
			//switch
			switch(client_choice1)
			{
				case "1":
					repeat1 = true;
					download(in, out);
					break;
				case "2":
					repeat1 = true;
					upload(in, out);
					break;
				case "3":
					repeat1 = true;
					System.out.println(in.readLine());
					out.println("Please choose the number: 1. Download, 2. Upload, 3. File List, 4. Disconnect");
					break;
				case "4":
					repeat1 = false;
					socket.close();
					break;
				default:
					repeat1 = true;
					break;
			} //end switch
		} //while loop
	}
	
	public void existing_user_server(BufferedReader in, PrintWriter out, File file) throws IOException, ClassNotFoundException
	{
		out.println("READY"); //when the client choose #2, the sever sends back an ack "READY"

		Boolean repeat0 = true;
		int count = 0;
		
		while(repeat0 && (count < 3))
		{
			String info = in.readLine(); //read the id and password combinition
			loggedin_user_info = info;
		
			String[] user_info_read = info.split("\\*"); //split the id and password by delimiter *
	
			if(user_info.contains(user_info_read[0] + " " + user_info_read[1]))
			{
				repeat0 = false;

				for(int i = 0; i < user_info.size(); i++)
				{
					BufferedReader br = new BufferedReader(new FileReader(file));
					String read_line = br.readLine(); //read the line of file of the user
					String user_line = "";
					
					System.out.println(read_line);
					
					//check if the line it's currently reading has the user id. if it has store them in to user_line
					//while(read_line != null)
					//{
						System.out.println("TEST3");
						
						if(read_line.contains(user_info_read[0]))// && read_line != null)
						{
							System.out.println("TEST4");
							
							user_line = read_line;
							out.println(user_line); //send the list of files owned by the user to the client side
							break;
						}
					//}
					
				} //for loop

				//after log in show the 4 option list
				choice_level_two(in, out, file);
			}
			else
			{
				out.println("LOGINERROR"); //send the ack when login fails
				count++;
				repeat0 = true;
			}
		} //while loop
		
		out.println("LOGINERROR"); //after tried three times send the ack "LOGINERROR" again
		out.println("Please choose the number: 1. New User, 2. Existing User, 3. Disconnect");
		mainchoice(out, in); //
	}
	
	public static void download(BufferedReader in, PrintWriter out) throws IOException
	{
		Boolean repeat = true;
		
		while(repeat)
		{
			out.println("Please enter the name of file that the you want to download");
			String name_of_file_d = in.readLine(); //read the file name
			File fileToDownload = new File(name_of_file_d);
			System.out.println("The file user wants to download is: " + name_of_file_d);
			
			if(fileToDownload.exists())
			{
				repeat = false;
				out.println("FOUND"); //send ack "FOUND" to the client and wait for the ack "READY"
				System.out.println(in.readLine());
				
				ObjectOutputStream obj_o = new ObjectOutputStream(socket.getOutputStream());
				obj_o.writeObject(name_of_file_d); //transfer the file to the client
				
				System.out.println(in.readLine()); //read the ack "DOWNLOADCOMPLETED"
				out.println("Please choose the number: 1. Download, 2. Upload, 3. File List, 4. Disconnect");
			}
			else
			{
				repeat = true;
				out.println("FILEDOWNLOADFAILED");
				out.println("Please choose the number: 1. Download, 2. Upload, 3. File List, 4. Disconnect");
			}
		} //while loop
	}
	
	public static void upload(BufferedReader in, PrintWriter out) throws IOException, ClassNotFoundException
	{
		out.println("READY"); //send ack "READY" to client
		String ack = in.readLine(); //read the ack if it's "ERROR" or actual file name
		
		if(ack.equals("ERROR"))
		{
			out.println("Please choose the number: 1. Download, 2. Upload, 3. File List, 4. Disconnect");
		}
		else
		{
			String name_of_file_u = ack;
			
			//deal with dupilcated file names
			File check_file = new File(name_of_file_u);
			
			String new_name_of_file_u = "";
			
			int count = 1;
			Boolean _copy_exist = true;
			
			while(_copy_exist)
			{
				if(check_file.exists()) //check if the file name is existed
				{
					String pure_file_name = name_of_file_u.substring(0, name_of_file_u.lastIndexOf('.')); //get the file name without ".txt"
				
					//rename the file
					if(pure_file_name.contains("_copy_")) //if the copy is already existed
					{
						_copy_exist = true;
						String[] main_file_name = pure_file_name.split("_");
						pure_file_name = main_file_name[0]; //take the main file name
						pure_file_name = pure_file_name + "_copy_" + count;
						
						check_file = new File(pure_file_name + ".txt"); //after after all the suffix go back and compare if the file name is still existed
						count++;
					}
					else
					{
						_copy_exist = false;
						pure_file_name = pure_file_name + "_copy_" + count;
						new_name_of_file_u = pure_file_name + ".txt";
					}
				
					new_name_of_file_u = pure_file_name + ".txt";
				}
				else
				{
					_copy_exist = false;
					//if the file wanted to be uploaded does not exist in the server then file name stay the same
					new_name_of_file_u = name_of_file_u;
				}
			} //while loop

			out.println("CONTINUE"); //after dealing with depulicat names of files send ack "CONTINUE"
			
			String contents_received = in.readLine(); //if the ack is not "ERROR", then the server receives the name of the file wanted to be uploaded
			
			/***********************************************************************************/
			//create and write the new file
			/*start to receive files*/
			ObjectInputStream obj_i = new ObjectInputStream(socket.getInputStream());
			File fileToUpload;
			fileToUpload = (File)obj_i.readObject(); //create a new file and read everything from the client

			FileInputStream f_in = new FileInputStream(fileToUpload); //read the contents of that file
			FileOutputStream f_out = new FileOutputStream(name_of_file_u);
			
			//write those contents into a new file(uploading)
			int i;
			while((i = f_in.read()) != -1)
			{
				f_out.write(i);
			}
			f_out.close();
			f_in.close();
			/***********************************************************************************/
			
			//upload the file "account.txt"
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			//String loggedin_user_info = in.readLine(); //take userid and password info
			
			loggedin_user_info = loggedin_user_info + "*" + new_name_of_file_u;
			String[] loggedin_user_info_Array = loggedin_user_info.split("\\*");
			
			BufferedWriter file_writer = new BufferedWriter(new FileWriter(file));
            PrintWriter printWriter = new PrintWriter(file_writer);
			
			for(int index = 0;  index < loggedin_user_info_Array.length-1; index++)
			{
				printWriter.print(loggedin_user_info_Array[i] + " ");
			}
		} //end if
	} //end upload method
}
