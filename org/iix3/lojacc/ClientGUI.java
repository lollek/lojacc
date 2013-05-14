/* lojacc ClientGUI
 * GUI Handling for Chat Applet
 * Requires ClientSocket from the same package
 */
//http://java-sl.com/tip_autoreplace_smiles.html

package org.iix3.lojacc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;

import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLDocument;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.iix3.lojacc.ClientSocket;

public class ClientGUI extends JApplet implements KeyListener {

  String _info = "lojacc v0.1.1b by Olle K";

  SimpleDateFormat dateFormat;
  JTextArea inputLine;
  JEditorPane chatWindow;
  Document cwText;
  HTMLDocument cwTextHTML;
  JPanel mainGUI;
  ClientSocket sock;

  public ClientGUI() {

    mainGUI = new JPanel(new BorderLayout(20,20));
    mainGUI.setBorder(new EtchedBorder(Color.white, Color.gray));
    
    // Writeable field
    inputLine = new JTextArea(2, 0);
    inputLine.setLineWrap(true);
    inputLine.addKeyListener(this);
    mainGUI.add(new JScrollPane(inputLine), BorderLayout.SOUTH);

    // Chat Window
    chatWindow = new JEditorPane();
    chatWindow.setContentType("text/HTML");
    chatWindow.setEditorKit(new HTMLEditorKit());
    cwText = chatWindow.getDocument();
    cwTextHTML = (HTMLDocument)cwText;
    Font c11 = new Font("Calibri", Font.PLAIN, 11);
    String cwCSS = "body { font-family: "+c11.getFamily()+"; font-size: "+c11.getSize()+"pt;}";
    cwTextHTML.getStyleSheet().addRule(cwCSS);
    DefaultCaret caret = (DefaultCaret)chatWindow.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    chatWindow.setEditable(false);
    mainGUI.add(new JScrollPane(chatWindow), BorderLayout.CENTER);

    // Config
    dateFormat = new SimpleDateFormat("HH.mm");
    
    setContentPane(mainGUI);
    validate();

    chatPrint(_info);
    sock = new ClientSocket(this);
  }

  public void keyPressed(KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.VK_ENTER) {

      String msgString = inputLine.getText();
      if (msgString.length() < 1) return;
      inputLine.setText(null);
      if (msgString.substring(0, 1).equals("/")) {

        if (msgString.equals("/connect")) {
          if (sock != null) chatPrint("/disconnect your current socket first!");
          else sock = new ClientSocket(this);
        }

        else if (msgString.equals("/disconnect")) {
          if (sock == null) chatPrint("You are not connected");
          else {
            sock.kill();
            sock = null;
          }
        }

        else if (msgString.equals("/reconnect")) {
          if (sock != null) sock.kill();
          sock = new ClientSocket(this);
        }
      }
      else if (sock != null) sock.send(msgString);
      else chatPrint(msgString);
    }
  }

  public void keyReleased(KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.VK_ENTER)
      inputLine.setText(null);
  }

  public void chatPrint(String msgString) {
    
    //Reencode the string to UTF-8 in case it's ISO-8859-1 or something
    try {
      msgString = new String(msgString.getBytes(Charset.defaultCharset()), "UTF-8"); 
    } catch (UnsupportedEncodingException ueeError) {
      msgString = "Error encoding string"; 
    }

    // Escape some HTML:
    msgString = msgString.replace("&", "&amp;");
    msgString = msgString.replace("<", "&lt;");
    msgString = msgString.replace(">", "&gt;");

    try {
      Element len = cwTextHTML.getParagraphElement(cwTextHTML.getLength());
      if (msgString.substring(0, 5).equals("&lt;~"))
        cwTextHTML.insertBeforeEnd(len, timestamp() + "<b> " + msgString + "\n</b><br>");
      else
        cwTextHTML.insertBeforeEnd(len, timestamp() + " " + msgString + "\n<br>");
    } catch(BadLocationException blError) {
    } catch(IOException ioError) {
    }
  }

  

  private String timestamp() {
    return "[" + dateFormat.format(new Date()) + "]";
  }
  
  public void keyTyped(KeyEvent event) { return; }
}
