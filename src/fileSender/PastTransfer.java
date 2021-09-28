package fileSender;


public class PastTransfer {

    private long bytesSkipped;
    private long bytesRead=0;
    private long bytesToRead;

    public PastTransfer(long bytesSkipped, long bytesToRead) {
        this.bytesSkipped = bytesSkipped;
        this.bytesToRead = bytesToRead;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    public long getBytesSkipped() {
        return bytesSkipped;
    }

    @Override
    public String toString() {
        return "PastTransfer{" +
                "bytesSkipped=" + bytesSkipped +
                ", bytesRead=" + bytesRead +
                ", bytesToRead=" + bytesToRead +
                '}';
    }
}
