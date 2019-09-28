PATH=$JEVAL_PATH:$PATH

jeval -e 'out.println("Hello world")'
jeval -e "range(1,10).forEach(out::println)"
jeval -e '{Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("/home/id/workspace/n.xml")); out.println(XPathFactory.newInstance().newXPath().evaluate("//note/to", d));}'
echo "enter integer"
jeval -e "Integer.toBinaryString(new Scanner(in).nextInt())"
echo 14 | jeval -e "Integer.toBinaryString(new Scanner(in).nextInt())"
jeval -e 'Files.createTempFile(null, "tmp")'
echo "enter lines"
jeval -e 'stdin.lines().collect(joining(","))'
echo '{"menu":123}' | jeval -e 'new ScriptEngineManager().getEngineByName("nashorn").eval("var v = " + stdin.lines().collect(joining("\n")) + "; v[\"menu\"]");'
jeval -e 'out.println("args " + args[1])' "Hello world"
jeval -e 'new Exec("curl -L -G http://google.com").run().stdout.forEach(out::println)'
jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "//note/to/@test"))'
jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note/to[@test=\"ggg2\"]"))'
jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note/to"))'
jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note[2]"))'
jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note/to/@test"))'
jeval -e 'findMatches("\\d.jpg", "1.jpg 2.png 3.jpg 4.txt 5.txt").forEach(out::println)'