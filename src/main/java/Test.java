public class Test {

    public static void main(String[] args) {
        Thread node1 = new Thread(new Node("127.0.0.1", 9521, "dinga1"));
        Thread node2 = new Thread(new Node("127.0.0.1", 9522, "dinga2"));
        Thread node3 = new Thread(new Node("127.0.0.1", 9523, "dinga3"));
        Thread node4 = new Thread(new Node("127.0.0.1", 9524, "dinga4"));
        Thread node5 = new Thread(new Node("127.0.0.1", 9525, "dinga5"));

        node1.start();
        node2.start();
        node3.start();
//        node4.start();
//        node5.start();
    }
}
