package id.jeval;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SmartArgs {

    private Map<String, Consumer<String>> handlers;
    private Function<String, Boolean> defaultHandler;

    public SmartArgs(Map<String, Consumer<String>> handlers, Function<String, Boolean> defaultHandler) {
        this.handlers = handlers;
        this.defaultHandler = defaultHandler;
    }
    
    public void parse(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            boolean expectValue = handlers.containsKey(args[i]);
            if (expectValue && i + 1 == args.length)
                throw new Exception();
            if (!expectValue && !defaultHandler.apply(args[i]))
                return;
            if (!expectValue)
                continue;
            handlers.get(args[i]).accept(args[i + 1]);
            i++;
        }
    }
}
