package jebl.evolution.io;

import java.io.IOException;

import jebl.evolution.sequences.Sequence;
import jebl.util.ProgressListener;

/**
 *
 * A sequence importer sending the sequences back one by one, which makes it
 * possible to import larger documents if handled wisely on the other side.
 * 
 * @author Joseph Heled
 * @version $Id: ImmediateSequenceImporter.java 465 2006-10-04 04:24:20Z twobeers $
 *
 */
public interface ImmediateSequenceImporter {
    public interface Callback {
        void add(Sequence seq);
    }

    void importSequences(Callback callback, ProgressListener progressListener) throws IOException, ImportException;
}
