import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RunResourceTest {

    @RegisterExtension
    public static PostgresTestcontainerExtension db = new PostgresTestcontainerExtension();

    private static RunResource runResource;

    @BeforeAll
    public static void beforeAll() {
        var runService = mock(RunService.class);
        when(runService.executeRun(anyInt())).thenReturn(true);
        runResource = new RunResource(db.getJdbi(), runService);
    }

    @Test
    @DisplayName("Empty list is returned when asked for runs without having existing runs")
    public void t1() {
        assertThat(runResource.getRuns())
                .isEmpty();
    }
}
