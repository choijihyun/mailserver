package networkProject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import javax.mail.internet.MimeUtility;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import sun.misc.BASE64Decoder;

public class PopClient {

   public String popServer;
   public int popPort;
   public String popAccount;
   public String popPassword;

   public void read() throws Exception {

      Socket socket = new Socket(popServer, popPort); // make socket with server's host name and port number.
      SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault(); // make SSLSocketFactory
                                                                        // for ssl
                                                                        // certification.

      SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket,
            socket.getInetAddress().getHostAddress(), socket.getPort(), true); // add ssl property to socket.

      BufferedReader ipStream = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

      // sslSocket go to server and get response, and store that in ipStream.
      OutputStream opStream = sslSocket.getOutputStream(); // attach OutputStream at sslSocket to send data at server.

      String resp = ipStream.readLine(); // read data that in ipStream. ipStream has server's response.
      System.out.println(">" + resp); // print server's response.

      if (resp.charAt(0) == '+') { // if response is "+OK...." that means server is exist.
         System.out.println("Pop3 Server OK");
      } else { // if response is "-ERR...." that means server is not exist.
         socket.close(); // if an error has occurred, close socket and finish.
         System.out.println("POP3 Server ERR");
      }
      String command = "USER " + popAccount + "\r\n"; // make command "USER : declare username".
      System.out.println(command);

      opStream.write(command.getBytes()); // client sends command to server.

      resp = ipStream.readLine(); // read data that in ipStream. ipStream has server's response.

      System.out.println(">" + resp); // print server's response.

      if (resp.charAt(0) == '+') { // if response is "+OK" that means user is exist
         System.out.println("User name OK");
      } else { // if response is "-ERR" that means user is not exist
         socket.close(); // if an error has occurred, close socket and finish.
         System.out.println("User name ERR");
      }
      command = "PASS " + popPassword + "\r\n"; // make command "PASS: password".

      System.out.println(command);

      opStream.write(command.getBytes()); // client sends command to server.

      resp = ipStream.readLine(); // read data that in ipStream. ipStream has server's response.

      System.out.println(">" + resp);// print server's response.

      if (resp.charAt(0) == '+') { // if response is "+OK" that means password is correct
         System.out.println("User Password OK");
      } else { // if response is "-ERR" that means password is wrong
         sslSocket.close(); // if an error has occurred, close socket and finish.
         System.out.println("User Password ERR");
      }
      command = "LIST\r\n"; // make command. it means get mail message's list

      opStream.write(command.getBytes()); // client sends command to server.

      resp = ipStream.readLine(); // read data that in ipStream. ipStream has server's response.

      System.out.println(resp); // print server's response.

      int n = 0; // size of all list

      if (resp.charAt(0) == '+') { // if response is "+OK...." that means success to get message's list.
         while (true) {
            n++;
            resp = ipStream.readLine(); // read data that in ipStream. ipStream has server's response.
            System.out.println(resp); // resp contains a list number and a list size.

            if (resp.equals(".")) { // if list is finished.
               n--;
               System.out.println(">>>>>>>>>>>> end >>>>>>>>>>>"); // print(notice) List is finished to user.
               System.out.println("Total : " + n); // print(notice) size of List to user.
               break;
            }
         }
      } else {// if response is "-ERR...." that means password is correct.
         socket.close(); // if an error has occurred, close socket and finish.
         System.out.println("list ERR");
      }
      Scanner num = new Scanner(System.in);

      int retrORdele;
      int type; // number of list

      while (true) {
         System.out.println("If you want quit, you should type '0'."); // inform escape condition to user.
         System.out.print("Please enter the list number : "); // inform escape condition to user.

         type = num.nextInt(); // user select message's list number.

         if (type == 0) { // if user want to quit,
            command = "QUIT\r\n";

            System.out.println(command); // print user's command
            opStream.write(command.getBytes()); // client sends "QUIT" command to server.

            resp = ipStream.readLine(); // read data that in ipStream. ipStream has server's response.

            System.out.println(">" + resp); // print server's response.

            if (resp.charAt(0) == '+') { // if response is "+OK...." that means server signing off.
               System.out.println("Quit OK");
            } else { // if response is "-ERR...." that means server fail signing off.
               System.out.println("Quit ERR");
            }
            socket.close(); // if client want to quit, close socket and finish.
         }
         // if client enter invalid number.

         if (type <= 0 || type > n) {
            System.out.println(" list " + type + " is not exist");
            break;
         }

         command = "RETR " + type + "\r\n"; // make command "RETP : retrieve message by number".

         opStream.write(command.getBytes()); // client sends command to server.

         resp = ipStream.readLine(); // read data that in ipStream. ipStream has server's response.

         System.out.println(resp); // print server's response.

         /// ��� resp �޾ƿ���
         ArrayList<String> respBody = new ArrayList<>();
         while (true) { // ���� ����Ʈ �޾ƿ���
            resp = ipStream.readLine();
            if (resp.equals(".")) {
               respBody.add(resp);
               System.out.println(">>>>>>>>>>>>>>>>>>>end>>>>>>>>>>>>>>>>>>>");
               break;
            }
            // Add read line to the list of lines
            respBody.add(resp);
            System.out.println(resp);
         }

         // parsing sender,subject and content-Type, charset,content-transfer-encoding
         // for decoding

         String sender, subject, contentType, charset, contentTransferEncoding, attachmentName, resp1, boundary1, boundary2;
         
         int i, j, k, f; // variable for method "indexOf()"

         int index = 0;
         int flag = 0; 
         int multi = 0;
         int quoted = 0;
         int bit8 = 0;
         int bit7 = 0;
         int binary = 0;
         int base64 = 0;

         sender = subject = contentType = charset = contentTransferEncoding = attachmentName = boundary1 = boundary2 = ""; // initialize

         BufferedWriter out = //new BufferedWriter(new FileWriter(type + " .html"));
          new BufferedWriter(new OutputStreamWriter(new FileOutputStream(type + " .html"), "utf-8"));
         while (true) {
            resp = respBody.get(index++);

            System.out.println(resp);
         /*   if (resp.toLowerCase().contains("content-type:")) { // parsing(case "content-type:" and "charset" is in
                                                   // same line)
               i = resp.indexOf(":"); // find the first index for ":" , it is in "content-type:".
               j = resp.indexOf(";"); // find the first index for ";" , it is in "content-type: ....;".

               if (resp.contains("multipart"))
                  multi = 1;

               contentType = resp.substring(i + 1, j);

            } // �Ϸ�*/

            if (resp.contains("Subject:")) { // parsing "subject" ,, from is under subject
               while (true) {
                  i = resp.indexOf("="); // =�� �������� �Ľ�
                  j = resp.lastIndexOf("=");

                  if (i != j && i != -1 && j != -1 && resp.substring(i + 1, i + 2).equals("?")
                        && resp.substring(j - 1, j).equals("?") && !(resp.toLowerCase().contains("from:"))) {// ���� ���Ŀ� ������ decoding from ����

                     subject += MimeUtility.decodeText(resp.substring(i, j + 1)); //subject ���ڵ�

                     resp = respBody.get(index++);// ���پ� �����ͼ� Ȯ��

                  } else break;
               }
            }
            if (resp.toLowerCase().contains("from:")) { // parsing "from" ,, from is under subject

               i = resp.indexOf("<"); // find the index for ">" , it is in "<someone@somemail.com>".
               j = resp.indexOf(">"); // find the index for ">" , it is in "<someone@somemail.com>".

               if (i != -1) { // if "<" is not exist
                  sender = resp.substring(i, j + 1); // parsing sender's mail address.
               }

            }

            if (resp.toLowerCase().contains("content-transfer-encoding:")) { // parsing "content-transfer-encoding:"
               contentTransferEncoding = resp.substring(resp.indexOf(":") + 1); 
               
               //���� �ι� ��� ����
               if(contentTransferEncoding.equals(" quoted-printable")) quoted++;
               else if(contentTransferEncoding.equals(" 8bit")) bit8++;
               else if(contentTransferEncoding.equals(" 7bit"))  bit7++;
               else if(contentTransferEncoding.equals(" binary")) binary++;
               else if(contentTransferEncoding.equals(" base64")) base64++;
               
            }

            if (resp.toLowerCase().contains("content-type:") && resp.toLowerCase().contains("charset=")&&multi!=1) { // parsing(case
               i = resp.indexOf(":"); // find the first index for ":" , it is in "content-type:".
               j = resp.indexOf(";"); // find the first index for ";" , it is in "content-type: ....;".
               k = resp.indexOf("="); // find the first index for "=" , it is in "charset=".
               f = resp.indexOf(";",k); //charset�� �Ľ� ���� ; index ����
               
               contentType = resp.substring(i + 1, j);//contentType �Ľ�
               if(f!=-1) charset = resp.substring(k + 1,f); //; �� �ִ� ������ �Ľ�
               else charset = resp.substring(k+1);// ;�� ������ ������ �Ľ�

            } 
            else if (resp.toLowerCase().contains("content-type:")) { // parsing(case "content-type:" and "charset"
                                                         // is in different line)
               resp1 = respBody.get(index++);

               //multipart�� �ƴѰ�� charset �� ����
               if (resp1.toLowerCase().contains("charset=")&&multi!=1) {

                  k = resp1.indexOf("="); // find the first index for "=" , it is in "charset=".
                  f = resp1.indexOf(";",k);
                  
                  if(f!=-1) charset = resp1.substring(k + 1,f);
                  else charset = resp1.substring(k+1);
                  
               }
               else index--;
               
               i = resp.indexOf(":"); // find the first index for ":" , it is in "content-type:".
               j = resp.indexOf(";"); // find the first index for ";" , it is in "content-type: ....;".
               
               //multipart flag ǥ��
               if (resp.contains("multipart"))
                  multi = 1;

               if(i!=j&&i!=-1&&j!=-1)   contentType = resp.substring(i + 1, j);
               
            }
            
            //boundary �Ľ�
            if (resp.contains("boundary=") && (boundary1.equals("") || boundary2.equals("")) ) {
               i = resp.indexOf("="); //=�� �������� boundary1�� boundary2 �Ľ�
               
               if( boundary1.equals("") ) {
                  boundary1 = resp.substring(i+1);   
               }
               else if( boundary2.equals("") ) {
                  boundary2 = resp.substring(i+1);   
               }
            }

            // parsing(case "content-disposition:" and "filename" is in same line)

               if (resp.toLowerCase().contains("content-disposition:") && resp.toLowerCase().contains("filename=")) {
                  k = resp.indexOf("="); // find the first index for "=" , it is in "filename=".
                  i = resp.indexOf("=",k+1); // =�� �������� �Ľ�
                  j = resp.lastIndexOf("=");

                  if (i != j && i != -1 && j != -1 && resp.substring(i + 1, i + 2).equals("?")
                        && resp.substring(j - 1, j).equals("?") ) {// ���� ���Ŀ� ������ decoding from ����

                     attachmentName += MimeUtility.decodeText(resp.substring(i, j + 1)); //file name ���ڵ�
                  } 
               }// attached of inlined file.

               else if (resp.toLowerCase().contains("content-disposition:")) { // parsing(case "content-disposition:"
                                                               // and "filename" is in different line)
                  resp1 = respBody.get(index++); // (resp = "filename")

                  if (resp1.toLowerCase().contains("filename=")) {
                     k = resp1.indexOf("="); // find the first index for "=" , it is in "filename=".
                      i = resp1.indexOf("=",k+1); // =�� �������� �Ľ�
                      j = resp1.lastIndexOf("=");

                      if (i != j && i != -1 && j != -1 && resp1.substring(i + 1, i + 2).equals("?")
                            && resp1.substring(j - 1, j).equals("?") ) {
                         attachmentName += MimeUtility.decodeText(resp1.substring(i, j + 1)); 
                         
                      } 
                  }
                  else index--; //���� ���� ������ �б����� ó��
               }//else if

            
            if (resp.equals(".")) { // response is finished.
               
               //boundary �� ����ǥ�� �ִ� ��쿡 �Ľ� 
               i = boundary1.indexOf('"');
               j = boundary1.lastIndexOf('"');
               
               if (i != j)
                  boundary1 = boundary1.substring(i + 1, j);
               
               i = boundary2.indexOf('"');
               j = boundary2.lastIndexOf('"');
               
               if (i != j)
                  boundary2 = boundary2.substring(i + 1, j);
               
               break;
            }
         } // while (read resp)

         //quoted-printable ���ڵ��� ���� inputStream
         String initialString = resp;
         InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());

         String str="";
         index = 0;
         BASE64Decoder decoder = new BASE64Decoder(); //base64 ���ڴ�
         
         byte[] decoded;
         byte[] data = new byte[512]; //���� ������

         ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); //base64 �� ���� outputstream

         flag = 0;
         int nRead;// nRead�� targetstream�� the byte value�� int�� ��ȯ
         if(multi==1) contentTransferEncoding="";//multipart �� ��쿡�� ó������ ���ڵ�Ÿ���� �ʱ�ȭ
         while (true) {
            resp = respBody.get(index++);

            // subject is not encoding. (subject dosen't contain '=' & '?')
            if (subject.equals("") && resp.toLowerCase().contains("subject:")) {
               i = resp.indexOf(":");
               subject += MimeUtility.decodeText(resp.substring(i + 1));
            }
            //multipart ���� Ȯ���ϰ� boundary1�� boundary2�� �������� ������.
            if (multi == 1 && ( ( resp.contains(boundary1) && !boundary1.equals(""))  || ( resp.contains(boundary2)) &&  !boundary2.equals(""))) {
            
               if (base64==1) outputStream.reset(); //���� �ߺ� ����   
               
               //boundary�� �������� charset�� content-transfer-encoding �Ľ�
               while (!resp.equals("")) {
               
                  if(resp.equals(".")) {
                     break;
                  }//���� ��
                  
                  resp = respBody.get(index++);
                  
                  
                  //charset �Ľ�
                  if (resp.toLowerCase().contains("charset=")) {
                     k = resp.indexOf("="); // find the first index for "=" , it is in "charset=".
                     f = resp.indexOf(";",k);
                     
                     if(f!=-1) charset = resp.substring(k + 1,f);
                     else charset = resp.substring(k+1);
                     
                  }
                  
                  //content-transfer-encoding �Ľ�
                  if (resp.toLowerCase().contains("content-transfer-encoding:")) { // parsing
                                                                  // "content-transfer-encoding:"
                     contentTransferEncoding = resp.substring(resp.indexOf(":") + 1);
                     
                     //���� �ι� ��� ����
                     if(contentTransferEncoding.equals(" quoted-printable")) quoted--;
                     else if(contentTransferEncoding.equals(" 8bit")) bit8--;
                     else if(contentTransferEncoding.equals(" 7bit"))  bit7--;
                     else if(contentTransferEncoding.equals(" binary")) binary--;
                     else if(contentTransferEncoding.equals(" base64")) base64--;
                  }
                  
                  flag = 0; //���� �� �˸�
               }
               
            }//if

            //base64 ���ڵ�
            if (contentTransferEncoding.contains("base64")) {
               
               //���� ���� ������
               if (resp.equals("")) { //�� �� pass
                  while (resp.equals("")) {
                     resp = respBody.get(index++);
                  }
                  flag = 1;//���� ǥ��
               }

               //���� ���ڵ�
               if (flag == 1) {
                  decoded = decoder.decodeBuffer(resp);//base64 ���ڴ��� resp �־� ���ڵ�
                  outputStream.write(decoded);//�� ��� ���� ���� ���� write
               }

            }//base64 
            
            //quoted-printable ���ڵ�//quoted==1(�ߺ�) multi!=1(multipart x)//quoted�� �ߺ����� �ʰų� multipart �� �ƴ� �ÿ� ���
            else if (contentTransferEncoding.contains("quoted-printable")&&(quoted!=1||multi!=1)) {
               
               if (resp.equals("")) {// ���� ���� �κ� ã��
                  while (resp.equals("")) {
                     resp = respBody.get(index++);
                  }
                  flag = 1; // flag= 1�� �й� �������� �˸�
               }
               int check = 0; //���� Ȯ���ϴ� ���� �� �������̶� �̾��� ������ Ȯ���ϴ� ����

               if (flag == 1) {
                  
                  String line = "";

                  resp = resp.replace("3D", ""); // quoted-printable ���ڵ� ����� ���ڿ��� 3D���� ������ ����

                  while (resp.length() > 4) {
                     check = 0; // ������ ������ ���ڿ��� ����
                     line = resp;

                     if (line.substring(line.length() - 2, line.length()).equals(" =")) {// �������̶� �̾��� �ִµ� ������� �־�����ϴ� �κ�
                        line = line.substring(0, line.length() - 2);// ���ڿ� �ڸ���
                        line += " "; // �������� ������� �־��ֱ�
                        
                        resp = respBody.get(index++);// ������ ��������
                        line = line + resp; // line�� �����ؼ� �����ϱ�
                        check = 1; // �������̶� �̾ �ٿ������� ǥ����
                     }//if

                     else if (line.substring(line.length() - 1, line.length()).equals("=")) { // �������̶� �̾����ִµ� ������� ���� ���̴� �κ�
                        line = line.substring(0, line.length() - 1);
                        resp = respBody.get(index++);
                        line = line + resp;
                        check = 1;
                     }//else if

                     if (check == 0)   break; // �̾� ���ΰ� ��� ���� ���ڿ� resp�״�� ���� ��
                     else resp = line;// �ؿ��� resp�� ���ڵ��ϴϱ� resp�� ���ڿ� �̾���� line ����
                  }//while

                  targetStream = new ByteArrayInputStream(resp.getBytes());// mimeutility�� decode�� ���ڷ� inputstream�� �ޱ⶧���� string�� inputstream���� ��ȯ
                  targetStream = MimeUtility.decode(targetStream, "quoted-printable"); // mimeutility�� decode�� �̿��Ͽ� quoted-printableŸ������ inputstream�� ���ڵ��� 

                  ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                  while ((nRead = targetStream.read(data)) != -1) {// targetstream���� �о data�� ���� �о�°� ������ -1 ��ȯ
                     buffer.write(data, 0, nRead);// buffer�� nRead��ŭ�� data�� 0��° index���� ��
                  }
                  buffer.flush();
                  i = charset.indexOf('"');
                  j = charset.lastIndexOf('"');

                  if (i != j)
                     charset = charset.substring(i + 1, j);
                  str = new String(buffer.toByteArray(), charset);// ���Ͽ� ���� charset���� ����Ʈ ������ ���ڿ��� ���� 
                  System.out.println("str : " + str);

                  try {
                     out.write(str); // ���ڵ� �ϰ� �ٷ� ���Ͽ� ������
                     out.newLine(); // "\n"
                     out.flush();
                  } catch (IOException e) {
                     System.out.println(e);
                     System.exit(1);
                  }
               }//if

            }//quoted-printable
            
            //8bit, 7��Ʈ, binary, ���ڵ� ���� ���� resp file write
            else if ((contentTransferEncoding.contains("8bit")&&(bit8!=1||multi!=1)) || 
                  (contentTransferEncoding.contains("7bit")&&(bit7!=1||multi!=1))||
                  (contentTransferEncoding.contains("binary")&&(binary!=1||multi!=1)) ||
                  (contentTransferEncoding.equals("")&&multi!=1)) {
            	
            	if(contentTransferEncoding.equals(""))flag=1;
           
            	if (resp.equals("") && flag==0) { //�� �� pass
                    while (resp.equals("")) {
                       resp = respBody.get(index++);
                    }
                    flag = 1;//���� ǥ��
                 }

               //���� ����
               if (flag == 1) {
                  out.write(resp);// html ���
                  out.newLine(); // "\n"
                  out.flush();
               }
            }
           
            if (resp.equals(".")) { // response is finished.
               break;
            }
         }//while

         //base64 ���ڵ� 
         if (contentTransferEncoding.contains("base64")) {
            
            i = charset.indexOf('"');
            j = charset.lastIndexOf('"');

            //charset " ����
            if (i != j)
               charset = charset.substring(i + 1, j);

            //byte�� string���� ��ȯ
            str = new String(outputStream.toByteArray(), charset);

            out.write(str);// html ���
            out.newLine(); // "\n"
            out.flush();
            outputStream.flush();
         }
         out.close(); //html ���� �ݱ�

         //sender,subject,contentTransferEncoding,contentType,charset,fileName ���
         System.out.println("==============From = " + sender + "========================================");
         System.out.println("==============Subject = " + subject + "====================================");
         System.out.println("==============contentTransferEncoding = " + contentTransferEncoding + "=======================");
         System.out.println("==============contentType = " + contentType + "===========================");
         System.out.println("==============charset = " + charset + "==============================");
         System.out.println("==============attachmentName = " + attachmentName + "======================");

         command = "DELE " + type + "\r\n"; // make command "PASS: password".
         System.out.println(command);

         opStream.write(command.getBytes()); // client sends command to server.

         resp = ipStream.readLine(); // read data that in ipStream. ipStream has server's response.
         System.out.println(">" + resp);// print server's response.

         if (resp.charAt(0) == '+') { // if response is "+OK" that means password is correct
            System.out.println("Delete OK");
         } 
         else { // if response is "-ERR" that means password is wrong
            socket.close(); // if an error has occurred, close socket and finish.
            System.out.println("Delete ERR");
         }

      } // readMail (while)

   }//read

}//popClient