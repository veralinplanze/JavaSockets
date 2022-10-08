import java.io.*;
import java.net.Socket;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

class Client {
    static final short PKG_SZ = 1024;

    /* You have to specify an exact address and port to connect a socket */
    public Client(String addr, int port, String filePath) {
        try {
            Socket client = new Socket(addr, port);
            System.out.println("Starting file tranferring...");
            File file = new File(filePath);
            byte[] checksum;

            /* Buffered stream wrappers are for efficient use of stream-disk calls */
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            /* DataOutputStream let`s send primitive data types, useful for sending the size of incoming byte array */
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));

            /* Collecting info to send */
            checksum = getCheckSum(file);
            byte[] nameBytes = file.getName().getBytes();
            byte[] contentsBytes = new byte[(int) file.length()];
            in.read(contentsBytes);

            /* Sending info to server, size of byte array first, next the contents of it */
            out.writeInt(nameBytes.length);
            out.write(nameBytes);
            out.writeInt(contentsBytes.length);
            out.write(contentsBytes);
            out.writeInt(checksum.length);
            out.write(checksum);

            System.out.println("Transferring finished...");
            in.close();
            out.close();
            client.close();
            System.out.println("Client shutdown.");
        } catch (NoSuchAlgorithmException | IOException e) {
            System.out.println("Exception at client constructor");
            e.printStackTrace();
        }
    }

    private byte[] getCheckSum(File file) throws NoSuchAlgorithmException {
        System.out.println("Generating checksum...");
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md)) {
            while (dis.read() != -1) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] checksum = md.digest();
        System.out.println("Generated checksum: " + Arrays.toString(checksum));
        return checksum;
    }


}

public class sockclient {

    public static void main(String[] args) {
        Client client = new Client(args[0], Integer.parseInt(args[1]), args[2]);
    }
}
