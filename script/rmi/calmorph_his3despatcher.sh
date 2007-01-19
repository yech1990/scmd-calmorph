#!/bin/zsh

WORK=/home/leo/work/scmd/script

for i in $(cat $WORK/serverlist.txt);do 
    ssh $i "$WORK/calmorph_his3client.sh" &
    ssh $i "$WORK/calmorph_his3client.sh" &
done
