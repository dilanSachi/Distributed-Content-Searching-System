import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

public class Node implements Runnable {

    private String ip_address;
    private int port;
    private String username;

    private ArrayList<Neighbour> neighbours = new ArrayList<Neighbour>();
    private File[] my_files;

    public Node(String ip_address, int port, String username) {
        this.ip_address = ip_address;
        this.port = port;
        this.username = username;
    }

    public void run() {
        DatagramSocket sock = null;
        String s;

        try
        {
            sock = new DatagramSocket(this.port);
            this.reg_with_BS(sock);
            echo("Node started at " + this.port + ". Waiting for incoming data...");

            while(true)
            {
                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                sock.receive(incoming);

                byte[] data = incoming.getData();
                s = new String(data, 0, incoming.getLength());

                //echo the details of incoming data - client ip : client port - client message
                echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);

                StringTokenizer st = new StringTokenizer(s, " ");

                String length = st.nextToken();
                String command = st.nextToken();

                if (command.equals("REGOK")) {
                    int node_count = Integer.parseInt(st.nextToken());
                    if (node_count == 1) {
                        String neighbor_ip = st.nextToken();
                        int neighbor_port = Integer.parseInt(st.nextToken());
                        neighbours.add(new Neighbour(neighbor_ip, neighbor_port, ""));
                        reg_with_neighbor(neighbours.get(0), sock);
                    } else if (node_count == 2) {
                        String neighbor_ip = st.nextToken();
                        int neighbor_port = Integer.parseInt(st.nextToken());
                        neighbours.add(new Neighbour(neighbor_ip, neighbor_port, ""));
                        String neighbor_ii_ip = st.nextToken();
                        int neighbor_ii_port = Integer.parseInt(st.nextToken());
                        neighbours.add(new Neighbour(neighbor_ii_ip, neighbor_ii_port, ""));
                        reg_with_neighbor(neighbours.get(0), sock);
                        reg_with_neighbor(neighbours.get(1), sock);
                    }
                } else if (command.equals("UNROK")) {

                } else if (command.equals("JOIN")) {
                    String neighbor_ip = st.nextToken();
                    int neighbor_port = Integer.parseInt(st.nextToken());
                    neighbours.add(new Neighbour(neighbor_ip, neighbor_port, ""));
                    int status = 0;
                    for (int i = 0; i < neighbours.size(); i ++) {
                        if (neighbours.get(i).getIp().equals(neighbor_ip) && neighbours.get(i).getPort() == neighbor_port) {
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
                        send_msg_via_socket(sock, InetAddress.getByAddress(neighbor_ip.getBytes()), neighbor_port, response);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                } else if (command.equals("JOINOK")) {
                    int status = Integer.parseInt(st.nextToken());
                    if (status == 0) {
                        echo("Successfully joined with node");
                    } else {
                        echo("Error when joining. Status code - " + status);
                    }
                }
//                    int port = Integer.parseInt(st.nextToken());
//                    String username = st.nextToken();
//                    if (nodes.size() == 0) {
//                        reply += "0";
//                        nodes.add(new Neighbour(ip, port, username));
//                    } else {
//                        boolean isOkay = true;
//                        for (int i=0; i<nodes.size(); i++) {
//                            if (nodes.get(i).getPort() == port) {
//                                if (nodes.get(i).getUsername().equals(username)) {
//                                    reply += "9998";
//                                } else {
//                                    reply += "9997";
//                                }
//                                isOkay = false;
//                            }
//                        }
//                        if (isOkay) {
//                            if (nodes.size() == 1) {
//                                reply += "1 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort();
//                            } else if (nodes.size() == 2) {
//                                reply += "2 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort() + " " + nodes.get(1).getIp() + " " + nodes.get(1).getPort();
//                            } else {
//                                Random r = new Random();
//                                int Low = 0;
//                                int High = nodes.size();
//                                int random_1 = r.nextInt(High-Low) + Low;
//                                int random_2 = r.nextInt(High-Low) + Low;
//                                while (random_1 == random_2) {
//                                    random_2 = r.nextInt(High-Low) + Low;
//                                }
//                                echo (random_1 + " " + random_2);
//                                reply += "2 " + nodes.get(random_1).getIp() + " " + nodes.get(random_1).getPort() + " " + nodes.get(random_2).getIp() + " " + nodes.get(random_2).getPort();
//                            }
//                            nodes.add(new Neighbour(ip, port, username));
//                        }
//                    }
//
//                    reply = String.format("%04d", reply.length() + 5) + " " + reply;
//
//                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
//                    sock.send(dpReply);
//                } else if (command.equals("UNREG")) {
//                    String ip = st.nextToken();
//                    int port = Integer.parseInt(st.nextToken());
//                    String username = st.nextToken();
//                    for (int i=0; i<nodes.size(); i++) {
//                        if (nodes.get(i).getPort() == port) {
//                            nodes.remove(i);
//                            String reply = "0012 UNROK 0";
//                            DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
//                            sock.send(dpReply);
//                        }
//                    }
//                } else if (command.equals("ECHO")) {
//                    for (int i=0; i<nodes.size(); i++) {
//                        echo(nodes.get(i).getIp() + " " + nodes.get(i).getPort() + " " + nodes.get(i).getUsername());
//                    }
//                    String reply = "0012 ECHOK 0";
//                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
//                    sock.send(dpReply);
//                }

            }
        }

        catch(IOException e)
        {
            System.err.println("IOException " + e);
        }
    }

    public void reg_with_BS(DatagramSocket socket) {
        try {
            String request = "REG " + this.ip_address + " " + this.port + " " + this.username;
            request = String.format("%04d", request.length() + 5) + " " + request;
            InetAddress bs_address = InetAddress.getLocalHost();
            send_msg_via_socket(socket, bs_address, 55555, request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unreg_with_BS() {}

    public void reg_with_neighbor(Neighbour neighbour, DatagramSocket socket) {
        try {
            String request = "JOIN " + this.ip_address + " " + this.port;
            request = String.format("%04d", request.length() + 5) + " " + request;
            InetAddress bs_address = InetAddress.getByAddress(neighbour.getIp().getBytes());
            send_msg_via_socket(socket, bs_address, neighbour.getPort(), request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unreg_with_neighbor() {}

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

    public void send_msg_via_socket(DatagramSocket socket, InetAddress bs_address, int port, String msg) {
        try {
            DatagramPacket out_packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, bs_address, port);
            socket.send(out_packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void echo(String msg)
    {
        System.out.println(msg);
    }
}
