import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SMTPSender {
    /* nslookup
       > set type=mx
       > [도메인 명] 으로 메일서버 주소를 확인할 수 있다.*/
    private String myMXAddress = "alt3.aspmx.l.google.com";
    private String domainName = "google";

    private SSLSocket socket;
    // 서버 응답을 읽어오는 객체
    private BufferedReader br;
    // 서버에 요구를 보내는 객체
    private PrintWriter pw;
    private String statusCode;

    private String id;
    private String password;

    // 자신의 SMTP 서버에 연결
    protected boolean connectServer(String user, String password) throws IOException {
        String line = "";
        // SMTP 메일 서버에 소켓 연결
        socket = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault())
                .createSocket(InetAddress.getByName("smtp.gmail.com"), 465);
        if(socket == null)
            return false;

        this.id = Base64.getEncoder().encodeToString(user.getBytes(StandardCharsets.UTF_8));
        this.password = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));

        // 생성된 소켓으로, 서버와 stream 통신을 할 수 있는 객체를 생성
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pw = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("서버 연결됨");
        System.out.println("response : " + br.readLine());

        return true;
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

    public boolean sendMail(String userID, String passwd, String from, String to, String content) {
        String line = "";
        try {
            // 본인 메일 서버 연결
            if(!connectServer(userID, passwd))
                return false;
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("본인 메일 서버 연결 실패");
            return false;
        }
        try {
            /*
            // HELO 명령 전송, 응답코드 250 확인
            if(!sendCommand("HELO ", localhost.getHostName(), "250"))
                return false;*/

            // EHLO 명령 전송, 응답코드 250 확인
            if(!sendCommand("EHLO ", "smtp.gmail.com", "250"))
                return false;

            pw.println("AUTH LOGIN");
            line = br.readLine();
            while(!line.substring(0,3).equals("334")) {
                System.out.println(line);
                line = br.readLine();
            }
            pw.println(this.id);
            System.out.println("response : " + br.readLine());
            pw.println(this.password);
            System.out.println("response : " + br.readLine());

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
            pw.println("From: Test <" + from + ">");
            pw.println("To: Me <" + to + ">");
            pw.println("Subject: Testing email from telnet");
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
                pw.println("QUIT");
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