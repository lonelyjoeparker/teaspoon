package teaspoon.adaptation;

import java.util.Map;

/**
 * Created by jayna on 12/03/16.
 */
public interface Analysis {

    void bmAnalysis();

    void bmAnalysisBootstrap(int bootstraps);

    void w3bAnalysis();

    void w3bAnalysisBootstrap(int bootstraps);

    void getDiversityStats();

    Value[][] getValue_matrix();

    String[] getDatasets();

    int[] getTimepoints_per_dataset();

    int getBootstraps();

    void setCodonMap(int[] map);

    void setWhichMap(Map<String, Integer> which);




}
