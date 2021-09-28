package fileSender.tasks;

import fileSender.Host;

import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Task2 {
    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        try {
            Host host=new Host(1,10000);
            System.out.println("Od ktorego klienta chcesz pobrac plik? (dostepne opcje 2,3)");
            try{
                int clientId=scanner.nextInt();
                if (clientId==2){
                    ArrayList<String> fileList=host.askForFileList("localhost",10001);
                    System.out.println("Ktory plik chcesz pobrac?");
                    for (int i=0;i<fileList.size();i++){
                        String [] fileArr=fileList.get(i).split(":");
                        System.out.println((i+1)+" - "+fileArr[0]);
                    }
                    int fileIndex=scanner.nextInt()-1;
                    if (fileIndex>=0&&fileIndex<fileList.size()){
                        String [] fileArr=fileList.get(fileIndex).split(":");
                        host.downloadFile("localhost",10001,fileArr[0],0,-1);
                    }
                }else if (clientId==3){
                    ArrayList<String> fileList=host.askForFileList("localhost",10002);
                    System.out.println("Ktory plik chcesz pobrac?");
                    for (int i=0;i<fileList.size();i++){
                        String [] fileArr=fileList.get(i).split(":");
                        System.out.println((i+1)+" - "+fileArr[0]);
                    }
                    int fileIndex=scanner.nextInt()-1;
                    if (fileIndex>=0&&fileIndex<fileList.size()){
                        String [] fileArr=fileList.get(fileIndex).split(":");
                        host.downloadFile("localhost",10002,fileArr[0],0,-1);
                    }
                }else{
                    System.out.println("Nie ma takiego klienta");
                }
            }catch (InputMismatchException e){
                System.out.println("Wprowadzono niepoprawna wartosc");
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
