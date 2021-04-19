kill $(lsof -t -i:55555);
port=57000
for i in $(seq 0 3)
do
  for j in $(seq 0 2)
  do
    kill -9 $(lsof -t -i:$port);
    port=$((port+1));
  done
done