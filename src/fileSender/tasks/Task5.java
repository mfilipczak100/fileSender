package fileSender.tasks;

import fileSender.Host;

import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Task5 {
    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        try {
            Host host=new Host(1,10000);
            ArrayList<String> fileList2=host.askForFileList("localhost",10001);
            ArrayList<String> fileList3=host.askForFileList("localhost",10002);
            ArrayList<String> resultFileList=new ArrayList<>();
            for (int i=0;i<fileList2.size();i++){
                for (int j=0;j<fileList3.size();j++){
                    if (fileList2.get(i).equals(fileList3.get(j))){
                        resultFileList.add(fileList2.get(i));
                    }
                }
            }

            if (resultFileList.size()>0){
                System.out.println("Na liscie znajduja sie pliki ktore posiada zarowno klient 2 jak i klient 3.");
                System.out.println("Ktory plik chcesz pobrac?");
                for (int i=0;i<resultFileList.size();i++){
                    String [] fileArr=resultFileList.get(i).split(":");
                    System.out.println((i+1)+" - "+fileArr[0]);
                }
                try{
                    int index=scanner.nextInt()-1;
                    if (index>=0&&index<resultFileList.size()){
                        String [] fileArr=resultFileList.get(index).split(":");
                        ArrayList<String> addresses=new ArrayList<>();
                        addresses.add("localhost:10001");
                        addresses.add("localhost:10002");
                        host.downloadFileFromMultipleHosts(fileArr[0],addresses,Long.parseLong(fileArr[2]));
                    }else{
                        System.out.println("Wprowadzono niepoprawny numer");
                    }
                }catch (InputMismatchException e){
                    System.out.println("Wprowadzono niepoprawna liczbe");
                }
            }else{
                System.out.println("Klienci 2 i 3 nie posiadaja zadnych takich samych plikow.");
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
