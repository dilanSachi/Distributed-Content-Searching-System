import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;

public class FileServer implements Runnable{

    private File file;

    private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;
    private Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        genFile();
        echo(file.getHash());

        try (Socket socket = new Socket("localhost",5000)) {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            sendFile("path/to/file1.pdf");

            dataInputStream.close();
            dataInputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void genFile() {
        BigInteger big_int = new BigInteger( 302400, new Random()); // 83886080   30240000
        file.setValue(big_int.toString());
    }

    private void sendFile(String path) throws Exception{
        int bytes = 0;

        InputStream stream = new ByteArrayInputStream(file.getValue().getBytes(StandardCharsets.UTF_8));

        // send file hash
        dataOutputStream.writeUTF(file.getHash());

        // break file into chunks
        byte[] buffer = new byte[4*1024];
        while ((bytes = stream.read(buffer)) != -1){
            dataOutputStream.write(buffer,0, bytes);
            dataOutputStream.flush();
        }
        stream.close();
    }

    private static void echo(String msg) {
        System.out.println(msg);
    }
}
