JEVAL_PATH=$(readlink -f "$0")
JEVAL_PATH=$(dirname "$JEVAL_PATH")/../../release/jeval/jeval

$JEVAL_PATH -e 'out.println("Hello world")'
$JEVAL_PATH -e "range(1,10).forEach(out::println)"
$JEVAL_PATH -e '{Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("/home/id/workspace/n.xml")); out.println(XPathFactory.newInstance().newXPath().evaluate("//note/to", d));}'
echo "enter integer"
$JEVAL_PATH -e "Integer.toBinaryString(new Scanner(in).nextInt())"
echo 14 | $JEVAL_PATH -e "Integer.toBinaryString(new Scanner(in).nextInt())"
$JEVAL_PATH -e 'Files.createTempFile(null, "tmp")'
echo "enter lines"
$JEVAL_PATH -e 'stdin.lines().collect(joining(","))'
echo '{"menu":123}' | $JEVAL_PATH -e 'new ScriptEngineManager().getEngineByName("nashorn").eval("var v = " + stdin.lines().collect(joining("\n")) + "; v[\"menu\"]");'
$JEVAL_PATH -e 'out.println("args " + args[1])' "Hello world"
