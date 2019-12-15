# SCMDUtils

> fork version of CalMolph maintained by Chang

---

### update

- change ant build to maven
- fix SJIS encoding to UTF-8

---

### todo

- make the Japanese annotation in to English

### how to build
```bash
cd $PROJECT_PATH
mvn package
```

### usage
#### args
|parameter name|description|
|--------------|-----------|
|o|output directory of the analysis results|
|i|input photo directory|
|n|specify the orf name|
|a|actin mode, opt: [true / false], default true|
|d|DAPI mode, opt: [true / false], default true|

#### example
```bash
export $JAVA_HOME=XXX
export $PATH=$PATH:$JAVA_HOME/bin
cd $JAR_PATH
java -jar scmd-0.1-HELAB.jar -o /Users/groza/Work/project/self/scmd-calmorph/data -i /Users/groza/Work/project/self/scmd-calmorph/test/sss -n sss -a false -d true
```