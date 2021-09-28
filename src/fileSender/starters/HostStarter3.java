package fileSender.starters;

import fileSender.Host;

import java.io.IOException;

public class HostStarter3 {
    public static void main(String[] args) {
        try {
            Host host=new Host(3,10002);
            host.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
