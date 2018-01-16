package com.talanlabs.mm.shared;

import com.talanlabs.mm.shared.model.IErrorType;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ErrorTypeTest {

    @Test
    public void testErrorType() {
        IErrorType errorType = IErrorType.of("test", ErrorRecyclingKind.WARNING, 42);

        Assertions.assertThat(errorType.getCode()).isEqualTo("test");
        Assertions.assertThat(errorType.getRecyclingKind()).isEqualTo(ErrorRecyclingKind.WARNING);
        Assertions.assertThat(errorType.getNextRecyclingDuration()).isEqualTo(42);
    }
}
