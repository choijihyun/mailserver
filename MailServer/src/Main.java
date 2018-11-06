package networkProject;

import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		while (true) {
			System.out.println("=============================");
			System.out.println("\tMAIL SERVICE\t");
			System.out.println("=============================");

			System.out.println("1. SEND MAIL");
			System.out.println("2. READ MAIL");
			System.out.println("3. CLOSE");

			System.out.printf("ENTER THE NUMBER : ");
			String type;
			int number;

			type = input.next();
			number = type.charAt(0)-'0';
			input.nextLine();

			switch (number) {
			case 1:
				SMTPSender sender = new SMTPSender();
				String MXAccount, MXPassword;
				String toEmail, Subject, Content;
				
				System.out.println("User Account : ");
				MXAccount = input.next();
				System.out.println("User Password : ");
				MXPassword = input.next();
				System.out.println("To : ");
				toEmail = input.next();
				System.out.println("Subject : ");
				Subject = input.next();
				System.out.println("Content : ");
				Content = input.next();
				if (sender.sendMail(MXAccount, MXPassword, toEmail, Subject, Content))
					System.out.println("Success to send mail!");
				else
					System.out.println("Fail to send mail!");

				break;
			case 2:
				PopClient client = new PopClient();
				client.popPort = 995;

				System.out.println("User Account : ");
				client.popAccount = input.next();
				System.out.println("User Password : ");
				client.popPassword = input.next();

				int index = client.popAccount.indexOf("@");
				String mail = client.popAccount.substring(index + 1);
				client.popServer = "pop." + mail;

				System.out.println(client.popServer + "\n");
				System.out.println(client.popAccount + "\n");

				try {
					client.read();
					System.out.println("Reading Success");
				} catch (Exception e) {
					System.out.println("Reading Fail");
				}
				break;
			case 3:
				System.exit(0);
				return;
			default:
				System.out.println("PLEASE REENTER");
				break;
			}

		}
	}
}
