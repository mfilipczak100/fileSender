package fileSender.tasks;

import fileSender.Host;

import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Task1 {
    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        try {
            Host host=new Host(1,10000);
            System.out.println("Wybierz klienta ktory ma ci przeslac liste plikow. (Dostepne opcje: 2,3)");
            try {
                int number = scanner.nextInt();
                if (number==2||number==3) {
                    ArrayList<String> list=null;
                    if (number==2){
                        list=host.askForFileList("localhost",10001);
                    }else if (number == 3){
                        list=host.askForFileList("localhost",10002);
                    }
                    System.out.println("Pliki ktore posiada wybrany klient to:");
                    for (String file : list) {
                        String[] fileArr = file.split(":");
                        System.out.println("Plik: " + fileArr[0] + " - Suma kontrolna: " + fileArr[1]);
                    }
                }else{
                    System.out.println("Niepoprawny klient");
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
