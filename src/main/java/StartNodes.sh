port=57000
for i in $(seq 0 3)
do
  for j in $(seq 0 2)
  do
    NEW_UUID=$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 32 | head -n 1);
    javac Node.java; java Node $port $NEW_UUID
    port=$((port+1));
    sleep 1
  done
done