package promptsanitizer.batchjob;

import java.util.HashMap;
import java.util.Map;

class ArgsParse {
    static Map<String, String> parseArgs(String[] args) {
        if(args == null || args.length != 6) {
            throw new IllegalArgumentException("Please specify options for --direction, --sensitive-file-loc and --safe-file-loc");
        }
        Map<String, String> parsed = new HashMap<>();
        for(int i=0; i<6; i+=2) {
            String arg = args[i];
            if(!"--direction".equals(arg) && !"--sensitive-file-loc".equals(arg) && !"--safe-file-loc".equals(arg)) {
                throw new IllegalArgumentException("Please specify options for --direction, --sensitive-file-loc and --safe-file-loc");
            }
            parsed.put(arg.replace("--", ""), args[i+1]);
        }
        return parsed;
    }
}
