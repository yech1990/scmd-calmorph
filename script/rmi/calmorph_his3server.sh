#!/bin/zsh
#. /etc/rc.d/init.d/functions

WORK=/home/leo/work/scmd/script

cd $WORK;

rmiregistry -J'-cp' -J"$WORK/bin" &

java -cp $WORK/bin -Djava.security.policy=$WORK/java.policy \
-Djava.rmi.server.codebase=file://$WORK/bin lab.cb.scmd.util.rmi.CalMorphDespatcher \
$WORK/data/his3/photo/

