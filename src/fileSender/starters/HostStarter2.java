package fileSender.starters;

import fileSender.Host;

import java.io.IOException;

public class HostStarter2 {
    public static void main(String[] args) {
        try {
            Host host=new Host(2,10001);
            host.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
