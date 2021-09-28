package fileSender.tasks;

import fileSender.Host;

import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Task4 {
    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        try {
            Host host=new Host(1,10000);
            System.out.println("Ten skrypt zatrzyma pobieranie pliku w połowie. Użytkownik będzie musiał wznowić je ręcznie.");
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
                        long fileLength=Long.parseLong(fileArr[2]);
                        host.downloadFile("localhost",10001,fileArr[0],0,fileLength/2);
                        System.out.println("Sciagnieto polowe pliku");
                        System.out.println("Aby kontynuowac pobieranie wprowadz \"resume\"");
                        String s=scanner.nextLine();
                        while (!s.equals("resume")){
                            s=scanner.nextLine();
                        }
                        ArrayList<String> addresses=new ArrayList<>();
                        addresses.add("localhost:10001");
                        host.resumeDownload(fileArr[0],addresses);
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
                        long fileLength=Long.parseLong(fileArr[2]);
                        host.downloadFile("localhost",10002,fileArr[0],0,fileLength/2);
                        System.out.println("Sciagnieto polowe pliku");
                        System.out.println("Aby kontynuowac pobieranie napisz \"resume\"");
                        String s=scanner.nextLine();
                        while (!s.equals("resume")){
                            s=scanner.nextLine();
                        }
                        ArrayList<String> addresses=new ArrayList<>();
                        addresses.add("localhost:10002");
                        host.resumeDownload(fileArr[0],addresses);
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
