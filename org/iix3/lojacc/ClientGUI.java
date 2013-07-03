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

  final JTextArea wArea;
  final HTMLDocument rAreaDoc;
  final SimpleDateFormat dateFormat = new SimpleDateFormat("HH.mm");

  int currFontSize = 11;
  String currFontName = "Calibri";
  ClientSocket sock;

  public ClientGUI() {
    
    /* Init: */
    final String welcome_info = "lojacc v0.2.1 (2013-07-03) by Olle K";
  
    /* GUI Window = MainGUI */
    final JPanel mainGUI = new JPanel(new GridBagLayout());
    mainGUI.setBorder(new EtchedBorder(Color.white, Color.gray));
    
    /* Writeable field = wArea */
    wArea = new JTextArea(2, 0);
    wArea.setLineWrap(true);
    wArea.addKeyListener(this);
    final GridBagConstraints wAreaCon = new GridBagConstraints();
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
    final JButton tSizeButton = new JButton("Text Size");
    final GridBagConstraints bCon = new GridBagConstraints();
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
    final JButton button = new JButton("Color");
    bCon.gridx = 1;
    bCon.insets = new Insets(0, 0, 0, 850);
    mainGUI.add(button, bCon);

    /* Chat Window = rArea */
    final JEditorPane rArea = new JEditorPane();
    rArea.setEditable(false);
    rArea.setContentType("text/HTML");
    rArea.setEditorKit(new HTMLEditorKit());
    rAreaDoc = (HTMLDocument)rArea.getDocument();
    // -- this should be changed?:
    final DefaultCaret rAreaDocCaret = (DefaultCaret)rArea.getCaret();
    rAreaDocCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    // ---
    final GridBagConstraints rAreaCon = new GridBagConstraints();
    rAreaCon.ipady = 400;
    rAreaCon.gridwidth = 2;
    rAreaCon.gridx = 0;
    rAreaCon.gridy = 0;
    rAreaCon.fill = GridBagConstraints.BOTH;
    rAreaCon.anchor = GridBagConstraints.NORTH;
    mainGUI.add(new JScrollPane(rArea), rAreaCon);

    /* Fontsize 13 is better for Win.. Maybe MAC too? */
    if (System.getProperty("os.name").length() != 5)
      currFontSize = 13;

    /* Last bit */
    setContentPane(mainGUI);
    validate();
    updateCSS();
    chatPrint(welcome_info);
    sock = new ClientSocket(this);
  }

  public void keyPressed(KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.VK_ENTER) {

      /* Fetch Text from wArea and clear it: */
      String msgString = wArea.getText();
      if (msgString.length() < 1) 
        return;
      wArea.setText(null);

      /* Text beginning with / is treated as a client command: */
      if (msgString.substring(0, 1).equals("/")) {

        /* /help */
        if (msgString.equals("/help")) {
          chatPrint("# Available commands:");
          chatPrint("/disconnect | //d - Disconnect from chatserver");
          chatPrint("/reconnect  | //r - Reconnect to chatserver");
        }

        /* /disconnect || //d */
        else if (msgString.equals("/disconnect")||
                 msgString.equals("//d")) {
          if (sock == null)  
            chatPrint("You are not connected");
          else 
            sock.kill();
        }
        
        /* /reconnect || //r */
        else if (msgString.equals("/reconnect")||
                 msgString.equals("//r")) {
          if (sock != null) 
            sock.kill();
          sock = new ClientSocket(this);
        }

      }
      
      /* Otherwise, treat it as a message and send: */
      else if (sock != null) 
        sock.send(msgString);
      
      /* If there's no socket; print warning: */
      else 
        chatPrint("You are not connected! Try typing /reconnect");
    }
  }

  public void keyReleased(KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.VK_ENTER)
      wArea.setText(null);
  }

  public void chatPrint(String msgString) {
    
    /* Reencoding the string to UTF-8 in case it's ISO-8859-1 or something */
    try {
      msgString = new String(msgString.getBytes(Charset.defaultCharset()), "UTF-8"); 
    } catch (UnsupportedEncodingException ueeError) {
      msgString = "Error encoding string"; 
    }

    /* Escape some HTML: */
    msgString = msgString.replace("&", "&amp;");
    msgString = msgString.replace("<", "&lt;");
    msgString = msgString.replace(">", "&gt;");


    /* SMILEYS! */
    msgString = msgString.replace(":/", 
                "<img src=\"http://iix3.org/chat/.img/down.gif\" alt=\":/\">");
    msgString = msgString.replace(":)", 
                "<img src=\"http://iix3.org/chat/.img/happy.gif\" alt=\":)\">");
    msgString = msgString.replace(":(", 
                "<img src=\"http://iix3.org/chat/.img/sad.gif\" alt=\":(\">");
    msgString = msgString.replace(":@", 
                "<img src=\"http://iix3.org/chat/.img/angry.gif\" alt=\":@\">");
    msgString = msgString.replace(":P", 
                "<img src=\"http://iix3.org/chat/.img/tongue.gif\" alt=\":P\">");
    msgString = msgString.replace(":D", 
                "<img src=\"http://iix3.org/chat/.img/bigsmile.gif\" alt=\":D\">");
    msgString = msgString.replace(";)", 
                "<img src=\"http://iix3.org/chat/.img/wink.gif\" alt=\";)\">");
    msgString = msgString.replace(":lol:",
                "<img src=\"http://iix3.org/chat/.img/laugh.gif\" alt=\":lol:\">");

    /* Write the text (in bold if it begins with <~): */
    try {
      Element len = rAreaDoc.getParagraphElement(rAreaDoc.getLength());
      if (msgString.substring(0, 5).equals("&lt;~"))
        rAreaDoc.insertBeforeEnd(len, timestamp() + "<b> " + msgString + "\n</b><br>");
      else
        rAreaDoc.insertBeforeEnd(len, timestamp() + " " + msgString + "\n<br>");
    } 
    catch(BadLocationException blError) {} 
    catch(IOException ioError) {} 
    catch(StringIndexOutOfBoundsException e) {}
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
