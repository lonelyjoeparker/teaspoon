/*
 * SequenceImporter.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.io;

import java.io.IOException;
import java.util.List;

import jebl.evolution.sequences.Sequence;

/**
 * Interface for importers that do sequences
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: SequenceImporter.java 442 2006-09-05 21:59:20Z matt_kearse $
 */
public interface SequenceImporter {

	/**
	 * importSequences.
	 */
	List<Sequence> importSequences() throws IOException, ImportException;
}
