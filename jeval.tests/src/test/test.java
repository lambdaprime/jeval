// -------------------------------------------

String json = <<EOF
{
  "menu": {
    "id": "1",
    "value": "File",
    "menuitem": [
      {"value": "2", "onclick": "CreateNewDoc()"},
      {"value": "3", "onclick": "OpenDoc()"},
      {"value": "4", "onclick": "CloseDoc()"}
    ]
  }
}
EOF;

JSObject menu = (JSObject)new ScriptEngineManager()
    .getEngineByName("nashorn")
    .eval("var v = " + json + "; v[\"menu\"]");

out.println(menu.getMember("id"));

JSObject items = (JSObject)menu.getMember("menuitem");

items.values().stream()
    .map(obj -> (JSObject)obj)
    .map(js -> js.getMember("value"))
    .forEach(out::println);
    
out.println(<<EOF
5
EOF);

out.println(<<EOF  
6
EOF     );

public class Task2_Volleyball_Match {
    
    static final int MOD = 1_000_000_007;

    static long pow(long n, long p) {
        if (p == 0)
            return 1;
        if (p == 1)
            return n;
        long res;
        if ((p & 1) == 1)
            res = n * pow(n, p - 1) % MOD;
        else
            res = pow(n * n % MOD, p / 2);
        return res == 0? 1: res;
    }

}

out.println(Task2_Volleyball_Match.pow(7, 1));

// -------------------------------------------
int f() {
    out.println("8");
    return 10;
}
f();

rangeClosed(10, 10)
    .forEach(out::println);
    
out.println("args: " + Arrays.toString(args));

new XExec("curl", "-L", "-G", "http://google.com")
    .run()
    .stdout()
    .forEach(out::println);
int ret = new XExec("ls /sdfgsdfg")
    .run()
    .code()
    .get();
assertTrue(ret == 2, "Return code is wrong");
    
Path xmlFile = Paths.get("src/test/test.xml");
out.println(Xml.query(xmlFile, "//note/to/@test"));
out.println(Xml.query(xmlFile, "/notes/note/to[@test=\"ggg2\"]"));
out.println(Xml.query(xmlFile, "/notes/note/to"));
out.println(Xml.query(xmlFile, "/notes/note[2]"));
out.println(Xml.query(xmlFile, "/notes/note/to/@test"));

findMatches("\\d.jpg", "1.jpg 2.png 3.jpg 4.txt 5.txt").forEach(out::println);

String s = <<EOF
line1
line2

lineN
EOF;

out.println(s);
assertTrue(s.chars().filter(c -> c == '\n').count() == 3);

var client = new HttpClientBuilder()
    .insecure()
    .tlsv1()
    .get()
    .followRedirects(HttpClient.Redirect.ALWAYS)
    .cookieHandler(new StaticCookieHandler("name1=value1; name2=value2"))
    .build();
var request = HttpRequest.newBuilder()
    .uri(URI.create("https://google.com"))
    .GET()
    .build();
var response = client.send(request, BodyHandlers.ofString());
out.println(response.body());

var lines = List.of("1");
lines = lines.stream()
    .collect(toList());
