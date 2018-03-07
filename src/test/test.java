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


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
 
public class HelloWorld extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
        Button btn = new Button();
        btn.setText("Say '9'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
                System.out.println("9");
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }
}

HelloWorld.main(null);

rangeClosed(10, 10)
    .forEach(out::println);
    
out.println("args: " + Arrays.toString(args));

Runnable r = () -> {
    try {
        Thread.sleep(100);
    } catch (InterruptedException e) {}
};

long l = new Microprofiler().measureRealTime(r);
System.out.println(l >= 100);

l = new Microprofiler().measureUserCpuTime(r);
System.out.println(l == 0);

l = new Microprofiler().measureUserCpuTime(() -> {
    String s = "";
    for (int i = 0; i < 100000; i++) {
        s += "x";
    }
    System.out.println(s);
});
System.out.println(l > 0);
