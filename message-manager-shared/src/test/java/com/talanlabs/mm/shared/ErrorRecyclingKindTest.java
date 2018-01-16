package com.talanlabs.mm.shared;

import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ErrorRecyclingKindTest {

    @Test
    public void testErrorRecyclingKind() {
        ErrorRecyclingKind warning = ErrorRecyclingKind.WARNING;
        ErrorRecyclingKind automatic = ErrorRecyclingKind.AUTOMATIC;
        ErrorRecyclingKind manual = ErrorRecyclingKind.MANUAL;
        ErrorRecyclingKind notRecyclable = ErrorRecyclingKind.NOT_RECYCLABLE;

        Assertions.assertThat(warning.getCriticity()).isEqualTo(0);
        Assertions.assertThat(automatic.getCriticity()).isEqualTo(1);
        Assertions.assertThat(manual.getCriticity()).isEqualTo(2);
        Assertions.assertThat(notRecyclable.getCriticity()).isEqualTo(3);

        Assertions.assertThat(ErrorRecyclingKind.getWorst(warning, automatic)).isEqualTo(automatic);
        Assertions.assertThat(ErrorRecyclingKind.getWorst(warning, manual)).isEqualTo(manual);
        Assertions.assertThat(ErrorRecyclingKind.getWorst(warning, notRecyclable)).isEqualTo(notRecyclable);

        Assertions.assertThat(ErrorRecyclingKind.getWorst(automatic, warning)).isEqualTo(automatic);
        Assertions.assertThat(ErrorRecyclingKind.getWorst(manual, warning)).isEqualTo(manual);
        Assertions.assertThat(ErrorRecyclingKind.getWorst(notRecyclable, warning)).isEqualTo(notRecyclable);

        Assertions.assertThat(ErrorRecyclingKind.getWorst(automatic, manual)).isEqualTo(manual);
        Assertions.assertThat(ErrorRecyclingKind.getWorst(automatic, notRecyclable)).isEqualTo(notRecyclable);

        Assertions.assertThat(ErrorRecyclingKind.getWorst(manual, automatic)).isEqualTo(manual);
        Assertions.assertThat(ErrorRecyclingKind.getWorst(notRecyclable, automatic)).isEqualTo(notRecyclable);

        Assertions.assertThat(ErrorRecyclingKind.getWorst(manual, notRecyclable)).isEqualTo(notRecyclable);

        Assertions.assertThat(ErrorRecyclingKind.getWorst(notRecyclable, manual)).isEqualTo(notRecyclable);

        Assertions.assertThat(ErrorRecyclingKind.getWorst(null, warning)).isEqualTo(warning);
        Assertions.assertThat(ErrorRecyclingKind.getWorst(null, automatic)).isEqualTo(automatic);
        Assertions.assertThat(ErrorRecyclingKind.getWorst(null, manual)).isEqualTo(manual);
        Assertions.assertThat(ErrorRecyclingKind.getWorst(null, notRecyclable)).isEqualTo(notRecyclable);

        Assertions.assertThat(ErrorRecyclingKind.getWorst(warning, null)).isEqualTo(warning);
        Assertions.assertThat(ErrorRecyclingKind.getWorst(automatic, null)).isEqualTo(automatic);
        Assertions.assertThat(ErrorRecyclingKind.getWorst(manual, null)).isEqualTo(manual);
        Assertions.assertThat(ErrorRecyclingKind.getWorst(notRecyclable, null)).isEqualTo(notRecyclable);
    }
}
