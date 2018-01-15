package com.talanlabs.mm.shared;

import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ErrorImpactTest {

    @Test
    public void testManualErrorImpact() {
        ErrorImpact manualErrorImpact = ErrorImpact.of(ErrorRecyclingKind.MANUAL);
        Assertions.assertThat(manualErrorImpact.getRecyclingKind()).isEqualTo(ErrorRecyclingKind.MANUAL);
        Assertions.assertThat(manualErrorImpact.getNextRecyclingDuration()).isNull();
    }

    @Test
    public void testAutomaticErrorImpact() {
        ErrorImpact automaticErrorImpact = new ErrorImpact(ErrorRecyclingKind.AUTOMATIC, 60);
        Assertions.assertThat(automaticErrorImpact.getRecyclingKind()).isEqualTo(ErrorRecyclingKind.AUTOMATIC);
        Assertions.assertThat(automaticErrorImpact.getNextRecyclingDuration()).isEqualTo(60);
    }
}
