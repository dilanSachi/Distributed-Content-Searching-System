import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Node implements Runnable {

    private String ip_address;
    private int port;
    private String username;
    private Logger logger;

    private static String[] availableFiles = new String[]{"Adventures of Tintin", "Jack and Jill", "Glee", "The Vampire Diarie",
            "King Arthur", "Windows XP", "Harry Potter", "Kung Fu Panda", "Lady Gaga", "Twilight", "Windows 8",
            "Mission Impossible", "Turn Up The Music", "Super Mario", "American Pickers", "Microsoft Office 2010",
            "Happy Feet", "Modern Family", "American Idol", "Hacking for Dummies"};

    private ArrayList<Neighbour> neighbours = new ArrayList<Neighbour>();
    private HashMap<String, File> my_files;

    public Node(String ip_address, int port, String username, Logger logger) {
        this.ip_address = ip_address;
        this.port = port;
        this.username = username;
        this.logger = logger;
    }

    public void run() {
        DatagramSocket sock = null;
        String s;
        HashMap<String, HashMap<String, Integer>> prevSearchMetadata = new HashMap<>();

        try {
            sock = new DatagramSocket(this.port);
            reg_with_BS(sock);
            logger.logMsg("Node started at " + this.port + ". Waiting for incoming data...");

            while(true) {
                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                sock.receive(incoming);

                byte[] data = incoming.getData();
                s = formatInputString(new String(data, 0, incoming.getLength()));
                StringTokenizer st = new StringTokenizer(s, " ");

                String length = st.nextToken();
                String command = st.nextToken();

                if (command.equals("REGOK")) {
                    int node_count = Integer.parseInt(st.nextToken());
                    if (node_count == 0) {
                        logger.logMsg("Succefully registered with BS... No nodes available...");
                    } else if (node_count == 9999) {
                        logger.logMsg("Failed. There is an error in the request command...");
                    } else if (node_count == 9998) {
                        logger.logMsg("Already registered. Unregistering and trying to re-register...");
                        unreg_with_BS(sock);
                        logger.logMsg("Unregistering with neighbors...");
                        for (Neighbour neighbour : neighbours) {
                            unreg_with_neighbor(neighbour, sock);
                        }
                        reg_with_BS(sock);
                    } else if (node_count == 9997) {
                        logger.logMsg("Failed, registered to another user, try a different IP and port..");
                    } else if (node_count == 9996) {
                        logger.logMsg("Failed, can’t register. BS full...");
                    } else if (node_count > 0) {
                        ArrayList<Neighbour> temp_neighbors = new ArrayList<Neighbour>();
                        for (int i = 0; i < node_count; i ++) {
                            String neighbor_ip = st.nextToken();
                            int neighbor_port = Integer.parseInt(st.nextToken());
                            temp_neighbors.add(new Neighbour(neighbor_ip, neighbor_port, ""));
                        }
                        Random my_random = new Random();
                        if (temp_neighbors.size() > 0) {
                            int rand_int = my_random.nextInt(temp_neighbors.size());
                            neighbours.add(temp_neighbors.get(rand_int));
                            temp_neighbors.remove(rand_int);
                            reg_with_neighbor(neighbours.get(0), sock);
                        }
                        if (temp_neighbors.size() > 0) {
                            int rand_int = my_random.nextInt(temp_neighbors.size());
                            neighbours.add(temp_neighbors.get(rand_int));
                            temp_neighbors.remove(rand_int);
                            reg_with_neighbor(neighbours.get(1), sock);
                        }
                    } else {
                        logger.logMsg("Unknown response code...");
                    }
                } else if (command.equals("UNROK")) {
                    int status = Integer.parseInt(st.nextToken());
                    if (status == 0) {
                        logger.logMsg("Successfully unregistered with the BS...");
                    } else if (status == 9999) {
                        logger.logMsg("Error while unregistering. IP and port may not be in the registry or command is incorrect...");
                    }
                } else if (command.equals("JOIN")) {
                    String neighbor_ip = st.nextToken();
                    int neighbor_port = Integer.parseInt(st.nextToken());
                    int status = 0;
                    for (Neighbour neighbour : neighbours) {
                        if (neighbour.getIp().equals(neighbor_ip) && neighbour.getPort() == neighbor_port) {
                            status = 9999;
                            break;
                        }
                    }
                    if (status == 0) {
                        neighbours.add(new Neighbour(neighbor_ip, neighbor_port, ""));
                    }
                    String response = "JOINOK " + status;
                    response = String.format("%04d", response.length() + 5) + " " + response;
                    try {
                        send_msg_via_socket(sock, InetAddress.getByName(neighbor_ip), neighbor_port, response);
                    } catch (UnknownHostException e) {
                        logger.logMsg(e.toString());
                    }
                } else if (command.equals("JOINOK")) {
                    int status = Integer.parseInt(st.nextToken());
                    if (status == 0) {
                        logger.logMsg("Successfully joined with node");
                    } else if (status == 9999) {
                        logger.logMsg("Error while adding new node to routing table...");
                    } else {
                        logger.logMsg("Error when joining. Status code - " + status);
                    }
                } else if (command.equals("LEAVE")) {
                    String neighbor_ip = st.nextToken();
                    int neighbor_port = Integer.parseInt(st.nextToken());
                    for (int i = 0; i < neighbours.size(); i++) {
                        if (neighbours.get(i).getIp().equals(neighbor_ip) && neighbours.get(i).getPort() == neighbor_port) {
                            neighbours.remove(i);
                            break;
                        }
                    }
                } else if (command.equals("LEAVEOK")) {
                    int status = Integer.parseInt(st.nextToken());
                    if (status == 0) {
                        logger.logMsg("Successfully left the connection with node " + incoming.getAddress().getHostAddress() + " : " + incoming.getPort());
                    } else if (status == 9999) {
                        logger.logMsg("Error while leaving the connection with node " + incoming.getAddress().getHostAddress() + " : " + incoming.getPort());
                    } else {
                        logger.logMsg("Error when leaving. Status code - " + status);
                    }
                } else if (command.equals("SER")) {
                    String requester_ip = st.nextToken();
                    int requester_port = Integer.parseInt(st.nextToken());
                    String query = st.nextToken();
                    int hops = Integer.parseInt(st.nextToken());
                    ArrayList<String> search_result = find_file(query.replace("$", " "));
                    try {
                        if (hops > 0) {
                            Neighbour requester = new Neighbour(requester_ip, requester_port, "");
                            for (Neighbour neighbour : neighbours) {
                                if (requester.getPort() != neighbour.getPort() || !requester.getIp().equals(neighbour.getIp())) {
                                    search_file_in_neighbor(neighbour, requester, query, hops - 1, sock);
                                }
                            }
                        }
                        if (search_result.size() == 0 && hops == 0) {
                            String response = "SEROK " + 0;
                            response = String.format("%04d", response.length() + 5) + " " + response;
                            try {
                                send_msg_via_socket(sock, InetAddress.getByName(requester_ip), requester_port, response);
                            } catch (UnknownHostException e) {
                                logger.logMsg(e.toString());
                            }
                        } else if (search_result.size() > 0) {
                            StringBuilder response = new StringBuilder("SEROK " + search_result.size() + " " + ip_address + " " + port + " " + (hops - 1));
                            String log = "Found file";
                            for (String value : search_result) {
                                response.append(" ").append(value);
                                log = log + " " + value;
                            }
                            response.insert(0, String.format("%04d", response.length() + 5) + " ");
                            logger.logMsg(log + " with " + hops + " remaining hops...");
                            try {
                                send_msg_via_socket(sock, InetAddress.getByName(requester_ip), requester_port, response.toString());
                            } catch (UnknownHostException e) {
                                logger.logMsg(e.toString());
                            }
                        }
                    } catch (Exception e) {
                        String response = "SEROK " + 9998;
                        response = String.format("%04d", response.length() + 5) + " " + response;
                        try {
                            send_msg_via_socket(sock, InetAddress.getByName(requester_ip), requester_port, response);
                        } catch (UnknownHostException ee) {
                            logger.logMsg(ee.toString());
                        }
                    }
                } else if (command.equals("SEROK")) {
                    int no_files = Integer.parseInt(st.nextToken());
                    if (no_files == 0) {
                        logger.logMsg("No files found in node " + incoming.getAddress().getHostAddress() + " : " + incoming.getPort());
                    } else if (no_files == 9999) {
                        logger.logMsg("Failure due to node " + incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " unreachable...");
                    } else if (no_files == 9998){
                        logger.logMsg("Error " + no_files + "in node " + incoming.getAddress().getHostAddress() + " : " + incoming.getPort());
                    } else if (no_files > 0) {
                        String file_owner_ip = st.nextToken();
                        int file_owner_port = Integer.parseInt(st.nextToken());
                        int hops = Integer.parseInt(st.nextToken());
                        String foundFilename;
                        for (int i = 0; i < no_files; i ++) {
                            foundFilename = st.nextToken();
                            if (prevSearchMetadata.containsKey(foundFilename)
                                    && prevSearchMetadata.get(foundFilename).containsKey(file_owner_ip) &&
                                prevSearchMetadata.get(foundFilename).get(file_owner_ip) == file_owner_port) {
                            } else {
                                prevSearchMetadata = new HashMap<>();
                                HashMap<String, Integer> tempMap = new HashMap<>();
                                tempMap.put(file_owner_ip, file_owner_port);
                                prevSearchMetadata.put(foundFilename, tempMap);
                                logger.logMsg("Found file " + foundFilename + " in node " + file_owner_ip + " : " + file_owner_port);
                            }
                        }
                    }
                } else if (command.equals("STARTSER")) {    // query --> xxxx STARTSER Avengers 5
                    String query = st.nextToken().replace("$", " ");
                    int hops = Integer.parseInt(st.nextToken());
                    ArrayList<String> search_result = find_file(query);
                    if (hops > 0) {
                        Neighbour requester = new Neighbour(ip_address, port, "");
                        for (Neighbour neighbour : neighbours) {
                            logger.logMsg(neighbour.getIp() + " " + neighbour.getPort());
                            search_file_in_neighbor(neighbour, requester, query, hops - 1, sock);
                        }
                    }
                    if (search_result.size() > 0) {
                        for (String value : search_result) {
                            logger.logMsg("Found file " + value + " in current node...");
                        }
                    }
                } else if (command.equals("SHOWFILES")) {
                    StringBuilder response = new StringBuilder("Node " + ip_address + " : " + port + " has files -");
                    for (String filename: my_files.keySet()) {
                        response.append(" ").append(filename);
                    }
                    logger.logMsg(response.toString());
                    send_msg_via_socket(sock, InetAddress.getByName(incoming.getAddress().getHostAddress()), incoming.getPort(), response.toString());
                } else if (command.equals("SHOWRTABLE")) {
                    StringBuilder response = new StringBuilder("Node " + ip_address + " : " + port + " has neighbors -");
                    for (Neighbour neighbour : neighbours) {
                        response.append(" ").append(neighbour.getIp()).append(": ").append(neighbour.getPort());
                    }
                    logger.logMsg(response.toString());
                    send_msg_via_socket(sock, InetAddress.getByName(incoming.getAddress().getHostAddress()), incoming.getPort(), response.toString());
                } else if (command.equals("DLOADR")) {
                    String owner_ip = st.nextToken();
                    int owner_port = Integer.parseInt(st.nextToken());
                    String filename = st.nextToken().replace("$", " ");
                    send_download_request(owner_ip, owner_port, filename, sock);
                } else if (command.equals("DLOAD")) {
                    String requester_ip = st.nextToken();
                    int requester_port = Integer.parseInt(st.nextToken());
                    String requested_filename = st.nextToken().replace("$", " ");
                    handle_file_exchange(requester_ip, requester_port, requested_filename);
                } else {
                    send_msg_via_socket(sock, InetAddress.getByName(incoming.getAddress().getHostAddress()), incoming.getPort(), "Not a valid command");
                }
            }
        } catch(IOException e) {
            System.err.println("IOException " + e);
        }
    }

    private static String formatInputString(String query) {
        query = query.replace("\n", "");
        if (query.indexOf('"') != query.lastIndexOf('"')) {
            String[] words = query.split("\"");
            words[1] = words[1].replace(" ", "$");
            String returnStr = "";
            for (String word: words) {
                returnStr = returnStr + word.replace("\"", "");
            }
            return returnStr;
        }
        return query;
    }

    private void reg_with_BS(DatagramSocket socket) {
        try {
            String request = "REG " + this.ip_address + " " + this.port + " " + this.username;
            request = String.format("%04d", request.length() + 5) + " " + request;
            InetAddress bs_address = InetAddress.getLocalHost();
            send_msg_via_socket(socket, bs_address, 55555, request);
        } catch (Exception e) {
            logger.logMsg(e.toString());
        }
    }

    private void unreg_with_BS(DatagramSocket socket) {
        try {
            String request = "UNREG " + this.ip_address + " " + this.port + " " + this.username;
            request = String.format("%04d", request.length() + 5) + " " + request;
            InetAddress bs_address = InetAddress.getLocalHost();
            send_msg_via_socket(socket, bs_address, 55555, request);
        } catch (Exception e) {
            logger.logMsg(e.toString());
        }
    }

    private void reg_with_neighbor(Neighbour neighbour, DatagramSocket socket) {
        try {
            String request = "JOIN " + this.ip_address + " " + this.port;
            request = String.format("%04d", request.length() + 5) + " " + request;
            InetAddress bs_address = InetAddress.getByName(neighbour.getIp());
            send_msg_via_socket(socket, bs_address, neighbour.getPort(), request);
        } catch (Exception e) {
            logger.logMsg(e.toString());
        }
    }

    private void unreg_with_neighbor(Neighbour neighbour, DatagramSocket socket) {
        try {
            String request = "LEAVE " + this.ip_address + " " + this.port;
            request = String.format("%04d", request.length() + 5) + " " + request;
            InetAddress bs_address = InetAddress.getByName(neighbour.getIp());
            send_msg_via_socket(socket, bs_address, neighbour.getPort(), request);
        } catch (Exception e) {
            logger.logMsg(e.toString());
        }
    }

    private ArrayList<String> find_file(String query) {
        ArrayList<String> found_files = new ArrayList<>();
        query = query.toLowerCase(Locale.ROOT);
        for (String filename : my_files.keySet()) {
            int location = filename.toLowerCase(Locale.ROOT).indexOf(query);
            if (query.length() > filename.length()) {
                continue;
            }
            if (location == 0 && (location + query.length()) == filename.length()) {
                found_files.add(filename.replace(" ", "_"));
                logger.logMsg("found " + filename);
            } else if (location == 0) {
                if (filename.substring(location + query.length(), location + query.length() + 1).equals(" ")) {
                    found_files.add(filename.replace(" ", "_"));
                    logger.logMsg("found " + filename);
                }
            } else if (location + query.length() == filename.length()) {
                if (filename.substring(location - 1, location).equals(" ")) {
                    found_files.add(filename.replace(" ", "_"));
                    logger.logMsg("found " + filename);
                }
            } else if (location > 0) {
                if (filename.substring(location - 1, location).equals(" ") && filename.substring(location + query.length(), location + query.length() + 1).equals(" ")) {
                    found_files.add(filename.replace(" ", "_"));
                    logger.logMsg("found " + filename);
                }
            }
        }
        return found_files;
    }

    private void search_file_in_neighbor(Neighbour neighbour, Neighbour requester, String query, int hops, DatagramSocket socket) {
        try {
            String request = "SER " + requester.getIp() + " " + requester.getPort() + " " + query + " " + hops;
            request = String.format("%04d", request.length() + 5) + " " + request;
            InetAddress bs_address = InetAddress.getByName(neighbour.getIp());
            send_msg_via_socket(socket, bs_address, neighbour.getPort(), request);
        } catch (Exception e) {
            logger.logMsg(e.toString());
        }
    }

    private void send_download_request(String owner_ip, int owner_port, String filename, DatagramSocket socket) {
        try {
            FileClient fileClient = new FileClient(filename, logger);
            new Thread(fileClient).start();
            String request = "DLOAD " + ip_address + " " + port + " " + filename;
            request = String.format("%04d", request.length() + 5) + " " + request;
            InetAddress bs_address = InetAddress.getByName(owner_ip);
            send_msg_via_socket(socket, bs_address, owner_port, request);
        } catch (UnknownHostException e) {
            logger.logMsg(e.toString());
        }
    }

    private void handle_file_exchange(String requester_ip, int requester_port, String requested_filename) {
        FileServer file_server = new FileServer(requested_filename, logger);
        file_server.setFile(new File(requested_filename));
        new Thread(file_server).start();
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public HashMap<String, File> getMy_files() {
        return my_files;
    }

    public void setMy_files(HashMap<String, File> my_files) {
        this.my_files = my_files;
    }

    private void send_msg_via_socket(DatagramSocket socket, InetAddress bs_address, int port, String msg) {
        try {
            DatagramPacket out_packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, bs_address, port);
            socket.send(out_packet);
        } catch (Exception e) {
            logger.logMsg(e.toString());
        }
    }

    private static HashMap<String, File> getRandomFileSet() {
        HashMap<String, File> tempFiles = new HashMap<>();
        Random random = new Random();
        String tempFilename;
        for (int i = 0; i < random.nextInt(3) + 3; i++) {
            tempFilename = availableFiles[random.nextInt(availableFiles.length)];
            tempFiles.put(tempFilename, new File(tempFilename));
        }
        return tempFiles;
    }

    public static void main(String[] args) {
        int tPort = Integer.parseInt(args[0]);
        String tUsername = args[1];
        try {
            String ip_addr = InetAddress.getLocalHost().getHostAddress();
            Logger logger = new Logger(ip_addr + ":" + tPort + "-" + tUsername + ".log");
            Node node = new Node(ip_addr, tPort, tUsername, logger);
            node.setMy_files(getRandomFileSet());
            new Thread(node).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}
