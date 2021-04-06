import java.util.HashMap;

public class Test {

    public static void main(String[] args) {
        Node node1 = new Node("127.0.0.1", 9521, "dinga1");
        HashMap<String, File> files1 = new HashMap<String, File>();
        files1.put("I am legend", new File("I am legend", ""));
        files1.put("Matrix", new File("Matrix", ""));
        files1.put("Iron man", new File("Iron man", ""));
        files1.put("Infinity war", new File("Infinity war", ""));
        files1.put("Need for speed", new File("Need for speed", ""));
        node1.setMy_files(files1);
        Thread thread1 = new Thread(node1);

        Node node2 = new Node("127.0.0.1", 9522, "dinga2");
        HashMap<String, File> files2 = new HashMap<String, File>();
        files2.put("I am legend", new File("Avengers", ""));
        files2.put("Matrix", new File("Terminator", ""));
        files2.put("Iron man", new File("Godfather", ""));
        files2.put("Infinity war", new File("Memento", ""));
        files2.put("Need for speed", new File("Tomb raider", ""));
        node2.setMy_files(files2);
        Thread thread2 = new Thread(node2);

        Node node3 = new Node("127.0.0.1", 9523, "dinga3");
        HashMap<String, File> files3 = new HashMap<String, File>();
        files3.put("I am legend", new File("Avatar", ""));
        files3.put("Matrix", new File("Asterix", ""));
        files3.put("Iron man", new File("Ninja turtles", ""));
        files3.put("Infinity war", new File("Spartacus", ""));
        files3.put("Need for speed", new File("Gods of olympus", ""));
        node3.setMy_files(files3);
        Thread thread3 = new Thread(node3);

        Node node4 = new Node("127.0.0.1", 9524, "dinga4");
        HashMap<String, File> files4 = new HashMap<String, File>();
        files4.put("I am legend", new File("Zombieland", ""));
        files4.put("Matrix", new File("Day of the dead", ""));
        files4.put("Iron man", new File("World war z", ""));
        files4.put("Infinity war", new File("28 days later", ""));
        files4.put("Need for speed", new File("Resident evil", ""));
        node4.setMy_files(files4);
        Thread thread4 = new Thread(node4);

        Node node5 = new Node("127.0.0.1", 9525, "dinga5");
        HashMap<String, File> files5 = new HashMap<String, File>();
        files5.put("I am legend", new File("Black widow", ""));
        files5.put("Matrix", new File("Captain marvel", ""));
        files5.put("Iron man", new File("Black panther", ""));
        files5.put("Infinity war", new File("Thor", ""));
        files5.put("Need for speed", new File("Guardians of the galaxy", ""));
        node5.setMy_files(files5);
        Thread thread5 = new Thread(node5);

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
    }
}
