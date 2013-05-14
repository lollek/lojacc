/* lojacc ClientSocket
 * This is a socket class for ClientGUI from the same package
 *
 * Scope is to connect to iix3.org @ port 7777, thus, everything is hardcoded
 * It should not be hard to change though
 */

package org.iix3.lojacc;

import java.net.Socket;
import java.net.UnknownHostException;

import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import org.iix3.lojacc.ClientGUI;

public class ClientSocket extends Thread {

  ClientGUI master;
  Socket socket;

  String hostname = "iix3.org";
  int port = 7777;
  boolean runThread = false;

  BufferedReader inBuff;
  BufferedOutputStream outBuff;

  public ClientSocket(ClientGUI _master) {

    master = _master;

    try {
      master.chatPrint("Connecting to " + hostname);

      socket = new Socket(hostname, port);
      inBuff = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      outBuff = new BufferedOutputStream(socket.getOutputStream());

      master.chatPrint("Connection Established");
      runThread = true;
      
    } catch(UnknownHostException uhError) {
      master.chatPrint("Connection failed: " + uhError.getMessage());
    } catch(IOException ioError) {
      master.chatPrint("Connection failed: " + ioError.getMessage());
    }

    if (runThread)
      this.start();
  }

  public void run() {

    String recvmsg = null;
    do {
      try {
        if ((recvmsg = inBuff.readLine()) != null)
          master.chatPrint(recvmsg);
      }
      catch (IOException ioError) {
        master.chatPrint("Exception raised: " + ioError.getMessage() +
                         " - Try typing /reconnect if you're experiencing issues");
      }
    } while (recvmsg != null && runThread);
  }

  public void send(String msgString) {
    msgString = msgString + "\n";
    try {
      outBuff.write(msgString.getBytes("UTF-8"));
      outBuff.flush();
    } catch (IOException ioError) {
      master.chatPrint("Error sending: " + ioError.getMessage() +
                       " - Try typing /reconnect in case you've lost connection");
    }
  }

  public void kill() {
    try {
      runThread = false;
      socket.close();
    } catch (IOException ioError) {
      return;
    }
  }
}