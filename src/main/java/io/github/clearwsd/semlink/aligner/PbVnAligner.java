package io.github.clearwsd.semlink.aligner;

import lombok.NonNull;

/**
 * PropBank VerbNet alignment phase.
 *
 * @author jgung
 */
public interface PbVnAligner {

    void align(@NonNull PbVnAlignment alignment);

}
