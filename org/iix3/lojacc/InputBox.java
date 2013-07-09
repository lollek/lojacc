package org.iix3.lojacc;

public class InputBox 
  extends javax.swing.JTextArea
  implements java.awt.event.KeyListener {
  
  private final org.iix3.lojacc.ClientGUI master;

  public InputBox(org.iix3.lojacc.ClientGUI master) {
    this.setLineWrap(true);
    this.addKeyListener(this);
    this.master = master;
  }
  
  public void keyPressed(java.awt.event.KeyEvent event) {
    if (event.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
      String writtenText = this.getText();

      if (writtenText.length() < 1) return;
      this.setText(null);

      if (writtenText.substring(0, 1).equals("/")) {
        if (writtenText.equals("/help")) {
          master.printHelp();
          
        } else if (writtenText.equals("/disconnect") ||
                   writtenText.equals("//d")) {
          master.disconnectSocket();
          
        } else if (writtenText.equals("/reconnect") ||
                   writtenText.equals("//r")) {
          master.reconnectSocket();
        }

      } else {
        master.sendMessage(writtenText);
      }
    }
  }

  public void keyReleased(java.awt.event.KeyEvent event) {
    if (event.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
      this.setText(null);
  }

  public void keyTyped(java.awt.event.KeyEvent event) {}
}