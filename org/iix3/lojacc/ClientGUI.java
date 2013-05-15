/* lojacc ClientGUI
 * GUI Handling for Chat Applet
 * Requires ClientSocket from the same package
 */
//http://java-sl.com/tip_autoreplace_smiles.html

package org.iix3.lojacc;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLDocument;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.iix3.lojacc.ClientSocket;

public class ClientGUI extends JApplet implements KeyListener {

  String _info = "lojacc v0.2 (2013-05-15) by Olle K";

  JPanel mainGUI;
  JTextArea wArea;
  HTMLDocument rAreaDoc;

  SimpleDateFormat dateFormat = new SimpleDateFormat("HH.mm");
  int currFontSize = 11;
  String currFontName = "Calibri";
  ClientSocket sock;

  public ClientGUI() {
    
    /* GUI Window = MainGUI */
    mainGUI = new JPanel(new GridBagLayout());
    mainGUI.setBorder(new EtchedBorder(Color.white, Color.gray));
    
    /* Writeable field = wArea */
    wArea = new JTextArea(2, 0);
    wArea.setLineWrap(true);
    wArea.addKeyListener(this);
    GridBagConstraints wAreaCon = new GridBagConstraints();
    wAreaCon.ipady = 25;
    wAreaCon.weightx = 1;
    wAreaCon.weighty = 1;
    wAreaCon.gridwidth = 2;
    wAreaCon.gridx = 0;
    wAreaCon.gridy = 2;
    wAreaCon.fill = GridBagConstraints.HORIZONTAL;
    wAreaCon.anchor = GridBagConstraints.SOUTH;
    mainGUI.add(new JScrollPane(wArea), wAreaCon);

    /* Text Size Button */
    JButton tSizeButton = new JButton("Text Size");
    GridBagConstraints bCon = new GridBagConstraints();
    bCon.fill = GridBagConstraints.HORIZONTAL;
    bCon.gridx = 0;
    bCon.gridy = 1;
    final JPopupMenu tSizePopup = new JPopupMenu();
    for (int i = 10; i <= 20; i++)
      tSizePopup.add(returnSizePop(i));
    tSizeButton.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          tSizePopup.show(e.getComponent(), e.getX(), e.getY());
        }
      });
    mainGUI.add(tSizeButton, bCon);

    /* Aux Button*/
    JButton button = new JButton("Color");
    bCon.gridx = 1;
    bCon.insets = new Insets(0, 0, 0, 850);
    mainGUI.add(button, bCon);

    /* Chat Window = rArea */
    JEditorPane rArea = new JEditorPane();
    rArea.setEditable(false);
    rArea.setContentType("text/HTML");
    rArea.setEditorKit(new HTMLEditorKit());
    rAreaDoc = (HTMLDocument)rArea.getDocument();
    // -- this should be changed:
    DefaultCaret rAreaDocCaret = (DefaultCaret)rArea.getCaret();
    rAreaDocCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    // ---
    GridBagConstraints rAreaCon = new GridBagConstraints();
    rAreaCon.ipady = 400;
    //rAreaCon.weightx = 1;
    //rAreaCon.weighty = 1;
    rAreaCon.gridwidth = 2;
    rAreaCon.gridx = 0;
    rAreaCon.gridy = 0;
    rAreaCon.fill = GridBagConstraints.BOTH;
    rAreaCon.anchor = GridBagConstraints.NORTH;
    mainGUI.add(new JScrollPane(rArea), rAreaCon);


    /* Last bit */
    setContentPane(mainGUI);
    validate();

    
    updateCSS();
    chatPrint(_info);
    sock = new ClientSocket(this);
  }

  public void keyPressed(KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.VK_ENTER) {

      String msgString = wArea.getText();
      if (msgString.length() < 1) return;
      wArea.setText(null);
      if (msgString.substring(0, 1).equals("/")) {
        String[] msgList = msgString.split(" ");

        if (msgString.equals("/help")) {
          chatPrint("# Available commands:");
          chatPrint("/fsize <number> - set font size");
          chatPrint("/disconnect - Disconnect from chatserver");
          chatPrint("/reconnect - Reconnect to chatserver");
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

        else if (msgList[0].equals("/fsize")) {
          try {
          currFontSize = Integer.parseInt(msgList[1]);
          updateCSS();
          } catch(NumberFormatException e) {}
          catch (ArrayIndexOutOfBoundsException er) {}
        }
      }
      else if (sock != null) sock.send(msgString);
      else chatPrint(msgString);
    }
  }

  public void keyReleased(KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.VK_ENTER)
      wArea.setText(null);
  }

  public void chatPrint(String msgString) {
    
    // Reencoding the string to UTF-8 in case it's ISO-8859-1 or something
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
      Element len = rAreaDoc.getParagraphElement(rAreaDoc.getLength());
      if (msgString.substring(0, 5).equals("&lt;~"))
        rAreaDoc.insertBeforeEnd(len, timestamp() + "<b> " + msgString + "\n</b><br>");
      else
        rAreaDoc.insertBeforeEnd(len, timestamp() + " " + msgString + "\n<br>");
    } catch(BadLocationException blError) {
    } catch(IOException ioError) {
    } catch(StringIndexOutOfBoundsException e) {
    }
  }

  
  private JMenuItem returnSizePop(final int tSize) {
    final JMenuItem retPop = new JMenuItem(new AbstractAction(Integer.toString(tSize)) {
        public void actionPerformed(ActionEvent e) {
          currFontSize = tSize;
          updateCSS();
        }
      });
    return retPop;
  }
  
  private void updateCSS() {
    Font currFont = new Font(currFontName, Font.PLAIN, currFontSize);
    String CSS = "body { font-family: "+currFont.getFamily()+
      "; font-size: "+currFont.getSize()+"pt;}";
    rAreaDoc.getStyleSheet().addRule(CSS);
  }

  private String timestamp() {
    return "[" + dateFormat.format(new Date()) + "]";
  }

  public void keyTyped(KeyEvent event) {}
}
