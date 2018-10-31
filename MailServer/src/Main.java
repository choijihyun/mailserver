public class Main {

    public static void main(String[] args) {
        SMTPSender sender = new SMTPSender();
        if(sender.sendMail("go1217jo@naver.com", "go1217jo@gmail.com", "메일 전송 시스템 확인"))
            System.out.println("메일 전송 성공!");
        else
            System.out.println("메일 전송 실패!");
    }
}
