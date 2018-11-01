import java.net.*;
import java.io.*;

public class SMTPSender {
    /* nslookup
       > set type=mx
       > [도메인 명] 으로 메일서버 주소를 확인할 수 있다.*/
    private String myMXAddress = "alt3.aspmx.l.google.com";
    private String domainName = "google";
    private Socket socket;
    // 서버 응답을 읽어오는 객체
    private BufferedReader br;
    // 서버에 요구를 보내는 객체
    private PrintWriter pw;
    private String statusCode;

    // 자신의 SMTP 서버에 연결
    protected boolean connectMyServer() throws IOException {
        // SMTP 메일 서버에 소켓 연결
        socket = new Socket(myMXAddress, 25);

        // 생성된 소켓으로, 서버와 stream 통신을 할 수 있는 객체를 생성
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pw = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("서버 연결됨");

        String line = br.readLine();
        System.out.println("response : " + line);
        statusCode = line.substring(0,3);
        if(statusCode.equals("220")) {
            System.out.println("서버 응답 성공");
            return true;
        }
        return false;
    }

    protected boolean sendCommand(String command, String param, String responseCode) throws IOException {
        String line = "", code;
        pw.println(command + param);
        line = br.readLine();
        System.out.println("response : " + line);
        code = line.substring(0,3);
        if(!code.equals(responseCode))
            return false;

        return true;
    }

    public boolean sendMail(String from, String to, String content) {
        String line = "";
        try {
            // 본인 메일 서버 연결
            connectMyServer();
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("본인 메일 서버 연결 실패");
            return false;
        }
        try {
            // HELO 명령 전송, 응답코드 250 확인
            if(!sendCommand("HELO ", domainName, "250"))
                return false;

            // EHLO 명령 전송, 응답코드 250 확인
            if(!sendCommand("EHLO ", domainName, "250"))
                return false;

            pw.println("AUTH LOGIN");
            // http://www.utilities-online.info/base64/#.W9pE6T5bRPY
            pw.println("base64로 암호화된 아이디");
            pw.println("base64로 암호화된 비밀번호");

            // MAIL FROM 명령 전송, 응답코드 250 확인
            if(!sendCommand("MAIL FROM: ", "<" + from + ">", "250"))
                return false;

            // RCPT 명령 전송, 응답코드 250 확인
            if(!sendCommand("RCPT TO: ", "<" + to + ">", "250"))
                return false;

            // DATA 명령 전송, 응답코드 354 확인
            if(!sendCommand("DATA", "", "354"))
                return false;

            // 본문 전송
            pw.println(content);
            // 본문 전송 마무리
            if(!sendCommand(".", "", "250"))
                return false;

        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("메일을 보내는 중 오류가 발생했습니다.");
            return false;
        }finally {
            try {
                pw.println("quit");
                br.close();
                pw.close();
                socket.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}