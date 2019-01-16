package multithreadftp;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class MultiThreadServer 
{
	private static ArrayList<String> user_id;
	private static ArrayList<String> user_info;
	private static File file;
	
	public static void main(String[] args) throws IOException 
	{
		ServerSocket server = new ServerSocket(9000); //set port number
		readFile();
		System.out.println("Waiting for connection...");
		
		try 
		{
			int count = 0; //count the number of thread has been created
			Boolean connection = true;
	
			while(connection)
			{
				Socket socket = server.accept();
				OutputStream out = socket.getOutputStream();
				PrintWriter pw = new PrintWriter(out, true);
				count++;
				
				if (count > 5)
				{
					pw.println("maximum connections being made and can't take any additional connection at this time. Please try again!!");
					socket.close();
					break;
				}
				new Thread(new Threads(socket, user_id, user_info, file)).start();
				System.out.println("Connected. Current client: " + count);
				pw.println("connection has been established");
			}
		}
		finally
		{
			server.close();
		}
	} //main method

	public static void readFile() //method to show the contents of the file
	{
		try
		{

			file = new File("account.txt");
			//store user ids
			user_id = new ArrayList<>();
			//store user ids and their passwords
			user_info = new ArrayList<>();
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader); 
			String line;

			//link the id and password and store them together in the arraylist
			while((line = bufferedReader.readLine()) != null)
			{
				if(!line.isEmpty())
				{
					String[] info = line.split("\\s+");
					user_id.add(info[0]);
					user_info.add(info[0] + " " + info[1]);

					System.out.println(line);
				}
			}
			bufferedReader.close();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	} //read file method
}

