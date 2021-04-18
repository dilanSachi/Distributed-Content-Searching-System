kill $(lsof -t -i:55555);
sleep 1
javac BootstrapServer.java; java BootstrapServer