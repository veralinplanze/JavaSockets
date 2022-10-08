import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

class Server extends Thread {
    private ServerSocket server;
    private Socket client;
    private int timeout;
    private final short PKG_SZ = 512;

    public void setTimeout(int timeout) {
        this.timeout = timeout * 1000;
    }

    public int getTimeout() {
        return timeout / 1000;
    }

    public boolean isClientConnected() {
        return client != null;
    }

    public Server() {
        this(8888, 10);
    }

    public Server(int port) {
        this(port, 10);
    }

    public Server(int port, int timeout) {
        try {
            /* Initializing server socket and binding it to specified port */
            server = new ServerSocket(port);
            System.out.printf("Server Socket has been initialized...\n Bound to port %d...\n", port);
            this.setTimeout(timeout);
            System.out.printf("Server timeout is set to %d seconds.\n", this.getTimeout());
            server.setSoTimeout(this.timeout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            System.out.println("Waiting for client to connect...");
            client = server.accept();

            System.out.printf("%s has connected to the server... \n", client.getInetAddress());

            /* Creating an input stream */
            DataInputStream in = new DataInputStream(new BufferedInputStream(client.getInputStream()));

            /* Receiving name, contents and checksum of a file */
            byte[] nameBytes = new byte[in.readInt()];
            in.readFully(nameBytes, 0, nameBytes.length);
            byte[] contentBytes = new byte[in.readInt()];
            in.readFully(contentBytes, 0, contentBytes.length);
            byte[] checksumBytes = new byte[in.readInt()];
            in.readFully(checksumBytes, 0, checksumBytes.length);

            /* Creating an output stream and new file instance for writing a received file */
            File file = new File(new String(nameBytes));
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(contentBytes);

            /* Comparing received checksum with generated one */
            boolean check = compareCheckSum(checksumBytes, file);
            if(!check) System.out.println("Received data is corrupted according to the checksum");
            else System.out.println("Data received successfully...");

            /* Freeing sockets */
            client.close();
            server.close();
            in.close();
            out.close();
            System.out.println("Server shutdown.");

        } catch (SocketTimeoutException e) {
            System.out.println("Server timeout.");
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public boolean compareCheckSum(byte[] checkSumReceived, File file) throws NoSuchAlgorithmException {
        /* Using MessageDigest to access methods to generate MD5 file checksum */
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md)) {
            while (dis.read() != -1) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] checkSumCalculated = md.digest();
        System.out.println("Local  CheckSum: " + Arrays.toString(checkSumCalculated));
        System.out.println("Remote CheckSum: " + Arrays.toString(checkSumReceived));
        /* Comparing checksums as arrays */
        return Arrays.equals(checkSumCalculated, checkSumReceived);
    }
}

public class sockserver {
    public static void main(String[] args) {
        Server server = null;
        if (args.length > 2) {
            System.out.println("Wrong arguments\nUSAGE: sockserver [port [timeout]]");
            return;
        }
        if (args.length == 0) {
            server = new Server();
        }
        if (args.length == 1) {
            try {
                server = new Server(Integer.parseInt(args[0]));
            } catch (Exception e) {
                System.out.println("Incorrect or already occupied port number\nUSAGE: sockserver [port [timeout]]");
            }
        }
        if (args.length == 2) {
            server = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        }

        server.start();

        /* Useless cycle while server timeout */
        try {
            for (int i = server.getTimeout(); !server.isClientConnected() && i > 0; --i) {
                Thread.sleep(1000);
                System.out.printf("Timeout in %d\n", i);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
