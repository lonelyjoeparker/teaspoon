package jebl.evolution.alignments;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jebl.evolution.sequences.SequenceType;

/**
 * @author rambaut
 * @author Alexei Drummond
 * @version $Id: ConsensusSequence.java 365 2006-06-28 07:34:56Z pepster $
 */
public abstract class ConsensusSequence implements jebl.evolution.sequences.Sequence {
    /**
     * Creates a FilteredSequence wrapper to the given source sequence.
     *
     * @param source
     */
    public ConsensusSequence(jebl.evolution.taxa.Taxon taxon, Alignment source) {

        this.taxon = taxon;
        this.source = source;
    }

    /**
     * @return the type of symbols that this sequence is made up of.
     */
    public SequenceType getSequenceType() {
        return source.getSequenceType();
    }

    /**
     * @return a string representing the sequence of symbols.
     */
    public String getString() {
        if (sequence == null) {
            sequence = jebl.evolution.sequences.Utils.getStateIndices(constructConsensus(source));
        }

        SequenceType sequenceType = getSequenceType();
        StringBuilder buffer = new StringBuilder();
        for (int i : sequence) {
            buffer.append(sequenceType.getState(i).getCode());
        }
        return buffer.toString();
    }

    /**
     * @return an array of state objects.
     */
    public jebl.evolution.sequences.State[] getStates() {
        if (sequence == null) {
            sequence = jebl.evolution.sequences.Utils.getStateIndices(constructConsensus(source));
        }
        return getSequenceType().toStateArray(sequence);
    }

    public byte[] getStateIndices() {
        if (sequence == null) {
            sequence = jebl.evolution.sequences.Utils.getStateIndices(constructConsensus(source));
        }
        return sequence;
    }

    /**
     * @return the state at site.
     */
    public jebl.evolution.sequences.State getState(int site) {
        if (sequence == null) {
            sequence = jebl.evolution.sequences.Utils.getStateIndices(constructConsensus(source));
        }
        return getSequenceType().getState(sequence[site]);
    }

    /**
     * Returns the length of the sequence
     *
     * @return the length
     */
    public int getLength() {
        if (sequence == null) {
            sequence = jebl.evolution.sequences.Utils.getStateIndices(constructConsensus(source));
        }
        return sequence.length;
    }

    public static jebl.evolution.sequences.State[] constructConsensus(Alignment source) {
        jebl.evolution.sequences.State[] consensus = new jebl.evolution.sequences.State[source.getPatterns().size()];
        int i = 0;
        for (Pattern pattern : source.getPatterns()) {
            consensus[i] = pattern.getMostFrequentState();
            i++;
        }

        return consensus;
    }

    /**
     * @return that taxon that this sequence represents (primarily used to match sequences with tree nodes)
     */
    public jebl.evolution.taxa.Taxon getTaxon() {
        return taxon;
    }

    /**
     * Sequences are compared by their taxa
     *
     * @param o another sequence
     * @return an integer
     */
    public int compareTo(Object o) {
        return taxon.compareTo(((jebl.evolution.sequences.Sequence) o).getTaxon());
    }

    // Attributable implementation

    public void setAttribute(String name, Object value) {
        if (attributeMap == null) {
            attributeMap = new HashMap<String, Object>();
        }
        attributeMap.put(name, value);
    }

    public Object getAttribute(String name) {
        if (attributeMap == null) {
            return null;
        }
        return attributeMap.get(name);
    }

    public Set<String> getAttributeNames() {
        if (attributeMap == null) {
            return Collections.emptySet();
        }
        return attributeMap.keySet();
    }

    // private members

    private final jebl.evolution.taxa.Taxon taxon;
    private final Alignment source;
    private byte[] sequence = null;

    private Map<String, Object> attributeMap = null;

}
