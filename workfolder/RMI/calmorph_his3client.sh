#!/export/home/sesejun/bin/bin_ia32/zsh
cd /export/home/sesejun/work/scmd/data/bin;
java -cp /export/home/sesejun/RMI/bin \
-Djava.security.policy=/export/home/sesejun/RMI/java.policy \
CalMorphClient cb01 \
/export/home/sesejun/work/scmd/data/his3/rename \
/export/home/sesejun/work/scmd/data/his3/result \
/export/home/sesejun/work/scmd/data/his3/xml
