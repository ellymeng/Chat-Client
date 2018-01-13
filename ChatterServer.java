package Chatter;
// DateServer.java
// Silberschatz et al OS Concepts with Java e6 p130+

/*
   This is a demo program for basic socket stuff, to be used with
   DateClient.java.  
   The Server starts by setting up a ServerSocket, a place for clients 
   to connect to.  Then it waits (repeatedly) for a client to
   call.  When client calls, we have an active Socket.  We open
   an input stream to read from it and an output stream to write
   to it.  Note: the sequence of when the server and client each
   write and read must be exactly agreed upon between the two.
   If not, the pair will hang.
   In this case, the Server expects a message from the Client
   and then the Server sends the date.  
   (In this demo pair, the message is "What time is it?",
   but in general the message could be whatever and the 
   Server could respond specifically to it).  
*/

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.Socket;
//import org.omg.CORBA.PUBLIC_MEMBER;
//import com.sun.corba.se.impl.naming.pcosnaming.NameServer;
//import com.sun.security.ntlm.Client;

public class ChatterServer
{
   private static final int PORT = 8008;
   private static ArrayList<String> names = new ArrayList<>();
   private static ArrayList<PrintWriter> writers = new ArrayList<>();
 
   public static void main( String[] args ) throws Exception //throws IOException
   {
	   new ChatterServer();
   }
   
   ChatterServer() throws Exception{
	   System.out.println("ChatterServer stated running.");
	   
	   ServerSocket sock = new ServerSocket(PORT);
	   try {
		   while(true) {
			   new ThreadClass(sock.accept()).start();
		   }
	   }
	   finally {
		   sock.close();
	   }
   }
   ////////////////////////////// THREAD CLASS ///////////////////////////////
   
   public static class ThreadClass extends Thread{ // only needs method start() from Thread
	   
	   private String name;
	   private Socket socket;
	   private BufferedReader bin;
	   private PrintWriter pout;
	   
	   public ThreadClass(Socket socket) {
		   this.socket = socket;
	   }

	   public void run() 
	   {
		   try {
			   name = "Anonymous" + (names.size() + 1);
			   bin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			   pout = new PrintWriter(socket.getOutputStream(), true);
  
			   synchronized (names)
			   {
				   names.add(name);
				   pout.println("SETNAME " + name);
				   pout.println("Welcome to chatroom "+ name+"!");
				   writers.add(pout);
				   for (PrintWriter writer : writers) {
					   writer.println("/updatenl " + names);							   }			   }
			   
			   while(true)
			   {
				   String msg = bin.readLine();
				   if (msg == null){
					   continue;
					   }
				   else if (msg.startsWith("/nick")) { // USE CASE 1: BROADCASTS NAME CHANGE (ENABLES ALL CLIENTS TO UPDATE GUI)
					   
					   int pos = msg.indexOf("***");
					   String newname = msg.substring(5,pos);
					   if(names.contains(newname))
					   {
						   String orig = msg.substring(pos+3);
						   pout.println("SETNAME " + orig);
						   pout.println(newname + " Already taken setting to first");
						   } else {
							   pos = names.indexOf(name);
							   names.remove(name);
							   names.add(pos,newname);
							   pout.println("SETNAME " + newname);
							   pout.println("Your nickname has been changed from "+name+ " to " +newname);
							   for (PrintWriter writer : writers) {
								   writer.println("/updatenl " + names);							   
								   }
						   }
					   }
				   else if(msg.startsWith("PRIVATE")) { // USE CASE 2: UPDATE PRIVATE CLIENT CHAT 
					   
					   int i1 = msg.indexOf("+++");
					   int i2 = msg.indexOf("---");
					   int i3 = msg.indexOf("***");
					   
					   String from = msg.substring(i1+3, i2); // person pm is sent by
					   String forr = msg.substring(i2+3,i3); //person pm is intended for
					   String msg2 = msg.substring(i3+3); //actual message
					   
					   int indexFor = names.indexOf(forr);
					   int indexFrom = names.indexOf(from);
					   
					   PrintWriter writer2 = writers.get(indexFor);
					   PrintWriter writer = writers.get(indexFrom);
					   writer.println("Private message from " + from + ": " +msg2);
					   writer2.println("Private message to " + forr + ": " +msg2);
				   }
				   else {
					   for (PrintWriter writer : writers) { // USE CASE 3: UPDATE ALL CLIENT CHATS
						   writer.println(msg);
					   }
				   }
			   }
		   }
		   catch (IOException e) {
				e.printStackTrace();
			} finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                }
                if (pout != null) {
                    writers.remove(pout);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
