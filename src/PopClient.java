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

         /// 모든 resp 받아오기
         ArrayList<String> respBody = new ArrayList<>();
         while (true) { // 메일 리스트 받아오기
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

            } // 완료*/

            if (resp.contains("Subject:")) { // parsing "subject" ,, from is under subject
               while (true) {
                  i = resp.indexOf("="); // =를 기준으로 파싱
                  j = resp.lastIndexOf("=");

                  if (i != j && i != -1 && j != -1 && resp.substring(i + 1, i + 2).equals("?")
                        && resp.substring(j - 1, j).equals("?") && !(resp.toLowerCase().contains("from:"))) {// 제목 형식에 맞으면 decoding from 까지

                     subject += MimeUtility.decodeText(resp.substring(i, j + 1)); //subject 디코딩

                     resp = respBody.get(index++);// 한줄씩 가져와서 확인

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
               
               //본문 두번 출력 방지
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
               f = resp.indexOf(";",k); //charset을 파싱 위해 ; index 구함
               
               contentType = resp.substring(i + 1, j);//contentType 파싱
               if(f!=-1) charset = resp.substring(k + 1,f); //; 이 있는 곳까지 파싱
               else charset = resp.substring(k+1);// ;이 없으면 끝까지 파싱

            } 
            else if (resp.toLowerCase().contains("content-type:")) { // parsing(case "content-type:" and "charset"
                                                         // is in different line)
               resp1 = respBody.get(index++);

               //multipart가 아닌경우 charset 을 저장
               if (resp1.toLowerCase().contains("charset=")&&multi!=1) {

                  k = resp1.indexOf("="); // find the first index for "=" , it is in "charset=".
                  f = resp1.indexOf(";",k);
                  
                  if(f!=-1) charset = resp1.substring(k + 1,f);
                  else charset = resp1.substring(k+1);
                  
               }
               else index--;
               
               i = resp.indexOf(":"); // find the first index for ":" , it is in "content-type:".
               j = resp.indexOf(";"); // find the first index for ";" , it is in "content-type: ....;".
               
               //multipart flag 표시
               if (resp.contains("multipart"))
                  multi = 1;

               if(i!=j&&i!=-1&&j!=-1)   contentType = resp.substring(i + 1, j);
               
            }
            
            //boundary 파싱
            if (resp.contains("boundary=") && (boundary1.equals("") || boundary2.equals("")) ) {
               i = resp.indexOf("="); //=를 기준으로 boundary1과 boundary2 파싱
               
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
                  i = resp.indexOf("=",k+1); // =를 기준으로 파싱
                  j = resp.lastIndexOf("=");

                  if (i != j && i != -1 && j != -1 && resp.substring(i + 1, i + 2).equals("?")
                        && resp.substring(j - 1, j).equals("?") ) {// 파일 형식에 맞으면 decoding from 까지

                     attachmentName += MimeUtility.decodeText(resp.substring(i, j + 1)); //file name 디코딩
                  } 
               }// attached of inlined file.

               else if (resp.toLowerCase().contains("content-disposition:")) { // parsing(case "content-disposition:"
                                                               // and "filename" is in different line)
                  resp1 = respBody.get(index++); // (resp = "filename")

                  if (resp1.toLowerCase().contains("filename=")) {
                     k = resp1.indexOf("="); // find the first index for "=" , it is in "filename=".
                      i = resp1.indexOf("=",k+1); // =를 기준으로 파싱
                      j = resp1.lastIndexOf("=");

                      if (i != j && i != -1 && j != -1 && resp1.substring(i + 1, i + 2).equals("?")
                            && resp1.substring(j - 1, j).equals("?") ) {
                         attachmentName += MimeUtility.decodeText(resp1.substring(i, j + 1)); 
                         
                      } 
                  }
                  else index--; //다음 줄의 정보를 읽기위한 처리
               }//else if

            
            if (resp.equals(".")) { // response is finished.
               
               //boundary 에 따옴표가 있는 경우에 파싱 
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

         //quoted-printable 디코딩을 위한 inputStream
         String initialString = resp;
         InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());

         String str="";
         index = 0;
         BASE64Decoder decoder = new BASE64Decoder(); //base64 디코더
         
         byte[] decoded;
         byte[] data = new byte[512]; //버퍼 사이즈

         ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); //base64 를 위한 outputstream

         flag = 0;
         int nRead;// nRead는 targetstream의 the byte value를 int로 반환
         if(multi==1) contentTransferEncoding="";//multipart 의 경우에는 처음부터 인코딩타입을 초기화
         while (true) {
            resp = respBody.get(index++);

            // subject is not encoding. (subject dosen't contain '=' & '?')
            if (subject.equals("") && resp.toLowerCase().contains("subject:")) {
               i = resp.indexOf(":");
               subject += MimeUtility.decodeText(resp.substring(i + 1));
            }
            //multipart 인지 확인하고 boundary1과 boundary2를 기준으로 나눈다.
            if (multi == 1 && ( ( resp.contains(boundary1) && !boundary1.equals(""))  || ( resp.contains(boundary2)) &&  !boundary2.equals(""))) {
            
               if (base64==1) outputStream.reset(); //본문 중복 제거   
               
               //boundary를 기준으로 charset과 content-transfer-encoding 파싱
               while (!resp.equals("")) {
               
                  if(resp.equals(".")) {
                     break;
                  }//본문 끝
                  
                  resp = respBody.get(index++);
                  
                  
                  //charset 파싱
                  if (resp.toLowerCase().contains("charset=")) {
                     k = resp.indexOf("="); // find the first index for "=" , it is in "charset=".
                     f = resp.indexOf(";",k);
                     
                     if(f!=-1) charset = resp.substring(k + 1,f);
                     else charset = resp.substring(k+1);
                     
                  }
                  
                  //content-transfer-encoding 파싱
                  if (resp.toLowerCase().contains("content-transfer-encoding:")) { // parsing
                                                                  // "content-transfer-encoding:"
                     contentTransferEncoding = resp.substring(resp.indexOf(":") + 1);
                     
                     //본문 두번 출력 방지
                     if(contentTransferEncoding.equals(" quoted-printable")) quoted--;
                     else if(contentTransferEncoding.equals(" 8bit")) bit8--;
                     else if(contentTransferEncoding.equals(" 7bit"))  bit7--;
                     else if(contentTransferEncoding.equals(" binary")) binary--;
                     else if(contentTransferEncoding.equals(" base64")) base64--;
                  }
                  
                  flag = 0; //본문 끝 알림
               }
               
            }//if

            //base64 디코딩
            if (contentTransferEncoding.contains("base64")) {
               
               //본문 구분 시작점
               if (resp.equals("")) { //빈 줄 pass
                  while (resp.equals("")) {
                     resp = respBody.get(index++);
                  }
                  flag = 1;//본문 표시
               }

               //본문 디코딩
               if (flag == 1) {
                  decoded = decoder.decodeBuffer(resp);//base64 디코더에 resp 넣어 디코딩
                  outputStream.write(decoded);//줄 띄움 제거 위한 버퍼 write
               }

            }//base64 
            
            //quoted-printable 디코딩//quoted==1(중복) multi!=1(multipart x)//quoted가 중복되지 않거나 multipart 가 아닐 시에 출력
            else if (contentTransferEncoding.contains("quoted-printable")&&(quoted!=1||multi!=1)) {
               
               if (resp.equals("")) {// 본문 시작 부분 찾기
                  while (resp.equals("")) {
                     resp = respBody.get(index++);
                  }
                  flag = 1; // flag= 1로 분문 시작함을 알림
               }
               int check = 0; //현재 확인하는 줄이 이 다음줄이랑 이어진 줄인지 확인하는 변수

               if (flag == 1) {
                  
                  String line = "";

                  resp = resp.replace("3D", ""); // quoted-printable 인코딩 방식인 문자열에 3D끼워 넣은거 삭제

                  while (resp.length() > 4) {
                     check = 0; // 아직은 연결한 문자열이 없음
                     line = resp;

                     if (line.substring(line.length() - 2, line.length()).equals(" =")) {// 다음줄이랑 이어져 있는데 띄워쓰기 넣어줘야하는 부분
                        line = line.substring(0, line.length() - 2);// 문자열 자르기
                        line += " "; // 마지막에 띄워쓰기 넣어주기
                        
                        resp = respBody.get(index++);// 다음줄 가져오기
                        line = line + resp; // line에 연결해서 저장하기
                        check = 1; // 다음줄이랑 이어서 붙여놓음을 표시함
                     }//if

                     else if (line.substring(line.length() - 1, line.length()).equals("=")) { // 다음줄이랑 이어져있는데 띄워쓰기 없이 붙이는 부분
                        line = line.substring(0, line.length() - 1);
                        resp = respBody.get(index++);
                        line = line + resp;
                        check = 1;
                     }//else if

                     if (check == 0)   break; // 이어 붙인게 없어서 기존 문자열 resp그대로 쓰면 됌
                     else resp = line;// 밑에서 resp를 디코딩하니까 resp에 문자열 이어붙인 line 대입
                  }//while

                  targetStream = new ByteArrayInputStream(resp.getBytes());// mimeutility의 decode가 인자로 inputstream을 받기때문에 string을 inputstream으로 변환
                  targetStream = MimeUtility.decode(targetStream, "quoted-printable"); // mimeutility의 decode를 이용하여 quoted-printable타입으로 inputstream을 디코딩함 

                  ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                  while ((nRead = targetStream.read(data)) != -1) {// targetstream에서 읽어서 data에 저장 읽어온게 없으면 -1 반환
                     buffer.write(data, 0, nRead);// buffer에 nRead만큼의 data를 0번째 index부터 씀
                  }
                  buffer.flush();
                  i = charset.indexOf('"');
                  j = charset.lastIndexOf('"');

                  if (i != j)
                     charset = charset.substring(i + 1, j);
                  str = new String(buffer.toByteArray(), charset);// 파일에 적힌 charset으로 바이트 형식을 문자열로 저장 
                  System.out.println("str : " + str);

                  try {
                     out.write(str); // 디코딩 하고 바로 파일에 저장함
                     out.newLine(); // "\n"
                     out.flush();
                  } catch (IOException e) {
                     System.out.println(e);
                     System.exit(1);
                  }
               }//if

            }//quoted-printable
            
            //8bit, 7비트, binary, 인코딩 되지 않은 resp file write
            else if ((contentTransferEncoding.contains("8bit")&&(bit8!=1||multi!=1)) || 
                  (contentTransferEncoding.contains("7bit")&&(bit7!=1||multi!=1))||
                  (contentTransferEncoding.contains("binary")&&(binary!=1||multi!=1)) ||
                  (contentTransferEncoding.equals("")&&multi!=1)) {
            	
            	if(contentTransferEncoding.equals(""))flag=1;
           
            	if (resp.equals("") && flag==0) { //빈 줄 pass
                    while (resp.equals("")) {
                       resp = respBody.get(index++);
                    }
                    flag = 1;//본문 표시
                 }

               //본문 시작
               if (flag == 1) {
                  out.write(resp);// html 출력
                  out.newLine(); // "\n"
                  out.flush();
               }
            }
           
            if (resp.equals(".")) { // response is finished.
               break;
            }
         }//while

         //base64 디코딩 
         if (contentTransferEncoding.contains("base64")) {
            
            i = charset.indexOf('"');
            j = charset.lastIndexOf('"');

            //charset " 제거
            if (i != j)
               charset = charset.substring(i + 1, j);

            //byte를 string으로 변환
            str = new String(outputStream.toByteArray(), charset);

            out.write(str);// html 출력
            out.newLine(); // "\n"
            out.flush();
            outputStream.flush();
         }
         out.close(); //html 파일 닫기

         //sender,subject,contentTransferEncoding,contentType,charset,fileName 출력
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