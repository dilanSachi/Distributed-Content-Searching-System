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
        files2.put("Avengers", new File("Avengers", ""));
        files2.put("Terminator", new File("Terminator", ""));
        files2.put("Godfather", new File("Godfather", ""));
        files2.put("Memento", new File("Memento", ""));
        files2.put("Tomb raider", new File("Tomb raider", ""));
        node2.setMy_files(files2);
        Thread thread2 = new Thread(node2);

        Node node3 = new Node("127.0.0.1", 9523, "dinga3");
        HashMap<String, File> files3 = new HashMap<String, File>();
        files3.put("Avatar", new File("Avatar", ""));
        files3.put("Asterix", new File("Asterix", ""));
        files3.put("Ninja turtles", new File("Ninja turtles", ""));
        files3.put("Spartacus", new File("Spartacus", ""));
        files3.put("Gods of olympus", new File("Gods of olympus", ""));
        node3.setMy_files(files3);
        Thread thread3 = new Thread(node3);

        Node node4 = new Node("127.0.0.1", 9524, "dinga4");
        HashMap<String, File> files4 = new HashMap<String, File>();
        files4.put("Zombieland", new File("Zombieland", ""));
        files4.put("Day of the dead", new File("Day of the dead", ""));
        files4.put("World war z", new File("World war z", ""));
        files4.put("28 days later", new File("28 days later", ""));
        files4.put("Resident evil", new File("Resident evil", ""));
        node4.setMy_files(files4);
        Thread thread4 = new Thread(node4);

        Node node5 = new Node("127.0.0.1", 9525, "dinga5");
        HashMap<String, File> files5 = new HashMap<String, File>();
        files5.put("Black widow", new File("Black widow", ""));
        files5.put("Captain marvel", new File("Captain marvel", ""));
        files5.put("Black panther", new File("Black panther", ""));
        files5.put("Thor", new File("Thor", ""));
        files5.put("Guardians of the galaxy", new File("Guardians of the galaxy", ""));
        node5.setMy_files(files5);
        Thread thread5 = new Thread(node5);

        thread1.start();
//        thread2.start();
//        thread3.start();
//        thread4.start();
//        thread5.start();
    }
}
