前準備
ssh-agentの設定をしておくと便利です。
# クラスタのマシンにログインしたときに、
# いちいちパスワードを聞かれないため
- 鍵の設定
% ssh-keygen -t rsa
% cd $HOME/.ssh/
% cat id_dsa.pub > authorized_keys2
- 鍵をssh-agentに覚えさせる。
% ssh-agent <your shell>
% ssh-add
で <your shell>の子プロセスで、sshの鍵が使えます。

- コンパイル

(たぶん(笑))
% rmic CalMorphDespatcher
% javac CalMorphClient

- サーバの実行 

calmorph_his3server.sh を書き換えます。
rmiregistry の引数のパスには、classをおいた場所を指定してください。
java の引数で、
java.security.policyには、java.policyファイルの場所を、
java.rmi.server.codebaseには、classの場所を指定してください。
# ああ、shell変数にすればよかった。
- 
calmorph_his3server.shを起動 します。

最後、rmiregistryと、サーバのjavaが残るので、
pstree か何かでプロセスを見て、一番上位のプロセスを
killしてください。

- クライアントの実行
パスとサーバ(デフォルトでcb01)を適当に書き換えて、
実行したいクライアントで
/home/sesejun/work/scmd/data/his3/calmorph_his3despatcher.sh 
を起動
