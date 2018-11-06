package networkProject;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SMTPSender {

	/* nslookup > set type=mx > [domain name] 
	 * 
	 * we can check mailserver address with that one*/
	private String domainName = "google";

	private SSLSocket socket;
	//read the response from server
	private BufferedReader br;
	//send the request to server
	private PrintWriter pw;
	private String statusCode;

	private String id;
	private String password;

	// connect to own SMTP server
	protected boolean connectServer(String domainName, int socketNumber) throws IOException {
        String line = "";
        // SMTP 메일 서버에 소켓 연결
        socket = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault())
                .createSocket(InetAddress.getByName(domainName), socketNumber);
        if(socket == null)
            return false;

        // 생성된 소켓으로, 서버와 stream 통신을 할 수 있는 객체를 생성
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pw = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("서버 연결됨");
        System.out.println("response : " + br.readLine());

        return true;
    }

	// confirm the code with responseCode
	protected boolean sendCommand(String command, String param, String responseCode) throws IOException {
		String line = "", code;
		pw.println(command + param);
		line = br.readLine();
		System.out.println("response : " + line);
		code = line.substring(0, 3);
		if (!code.equals(responseCode))
			return false;

		return true;
	}
	
	 // 특수한 도메인네임에 대한 예외처리와 메일 서버 주소로 변환하여 반환
    protected String convertDomainName(String address) {
        // google로 입력이 들어왔으면 gmail로 변환
        if(address.equals("google.com"))
            return "smtp.gmail.com";
        else
            return "smtp." + address;
    }


	// send mail 
	public boolean sendMail(String userID, String passwd, String to, String Subject, String content) {
		 String line = "";
	        // @를 기준으로 도메인 이름을 알아낸다.
	        String[] temp = userID.split("@");
	        if(temp.length == 2)
	            domainName = temp[1];
	        else {
	            System.out.println("보내는 이메일 주소를 확인해주세요.");
	            return false;
	        }

	        try {
	            // 본인 메일 서버 연결
	            if(!connectServer(convertDomainName(domainName), 465))
	                return false;
	        }catch (IOException e) {
	            e.printStackTrace();
	            System.out.println("본인 메일 서버 연결 실패");
	            return false;
	        }

	        // 아이디, 비밀번호 암호화
	        this.id = Base64.getEncoder().encodeToString(userID.getBytes(StandardCharsets.UTF_8));
	        this.password = Base64.getEncoder().encodeToString(passwd.getBytes(StandardCharsets.UTF_8));

		try {
			/*
			 * // HELO request send, responsecode 250, if(!sendCommand("HELO ", localhost.getHostName(),
			 * "250")) return false;
			 */

			// EHLO request send, look responsecode is 250
			if(!sendCommand("EHLO ", convertDomainName(domainName), "250"))
				return false;
			
			pw.println("AUTH LOGIN");
			line = br.readLine();
			// the username and password are sent from the client to the server
			while (!line.substring(0, 3).equals("334")) {
				System.out.println(line);
				line = br.readLine();
			}
			
			pw.println(this.id);
			System.out.println("response : " + br.readLine());
			pw.println(this.password);
			System.out.println("response : " + br.readLine());

			// MAIL FROM request send, confirm code is 250
			if (!sendCommand("MAIL FROM: ", "<" + userID + ">", "250"))
				return false;

			// RCPT request send, confirm code is 250 
			if (!sendCommand("RCPT TO: ", "<" + to + ">", "250"))
				return false;

			// DATA request send, confirm code is 354
			//354 is Start mail input; end with <CRLF>.<CRLF>
			if (!sendCommand("DATA", "", "354"))
				return false;

			// send mail
			pw.println("From: <" + userID + ">");
			pw.println("To: <" + to + ">");
			pw.println("Subject:"+Subject);
			pw.println(content);
			// end the mail
			if (!sendCommand(".", "", "250"))
				return false;

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("During sending email, error is occur");
			return false;
		} finally {
			try {
				pw.println("QUIT");
				br.close();
				pw.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
