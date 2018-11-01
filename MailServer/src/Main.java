public class Main {

    public static void main(String[] args) {
        SMTPSender sender = new SMTPSender();
        String id = "", password = "";
        if(sender.sendMail(id, password,"go1217jo@gmail.com", "go1217jo@naver.com", "메일 전송 시스템 확인"))
            System.out.println("메일 전송 성공!");
        else
            System.out.println("메일 전송 실패!");
    }
}
