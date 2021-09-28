package fileSender;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Host {

    private String directoryPath;
    private int id;
    private ServerSocket serverSocket;
    private volatile Set<UnfinishedDownload> unfinishedDownloads;

    public Host(int id,int port) throws IOException {
        this.id=id;
        directoryPath="D:\\TORrent_"+id+"\\";
        serverSocket=new ServerSocket(port);
        unfinishedDownloads=new HashSet<>();
    }

    public ArrayList<String> askForFileList(String address,int port) throws IOException {
        Socket socket=new Socket(address,port);
        PrintWriter printWriter=new PrintWriter(socket.getOutputStream());
        sendRequestForFileList(printWriter);
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ArrayList<String> lines=readRequest(bufferedReader);
        if (lines.get(0).equals("NoFiles")){
            lines.remove(0);
        }

        printWriter.close();
        bufferedReader.close();
        socket.close();
        return lines;
    }

    public void downloadFileFromMultipleHosts(String fileName,ArrayList<String> addresses,long fileLength){
        long partSize=fileLength/addresses.size();
        long bytesDownloaded=0;
        for (int i=0;i<addresses.size();i++){
            if (i!=addresses.size()-1){
                String [] addressArr=addresses.get(i).split(":");
                int port=Integer.parseInt(addressArr[1]);
                long bytesDownloaded2=bytesDownloaded;
                Runnable runnable=new Runnable() {
                    @Override
                    public void run() {
                        try {
                            downloadFile(addressArr[0],port,fileName,bytesDownloaded2,partSize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Thread thread=new Thread(runnable);
                thread.start();
                bytesDownloaded+=partSize;
            }else {
                String [] addressArr=addresses.get(i).split(":");
                int port=Integer.parseInt(addressArr[1]);
                long bytesDownloaded2=bytesDownloaded;
                Runnable runnable=new Runnable() {
                    @Override
                    public void run() {
                        try {
                            downloadFile(addressArr[0],port,fileName,bytesDownloaded2,-1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Thread thread=new Thread(runnable);
                thread.start();
                bytesDownloaded+=partSize;
            }
        }
    }

    public void downloadFile(String address,int port,String fileName,long bytesToSkip,long bytesToDownload) throws IOException {
        Socket socket=new Socket(address,port);
        PrintWriter printWriter=new PrintWriter(socket.getOutputStream());
        sendRequestForFileInfo(printWriter,fileName);
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ArrayList<String> lines=readRequest(bufferedReader);
        bufferedReader.close();
        printWriter.close();
        socket.close();
        if (lines.get(0).equals("Ok")){
            String checkSum=lines.get(1);
            long fileLength=Long.parseLong(lines.get(2));
            Socket fileSocket=new Socket(address,port);
            PrintWriter printWriter1=new PrintWriter(fileSocket.getOutputStream());
            if (bytesToDownload<0){
                bytesToDownload=fileLength-bytesToSkip;
            }

            UnfinishedDownload unfinishedDownload=new UnfinishedDownload(fileName,fileLength);
            synchronized (this) {
                if (unfinishedDownloads.contains(unfinishedDownload)) {
                    for (UnfinishedDownload u : unfinishedDownloads) {
                        if (u.equals(unfinishedDownload)) {
                            unfinishedDownload = u;
                        }
                    }
                } else {
                    unfinishedDownloads.add(unfinishedDownload);
                }
            }
            PastTransfer pastTransfer=new PastTransfer(bytesToSkip,bytesToDownload);
            unfinishedDownload.getCurrentTransfers().add(pastTransfer);


            sendRequestForFileDownload(printWriter1,fileName,bytesToSkip,bytesToDownload);
            receiveFile(fileSocket,fileName,bytesToSkip,pastTransfer);
            fileSocket.close();
            synchronized (this) {
                unfinishedDownload.setBytesDownloaded(unfinishedDownload.getBytesDownloaded() + bytesToDownload);
                if (unfinishedDownload.getBytesDownloaded()==unfinishedDownload.getFileLength()){
                    unfinishedDownloads.remove(unfinishedDownload);
                    File file=new File(directoryPath+fileName);
                    String receivedFileCheckSum=getCheckSum(file);
                    if (receivedFileCheckSum.equals(checkSum)){
                        System.out.println("Poprawnie pobrano plik - suma kontrolna zgadza sie");
                    }else{
                        System.out.println("niestety suma kontrolna nie zgadza sie");
                    }
                }
            }

        }

    }

    public void uploadFile(String address,int port,File file) throws IOException {
        Socket socket=new Socket(address,port);
        PrintWriter printWriter=new PrintWriter(socket.getOutputStream());
        sendRequestForFileUpload(printWriter,file.getName(),file.length(),getCheckSum(file));
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ArrayList<String> lines=readRequest(bufferedReader);
        Socket fileSocket=new Socket(lines.get(0),Integer.parseInt(lines.get(1)));
        sendFile(fileSocket,file.getName(),0,file.length());
        fileSocket.close();
        bufferedReader.close();
        printWriter.close();
        socket.close();
    }

    private void receiveFile(Socket socket,String fileName,long bytesToSkip,PastTransfer pastTransfer) throws IOException {
        byte [] buffer=new byte[1024];
        FileOutputStream fileOutputStream=new FileOutputStream(directoryPath+fileName,true);
        FileChannel fileChannel=fileOutputStream.getChannel();
        InputStream inputStream=socket.getInputStream();
        int count;
        while ((count=inputStream.read(buffer))>0){
            ByteBuffer byteBuffer=ByteBuffer.wrap(buffer);
            if (count<1024){
                byte[]buffer2=new byte[count];
                for (int i=0;i<count;i++){
                    buffer2[i]=buffer[i];
                }
                byteBuffer=ByteBuffer.wrap(buffer2);
            }
            fileChannel.write(byteBuffer,bytesToSkip);
            pastTransfer.setBytesRead(pastTransfer.getBytesRead()+count);
            bytesToSkip+=count;
        }

        inputStream.close();
        fileOutputStream.close();
    }

    private void receiveFile(Socket socket,String fileName,long bytesToSkip) throws IOException {
        byte [] buffer=new byte[1024];
        FileOutputStream fileOutputStream=new FileOutputStream(directoryPath+fileName,true);
        FileChannel fileChannel=fileOutputStream.getChannel();
        InputStream inputStream=socket.getInputStream();
        int count;
        while ((count=inputStream.read(buffer))>0){
            ByteBuffer byteBuffer=ByteBuffer.wrap(buffer);
            if (count<1024){
                byte[]buffer2=new byte[count];
                for (int i=0;i<count;i++){
                    buffer2[i]=buffer[i];
                }
                byteBuffer=ByteBuffer.wrap(buffer2);
            }
            fileChannel.write(byteBuffer,bytesToSkip);
            bytesToSkip+=count;
        }

        inputStream.close();
        fileOutputStream.close();
    }

    void sendRequestForFileDownload(PrintWriter printWriter,String fileName,long bytesToSkip,long bytesToDownload) throws IOException {
        printWriter.println("Download");
        printWriter.println(fileName);
        printWriter.println(bytesToSkip);
        printWriter.println(bytesToDownload);
        printWriter.println();
        printWriter.flush();
    }

    private void sendRequestForFileInfo(PrintWriter printWriter,String fileName) throws IOException {
        printWriter.println("FileInfo");
        printWriter.println(fileName);
        printWriter.println();
        printWriter.flush();
    }

    private void sendRequestForFileList(PrintWriter printWriter){
        printWriter.println("FileList");
        printWriter.println();
        printWriter.flush();
    }

    private void sendRequestForFileUpload(PrintWriter printWriter,String fileName,long fileLength, String checkSum){
        printWriter.println("Upload");
        printWriter.println(fileName);
        printWriter.println(fileLength);
        printWriter.println(checkSum);
        printWriter.println();
        printWriter.flush();
    }

    private String renameFileIfExists(String fileName){
        String resultFileName=fileName;
        String path=directoryPath+fileName;
        if (new File(path).exists()){
            int number=1;
            String tempPath=path+"("+number+")";
            while (new File(path).exists()){
                number++;
                tempPath=path+"("+number+")";
            }
            path=tempPath;
            resultFileName=new File(path).getName();
        }

        return resultFileName;
    }

    public void resumeDownload(String fileName,ArrayList<String> addresses){
        UnfinishedDownload unfinishedDownload=null;
        for (UnfinishedDownload u:unfinishedDownloads){
            if (fileName.equals(u.getFileName())){
                unfinishedDownload=u;
            }
        }
        if (unfinishedDownload != null) {
            ArrayList<String> uncompletedSectors=getUnwrittenFileSectors(unfinishedDownload);
            /*for (String s:uncompletedSectors){
                System.out.println(s);
            }*/

            int index=0;
            for (String sector : uncompletedSectors){
                String [] sectorArr=sector.split(":");
                long bytesToSkip=Long.parseLong(sectorArr[0]);
                long bytesToRead=Long.parseLong(sectorArr[1]);
                String [] addressArr=addresses.get(index).split(":");
                String address=addressArr[0];
                int port=Integer.parseInt(addressArr[1]);
                Runnable runnable=new Runnable() {
                    @Override
                    public void run() {
                        try {
                            downloadFile(address,port,fileName,bytesToSkip,bytesToRead);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Thread thread=new Thread(runnable);
                thread.start();
                index++;
                if (index==addresses.size()){
                    index=0;
                }

            }
        }

    }

    private ArrayList<String> getUnwrittenFileSectors(UnfinishedDownload unfinishedDownload){
        ArrayList<PastTransfer> pastTransfers=unfinishedDownload.getCurrentTransfers();
        ArrayList<String> uncompletedSectors=new ArrayList<>();
        ArrayList<PastTransfer> transfersToCheck=new ArrayList<>();
        for (PastTransfer pastTransfer:pastTransfers){
            transfersToCheck.add(pastTransfer);
        }
        long bytesToCheck=0;

        while (transfersToCheck.size()!=0){
            long min=transfersToCheck.get(0).getBytesSkipped();
            long bytesToDownload=transfersToCheck.get(0).getBytesRead();
            int index=0;

            for (int i=0;i<transfersToCheck.size();i++){
                if (transfersToCheck.get(i).getBytesSkipped()<min){
                    min = transfersToCheck.get(i).getBytesSkipped();
                    bytesToDownload=transfersToCheck.get(i).getBytesRead();
                    index = i;

                }else if (transfersToCheck.get(i).getBytesSkipped()==min&&transfersToCheck.get(i).getBytesRead()>bytesToDownload){
                    min = transfersToCheck.get(i).getBytesSkipped();
                    bytesToDownload=transfersToCheck.get(i).getBytesRead();
                    index = i;
                }
            }
            if (bytesToCheck==min){
                bytesToCheck=min+bytesToDownload;
                for (int i=0;i<transfersToCheck.size();i++){
                    if (min==transfersToCheck.get(i).getBytesSkipped()){
                        transfersToCheck.remove(i);
                        i=-1;
                    }
                }
            }else{
                long bytesToRead=min-bytesToCheck;
                uncompletedSectors.add(bytesToCheck+":"+bytesToRead);
                bytesToCheck=min;
            }
        }
        if (bytesToCheck!=unfinishedDownload.getFileLength()){
            uncompletedSectors.add(bytesToCheck+":"+(unfinishedDownload.getFileLength()-bytesToCheck));
        }
        return uncompletedSectors;
    }

    //////////////////////////////////////////////////////////////////////////////
    public void listen() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            Runnable runnable=new Runnable() {
                @Override
                public void run() {
                    try {
                        hostService(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Thread thread=new Thread(runnable);
            thread.start();
        }
    }

    private void hostService(Socket socket) throws IOException {
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ArrayList<String> lines=readRequest(bufferedReader);
        for (int i=0;i<lines.size();i++){
            System.out.println(lines.get(i));
        }
        System.out.println();
        System.out.println();
        if (lines.get(0).equals("FileInfo")){
            sendResponseWithFileInfo(socket,lines);
        }else if (lines.get(0).equals("Download")){
            sendFile(socket,lines.get(1),Long.parseLong(lines.get(2)),Long.parseLong(lines.get(3)));
        }else if (lines.get(0).equals("FileList")){
            sendFileList(socket);
        }else if (lines.get(0).equals("Upload")){
            getFileFromUpload(socket,lines);
        }
        bufferedReader.close();
        socket.close();
    }

    private void getFileFromUpload(Socket socket,ArrayList<String> lines) throws IOException {
        ServerSocket fileServerSocket=new ServerSocket(0);
        String fileName=lines.get(1);
        long fileLength=Long.parseLong(lines.get(2));
        String checkSum=lines.get(3);
        PrintWriter printWriter=new PrintWriter(socket.getOutputStream());
        printWriter.println(fileServerSocket.getInetAddress().getHostAddress());
        printWriter.println(fileServerSocket.getLocalPort());
        printWriter.println();
        printWriter.flush();
        Socket fileSocket=fileServerSocket.accept();
        receiveFile(fileSocket,fileName,0);
        fileSocket.close();
        File file=new File(directoryPath+fileName);
        String recievedFileCheckSum=getCheckSum(file);
        if (checkSum.equals(recievedFileCheckSum)){
            System.out.println("Poprawnie odebrano plik - suma kontrolna zgadza sie");
        }else{
            System.out.println("Niestety suma kontrolna nie zgadza sie");
        }
        System.out.println();

        printWriter.close();
    }

    private void sendFileList(Socket socket) throws IOException {
        PrintWriter printWriter=new PrintWriter(socket.getOutputStream());
        File directory=new File(directoryPath);
        File[] files = directory.listFiles();
        if (files.length > 0) {
            for (File f:files){
                printWriter.println(f.getName()+":"+getCheckSum(f)+":"+f.length());
            }
            printWriter.println();
        } else {
            printWriter.println("NoFiles");
            printWriter.println();
        }
        printWriter.flush();
        printWriter.close();
    }

    private void sendFile(Socket socket,String fileName,long bytesToSkip,long bytesToRead) throws IOException {
        File fileToSend=new File(directoryPath+fileName);
        byte [] buffer=new byte[1024];
        FileInputStream fileInputStream=new FileInputStream(fileToSend);
        OutputStream outputStream=socket.getOutputStream();

        if (bytesToSkip>0){
            fileInputStream.skip(bytesToSkip);
        }
        long bytesRead=0;
        while (bytesRead<bytesToRead){
            if (bytesToRead-bytesRead<1024){
                buffer=new byte[(int)(bytesToRead-bytesRead)];
            }
            int newBytes=fileInputStream.read(buffer);
            outputStream.write(buffer,0,newBytes);
            bytesRead+=newBytes;
        }

        fileInputStream.close();
        outputStream.close();
    }

    private ArrayList<String> readRequest(BufferedReader bufferedReader) throws IOException {
        ArrayList <String> lines=new ArrayList<>();
        String line=bufferedReader.readLine();
        while (line!=null&&!line.equals("")){
            lines.add(line);
            line=bufferedReader.readLine();
        }
        return lines;
    }

    private void sendResponseWithFileInfo(Socket socket,ArrayList<String> lines) throws IOException {
        String fileName=lines.get(1);
        PrintWriter printWriter=new PrintWriter(socket.getOutputStream());
        File file=new File(directoryPath+fileName);
        if (file.exists()){
            printWriter.println("Ok");
            printWriter.println(getCheckSum(file));
            printWriter.println(file.length());
            printWriter.println();
            printWriter.flush();
        }else{
            printWriter.println("No");
            printWriter.println();
            printWriter.flush();
        }
        printWriter.close();
    }

    private String getCheckSum(File file){
        String checkSum=null;
        try {
            MessageDigest md=MessageDigest.getInstance("MD5");
            byte [] bytes= Files.readAllBytes(Paths.get(file.toURI()));
            md.update(bytes);
            checkSum=new BigInteger(1,md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return checkSum;
    }

    public Set<UnfinishedDownload> getUnfinishedDownloads() {
        return unfinishedDownloads;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public ArrayList<File> getFileList(){
        File directory=new File(directoryPath);
        File [] list=directory.listFiles();
        ArrayList<File> fileList=new ArrayList<>();
        if (list.length>0){
            for (File f:list){
                fileList.add(f);
            }
        }
        return fileList;
    }
}
