package pingtool.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgumentMapper {

    private List<ArgumentMapping> argumentMappings;
    private List<Argument> arguments = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public ArgumentMapper(ArgumentMapping... argumentMappings) {
        this.argumentMappings = Arrays.asList(argumentMappings);
    }

    public void map(String... args) {
        arguments.clear();
        errors.clear();;

        int i = 0;
        while (i < args.length) {
            String key = args[i];
            boolean valid = false;
            for (ArgumentMapping argumentMapping : argumentMappings) {
                try {
                    if (argumentMapping.isArgument(key)) {
                        if (argumentMapping.isFlag()) {
                            i--;
                            arguments.add(new Argument(argumentMapping.getKey(), "true"));
                        } else {
                            arguments.add(new Argument(argumentMapping.getKey(), args[i + 1]));
                        }
                        valid = true;
                        break;
                    }
                } catch (Exception e) {
                    break;
                }
            }
            if (!valid) {
                errors.add(key);
            }
            i += 2;
        }
    }

    public boolean hasError() {
        return !errors.isEmpty();
    }

    public String getError() {
        if (hasError()) {
            return errors.remove(0);
        }
        return null;
    }

    public boolean hasArgument() {
        return !arguments.isEmpty();
    }

    public boolean hasArgument(String key) {
        if (hasArgument()) {
            for (Argument argument : arguments) {
                if (argument.getKey().equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Argument getArgument(String key) {
        if (hasArgument()) {
            for (Argument argument : arguments) {
                if (argument.getKey().equals(key)) {
                    return argument;
                }
            }
        }
        return null;
    }

}
