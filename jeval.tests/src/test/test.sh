#!/bin/bash

PATH=$JEVAL_PATH:$PATH

echo "Test 0"
cat << EOF > /tmp/r
class X {
    static X create() {
        m(null, null);
        return null;
    }
}
class Y {
    X x;
    static Y c(Z h, List<String> d) {
        if (y != null) return y;
        return null;
    }
}
String m() {
    return null;
}
Y y = null;
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED="/tmp/r: 17: cannot find symbol
  symbol:   class Y
  location: class 
Y y = null;
^

/tmp/r: Unresolved symbol in the snippet starting at line 7: class Z

/tmp/r: Unresolved symbol in the snippet starting at line 7: variable y

/tmp/r: 3: method m in class  cannot be applied to given types;
  required: no arguments
  found: <nulltype>,<nulltype>
  reason: actual and formal argument lists differ in length
        m(null, null);
        ^"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 1"
cat << EOF > /tmp/r
class X1 {
    static X create() {
        m(null, null);
        return null;
    }
}
class X2 {
    static X create() {
        m(null, null);
        return null;
    }
}
Y y = null;
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED="/tmp/r: 13: cannot find symbol
  symbol:   class Y
  location: class 
Y y = null;
^

/tmp/r: Unresolved symbol in the snippet starting at line 7: class X

/tmp/r: Unresolved symbol in the snippet starting at line 7: method m(<nulltype>,<nulltype>)

/tmp/r: Unresolved symbol in the snippet starting at line 1: class X

/tmp/r: Unresolved symbol in the snippet starting at line 1: method m(<nulltype>,<nulltype>)"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 2"
cat << EOF > /tmp/r
List.of(1,2,3).stream()
    .filter(i -> i > 0)
    .map(i -> "-" + i)
    .peek(out::println)
    // asdd
    .collect(toSet()).stream()
    .forEach(out::println);
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED="-1
-2
-3
-1
-2
-3"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

# Test that jeval supports JAVA_ARGS
echo "Test 3"
OUT=$(JAVA_ARGS="-Dtest=hello" jeval -e 'System.getProperty("test")')
EXPECTED="\"hello\""
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

# Test askConfir
echo "Test 4"
OUT=$(yes yes | jeval -e 'cli.askConfirm("Execute this?")')
EXPECTED="Execute this?
Please confirm [yes/no]: true"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

# Test multiple arguments are available from args
echo "Test 5"
OUT=$(jeval -e 'printf("args %s, %s\n", args[0], args[1])' "arg1" "arg2")
EXPECTED="args arg1, arg2"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

# Test that jeval shows unresolved references
echo "Test 6"
cat << EOF > /tmp/r
void f() {
    printf("ggg");
    printf(g);
}

f();
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED='jdk.jshell.UnresolvedReferenceException: Attempt to use definition snippet with unresolved references in MethodSnippet:f/()void-void f() {
    printf("ggg");
    printf(g);
}

	at .f(#48:1)
	at .(#49:1)
/tmp/r: Unresolved symbol in the snippet starting at line 1: variable g'
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 7"
OUT=$(jeval -e 'assertTrue(2 == new XExec("ls /sdfgsdfg").run().code().get(), "Return code is wrong")')
EXPECTED=""
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 8"
OUT=$(jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note/to[@test=\"ggg2\"]"))')
EXPECTED="[Bove]"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 9"
OUT=$(jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note/to"))')
EXPECTED="[Tove, Bove]"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 10"
OUT=$(jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note[2]"))')
EXPECTED="[Bove]"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 11"
OUT=$(jeval -e 'out.println(Xml.query("<notes><note><to test=\"ggg1\">Tove</to></note><note><to test=\"ggg2\">Bove</to></note></notes>", "/notes/note/to/@test"))')
EXPECTED="[ggg1, ggg2]"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

# Test that jeval 
echo "Test 12"
cat << EOF > /tmp/r
void g() {
    var y = new Exec("ffff")
        .run();
}
Runnable r = () -> {
    try {
        g();
    } catch (Exception e) {
    }
};
var exec = Executors.newSingleThreadExecutor();
exec.submit(r);
exec.shutdown();
exec.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED='/tmp/r: Unresolved symbol in the snippet starting at line 1: class Exec'
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 13"
cat << EOF > /tmp/r
/open rr
EOF
cat << EOF > /tmp/rr
/open rrr
EOF
cat << EOF > /tmp/rrr
out.println("ggg");
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED="ggg"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 14"
cat << EOF > /tmp/r
/open rr
m();
EOF
cat << EOF > /tmp/rr
void m() {out.println("ggg");}
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED="ggg"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 15"
cat << EOF > /tmp/r
/open  /tmp/rr
m();
EOF
cat << EOF > /tmp/rr
void m() {out.println("ggg");}
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED="ggg"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 16"
cat << EOF > /tmp/r
/open  /tmp/rr
m();
EOF
cat << EOF > /tmp/rr
void m() {out.println(ggg);}
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED='jdk.jshell.UnresolvedReferenceException: Attempt to use definition snippet with unresolved references in MethodSnippet:m/()void-void m() {out.println(ggg);}

	at .m(#48:1)
	at .(#49:1)
/tmp/rr: Unresolved symbol in the snippet starting at line 1: variable ggg'
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED [$OUT] [$EXPECTED]"
    exit 1
fi

echo "Test 17"
cat << EOF > /tmp/r
void m() {
    lol.test();
}
exit(0);
EOF
OUT=$(jeval /tmp/r)
EXPECTED=""
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 18"
cat << EOF > /tmp/r
String calc(String line) {
    out.println("asdf");
    return COOKIE;
}

void writeToFile(String line) {

}

//Usage check.
if (args.length < 1) {
    out.println(<<EOF

fff
EOF);
    exit(0);
}

final String COOKIE = "ggggg";

out.println("he");

Stream.of("1")
	.parallel()
	.map(l -> calc(l))
	.map(l -> l.replace(";",","))
	.forEach(line -> writeToFile(line));
EOF
OUT=$(jeval /tmp/r /tmp/r)
EXPECTED="he
asdf"
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED $OUT"
    exit 1
fi

echo "Test 19 In case of unrecoverable error, print recoverable errors as well"
cat << EOF > /tmp/r
void m(X x) {
    int i;
    printf("" + i);
}

class X {
}
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED='/tmp/r: 1: cannot find symbol
  symbol:   class X
  location: class 
void m(X x) {
       ^

/tmp/r: 3: variable i might not have been initialized
    printf("" + i);
                ^'
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED [$OUT]"
    exit 1
fi

echo "Test 20 In case of unrecoverable error fail fast and do not execute further"
cat << EOF > /tmp/r
void m(X x) {
    int i;
    printf("" + i);
}

printf("hello");
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED='/tmp/r: 1: cannot find symbol
  symbol:   class X
  location: class 
void m(X x) {
       ^

/tmp/r: 3: variable i might not have been initialized
    printf("" + i);
                ^'
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED [$OUT]"
    exit 1
fi

echo "Test 21 Unresolved snippet position ignores blank lines"
cat << EOF > /tmp/r



void m() {
     printf(x);
}
EOF
OUT=$(jeval /tmp/r 2>&1)
EXPECTED='/tmp/r: Unresolved symbol in the snippet starting at line 4: variable x'
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED [$OUT]"
    exit 1
fi

# Test multiple arguments are available from args
echo "Test 22 Error output with -e"
OUT=$(jeval -e 'pr' 2>&1)
EXPECTED=': 1: cannot find symbol
  symbol:   variable pr
  location: class 
pr
^'
if [ "$OUT" != "$EXPECTED" ]; then
    echo "FAILED [$OUT]"
    exit 1
fi
