jeval -e 'out.println("Hello world")'
jeval -e "range(1,10).forEach(out::println)"
jeval -e '{Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("/home/id/workspace/n.xml")); out.println(XPathFactory.newInstance().newXPath().evaluate("//note/to", d));}'
jeval -e "Integer.toBinaryString(new Scanner(in).nextInt())"
echo 14 | jeval -e "Integer.toBinaryString(new Scanner(in).nextInt())"
jeval -e 'Files.createTempFile(null, "tmp")'
jeval -e 'stdin.lines().collect(joining(","))'
echo '{"menu":123}' | jeval -e 'new ScriptEngineManager().getEngineByName("nashorn").eval("var v = " + stdin.lines().collect(joining("\n")) + "; v[\"menu\"]");'
jeval -e 'out.println("args " + args[1])' "Hello world"
