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

  final ClientGUI master;
  Socket socket;

  boolean runThread = false;

  BufferedReader inBuff;
  BufferedOutputStream outBuff;

  public ClientSocket(ClientGUI _master, String hostname, int port) {

    master = _master;
    master.chatPrint("Connecting to " + hostname);

    try {
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
  public ClientSocket(ClientGUI _master) {
    this(_master, "iix3.org", 7777);
  }

  public void run() {

    String recvmsg = null;
    try {
      do {
        if ((recvmsg = inBuff.readLine()) == null)
          continue;
        master.chatPrint(recvmsg);

      } while (recvmsg != null && runThread);
    } catch (IOException e) {
      kill();
    }
  }

  public void send(String msgString) {
    msgString = msgString + "\n";
    try {
      outBuff.write(msgString.getBytes("UTF-8"));
      outBuff.flush();
    } catch (IOException e) {
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
      master.sock = null;
      master.chatPrint("Disconnected from server - Type /reconnect to reconnect");
    } catch (IOException ioError) {}
  }
}