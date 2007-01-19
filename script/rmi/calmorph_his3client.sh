#!/bin/zsh

WORK=/home/leo/work/scmd/script

cd $WORK;
java -cp $WORK/bin -Djava.security.policy=$WORK/java.policy \
lab.cb.scmd.util.rmi.CalMorphClient hx02 \
$WORK/data/his3/photo \
$WORK/data/his3/result \
$WORK/data/his3/xml
