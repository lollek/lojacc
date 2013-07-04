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

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import netscape.javascript.JSObject;
import netscape.javascript.JSException;

import org.iix3.lojacc.ClientSocket;

public class ClientGUI extends JApplet implements KeyListener {

  final String welcome_info = "lojacc v0.2.5 (2013-07-04) by Olle K";

  final private JTextArea wArea;
  final private HTMLDocument rAreaDoc;
  final private SimpleDateFormat dateFormat = new SimpleDateFormat("HH.mm");
  private JSObject jsWindow;
  private AudioClip audioClick;

  private boolean windowIsInFocus = false;
  private int currFontSize = 11;
  private String currFontName = "Calibri";
  ClientSocket sock;

  public ClientGUI() {
    
    /* GUI Window = MainGUI */
    final JPanel mainGUI = new JPanel(new GridBagLayout());
    mainGUI.setBorder(new EtchedBorder(Color.white, Color.gray));
    
    /* Writeable field = wArea */
    wArea = new JTextArea(2, 0);
    wArea.setLineWrap(true);
    wArea.addKeyListener(this);
    wArea.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) { 
          windowIsInFocus = true; 
          if (jsWindow != null)
            jsWindow.eval("setTitle(\"Lollian Chat\")");
        }
        public void focusLost(FocusEvent e) {
          windowIsInFocus = false;
        }
      });
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
    for (int i = 10; i <= 20; i++) {
      final int I = i;
      tSizePopup.add(new JMenuItem(new AbstractAction(Integer.toString(i)) {
          public void actionPerformed(ActionEvent e) {
            currFontSize = I;
            updateCSS();
          }
        }));
    }
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

    /* Load sound: */
    try {
      URL u = new URL("http://iix3.org/chat/.img/click.au");
      if (u == null)
        chatPrint("AudioURL is null");
      else
        audioClick = Applet.newAudioClip(u);
    } catch (MalformedURLException e) {
      chatPrint("ERROR: Failed to load audio");
    } catch (NullPointerException e) {
      chatPrint("ERROR: Failed to load audio (null pointer)");
    }

    /* Fontsize 13 is better for Win.. Maybe MAC too? 
       System.getProperty("os.name") is not "Linux", so this is a temp hack: */
    if (System.getProperty("os.name").length() != 5)
      currFontSize = 13;

    /* Last bit */
    setContentPane(mainGUI);
    validate();
    updateCSS();
    chatPrint(welcome_info);
    sock = new ClientSocket(this);
  }

  /* Load jsWindow so we can do some JS: 
     It seems that this cannot safely be in the creator..*/
  public void setupJSObject() {
    try {
      // this should be an Applet instead of JApplet?
      jsWindow = JSObject.getWindow(this);
      jsWindow.eval("setTitle(\"Lollian Chat\")");
    } catch (JSException e) {
      chatPrint("ERROR: Failed to load JS");
    } catch (NullPointerException e) {
      chatPrint("ERROR: Failed to load JS (null pointer) <-- this may be an issue with icedtea?");
    }
  }

  /* GUI Keypress: */
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
          chatPrint(welcome_info);
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

  /* GUI Keyup: */
  public void keyReleased(KeyEvent event) {
    /* Without this, ENTER creates an ugly newline: */
    if (event.getKeyCode() == KeyEvent.VK_ENTER)
      wArea.setText(null);
  }

  /* This replaces println, i.e. adds strings to GUI: */
  public void chatPrint(String msgString) {
    
    /* Reencoding the string to UTF-8 in case it's ISO-8859-1 or something */
    try {
      msgString = new String(msgString.getBytes(Charset.defaultCharset()), "UTF-8"); 
    } catch (UnsupportedEncodingException ueeError) {
      msgString = "Error encoding string"; 
    }

    /* Replace stuff in text: 
       Not sure if this C-style way of doing it works better than .replace()
       But since I'm replacing so much, it should be? */
    StringBuilder sb = new StringBuilder();
    char [] msgArr = msgString.toCharArray();
    for (int i = 0; i < msgArr.length; i++) {
      
      /* Escape some HTML: */
      if (msgArr[i] == '&') sb.append("&amp;");
      else if (msgArr[i] == '<') sb.append("&lt;");
      else if (msgArr[i] == '>') sb.append("&gt;");

      /* Escape some smileys: */
      /* : */
      else if (msgArr[i] == ':' && msgArr.length - i > 1) {
        if (msgArr[i+1] == '/' && (msgArr.length - i == 2 || 
                                   msgArr.length - i > 2 && msgArr[i+2] != '/')) { 
          sb.append(emoticonURL("down.gif", ":/")); i++; }
        else if (msgArr[i+1] == ')') { sb.append(emoticonURL("happy.gif", ":)")); i++; }
        else if (msgArr[i+1] == '(') { sb.append(emoticonURL("sad.gif", ":(")); i++; }
        else if (msgArr[i+1] == '@') { sb.append(emoticonURL("angry.gif", ":@")); i++; }
        else if (msgArr[i+1] == 'P') { sb.append(emoticonURL("tongue.gif", ":P")); i++; }
        else if (msgArr[i+1] == 'D') { sb.append(emoticonURL("bigsmile.gif", ":D")); i++; }
        else if (msgArr[i+1] == '\'' && msgArr.length - i > 2 && msgArr[i+2] == '(') {
          sb.append(emoticonURL("crying.gif", ":'(")); i += 2;
        } else sb.append(msgString.charAt(i));
      }

      /* ( )*/
      else if (msgArr[i] == '(' && msgArr.length - i > 2 && msgArr[i+2] == ')') {
        if (msgArr[i+1] == 'n' || msgArr[i+1] == 'N') {
          sb.append(emoticonURL("disagree.gif", "(n)")); i += 2; }
        else if (msgArr[i+1] == 'y' || msgArr[i+1] == 'Y') {
          sb.append(emoticonURL("agree.gif", "(y)")); i += 2; }
      }
      
      /* ; */
      else if (msgArr[i] == ';' && msgArr.length - i > 1 && msgArr[i+1] == ')') {
        sb.append(emoticonURL("wink.gif", ";)")); i++; 
      }
      
      /* Special A-Za-z */
      else if (msgArr[i] == 'L' && msgArr.length - i > 2 &&
               msgArr[i+1] == 'O' && msgArr[i+2] == 'L') {
        sb.append(emoticonURL("laugh.gif", "LOL")); i += 2;
      }
      
      else sb.append(msgString.charAt(i));
    }
    msgString = sb.toString();
    
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

    /* If window is not in focus; 
       play a click and update Title: */
    if (!windowIsInFocus && audioClick != null)
      audioClick.play();
    if (jsWindow != null)
      jsWindow.eval("setTitle(\"*New Message*\")");
  }

  /* This refreshes the GUI CSS/style after changes have been made: */
  private void updateCSS() {
    Font currFont = new Font(currFontName, Font.PLAIN, currFontSize);
    String CSS = "body { font-family: "+currFont.getFamily()+
      "; font-size: "+currFont.getSize()+"pt;}";
    rAreaDoc.getStyleSheet().addRule(CSS);
  }

  /* This returns a timestamp: */
  public String timestamp() {
    return "[" + dateFormat.format(new Date()) + "]";
  }

  /* This creates an URL for emoticons: */
  public static String emoticonURL(String image, String alttext) {
    return "<img src=\"http://iix3.org/chat/.img/" + image + "\" alt=\"" + alttext + "\">";
  }

  /* Unused from keylistener: */
  public void keyTyped(KeyEvent event) {}
}
