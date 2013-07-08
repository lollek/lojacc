/* lojacc ClientGUI
 * GUI Handling for Chat Applet
 * Requires ClientSocket from the same package
 */
//http://java-sl.com/tip_autoreplace_smiles.html

package org.iix3.lojacc;

public class ClientGUI 
  extends javax.swing.JApplet 
  implements java.awt.event.KeyListener {

  final String INFO = "lojacc v0.2.7 (2013-07-08) by Olle K";

  private final javax.swing.JEditorPane readArea;
  private final javax.swing.JTextArea writeArea;
  private netscape.javascript.JSObject jsWindow;
  private java.applet.AudioClip audioClick;

  private boolean windowIsInFocus = false;
  private int currFontSize = 11;
  private String currFontName = "Calibri";
  protected org.iix3.lojacc.ClientSocket sock;

  public ClientGUI() {
    
    final java.awt.GridBagConstraints buttonConstraint;
    final java.awt.GridBagConstraints writeAreaConstraint;
    final java.awt.GridBagConstraints readAreaConstraint;
    final java.net.URL audioClickURL;
    final javax.swing.JButton auxButton;
    final javax.swing.JButton textSizeButton;
    final javax.swing.JPanel mainGUI;
    final javax.swing.JPopupMenu textSizeButtonPopup;

    /* GUI Window = MainGUI */
    mainGUI = new javax.swing.JPanel(new java.awt.GridBagLayout());
    mainGUI.setBorder(new javax.swing.border.EtchedBorder(java.awt.Color.white, java.awt.Color.gray));
    
    /* Writeable field = writeArea */
    writeArea = new javax.swing.JTextArea(2, 0);
    writeArea.setLineWrap(true);
    writeArea.addKeyListener(this);
    writeArea.addFocusListener(new java.awt.event.FocusListener() {
        public void focusGained(java.awt.event.FocusEvent e) { 
          windowIsInFocus = true; 
          if (jsWindow != null)
            jsWindow.eval("setTitle(\"Lollian Chat\")");
        }
        public void focusLost(java.awt.event.FocusEvent e) {
          windowIsInFocus = false;
        }
      });
    writeAreaConstraint = new java.awt.GridBagConstraints();
    writeAreaConstraint.ipady = 25;
    writeAreaConstraint.weightx = 1;
    writeAreaConstraint.weighty = 1;
    writeAreaConstraint.gridwidth = 2;
    writeAreaConstraint.gridx = 0;
    writeAreaConstraint.gridy = 2;
    writeAreaConstraint.fill = java.awt.GridBagConstraints.HORIZONTAL;
    writeAreaConstraint.anchor = java.awt.GridBagConstraints.SOUTH;
    mainGUI.add(new javax.swing.JScrollPane(writeArea), writeAreaConstraint);

    /* Text Size Button */
    textSizeButton = new javax.swing.JButton("Text Size");
    buttonConstraint = new java.awt.GridBagConstraints();
    buttonConstraint.fill = java.awt.GridBagConstraints.HORIZONTAL;
    buttonConstraint.gridx = 0;
    buttonConstraint.gridy = 1;
    textSizeButtonPopup = new javax.swing.JPopupMenu();
    for (int i = 10; i <= 20; i++) {
      final int I = i;
      textSizeButtonPopup.add(new javax.swing.JMenuItem(new javax.swing.AbstractAction(Integer.toString(i)) {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            currFontSize = I;
            updateCSS();
          }
        }));
    }
    textSizeButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mousePressed(java.awt.event.MouseEvent e) {
          textSizeButtonPopup.show(e.getComponent(), e.getX(), e.getY());
        }
      });
    mainGUI.add(textSizeButton, buttonConstraint);

    /* Aux Button*/
    auxButton = new javax.swing.JButton("...");
    buttonConstraint.gridx = 1;
    buttonConstraint.insets = new java.awt.Insets(0, 0, 0, 850);
    mainGUI.add(auxButton, buttonConstraint);

    /* Chat Window = readArea(Pane) */
    readArea = new javax.swing.JEditorPane();
    readArea.setEditable(false);
    readArea.setContentType("text/HTML");
    readArea.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
    readAreaConstraint = new java.awt.GridBagConstraints();
    readAreaConstraint.ipady = 400;
    readAreaConstraint.gridwidth = 2;
    readAreaConstraint.gridx = 0;
    readAreaConstraint.gridy = 0;
    readAreaConstraint.fill = java.awt.GridBagConstraints.BOTH;
    readAreaConstraint.anchor = java.awt.GridBagConstraints.NORTH;
    mainGUI.add(new javax.swing.JScrollPane(readArea), readAreaConstraint);

    /* Load sound: */
    try {
      audioClickURL = new java.net.URL("http://iix3.org/chat/.img/click.au");
      audioClick = java.applet.Applet.newAudioClip(audioClickURL);
    } catch (java.net.MalformedURLException e) {
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
    chatPrint(INFO);
    sock = new org.iix3.lojacc.ClientSocket(this);
  }

  /* Load jsWindow so we can do some JS: 
     It seems that this cannot safely be in the creator..*/
  public void setupJSObject() {
    try {
      jsWindow = netscape.javascript.JSObject.getWindow(this);
      jsWindow.eval("setTitle(\"Lollian Chat\")");
    } catch (netscape.javascript.JSException e) {
      chatPrint("ERROR: Failed to load JS");
    } catch (NullPointerException e) {
      chatPrint("ERROR: Failed to load JS (null pointer) <-- this may be an issue with icedtea?");
    }
  }

  /* GUI Keypress: */
  public void keyPressed(java.awt.event.KeyEvent event) {
    if (event.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {

      /* Fetch Text from writeArea and clear it: */
      String msgString = writeArea.getText();
      if (msgString.length() < 1) 
        return;
      writeArea.setText(null);

      /* Text beginning with / is treated as a client command: */
      if (msgString.substring(0, 1).equals("/")) {

        /* /help */
        if (msgString.equals("/help")) {
          chatPrint(INFO);
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
          sock = new org.iix3.lojacc.ClientSocket(this);
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
  public void keyReleased(java.awt.event.KeyEvent event) {
    /* Without this, ENTER creates an ugly newline: */
    if (event.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
      writeArea.setText(null);
  }

  /* This replaces println, i.e. adds strings to GUI: */
  public void chatPrint(String msgString) {
    
    /* Reencoding the string to UTF-8 in case it's ISO-8859-1 or something */
    try {
      msgString = new String(msgString.getBytes(java.nio.charset.Charset.defaultCharset()), "UTF-8"); 
    } catch (java.io.UnsupportedEncodingException ueeError) {
      msgString = "Error encoding string"; 
    }

    /* Replace stuff in text: 
       Not sure if this C-style way of doing it works better than .replace()
       But since I'm replacing so much, it should be? */
    boolean setBold = false;
    StringBuilder sb = new StringBuilder();
    char [] msgArr = msgString.toCharArray();

    /* Add timestamp 
       Write the text (in bold if it begins with <~): */
    sb.append(timestamp());
    if (msgArr.length >= 2 && msgArr[0] == '<' && msgArr[1] == '~') {
      setBold = true;
      sb.append("<b>");
    }

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
    if (setBold)
      sb.append("</b>");
    sb.append("\n<br>");

    msgString = sb.toString();
    
    try {
      final javax.swing.text.html.HTMLDocument readAreaDoc;
      final javax.swing.text.Element len;

      readAreaDoc = (javax.swing.text.html.HTMLDocument)readArea.getDocument();
      len = readAreaDoc.getParagraphElement(readAreaDoc.getLength());

      readAreaDoc.insertBeforeEnd(len, msgString);
      readArea.setCaretPosition(readAreaDoc.getLength());
    } 
    catch(javax.swing.text.BadLocationException e) {} 
    catch(java.io.IOException e) {} 
    catch(StringIndexOutOfBoundsException e) {}

    /* If window is not in focus; 
       play a click and update Title: */
    if (!windowIsInFocus) { 
      if (audioClick != null)
        audioClick.play();
      if (jsWindow != null)
        jsWindow.eval("setTitle(\"*New Message*\")");
    }
  }

  /* This refreshes the GUI CSS/style after changes have been made: */
  private void updateCSS() {
    
    final javax.swing.text.html.HTMLDocument readAreaDoc;
    final java.awt.Font newFont;
    final String newCSS;

    readAreaDoc = (javax.swing.text.html.HTMLDocument)readArea.getDocument();

    newFont = new java.awt.Font(currFontName, java.awt.Font.PLAIN, currFontSize);
    newCSS = "body { font-family:"+newFont.getFamily()+";font-size:"+newFont.getSize()+"pt;}";
    readAreaDoc.getStyleSheet().addRule(newCSS);
  }

  /* This returns a timestamp: */
  public static String timestamp() {
    return "[" + new java.text.SimpleDateFormat("HH.mm").format(new java.util.Date()) + "] ";
  }

  /* This creates an URL for emoticons: */
  public static String emoticonURL(String image, String alttext) {
    return "<img src=\"http://iix3.org/chat/.img/" + image + "\" alt=\"" + alttext + "\">";
  }

  /* Unused from keylistener: */
  public void keyTyped(java.awt.event.KeyEvent event) {}
}
