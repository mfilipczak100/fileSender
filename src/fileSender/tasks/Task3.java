package fileSender.tasks;

import fileSender.Host;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Task3 {
    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        try {
            Host host=new Host(1,10000);
            ArrayList<File> fileList=host.getFileList();
            System.out.println("Do ktorego klienta chcesz wyslac plik? (dostepne opcje 2,3)");
            try {
                int clientId=scanner.nextInt();
                if (clientId==2||clientId==3) {
                    System.out.println("Ktory plik chcesz przeslac?");
                    for (int i = 0; i < fileList.size(); i++) {
                        System.out.println((i + 1) + " - " + fileList.get(i).getName());
                    }
                    int fileIndex=(scanner.nextInt()-1);
                    if (clientId==2){
                        host.uploadFile("localhost", 10001, fileList.get(fileIndex));
                        System.out.println("Ukonczono przesylanie pliku");
                    }else if (clientId==3){
                        host.uploadFile("localhost", 10002, fileList.get(fileIndex));
                        System.out.println("Ukonczono przesylanie pliku");
                    }
                }else {
                    System.out.println("Nie ma takiego klienta");
                }
            }catch (InputMismatchException e){
                System.out.println("Wprowadzono niepoprawny argument");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Aby zakonczyc program napisz \"exit\" lub zamknij terminal. (zaczekaj az pojawi sie komunikat o skonczeniu wysylania pliku)");
        String exit=scanner.nextLine();
        while (!exit.equals("exit")){
            exit=scanner.nextLine();
        }
        scanner.close();
    }
}
