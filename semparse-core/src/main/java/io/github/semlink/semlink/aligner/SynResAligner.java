package io.github.semlink.semlink.aligner;

import io.github.clearwsd.type.FeatureType;
import io.github.semlink.semlink.PropBankPhrase;
import io.github.semlink.verbnet.type.FramePhrase;
import io.github.semlink.verbnet.type.NounPhrase;
import io.github.semlink.verbnet.type.PrepType;
import io.github.semlink.verbnet.type.Preposition;
import java.util.Optional;
import lombok.NonNull;

/**
 * Aligner based on syntactic cues/restrictions.
 *
 * @author jgung
 */
public class SynResAligner implements PbVnAligner {

    @Override
    public void align(@NonNull PbVnAlignment alignment) {
        for (PropBankPhrase pbPhrase : alignment.sourcePhrases()) {
            filterInvalid(alignment, pbPhrase);
        }
    }

    private void filterInvalid(@NonNull PbVnAlignment alignment, @NonNull PropBankPhrase pbPhrase) {
        Optional<PrepType> prep = getPrep(pbPhrase);
        if (!prep.isPresent()) {
            return;
        }
        if (alignment.alignedSource(pbPhrase)) {
            for (FramePhrase phrase : alignment.alignedPhrases(pbPhrase)) {
                if (phrase instanceof NounPhrase && ((NounPhrase) phrase).preposition().isPresent()) {
                    Preposition preposition = ((NounPhrase) phrase).preposition().get();
                    if (!preposition.valid().contains(prep.get())) {
                        alignment.remove(pbPhrase, phrase);
                    }
                }
            }
        }
    }

    public static Optional<PrepType> getPrep(PropBankPhrase phrase) {
        String startText = phrase.start().feature(FeatureType.Text);
        if (phrase.tokens().size() > 1) {
            // e.g. "out of" or "in between"
            String concatenated = startText + "_" + phrase.tokens().get(1).feature(FeatureType.Text);
            try {
                return Optional.of(PrepType.valueOf(concatenated.toUpperCase()));
            } catch (Exception ignored) {
            }
        }
        try {
            return Optional.of(PrepType.valueOf(startText.toUpperCase()));
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

}
