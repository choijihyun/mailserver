public class Main {

    public static void main(String[] args) {
        SMTPSender sender = new SMTPSender();
        String id = "아이디", password = "비밀번호";
        String fromEmail = "전송자";
        String toEmail = "수신자";
        if(sender.sendMail(id, password, fromEmail, toEmail, "네이버 메일에서 다음 메일로 전송\n"))
            System.out.println("메일 전송 성공!");
        else
            System.out.println("메일 전송 실패!");
    }
}
