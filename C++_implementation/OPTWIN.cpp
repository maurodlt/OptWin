#include "OPTWIN.hpp"
#include <deque>
#include <fstream>
#include <sstream>


using namespace drift_detector;

OPTWIN::OPTWIN(int max_widowLoss, std::string file_cuts){
    this->max_widowLoss = max_widowLoss;
    
    if(file_cuts.empty()){
        char* path_value = std::getenv("OPTWIN_CUT_PATH");
        file_cuts = std::string(path_value) + "/cut_30-25000_0.01_0.5r.csv";
    }


    this->file_cuts = file_cuts;

    readCuts(file_cuts);
}


void OPTWIN::insert_to_W(float loss){
    float popped = this->W.add(loss);
    add_running_stdev(false, std::vector<float>{loss});

    if(popped != -1){ // list is full
        //remove excedent value from running stdev
        pop_from_running_stdev(true, std::vector<float>{popped});

        //walk with sliding window
        pop_from_running_stdev(false, std::vector<float>{W.get(last_opt_cut)});
        add_running_stdev(true, std::vector<float>{W.get(last_opt_cut)});
    }

    this->itt++;
    
    return;
}


void OPTWIN::add_running_stdev(bool historical, std::vector<float> x){
    if(historical){
        for(int i = 0; i < x.size(); i++){
            summation_h += x[i];
            S_h += x[i]*x[i];
        }
        count_h += x.size();

        if (count_h > 1 && S_h > 0){
            stdev_h = sqrt((count_h * S_h)-(summation_h*summation_h)) / count_h;
        }else{
            stdev_h = 0;
        }
    }else{
        for(int i = 0; i < x.size(); i++){
            summation_new += x[i];
            S_new += x[i]*x[i];
        }
        count_new += x.size();
        
        if (count_new > 1 && S_new > 0){
            stdev_new = sqrt((count_new * S_new)-(summation_new*summation_new)) / count_new;
        }else{
            stdev_new = 0;
        }
    }
}

void OPTWIN::pop_from_running_stdev(bool historical, std::vector<float> x){
    if(historical){
        for(int i = 0; i < x.size(); i++){
            summation_h -= x[i];
            S_h -= x[i]*x[i];
        }
        count_h -= x.size();

        if(count_h > 1 && S_h > 0){
            stdev_h = sqrt((count_h * S_h)-(summation_h*summation_h)) / count_h;
        }else{
            stdev_h = 0;
        }
    }else{
        for(int i = 0; i < x.size(); i++){
            summation_new -= x[i];
            S_new -= x[i]*x[i];
        }
        count_new -= x.size();

        if(count_new > 1 && S_new > 0){
            stdev_new = sqrt((count_new * S_new)-(summation_new*summation_new)) / count_new;
        }else{
            stdev_new = 0;
        }
    }

}


bool OPTWIN::update(float x){
    //add new element to window
    iteration++;
    insert_to_W(x);
    delay = 0;


    //check if window is too small
    if(W.length < min_widowLoss){
        return false;
    }

    int optimal_cut = opt_cut[W.length];
    float phi_opt = opt_phi[W.length];

    
    //update running stdev and avg
    if(optimal_cut > last_opt_cut){ //remove elements from window_new and add them to window_h
        pop_from_running_stdev(false, W.getInterval(last_opt_cut, optimal_cut));
        add_running_stdev(true, W.getInterval(last_opt_cut,optimal_cut));
    }else{ //remove elements from window_h and add them to window_new
        pop_from_running_stdev(true, W.getInterval(optimal_cut, last_opt_cut));
        add_running_stdev(false, W.getInterval(optimal_cut, last_opt_cut));
    }

   
    float avg_h = summation_h / count_h;
    float avg_new = summation_new / count_new;
    stdev_h = sqrt((count_h * S_h)-(summation_h*summation_h)) / count_h;
    stdev_new = sqrt((count_new * S_new)-(summation_new*summation_new)) / count_new;

    last_opt_cut = optimal_cut;

    //add minimal noise to stdev
    stdev_h += minimum_noise;
    stdev_new += minimum_noise;

    //check t-stat
    float t_stat_value = t_stats[W.length];

    //t-test
    float t_test_result = (avg_new-avg_h) / (sqrt((stdev_new/(W.length-optimal_cut))+(stdev_h/optimal_cut)));
    if(t_test_result > t_stat_value){
        drift_reaction();
        return true;
    }

    if (((stdev_new*stdev_new)/(stdev_h*stdev_h)) > phi_opt){
        if(avg_h < avg_new){
            drift_reaction();
            return true;
        }
    }
    
    return false;

}

void OPTWIN::drift_reaction(){
    W.reset();
    stdev_new = 0;
    summation_new = 0;
    count_new = 0;
    S_new = 0;
    stdev_h = 0;
    summation_h = 0;
    count_h = 0;
    S_h = 0;
    last_opt_cut = 0;

    currentLoss.clear();
    historicalLoss.clear();
    mean_historicalLoss = 1000000000.0;
    sd_historicalLoss = 0.0;
}

int OPTWIN::readCuts(std::string filename){
    std::ifstream file(filename);

    if (!file.is_open()) {
        std::cerr << "Error opening optwin file" << std::endl;
        return 1;
    }

    std::string line;

    std::getline(file, line);
    std::stringstream lineStream(line);
    std::string cell;


    getline(lineStream, cell, ',');
    float confidence = std::stof(cell);
    getline(lineStream, cell, ',');
    float rho = std::stof(cell);
    getline(lineStream, cell, ',');
    int max_window_size = std::stoi(cell);
    getline(lineStream, cell, ',');
    int min_window_size = std::stoi(cell);
    getline(lineStream, cell, ',');

    for(int i = 0; i < max_window_size+2; i++){
        getline(lineStream, cell, ',');
        opt_cut.push_back(std::stoi(cell));
    }

    for(int i = 0; i < max_window_size+2; i++){
        getline(lineStream, cell, ',');
        opt_phi.push_back(std::stof(cell));
    }

    for(int i = 0; i < max_window_size+2; i++){
        getline(lineStream, cell, ',');
        t_stats.push_back(std::stof(cell));
    }

    for(int i = 0; i < max_window_size+2; i++){
        getline(lineStream, cell, ',');
        t_warning.push_back(std::stof(cell));
    }

    file.close();
    return 0;
}




