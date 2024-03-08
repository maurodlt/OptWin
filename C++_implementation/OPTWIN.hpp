#include "CircularList.cpp"
#include <vector>

namespace drift_detector { 

class OPTWIN{

public:
    /*
    max_widowLoss: maximum sliding window size
    file_cuts: location of OPTWIN pre-computed cuts (eg. OPTWIN/pre_computed_cuts/cut_30-25000_0.01_0.5r.csv)
    */
    OPTWIN(int max_widowLoss = 1000, std::string file_cuts = "");

    /*
    x: error
    returns true if a concept drift is detected and reset drift detector
    */
    bool update(float x); 
    
private:
    int readCuts(std::string filename);
    void insert_to_W(float loss);
    void add_running_stdev(bool historical, std::vector<float> x);
    void pop_from_running_stdev(bool historical, std::vector<float> x);
    void drift_reaction();

    //store historical loss data
    std::vector<float> historicalLoss;
    float mean_historicalLoss = 1000000000.0;
    float sd_historicalLoss = 0.0;

    //store current loss data
    std::vector<float> currentLoss;

    bool checkConvergence = false;
    bool checkDrift = false;

    int currentDrift = 1;

    int drift_detector_rank = 0;
    int max_widowLoss;

    CircularList W;

    //Running stdev and avg
    float stdev_new = 0;
    float summation_new = 0;
    int count_new = 0;
    float S_new = 0;
    float stdev_h = 0;
    float summation_h = 0;
    int count_h = 0;
    float S_h = 0;
    int itt = 0;

    int last_opt_cut;
    int iteration = 0;
    int delay = 0;
    int min_widowLoss = 30;

    std::vector<int> opt_cut;
    std::vector<float> opt_phi;
    std::vector<float> t_stats;
    std::vector<float> t_warning;
    std::string file_cuts;


    float minimum_noise = 1e-6;
};
};
