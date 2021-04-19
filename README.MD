1. First run makefile to compile all the java classes
2. Then run **chmod 775 *.sh** to all shell scripts for execution access.
3. To compile and run the BootstrapServer, run **./StartBS.sh**.
4. Then run **./StartNodes.sh** to run 12 nodes having random files (in ports 57001 - 570011).
5. Then run **./QueryNodes.sh** to randomly select 3 nodes among the 12 nodes and query each node with the filenames given in the file **Queries**.
6. Run **./KillNodes&BS.sh** to kill all BootstrapServer and Node processes.

All the logs of nodes and query time are stored in *logs* folder.