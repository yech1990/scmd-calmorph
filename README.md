# SCMDUtils

> fork version of CalMolph maintained by Chang

---

### How to build?

```bash
cd $PROJECT_PATH
mvn package
```

### How to use?

```bash
cd $PROJECT_PATH
java -jar ./target/CalMolph-2.0.0.jar -i ./test/zzz -o zzz -a false -d true -v
```

> parameters

| name | description                                   |
| ---- | --------------------------------------------- |
| o    | output directory of the analysis results      |
| i    | input photo directory                         |
| n    | specify the strain name                       |
| a    | actin mode, opt: [true / false], default true |
| d    | DAPI mode, opt: [true / false], default true  |


---

### Update

- change ant build to maven
- fix SJIS encoding to UTF-8

### TODO

- make the Japanese annotation in to English
