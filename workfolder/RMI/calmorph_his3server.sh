#!/export/home/sesejun/bin/bin_ia32/zsh
#. /etc/rc.d/init.d/functions

cd /export/home/sesejun/work/scmd/data/bin

rmiregistry -J'-cp' -J'/export/home/sesejun/RMI/bin' &

java -cp /export/home/sesejun/RMI/bin \
-Djava.security.policy=/export/home/sesejun/RMI/java.policy \
-Djava.rmi.server.codebase=file:///export/home/sesejun/RMI/bin CalMorphDespatcher \
/export/home/sesejun/work/scmd/data/his3/rename/

