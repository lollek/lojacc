package org.iix3.lojacc;

public class OutputBox
  extends javax.swing.JEditorPane {

  private final org.iix3.lojacc.ClientGUI master;
  private final javax.swing.text.html.HTMLDocument HTMLDoc;
  
  public OutputBox(org.iix3.lojacc.ClientGUI master) {
    this.master = master;
    this.setEditable(false);
    this.setContentType("text/HTML");
    this.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
    this.HTMLDoc = (javax.swing.text.html.HTMLDocument)this.getDocument();
  }

  public void println(String message) {

    /* Reencode:
       Reencoding the string to UTF-8 in case it's ISO-8859-1 or something */
    try {
      message = new String(
        message.getBytes(
          java.nio.charset.Charset.defaultCharset()), "UTF-8"); 
    } catch (java.io.UnsupportedEncodingException e) {
      message = "Error encoding string"; 
    }
    
    /* Replace stuff in text: 
       Not sure if this C-style way of doing it works better than .replace()
       But since I'm replacing so much, it should be? */
    boolean setBold = false;
    StringBuilder sb = new StringBuilder();
    char [] msgArr = message.toCharArray();
    
    /* Add timestamp: */
    sb.append("[");
    sb.append(new java.text.SimpleDateFormat("HH.mm").format(new java.util.Date()));
    sb.append("] ");
    
    /* Write text in bold if it begins with <~: */
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
        } else sb.append(message.charAt(i));
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
      
      else sb.append(message.charAt(i));
    }
    if (setBold)
      sb.append("</b>");
    sb.append("\n<br>");

    message = sb.toString();

    
    /* Print: */
    try {
      final javax.swing.text.Element insertPosition;
      insertPosition = HTMLDoc.getParagraphElement(HTMLDoc.getLength());
      HTMLDoc.insertBeforeEnd(insertPosition, message);
      this.setCaretPosition(HTMLDoc.getLength());

    } 
    catch(javax.swing.text.BadLocationException e) {} 
    catch(java.io.IOException e) {} 
    catch(StringIndexOutOfBoundsException e) {}

    master.signalUpdate();
  }

  public void setStyle(String fontName, int fontSize) {
    final java.awt.Font newFont;
    final String newCSS;
    newFont = new java.awt.Font(fontName, java.awt.Font.PLAIN, fontSize);
    newCSS = "body { font-family:"+newFont.getFamily()+";font-size:"+newFont.getSize()+"pt;}";
    HTMLDoc.getStyleSheet().addRule(newCSS);
  }

  /* This creates an URL for emoticons: */
  public static String emoticonURL(String image, String alttext) {
    return "<img src=\"http://iix3.org/chat/.img/" + image + "\" alt=\"" + alttext + "\">";
  }
}