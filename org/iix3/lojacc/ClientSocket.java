package org.iix3.lojacc;

public class ClientSocket 
  extends Thread {

  final org.iix3.lojacc.ClientGUI master;
  final String HOSTNAME = "iix3.org";
  final int PORT = 7777;
  boolean runThread = false;
  java.net.Socket socket;
  java.io.BufferedReader inBuff;
  java.io.BufferedOutputStream outBuff;

  public ClientSocket(ClientGUI _master) {

    master = _master;
    master.chatPrint("Connecting to " + HOSTNAME);

    try {
      socket = new java.net.Socket(HOSTNAME, PORT);
      inBuff = new java.io.BufferedReader(
        new java.io.InputStreamReader(
          socket.getInputStream()));
      outBuff = new java.io.BufferedOutputStream(socket.getOutputStream());

      master.chatPrint("Connection Established");
      runThread = true;
      
    } catch(java.net.UnknownHostException e) {
      master.chatPrint("Connection failed: " + e.getMessage());
    } catch(java.io.IOException e) {
      master.chatPrint("Connection failed: " + e.getMessage());
    }

    if (runThread)
      this.start();
  }

  public void run() {

    String recvmsg = null;
    try {
      do {
        if ((recvmsg = inBuff.readLine()) != null)
          master.chatPrint(recvmsg);
      } while (recvmsg != null && runThread);
    } catch (java.io.IOException e) {
      kill();
    }
  }

  public void send(String msgString) {
    msgString = msgString + "\n";
    try {
      outBuff.write(msgString.getBytes("UTF-8"));
      outBuff.flush();
    } catch (java.io.IOException e) {
      master.chatPrint("Error sending: " + e.getMessage() +
                       " - Try typing /reconnect in case you've lost connection");
    }
  }

  public void kill() {
    
    if (!runThread)
      return;

    try {
      runThread = false;
      socket.close();
      master.disconnectSocket();
      master.chatPrint("Disconnected from server - Type /reconnect to reconnect");
    } catch (java.io.IOException e) {}
  }
}