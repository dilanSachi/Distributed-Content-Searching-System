kill $(lsof -t -i:55555);
sleep 1
java BootstrapServer &