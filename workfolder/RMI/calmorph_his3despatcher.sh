#!/export/home/sesejun/bin/bin_ia32/zsh

for i in {01..14};do 
    ssh cb$i '/export/home/sesejun/work/scmd/data/his3/calmorph_his3client.sh' &
    ssh cb$i '/export/home/sesejun/work/scmd/data/his3/calmorph_his3client.sh' &
done
