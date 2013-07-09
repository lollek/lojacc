package org.iix3.lojacc;

public class ClientGUI 
  extends javax.swing.JApplet 
  implements java.awt.event.FocusListener {

  final String INFO = "lojacc v0.2.8 (2013-07-08) by Olle K";

  private netscape.javascript.JSObject jsWindow;
  private org.iix3.lojacc.ClientSocket sock;
  private final org.iix3.lojacc.OutputBox readArea;
  private final org.iix3.lojacc.InputBox writeArea;

  private java.applet.AudioClip audioClick;

  private boolean windowIsInFocus = false;

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
    mainGUI.setBorder(new javax.swing.border.EtchedBorder(
                        java.awt.Color.white, 
                        java.awt.Color.gray));
    
    /* Writeable field = writeArea */
    writeArea = new org.iix3.lojacc.InputBox(this);
    writeArea.addFocusListener(this);
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
      final int FONT_SIZE = i;
      textSizeButtonPopup.add(
        new javax.swing.JMenuItem(
          new javax.swing.AbstractAction(Integer.toString(FONT_SIZE)) {
            public void actionPerformed(java.awt.event.ActionEvent e) {
              readArea.setStyle("Calibri", FONT_SIZE);
            }
          }));
    }
    textSizeButton.addMouseListener(
      new java.awt.event.MouseAdapter() {
        public void mousePressed(java.awt.event.MouseEvent e) {
          textSizeButtonPopup.show(e.getComponent(), e.getX(), e.getY());
        }
      });
    mainGUI.add(textSizeButton, buttonConstraint);

    /* Aux Button - this will be used later */
    auxButton = new javax.swing.JButton("...");
    buttonConstraint.gridx = 1;
    buttonConstraint.insets = new java.awt.Insets(0, 0, 0, 850);
    mainGUI.add(auxButton, buttonConstraint);

    /* Chat Window = readArea(Pane) */
    readArea = new org.iix3.lojacc.OutputBox(this);
    readArea.addFocusListener(this);
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

    /* Fontsize 11 for Linux, 13 for others: */
    if (System.getProperty("os.name").length() == 5)
      readArea.setStyle("Calibri", 11);
    else
      readArea.setStyle("Calibri", 13);

    /* Last bit */
    setContentPane(mainGUI);
    validate();
    printHelp();
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

  public void chatPrint(String message) {
    readArea.println(message);
  }
  
  public void printHelp() {
    chatPrint(INFO);
    chatPrint("# Available commands:");
    chatPrint("/disconnect | //d - Disconnect from chatserver");
    chatPrint("/reconnect  | //r - Reconnect to chatserver");
  }
  
  public void disconnectSocket() {
    if (sock == null)
      chatPrint("You are not connected");
    else {
      sock.kill();
      sock = null;
    }
  }

  public void reconnectSocket() {
    if (sock != null)
      sock.kill();
    sock = new org.iix3.lojacc.ClientSocket(this);
  }

  public void sendMessage(String message) {
    if (sock != null)
      sock.send(message);
    else
      chatPrint("Unable to send, try typing /reconnect");
  }

  public void signalUpdate() {
    if (!windowIsInFocus) {
      if (audioClick != null)
        audioClick.play();
      if (jsWindow != null)
        jsWindow.eval("setTitle(\"*New Message*\")");
    }
  }
  public void focusGained(java.awt.event.FocusEvent e) { 
    windowIsInFocus = true; 
    if (jsWindow != null)
      jsWindow.eval("setTitle(\"Lollian Chat\")");
  }

  public void focusLost(java.awt.event.FocusEvent e) {
    windowIsInFocus = false;
  }
}