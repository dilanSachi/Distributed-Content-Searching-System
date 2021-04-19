rm ./logs/*.log

port=57000
for i in $(seq 0 3)
do
  for j in $(seq 0 2)
  do
    uname=$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 32 | head -n 1);
    kill -9 $(lsof -t -i:$port);
    java Node $port $uname &
    port=$((port+1));
    sleep 1
  done
done