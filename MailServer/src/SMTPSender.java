import java.net.*;
import java.io.*;

public class SMTPSender {
    /* nslookup
       > set type=mx
       > [도메인 명] 으로 메일서버 주소를 확인할 수 있다.*/
    private String myMXAddress = "mx2.naver.com";
    private String domainName = "naver.com";
    // 서버 응답을 읽어오는 객체
    private BufferedReader br;
    // 서버에 요구를 보내는 객체
    private PrintWriter pw;
    private String statusCode;

    // 자신의 SMTP 서버에 연결
    protected boolean connectMyServer() throws IOException {
        // SMTP 메일 서버에 소켓 연결
        Socket socket = new Socket(myMXAddress, 25);

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
        String line = "";
        pw.println(command + param);
        line = br.readLine();
        System.out.println("response : " + line);
        if(line.substring(0,2).equals(responseCode))
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

            // MAIL FROM 명령 전송, 응답코드 250 확인
            if(!sendCommand("MAIL FROM: ", from, "250"))
                return false;

            // RCPT 명령 전송, 응답코드 250 확인
            if(!sendCommand("RCPT TO: ", to, "250"))
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
        }
        return true;
    }
}