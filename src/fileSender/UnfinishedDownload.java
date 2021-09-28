package fileSender;

import java.util.ArrayList;
import java.util.Objects;

public class UnfinishedDownload {

    private String fileName;
    volatile private long bytesDownloaded=0;
    private long fileLength;
    volatile private ArrayList<PastTransfer> pastTransfers=new ArrayList<>();

    UnfinishedDownload(String fileName,long fileLength){
        this.fileName=fileName;
        this.fileLength=fileLength;
    }

    public String getFileName() {
        return fileName;
    }

    public ArrayList<PastTransfer> getCurrentTransfers() {
        return pastTransfers;
    }

    public long getBytesDownloaded() {
        return bytesDownloaded;
    }

    public void setBytesDownloaded(long bytesDownloaded) {
        this.bytesDownloaded = bytesDownloaded;
    }

    public long getFileLength() {
        return fileLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnfinishedDownload that = (UnfinishedDownload) o;
        return fileLength == that.fileLength &&
                Objects.equals(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fileLength);
    }

    @Override
    public String toString() {
        return "UnfinishedDownload{" +
                "fileName='" + fileName + '\'' +
                ", bytesDownloaded=" + bytesDownloaded +
                ", fileLength=" + fileLength +
                ", pastTransfers=" + pastTransfers +
                '}';
    }
}
