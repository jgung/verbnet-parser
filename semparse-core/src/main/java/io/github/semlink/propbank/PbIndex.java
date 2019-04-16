package io.github.semlink.propbank;

import java.util.List;

import io.github.semlink.propbank.frames.Roleset;

/**
 * PropBank frame index.
 *
 * @author jgung
 */
public interface PbIndex {

    /**
     * Returns a {@link Roleset} for a given roleset ID, e.g. 'take.01'.
     *
     * @param rolesetId roleset ID e.g. 'take.01'
     * @return roleset corresponding to ID
     */
    Roleset getById(String rolesetId);

    /**
     * Returns all rolesets for a given lemma.
     *
     * @param lemma base lemma of verb/noun/adjective
     * @return list of matching rolesets
     */
    List<Roleset> getByLemma(String lemma);


}
