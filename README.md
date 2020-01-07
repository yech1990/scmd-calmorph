# SCMDUtils

> fork version of CalMolph maintained by Chang

---

### How to build?

```bash
cd $PROJECT_PATH
make
```

### How to use?

```bash
cd $PROJECT_PATH
java -jar ./target/CalMolph-2.0.2.jar -i ./test/zzz -o ./test/out -x ./test/xml -v
```

> parameters

| name | description                                   |
| ---- | --------------------------------------------- |
| i    | input photo directory                         |
| o    | output directory of the analysis results      |
| x    | output directory of the point coordinates     |
| n    | specify the strain name                       |
| s    | specify the suffix of image file              |
| a    | actin mode, opt: [true / false], default true |
| d    | DAPI mode, opt: [true / false], default true  |


---

### Update

- change ant build to maven
- fix SJIS encoding to UTF-8
- replace log4j 1.x with 2.13

### TODO

- make the Japanese annotation in to English
