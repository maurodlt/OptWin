#Optwin
from river.base import DriftDetector
from river import drift
import numpy as np
from scipy.stats import t as t_stat
from scipy.optimize import fsolve
import scipy.stats
import math

import warnings

class Optwin_river(DriftDetector):
    class Circular_list():
        def actual_position(self, idx):
            if self.init + idx < len(self.W):
                return self.init+idx
            else:
                return self.init + idx - len(self.W)

        def add(self, x):
            position = self.actual_position(self.lenght)
            self.lenght = self.lenght +1
            self.W[position] = x

        def pop_first(self):
            x = self.W[self.init]
            position = self.actual_position(1)
            self.init = position
            self.lenght = self.lenght-1
            return x

        def get(self, idx):
            position = self.actual_position(idx)
            return self.W[position]

        def get_interval(self, idx1, idx2):
            position1 = self.actual_position(idx1)
            position2 = self.actual_position(idx2)
            if position1 <= position2:
                return self.W[position1:position2]
            else:
                return self.W[position1:]+self.W[:position2]

        def __init__(self, maxSize):
            self.W = [0 for i in range(maxSize)]
            self.lenght = 0
            self.init = 0        
    
    #pre-compute optimal cut for all possible window sizes
    def pre_compute_cuts(self, opt_cut, opt_phi, t_stats, t_stats_warning):
        if len(opt_cut) != 0 and len(opt_phi) != 0 and len(t_stats) != 0 and len(t_stats_warning) != 0:
            return opt_cut, opt_phi, t_stats, t_stats_warning
        self.W = self.Circular_list(self.w_lenght_max)
        for i in range(self.w_lenght_max+1):
            if i < self.w_lenght_min:
                opt_cut.append(0)
                opt_phi.append(0)
                t_stats.append(0.0)
                t_stats_warning.append(0.0)
            else:
                optimal_cut = fsolve(self.t_test, (self.W.lenght-30)/self.W.lenght)
                #check if opt_cut was found
                tolerance = 1e-6
                if abs(self.t_test(optimal_cut[0])) <= tolerance:
                    optimal_cut = math.floor(optimal_cut[0]*self.W.lenght) #parse to integer
                else: #opt_cut not found
                    optimal_cut = math.floor((self.W.lenght/2)+1)
                
                #phi_opt = scipy.stats.f.ppf(q=self.confidence_two_tailes, dfn=optimal_cut-1, dfd=len(self.W)-optimal_cut-1) 
                phi_opt = scipy.stats.f.ppf(q=self.confidence, dfn=optimal_cut-1, dfd=self.W.lenght-optimal_cut-1) 
                opt_cut.append(optimal_cut)
                opt_phi.append(phi_opt)
                t_stats.append(self.t_score(optimal_cut/i))
                t_stats_warning.append(self.t_score_warning(optimal_cut/i))
            self.W.add(1)
        self.W = self.Circular_list(self.w_lenght_max)
        
        return opt_cut, opt_phi, t_stats, t_stats_warning
    
    def insert_to_W(self, x):
        self.W.add(x)
        
        #add new value to running stdev
        self.stdev_new, self.summation_new, self.count_new, self.S_new = self.add_running_stdev(self.summation_new, self.count_new, self.S_new, [x])
        
        #check if window is too large
        if self.W.lenght > self.w_lenght_max:
            pop = self.W.pop_first()
            #pop = self.W.popleft()
            
            #remove excedent value from running stdev
            self.stdev_h, self.summation_h, self.count_h, self.S_h = self.pop_from_running_stdev(self.summation_h, self.count_h, self.S_h, [pop])
            #walk with sliding window
            self.stdev_new, self.summation_new, self.count_new, self.S_new = self.pop_from_running_stdev(self.summation_new, self.count_new, self.S_new, [self.W.get(self.last_opt_cut)])
            self.stdev_h, self.summation_h, self.count_h, self.S_h = self.add_running_stdev(self.summation_h, self.count_h, self.S_h, [self.W.get(self.last_opt_cut)])
            
        self.itt += 1
        return
    
    #https://stats.stackexchange.com/questions/24878/computation-of-new-standard-deviation-using-old-standard-deviation-after-change
    def add_running_stdev(self, summation, count, S, x):
        summation += sum(x)
        count += len(x)
        S += sum([i*i for i in x])
                
        if (count > 1 and S > 0):
            stdev = math.sqrt((count*S) - (summation*summation)) / count
            return stdev, summation, count, S
        else:
            return 0, summation, count, S
        
    def pop_from_running_stdev(self,summation, count, S, x):
        summation -= sum(x)
        count -= len(x)
        S -= sum([i*i for i in x])
        
        if (count > 1 and S > 0):
            stdev = math.sqrt((count*S) - (summation*summation)) / count
            return stdev, summation, count, S
        else:
            return 0, summation, count, S 
    
    #add new element to window
    def update(self, x):
        #add new element to window
        self.iteration += 1
        self.insert_to_W(x)
        self.delay = 0
        
        #check if window is too small
        if self.W.lenght < self.w_lenght_min:
            self._drift_detected = False
            self.in_warning_zone = False
            return False
        
        #check optimal window cut and phi
        #get pre-calculated optimal window cut and phi
        optimal_cut = self.opt_cut[self.W.lenght]
        phi_opt = self.opt_phi[self.W.lenght]
                     
        #update running stdev and avg
        if optimal_cut > self.last_opt_cut: #remove elements from window_new and add them to window_h
            self.stdev_new, self.summation_new, self.count_new, self.S_new = self.pop_from_running_stdev(self.summation_new, self.count_new, self.S_new, self.W.get_interval(self.last_opt_cut,optimal_cut))
            self.stdev_h, self.summation_h, self.count_h, self.S_h = self.add_running_stdev(self.summation_h, self.count_h, self.S_h, self.W.get_interval(self.last_opt_cut,optimal_cut))
            #using deque
            #self.stdev_new, self.summation_new, self.count_new, self.S_new = self.pop_from_running_stdev(self.summation_new, self.count_new, self.S_new, list(islice(self.W,self.last_opt_cut,optimal_cut)))
            #self.stdev_h, self.summation_h, self.count_h, self.S_h = self.add_running_stdev(self.summation_h, self.count_h, self.S_h, list(islice(self.W,self.last_opt_cut,optimal_cut)))
        elif optimal_cut < self.last_opt_cut: #remove elements from window_h and add them to window_new
            self.stdev_h, self.summation_h, self.count_h, self.S_h = self.pop_from_running_stdev(self.summation_h, self.count_h, self.S_h, self.W.get_interval(optimal_cut,self.last_opt_cut))
            self.stdev_new, self.summation_new, self.count_new, self.S_new = self.add_running_stdev(self.summation_new, self.count_new, self.S_new, self.W.get_interval(optimal_cut,self.last_opt_cut))
            #using deque
            #self.stdev_h, self.summation_h, self.count_h, self.S_h = self.pop_from_running_stdev(self.summation_h, self.count_h, self.S_h, list(islice(self.W,optimal_cut,self.last_opt_cut)))
            #self.stdev_new, self.summation_new, self.count_new, self.S_new = self.add_running_stdev(self.summation_new, self.count_new, self.S_new, list(islice(self.W,optimal_cut,self.last_opt_cut)))

        avg_h = self.summation_h / self.count_h
        avg_new = self.summation_new / self.count_new
        #stdev_h = self.stdev_h
        #stdev_new = self.stdev_new
        stdev_h = math.sqrt((self.count_h*self.S_h) - (self.summation_h*self.summation_h)) / self.count_h
        stdev_new = math.sqrt((self.count_new*self.S_new) - (self.summation_new*self.summation_new)) / self.count_new
        
        self.last_opt_cut = optimal_cut

        
        #add minimal noise to stdev
        stdev_h += self.minimum_noise
        stdev_new += self.minimum_noise
        
        #check t-stat
        if self.pre_compute_optimal_cut:
            t_stat = self.t_stats[self.W.lenght]
            t_stat_warning = self.t_stats_warning[self.W.lenght]
        else:
            t_stat = self.t_score(optimal_cut/self.W.lenght)
            t_stat_warning = self.t_score_warning(optimal_cut/self.W.lenght)
        
        
        #t-test
        t_test_result = (avg_new-avg_h) / (math.sqrt((stdev_new/(self.W.lenght-optimal_cut))+(stdev_h/optimal_cut)))
        if  t_test_result > t_stat:
            self.drift_reaction("t")
            self.insert_to_W(x)
            return True
        elif t_test_result > t_stat_warning:
            self.in_warning_zone = True
            #return False
        else:
            self.in_warning_zone = False
            self._drift_detected = False
        
        
        #check only one side of f and t-test (if the loss decreases it means that the model is learning, not that a concept drift occurred
        #f-test
        if (stdev_new*stdev_new/(stdev_h*stdev_h)) > phi_opt:
            #self.drift_reaction("f")
            #return True
        
            if avg_h - avg_new < 0:
                self.drift_reaction("f")
                self.insert_to_W(x)
                return True
            else:
                self.empty_window()
                self.insert_to_W(x)
                self._drift_detected = False
                self.in_warning_zone = False
                return False
        
        self._drift_detected = False
        self.in_warning_zone = False
        return False
    
    def drift_reaction(self, drift_type):            
        self.drift_type.append(drift_type)
        self.drifts.append(self.iteration)                
        self.drift_detected_last_it = True
        self._drift_detected = True
        self.empty_window()
        
        return True
        
    def _reset(self):
        self.empty_window()
        self.drift_detected_last_it = False
        self._drift_detected = False
        super()._reset()
        
    
    def empty_window(self):
        self.W = self.Circular_list(self.w_lenght_max)
        self.stdev_new = 0
        self.summation_new = 0
        self.count_new = 0
        self.S_new = 0
        self.stdev_h = 0
        self.summation_h = 0
        self.count_h = 0
        self.S_h = 0
        self.last_opt_cut = 0
            
    def detected_change(self):
        return self.drift_detected_last_it
    
    def get_length_estimation(self):
        self.estimation = self.W.lenght
        return self.W.lenght
    
    def detected_warning_zone(self):
        return self.in_warning_zone
    
    def __init__(self, confidence_final = 0.999, rigor = 0.5, empty_w=True, w_lenght_max = 50000, w_lenght_min = 30, minimum_noise = 1e-6, opt_cut = [], opt_phi = [], t_stats = [], t_stats_warning = []):
        #init variables
        super().__init__()
        warnings.filterwarnings('ignore', 'The iteration is not making good progress')
        warnings.filterwarnings("ignore", message="divide by zero encountered in divide")
        self.confidence_final = confidence_final #confidence value chosen by user
        self.rigor = rigor #rigorousness of drift identification
        self.w_lenght_max = w_lenght_max #maximum window size
        self.w_lenght_min = w_lenght_min #minimum window size
        self.minimum_noise = minimum_noise #noise to be added to stdev in case it is 0
        self.pre_compute_optimal_cut = True #pre_compute all possible window sizes?
        self.empty_w = empty_w #empty window when drift is detected
        
        self.W =  self.Circular_list(w_lenght_max)
        self.opt_cut = opt_cut #pre-calculated optimal cut for all possible windows
        self.opt_phi = opt_phi #pre-calculated optimal phi for all possible windows
        self.t_stats = t_stats
        self.t_stats_warning = t_stats_warning
        self.last_opt_cut = 0
        self.drifts = [] #drifts identified 
        self.drift_type = [] #types of drifts identified
        self.iteration = 0 #current iteration step
        self.confidence = pow(self.confidence_final, 1/4) #confidence used on the t-test
        self.confidence_warning = 0.98
        #self.confidence_two_tailes = 1-(1-self.confidence)/2 #confidence used on the f-test
        self.t_score = lambda n : t_stat.ppf(self.confidence, df=self.degree_freedom(n)) #t_value to achieve desired confidence
        self.t_score_warning = lambda n : t_stat.ppf(self.confidence_warning, df=self.degree_freedom(n)) #t_value to achieve desired confidence
        self.f_test = lambda n : scipy.stats.f.ppf(q=self.confidence, dfn=(n*self.W.lenght)-1, dfd=self.W.lenght-(n*self.W.lenght)-1) #f-test formula
        self.degree_freedom = lambda n : pow(((1/max(self.W.lenght*n,1e-15))+((1/pow(self.f_test(n),2))/((1-n)*self.W.lenght))),2)/((1/max((pow((self.W.lenght*n),2)*((self.W.lenght*n)-1)),1e-15))+(pow((1/pow(self.f_test(n),2)),2)/max((pow(((1-n)*self.W.lenght),2)*(((1-n)*self.W.lenght)-1)),1e-15)))
        self.t_test = lambda n : self.rigor - (self.t_score(n) * np.sqrt((1/(self.W.lenght*n))+((1* self.f_test(n))/((1-n)*self.W.lenght)))) #t-test formula (stdev_h=1 because it is cancelled during solution)
    
        #Running stdev and avg
        self.stdev_new = 0
        self.summation_new = 0
        self.count_new = 0
        self.S_new = 0
        self.stdev_h = 0
        self.summation_h = 0
        self.count_h = 0
        self.S_h = 0
        
        self.itt = 0
    
        self.drift_detected_last_it = False
        self.in_concept_change = False
        self.in_warning_zone = False
        self.estimation = 0.0
        self.delay = 0.0
        self.sequence_drifts = 0
        self.sequence_no_drifts = 0
    
        #pre-compute optimal cut for all possible window sizes (if True)
        if self.pre_compute_optimal_cut:
            self.opt_cut, self.opt_phi, self.t_stats, self.t_stats_warning = self.pre_compute_cuts(self.opt_cut, self.opt_phi, self.t_stats, self.t_stats_warning)
            
        if len(self.opt_cut) == 0:
            self.opt_cut = [0 for i in range(w_lenght_min)]
            self.opt_phi = [0 for i in range(w_lenght_min)]
            self.t_stats = [0.0 for i in range(w_lenght_min)]
            self.t_stats_warning = [0.0 for i in range(w_lenght_min)]
            
        if len(self.opt_cut) >= w_lenght_max and len(self.opt_phi) >= w_lenght_max:
            self.pre_compute_optimal_cut = True
            