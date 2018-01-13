package Chatter;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.*;

import java.awt.*;

//import com.sun.corba.se.spi.orbutil.fsm.Input;
//import sun.net.www.http.KeepAliveCache;
//import sun.text.resources.cldr.om.FormatData_om;
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class ChatterClient extends JFrame 
{
   int port = 8008;
   String ip = "localhost";
   String selUser;
   JPanel panel0;
   JPanel panel1;
   JPanel panel2;
   JButton send;
   JTextField chat;
   JTextArea ta;
   JLabel label = new JLabel();
   JPanel panel01 = new JPanel();
   JPanel panel02 = new JPanel();
   String nickname = " ";
   JButton exit;
   Scanner scan = new Scanner(System.in);
   BufferedReader bin = null;
   PrintWriter pout = null;
   JFrame frame = new JFrame("Client");
   Socket sock = null;
   boolean closed = false;
   private static ArrayList<String> namelist = new ArrayList<>();
   JList<String> myList = new JList<String>();
   DefaultListModel<String> nl = new DefaultListModel<>();


   ////////////////////////////////////////// MAIN ////////////////////////////////////////////
   
   public static void main( String[] args ) throws Exception // throws IOException
   {
	   ChatterClient client = new ChatterClient();
	   client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	   client.frame.setVisible(true);
	   client.run();
   }
      
   //////////////////////////////// CHATTERCLIENT CONSTRUCTOR //////////////////////////////////////

   public ChatterClient()
   {
	   
       myList = new JList<String>(nl);
	   panel0 = new JPanel();
	   panel0.setLayout(new BoxLayout(panel0, BoxLayout.Y_AXIS));
	   label.setText("Space left for the nickname");
	   panel0.setBackground( Color.white );
	   chat = new JTextField("type here", 80);
	   chat.setBackground(Color.WHITE);
	   panel01.add(label);
	   panel02.add(chat);
	   panel0.add(panel02);
	   panel0.add(panel01);
	   //	   panel0.add(chat);
	   ta = new JTextArea(60, 60);
	   ta.setBackground(Color.GRAY);
	   exit = new JButton("Close");
	   frame.setSize( new Dimension( 1000, 1000 ) );
	   panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
	   panel1.add(myList);
	   panel1.add(ta);
	   panel2 = new JPanel();
	   panel2.add(exit);
       frame.add(panel0, BorderLayout.NORTH);
       frame.add(panel1, BorderLayout.CENTER);
       frame.add(panel2, BorderLayout.SOUTH);
       panel0.add(label);
       
       ta.setEditable(false);
       
       exit.addActionListener(new ActionListener() {
		
			@Override
			public void actionPerformed(ActionEvent e) {
				pout.println("/close");
			}
       });
	   
       chat.addActionListener(new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {
			String msg = chat.getText();
			
		   if(msg.startsWith("/nick"))  // USE CASE 1: SENDS NEW NAME TO SERVER
		   {
				String orig = nickname;
				nickname = msg.substring(5);
				pout.println(msg + "***" + orig);
	    	   }
			else 
			{ 				
				if(!myList.isSelectionEmpty()) // USE CASE 2: SENDS PRIVATE MESSAGE TO SERVER 
				{
					selUser = myList.getSelectedValue();
					pout.println("PRIVATE "+ "+++" + nickname+ "---" + selUser + "***" + msg); 
					myList.clearSelection();
				}
			else 
			{
				pout.println("Message from " + nickname +": "+ msg); // USE CASE 3: SENDS MESSAGE TO SERVER
			}
			}
			chat.setText("");
		}
	});	   
   }

   public void run() throws Exception{
       sock = new Socket(ip, port); // if a server has the same port, then it will automatically accept this client
       bin = new BufferedReader(new InputStreamReader(sock.getInputStream()));
       pout = new PrintWriter(sock.getOutputStream(), true);
       while(true) {
    	   
	    	   String msg = bin.readLine();
	    	   
	    	   if (msg.startsWith("SETNAME")) {
	    		   nickname = msg.substring(8);
	    		   label.setText(nickname);
	    	   } else if(msg.startsWith("/close")) {
	    		   closed = true;
	    	   } else if(msg.startsWith("/updateNL")) {
	    		   String str = msg.substring(10);
	    		   str = str.substring(1,str.length()-1);
	    		   nl.removeAllElements();
	    		   namelist= new ArrayList<String>(Arrays.asList(str.split(", ")));
	    		   for(String s : namelist)
	    			   nl.addElement(s);    		   
	    		   panel1.repaint();
	    	   } else {
	    		   ta.append(msg + "\n");
	    	   }
	    	   
	    	   if(closed)
	    	   {
	    		   bin.close();
	    		   pout.close();
	    		   sock.close();
	    		   System.exit(0);
	    	   }
       }
   }
}
