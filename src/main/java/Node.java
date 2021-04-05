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
            reg_with_BS(sock);
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
                    if (node_count == 0) {
                        echo("Succefully registered with BS... No nodes available...");
                    } else if (node_count == 1) {
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
                    } else if (node_count == 9999) {
                        echo("Failed. There is an error in the request command...");
                    } else if (node_count == 9998) {
                        echo("Already registered. Unregistering and trying to re-register...");
                        unreg_with_BS(sock);
                        echo("Unregistering with neighbors...");
                        for (int i = 0; i < neighbours.size(); i ++) {
                            unreg_with_neighbor(neighbours.get(i), sock);
                        }
                        reg_with_BS(sock);
                    } else if (node_count == 9997) {
                        echo("Failed, registered to another user, try a different IP and port..");
                    } else if (node_count == 9996) {
                        echo("Failed, can’t register. BS full...");
                    } else {
                        echo("Unknown response code...");
                    }
                } else if (command.equals("UNROK")) {
                    int status = Integer.parseInt(st.nextToken());
                    if (status == 0) {
                        echo("Successfully unregistered with the BS...");
                    } else if (status == 9999) {
                        echo("Error while unregistering. IP and port may not be in the registry or command is incorrect...");
                    }
                } else if (command.equals("JOIN")) {
                    String neighbor_ip = st.nextToken();
                    int neighbor_port = Integer.parseInt(st.nextToken());
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
                        send_msg_via_socket(sock, InetAddress.getByName(neighbor_ip), neighbor_port, response);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                } else if (command.equals("JOINOK")) {
                    int status = Integer.parseInt(st.nextToken());
                    if (status == 0) {
                        echo("Successfully joined with node");
                    } else if (status == 9999) {
                        echo("Error while adding new node to routing table...");
                    }
                    else {
                        echo("Error when joining. Status code - " + status);
                    }
                } else if (command.equals("LEAVE")) {
                    String neighbor_ip = st.nextToken();
                    int neighbor_port = Integer.parseInt(st.nextToken());
                    for (int i = 0; i < neighbours.size(); i ++) {
                        if (neighbours.get(i).getIp().equals(neighbor_ip) && neighbours.get(i).getPort() == neighbor_port) {
                            neighbours.remove(i);
                            break;
                        }
                    }
                } else if (command.equals("LEAVEOK")) {
                    int status = Integer.parseInt(st.nextToken());
                    if (status == 0) {
                        echo("Successfully left the connection with node " + incoming.getAddress().getHostAddress() + " : " + incoming.getPort());
                    } else if (status == 9999) {
                        echo("Error while leaving the connection with node " + incoming.getAddress().getHostAddress() + " : " + incoming.getPort());
                    } else {
                        echo("Error when leaving. Status code - " + status);
                    }
                }
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

    public void unreg_with_BS(DatagramSocket socket) {
        try {
            String request = "UNREG " + this.ip_address + " " + this.port + " " + this.username;
            request = String.format("%04d", request.length() + 5) + " " + request;
            InetAddress bs_address = InetAddress.getLocalHost();
            send_msg_via_socket(socket, bs_address, 55555, request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reg_with_neighbor(Neighbour neighbour, DatagramSocket socket) {
        try {
            String request = "JOIN " + this.ip_address + " " + this.port;
            request = String.format("%04d", request.length() + 5) + " " + request;
            InetAddress bs_address = InetAddress.getByName(neighbour.getIp());
            send_msg_via_socket(socket, bs_address, neighbour.getPort(), request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unreg_with_neighbor(Neighbour neighbour, DatagramSocket socket) {
        try {
            String request = "LEAVE " + this.ip_address + " " + this.port;
            request = String.format("%04d", request.length() + 5) + " " + request;
            InetAddress bs_address = InetAddress.getByName(neighbour.getIp());
            send_msg_via_socket(socket, bs_address, neighbour.getPort(), request);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
