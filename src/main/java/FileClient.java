import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileClient implements Runnable{

    private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;
    private String receivingFilename;
    private Logger logger;

    public FileClient(String receivingFilename, Logger logger) {
        this.receivingFilename = receivingFilename;
        this.logger = logger;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("listening to port:5000");
            Socket clientSocket = serverSocket.accept();
            System.out.println(clientSocket+" connected.");
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

            receiveFile("NewFile1.txt");

            dataInputStream.close();
            dataOutputStream.close();
            clientSocket.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void receiveFile(String fileName) throws Exception{
        int bytes = 0;
        ByteArrayOutputStream received_bytes = new ByteArrayOutputStream();
        String received_hash = dataInputStream.readUTF();
        logger.logMsg("Received hash " + received_hash);

        byte[] buffer = new byte[4*1024];
        while ((bytes = dataInputStream.read(buffer, 0, buffer.length)) != -1) {
            received_bytes.write(buffer, 0, bytes);
        }
        File received_file = new File(fileName);
        received_file.setValue(received_bytes.toString("UTF-8"));

        logger.logMsg("Received file " + received_file.getFilename() + " has size - " + received_file.getSize());
        logger.logMsg("Received file " + received_file.getFilename() + " has hash - " + received_file.getHash());
//        logger.logMsg("Received data " + received_file.getValue().substring(0, 200));
        if (received_file.validateFileWithHash(received_hash)) {
            logger.logMsg("Received file is not corrupt...");
        } else {
            logger.logMsg("Hashes does not match. Files are probably corrupt...");
        }

    }
}
