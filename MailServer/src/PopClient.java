package networkProject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import com.sun.xml.internal.fastinfoset.Decoder;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;

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
					break;
				}

				// Add read line to the list of lines
				respBody.add(resp);
			}

			// parsing sender,subject and content-Type, charset,content-transfer-encoding
			// for decoding

			String sender, subject, contentType, charset, contentTransferEncoding, attachmentName, resp1;

			int i, j, k, f; // variable for method "indexOf()"

			int index = 0;
			int flag = 0, bodyStart = 0;
			int multi = 0;

			sender = subject = contentType = charset = contentTransferEncoding = attachmentName = ""; // initialize

			BufferedWriter out = new BufferedWriter(new FileWriter(type + "‪.html"));

			while (true) {
				resp = respBody.get(index++);
				if (resp.toLowerCase().contains("content-type:")) { // parsing(case "content-type:" and "charset" is in
																	// same line)
					i = resp.indexOf(":"); // find the first index for ":" , it is in "content-type:".
					j = resp.indexOf(";"); // find the first index for ";" , it is in "content-type: ....;".

					if (resp.contains("multipart"))
						multi = 1;

					contentType = resp.substring(i + 1, j);

				} // 완료

				if (resp.contains("Subject:")) { // parsing "subject" ,, from is under subject
					while (true) {
						i = resp.indexOf("=");
						j = resp.lastIndexOf("=");

						if (i != j && i != -1 && j != -1 && resp.substring(i + 1, i + 2).equals("?")
								&& resp.substring(j - 1, j).equals("?") && !(resp.toLowerCase().contains("from:"))) {// if

							subject += MimeUtility.decodeText(resp.substring(i, j + 1));

							resp = respBody.get(index++);

						} else
							break;
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

				}

				if (resp.toLowerCase().contains("content-type:") && resp.toLowerCase().contains("charset=")&&multi!=1) { // parsing(case
					i = resp.indexOf(":"); // find the first index for ":" , it is in "content-type:".
					j = resp.indexOf(";"); // find the first index for ";" , it is in "content-type: ....;".
					k = resp.indexOf("="); // find the first index for "=" , it is in "charset=".
					f = resp.indexOf(";",k);
					
					contentType = resp.substring(i + 1, j);
					if(f!=-1) charset = resp.substring(k + 1,f);
					else charset = resp.substring(k+1);

					System.out.println(charset);
				} // 완료

				else if (resp.toLowerCase().contains("content-type:")) { // parsing(case "content-type:" and "charset"
																			// is in different line)
					resp1 = respBody.get(index++);

					if (resp1.toLowerCase().contains("charset=")&&multi!=1) {

						i = resp.indexOf(":"); // find the first index for ":" , it is in "content-type:".
						j = resp.indexOf(";"); // find the first index for ";" , it is in "content-type: ....;".
						k = resp1.indexOf("="); // find the first index for "=" , it is in "charset=".
						f = resp1.indexOf(";",k);
						
						contentType = resp.substring(i + 1, j);
						if(f!=-1) charset = resp1.substring(k + 1,f);
						else charset = resp1.substring(k+1);
						
					}
				}

				// parsing(case "content-disposition:" and "filename" is in same line)

				if (resp.toLowerCase().contains("content-disposition:") && resp.toLowerCase().contains("filename=")) {

					k = resp.indexOf("="); // find the first index for "=" , it is in "filename=".

					attachmentName += MimeUtility.decodeText(resp.substring(k + 1)); // "+=" => mail can containing many
				}// attached of inlined file.

				else if (resp.toLowerCase().contains("content-disposition:")) { // parsing(case "content-disposition:"
																				// and "filename" is in different line)
					resp = ipStream.readLine(); // (resp = "filename")

					if (resp.toLowerCase().contains("filename=")) {

						k = resp.indexOf("=");

						attachmentName += MimeUtility.decodeText(resp.substring(k + 1));

					}

				}

				if (resp.equals(".")) { // response is finished.
					System.out.println("!!!!>>>>>>>>>>>> end >>>>>>>>>>>"); // notice that the response is finished.
					// out.close(); //close file.
					break;
				}
				System.out.println(resp);
			} // while (read resp)

			String initialString = resp;

			InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());

      String str,boundary;
			index = 0;
			BASE64Decoder decoder = new BASE64Decoder();
			str = "";
			byte[] decoded, sav;
			byte[] data = new byte[16384];

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			flag = 0;

			int flag1 = 0; // quoted만 검사하는 flag값
			int nRead;
			
			
			if(multi==1) contentTransferEncoding="none";
			while (true) {
				// if(resp.contains("base64")) flag1=1;

				resp = respBody.get(index++);

				System.out.println("Resp : " + resp);

				// subject is not encoding. (subject dosen't contain '=' & '?')
				if (subject.equals("") && resp.toLowerCase().contains("subject:")) {
					i = resp.indexOf(":");
					subject += MimeUtility.decodeText(resp.substring(i + 1));
				}

				if (multi == 1 && resp.contains("--")) {
					System.out.println("#@#@#@#@#@#@#@#");
					if (contentTransferEncoding.contains("base64")) {
						System.out.println("%$%$%$%$%$%$%$%$%$%$%$");
						System.out.println(charset);
						
						i = charset.indexOf('"');
						j = charset.lastIndexOf('"');

						if (i != j)
							charset = charset.substring(i + 1, j);

						str = new String(outputStream.toByteArray(), charset);
						System.out.println(str);// 여기가 콘솔출력

						out.write(str);// html 출력
						out.newLine(); // "\n"
						out.flush();
					}
										
					while (!resp.equals("")) {
						
						resp = respBody.get(index++);
						System.out.println("헤헤헤헤헤헤헤헤헤헤헤");
						System.out.println(resp);
						
						if (resp.toLowerCase().contains("charset=")) {
							k = resp.indexOf("="); // find the first index for "=" , it is in "charset=".
							f = resp.indexOf(";",k);
							
							if(f!=-1) charset = resp.substring(k + 1,f);
							else charset = resp.substring(k+1);
							
							System.out.println("#@#@#@#@#@#@#@#"+charset);
						}
						
						if (resp.toLowerCase().contains("content-transfer-encoding:")) { // parsing
																						// "content-transfer-encoding:"
							contentTransferEncoding = resp.substring(resp.indexOf(":") + 1);

						}
						flag = 0;
					}
					
					

				}//if

				if (contentTransferEncoding.contains("base64")) {
					if (resp.equals("")) {
						while (resp.equals("")) {
							resp = respBody.get(index++);
						}
						flag = 1;
					}

					if (flag == 1) {
						decoded = decoder.decodeBuffer(resp);
						outputStream.write(decoded);
						outputStream.flush();
					}

					if (flag == 0) {
						System.out.println(MimeUtility.decodeText(resp));
					}
				

				}//base64 
				else if (contentTransferEncoding.contains("quoted-printable")) {
					if (resp.equals("")) {
						while (resp.equals("")) {
							resp = respBody.get(index++);
						}
						flag1 = 1;
					}
					int check = 0;

					if (flag == 2 || flag1 == 1) {
						String line = "";

						resp = resp.replace("3D", "");

						while (resp.length() > 4) {
							check = 0;
							line = resp;

							if (line.substring(line.length() - 2, line.length()).equals(" =")) {
								line = line.substring(0, line.length() - 2);
								line += " ";
								
								resp = respBody.get(index++);
								line = line + resp;
								check = 1;
							}//if

							else if (line.substring(line.length() - 1, line.length()).equals("=")) {
								line = line.substring(0, line.length() - 1);
								resp = respBody.get(index++);
								line = line + resp;
								check = 1;
							}//else if

							if (check == 0)	break;
							else resp = line;
						}//while

						targetStream = new ByteArrayInputStream(resp.getBytes());
						targetStream = MimeUtility.decode(targetStream, "quoted-printable");

						ByteArrayOutputStream buffer = new ByteArrayOutputStream();

						while ((nRead = targetStream.read(data)) != -1) {
							buffer.write(data, 0, nRead);
						}
						buffer.flush();
						i = charset.indexOf('"');
						j = charset.lastIndexOf('"');

						if (i != j)
							charset = charset.substring(i + 1, j);
						str = new String(buffer.toByteArray(), charset);
						System.out.println("str : " + str);

						try {
							out.write(str);
							out.newLine(); // "\n"
						} catch (IOException e) {
							System.out.println(e);
							System.exit(1);
						}
					}//if

				}//quoted-printable
				else if (contentTransferEncoding.contains("8bit") || contentTransferEncoding.contains("7bit")||contentTransferEncoding.equals("")) {
					if (flag==0) {
						while (resp.equals("")) {
							resp = respBody.get(index++);
						}
						flag = 1;
						resp = respBody.get(index++);
					}
					else if (contentTransferEncoding.equals("") && resp.equals("")) {
						while (resp.equals("")) {
							resp = respBody.get(index++);
						}
						flag = 1;
					}

					if (flag == 1) {
						System.out.println("resp : " + resp);
						out.write(resp);// html 출력
						out.newLine(); // "\n"
						out.flush();
					}
				}

				// System.out.println(index + "||||||||||" + respBody.size());
				if (index == respBody.size()) { // response is finished.
					System.out.println(">>>>>>>>>>>> end >>>>>>>>>>>"); // notice that the response is finished.
					break;
				}
			}//while

			if (contentTransferEncoding.contains("base64")) {
				System.out.println(charset);
				
				i = charset.indexOf('"');
				j = charset.lastIndexOf('"');

				if (i != j)
					charset = charset.substring(i + 1, j);

				str = new String(outputStream.toByteArray(), charset);
				System.out.println(str);// 여기가 콘솔출력

				out.write(str);// html 출력
				out.newLine(); // "\n"
				out.flush();
			}
			out.close();

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
