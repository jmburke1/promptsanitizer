package promptsanitizer;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class MainAppTest {
    @Test
    void testMain() {
        try(
                MockedConstruction<Sanitizer> sanitizerMC = Mockito.mockConstruction(
                        Sanitizer.class,
                        (mock, context) -> {
                            assertEquals(1, context.arguments().size());
                            assertTrue(((String)context.arguments().getFirst()).contains("personal_dictionary.json"));
                        }
                )
        ) {
            String[] testArgs = {"test", "args"};
            MainApp.main(testArgs);
            Mockito.verify(sanitizerMC.constructed().getFirst()).createUI();
        }
    }

}