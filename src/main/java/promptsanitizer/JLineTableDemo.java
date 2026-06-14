package promptsanitizer;
import org.jline.console.Printer;
import org.jline.console.impl.DefaultPrinter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JLineTableDemo {

    public static void main(String[] args) throws Exception {
        try (Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build()) {

            Printer printer = new TerminalPrinter(terminal);

            List<Map<String, String>> rows = new ArrayList<>();

            rows.add(row("Alice", "Developer", "active", 42));
            rows.add(row("Bob", "DBA", "inactive", 7));
            rows.add(row("Carol", "Release Manager", "active", 128));

            terminal.writer().println("Default table:");
            printer.println(rows);

            terminal.writer().println();
            terminal.writer().println("Selected columns:");

            Map<String, Object> options = new HashMap<>();
            options.put(Printer.COLUMNS, List.of("name", "role", "status", "tickets"));
            options.put(Printer.SHORT_NAMES, true);

            printer.println(options, rows);

            terminal.writer().flush();
        }
    }

    private static Map<String, String> row(
            String name,
            String role,
            String status,
            int tickets
    ) {
        Map<String, String> row = new HashMap<>();
        row.put("name", name);
        row.put("role", role);
        row.put("status", status);
        row.put("tickets", Integer.toString(tickets));
        return row;
    }

    /**
     * DefaultPrinter expects to know what Terminal it is printing to.
     * JLine's own example does this by subclassing DefaultPrinter
     * and overriding terminal().
     */
    private static class TerminalPrinter extends DefaultPrinter {
        private final Terminal terminal;

        TerminalPrinter(Terminal terminal) {
            super(null);
            this.terminal = terminal;
        }

        @Override
        protected Terminal terminal() {
            return terminal;
        }
    }
}