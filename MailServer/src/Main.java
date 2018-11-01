public class Main {

    public static void main(String[] args) {
        SMTPSender sender = new SMTPSender();
        String id = "구글 아이디", password = "구글 비밀번호";
        String fromEmail = "";
        String toEmail = "";
        if(sender.sendMail(id, password, fromEmail, toEmail, "메일 전송 시스템 확인"))
            System.out.println("메일 전송 성공!");
        else
            System.out.println("메일 전송 실패!");
    }
}
