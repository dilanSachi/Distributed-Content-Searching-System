import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class FileServer implements Runnable{

    private File file;

    private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;
    private String sendingFilename;

    public FileServer(String sendingFilename) {
        this.sendingFilename = sendingFilename;
    }

    @Override
    public void run() {
        genFile();
        BootstrapServer.echo(file.getHash());

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
        BigInteger random_big_int = big_int.multiply(BigInteger.valueOf(new Random().nextInt(5) + 1));
        file.setValue(random_big_int.toString());
    }

    private void sendFile(String path) throws Exception{
        int bytes = 0;
        BootstrapServer.echo("Sending file " + file.getFilename() + " has size - " + file.getSize());
        BootstrapServer.echo("Sending file " + file.getFilename() + " has hash - " + file.getHash());
//        BootstrapServer.echo("sending data " + file.getValue().substring(0, 200));
        InputStream stream = new ByteArrayInputStream(file.getValue().getBytes(StandardCharsets.UTF_8));

        dataOutputStream.writeUTF(file.getHash());
        dataOutputStream.flush();

        byte[] buffer = new byte[4*1024];
        while ((bytes = stream.read(buffer)) != -1){
            dataOutputStream.write(buffer,0, bytes);
            dataOutputStream.flush();
        }
        stream.close();
    }
}
