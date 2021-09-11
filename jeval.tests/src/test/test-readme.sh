#!/bin/bash

PATH=$JEVAL_PATH:$PATH

echo "Test 0"
OUT=$(jeval -e 'out.println("Hello world")')
if [ "$OUT" != "Hello world" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 1"
OUT=$(jeval -e "range(1,10).forEach(out::println)")
EXPECTED="1
2
3
4
5
6
7
8
9"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 2"
cat << EOF > /tmp/r.xml
<notes>
    <note>
        <to test="ggg1">Tove</to>
    </note>
    <note>
        <to test="ggg2">Bove</to>
    </note>
</notes>
EOF
OUT=$(jeval -e 'out.println(XPathFactory.newInstance().newXPath().evaluate("//note/to", DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("/tmp/r.xml"))))')
EXPECTED="Tove"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 3"
OUT=$(echo 14 | jeval -e 'Integer.toBinaryString(new Scanner(in).nextInt())')
EXPECTED='"1110"'
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 4"
OUT=$(jeval -e 'Files.createTempFile(null, "tmp")')
if [ ! -f "$OUT" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 5"
OUT=$(echo -e "ab\ncd\nef" | jeval -e "stdin.lines().collect(joining(\",\"))")
EXPECTED='"ab,cd,ef"'
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 6"
OUT=$(echo '{"menu":123}' | jeval -e 'new ScriptEngineManager().getEngineByName("nashorn").eval("var v = " + stdin.lines().collect(joining("\n")) + "; v[\"menu\"]");')
EXPECTED="123"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 7"
OUT=$(jeval -e 'format("args %s, %s", args[0], args[1])' "arg1" "arg2")
EXPECTED='"args arg1, arg2"'
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 8"
OUT=$(jeval -e 'new XExec("curl -L -G -vvv http://google.com").run().stderr().forEach(out::println)')
echo "$OUT" | grep -q "HTTP\/1.1 200 OK"
if [ $? -ne 0 ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 9"
OUT=$(jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "//note/to/@test"))')
EXPECTED="[ggg1, ggg2]"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 10"
OUT=$(jeval -e 'findMatches("\\d.jpg", "1.jpg 2.png 3.jpg 4.txt 5.txt").forEach(out::println)')
if [ "$OUT" != "1.jpg"$'\n'"3.jpg" ]; then
    echo "FAILED 10"
    exit 1
fi

echo "Test 11"
rm /tmp/test-*
OUT=$(jeval -e 'try (var c = new ParallelConsumer(s -> new XExec("touch /tmp/test-" + s).run().await())) {XUtils.infiniteRandomStream(12).limit(20).forEach(c);}' && ls /tmp/test-* | wc -l)
if [ "$OUT" != "20" ]; then
    echo "FAILED 11"
    exit 1
fi

echo "Test 12"
cat << EOF > /tmp/script.java
//open parsers.java
printf(parseToHex(123) + "\n");
EOF
cat << EOF > /tmp/parsers.java
String parseToHex(int num) {
    return Integer.toHexString(num);
}
EOF
OUT=$(jeval /tmp/script.java 2>&1)
if [ "$OUT" != "7b" ]; then
    echo "FAILED $OUT"
    exit 1
fi
