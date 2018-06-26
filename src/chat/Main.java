package chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by prog1 on 19.03.2018.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        final Scanner scanner = new Scanner(System.in);
        final Socket socket = new Socket("10.0.87.163", 5000);
        final InputStream is = socket.getInputStream();
        final OutputStream os = socket.getOutputStream();
        System.out.println("Enter login: ");
        final String login = scanner.nextLine();
        Thread th = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        synchronized (Main.class){
                            Message msg = Message.readFromStream(is);
                            if (msg != null){
                                System.out.println(msg.toString());
                            }
                            Main.class.notifyAll();
                        }
                    }
                } catch (Exception e) {
                    return;
                }
            }
        };
        th.setDaemon(true);
        th.start();

        try {
            while (true) {
                synchronized (Main.class){
                    String s = scanner.nextLine();
                    if (s.isEmpty())
                        break;
                    int del = s.indexOf(':');
                    String to = "";
                    String text = s;
                    if (del > 0) {
                        to = s.substring(0, del);
                        text = s.substring(del + 1);
                    }
                    Message m = new Message();
                    m.text = text;
                    m.from = login;
                    m.to = to;
                    m.writeToStream(os);
                    Main.class.notifyAll();
                }
            }
        } finally {
            th.interrupt();
            socket.close();
        }
    }
}