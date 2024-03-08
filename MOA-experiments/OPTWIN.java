package moa.classifiers.core.driftdetection;
//import com.github.javacliparser.FloatOption;
import com.github.javacliparser.MultiChoiceOption;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.lang.Math;
import moa.classifiers.core.driftdetection.Circular_list;


public class OPTWIN extends AbstractChangeDetector {

    public MultiChoiceOption rigorness = new MultiChoiceOption(
            "rigor", 'r', "Rigor level.", new String[]{
            "0.05", "0.075", "0.1", "0.15", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0"}, new String[]{
            "0.05",
            "0.075",
            "0.1",
            "0.15",
            "0.2",
            "0.3",
            "0.4",
            "0.5",
            "0.6",
            "0.7",
            "0.8",
            "0.9",
            "1.0"}, 7);

    public MultiChoiceOption warning_confidence = new MultiChoiceOption(
            "warning_confidence", 'w', "Warning confidence level", new String[]{
            "0.95", "0.98", "0.99", "0.999", "0.9999", "0.99999", "0.999999"}, new String[]{
            "0.95",
            "0.98",
            "0.99",
            "0.999",
            "0.9999",
            "0.99999",
            "0.999999"}, 1);

    private double confidence_final;
    private double rigor;
    private int max_window_lenght;
    private int min_window_lenght;
    private double minimum_noise;
    private List<Integer> opt_cut;
    private List<Double> opt_phi;
    private List<Double> t_stat;
    private List<Double> t_stat_warning;
    private List<Double> w;
    private int last_opt_cut = 0;
    private List<Integer> drifts;
    private int iteration;
    private double stdev_new = 0;
    private double summation_new = 0;
    private int count_new = 0;
    private double S_new = 0;
    private double stdev_h = 0;
    private double summation_h = 0;
    private int count_h = 0;
    private double S_h = 0;

    private String std_rigor = "0.5";
    private String std_warning = "0.99";

    private boolean warningDetection = true;
    private double warning_rigor;

    private int itt;

    private boolean first_iteration = true;

    // Read the value of the environment variable
    private String optwin_path_cuts = System.getenv("OPTWIN_CUT_PATH");
    private String fileDrift = optwin_path_cuts+"/cut_30-25000_0.01_";
    private String fileWarning = "NA";


    public OPTWIN() {
        this.minimum_noise = 1e-6;
        this.itt = 0;
        this.iteration = 0;
        this.drifts = new ArrayList<Integer>();
        if (optwin_path_cuts == null || optwin_path_cuts.isEmpty()){
            System.out.println("\n\nYou forgot to define OPTWIN_CUT_PATH. Please define it with the path in which the optwin cuts are stored (e.g. optwin/pre_computed_cuts.)\n\n");
        }
        String fileDriftComplete = this.fileDrift+std_rigor+"r.csv";
        String fileWarningComplete = this.fileWarning+std_warning+".csv";
        initOptimalCuts(fileDriftComplete, fileWarningComplete);
        this.w = new Vector<Double>(this.max_window_lenght);
        resetLearning();
        this.isInitialized = true;
    }

    private void initOptimalCuts(String fileDrift, String fileWarning){
        Scanner sc;
        try {
            sc = new Scanner(new File(fileDrift));

            sc.useDelimiter(",");   //sets the delimiter pattern

            this.confidence_final = Double.parseDouble(sc.next());
            this.rigor = Double.parseDouble(sc.next());
            this.max_window_lenght = Integer.parseInt(sc.next());
            this.min_window_lenght = Integer.parseInt(sc.next());
            this.opt_cut = new Vector<Integer>(this.max_window_lenght + 5);
            this.opt_phi = new Vector<Double>(this.max_window_lenght + 5);
            this.t_stat = new Vector<Double>(this.max_window_lenght + 5);
            this.t_stat_warning = new Vector<Double>(this.max_window_lenght + 5);

            //read opt_cut
            for (int i = 0; i < this.max_window_lenght + 2; i ++){
                this.opt_cut.add(Integer.parseInt(sc.next()));
            }

            //read opt_phi
            for (int i = 0; i < this.max_window_lenght + 2; i ++){
                this.opt_phi.add(Double.parseDouble(sc.next()));
            }

            //read t_stat
            for (int i = 0; i < this.max_window_lenght + 2; i ++){
                this.t_stat.add(Double.parseDouble(sc.next()));
            }

            //read t_stat
            for (int i = 0; i < this.max_window_lenght + 2; i ++){
                this.t_stat_warning.add(Double.parseDouble(sc.next()));
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        /*
        //read warning levels for t-test
        try {
            sc = new Scanner(new File(fileWarning));
            sc.useDelimiter(",");   //sets the delimiter pattern

            this.t_stat_warning = new Vector<Double>(this.max_window_lenght + 5);

            if (Double.parseDouble(sc.next()) != Double.parseDouble(this.std_warning)){
                System.out.println("ERROR! Warning defined is different from the one read from file. Warnings are now deactivated...");
                this.t_stat_warning = this.t_stat;
                throw new RuntimeException();
            }

            //read t_stat_warning
            for (int i = 0; i < this.max_window_lenght + 2; i ++){
                this.t_stat_warning.add(Double.parseDouble(sc.next()));
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }*/

    }

    @Override
    public void resetLearning() {

        this.w.clear();
        this.stdev_new = 0;
        this.stdev_h = 0;
        this.count_new = 0;
        this.count_h = 0;
        this.summation_h = 0;
        this.summation_new = 0;
        this.S_new = 0;
        this.S_h = 0;
        this.last_opt_cut = 0;
        this.itt = 0;
        this.isChangeDetected = false;
        this.isWarningZone = false;

        boolean reinit = false;

        if (!this.std_rigor.equals(this.rigorness.getChosenLabel())){
            reinit = true;
            this.std_rigor = this.rigorness.getChosenLabel();
        }
        if (!this.std_warning.equals(this.warning_confidence.getChosenLabel())){
            reinit = true;
            this.std_warning = this.warning_confidence.getChosenLabel();
        }
        if (reinit) {
            initOptimalCuts(this.fileDrift + this.std_rigor + "r.csv", this.fileWarning + this.std_warning + ".csv");
        }
    }

    private void insert_to_w(double x){
        boolean reinit = false;
        if (!this.std_rigor.equals(this.rigorness.getChosenLabel())){
            reinit = true;
            this.std_rigor = this.rigorness.getChosenLabel();
        }
        if (!this.std_warning.equals(this.warning_confidence.getChosenLabel())){
            reinit = true;
            this.std_warning = this.warning_confidence.getChosenLabel();
        }
        if (reinit) {
            initOptimalCuts(this.fileDrift + this.std_rigor + "r.csv", this.fileWarning + this.std_warning + ".csv");
        }

        w.add(x);
        this.stdev_new = add_running_stdev(this.summation_new, this.count_new, this.S_new,  Collections.singletonList(x), false);
        this.estimation = this.summation_new / this.count_new;

        if(w.size() > this.max_window_lenght){
            double pop = w.remove(0);
            this.stdev_h = pop_from_running_stdev(this.summation_h, this.count_h, this.S_h, Collections.singletonList(pop), true);
            this.stdev_new = pop_from_running_stdev(this.summation_new, this.count_new, this.S_new, Collections.singletonList(w.get(this.last_opt_cut)), false);
            this.stdev_h = add_running_stdev(this.summation_h, this.count_h, this.S_h, Collections.singletonList(w.get(this.last_opt_cut)), true);
        }
        this.itt += 1;

    }

    private double pop_from_running_stdev(double summation, int count, double S, List<Double> x, boolean historic){
        double x_value = x.get(0);
        x = x.subList(1, x.size());

        summation -= x_value;
        count -= 1;
        S -= Math.pow(x_value, 2);

        if(!x.isEmpty()){
            return pop_from_running_stdev(summation, count, S, x, historic);
        }

        if (count <= 0){
            S = 0;
            count = 0;
            summation = 0;
        }

        if(historic){
            this.S_h = S;
            this.count_h = count;
            this.summation_h = summation;
        }else {
            this.S_new = S;
            this.count_new = count;
            this.summation_new = summation;
        }

        if(count == 0){
            return 0.0;
        }

        Double stdev = Math.sqrt(count*S - Math.pow(summation, 2))/count;
        //add minimum noise to stdev
        if (stdev.isNaN()) {
            stdev = 0.0;
        }
        if (stdev.isNaN()) {
            stdev = 0.0;
        }

        return stdev;

    }

    private double add_running_stdev(double summation, int count, double S, List<Double> x, boolean historic){
        double x_value = x.get(0);
        x = x.subList(1, x.size());

        summation += x_value;
        count += 1;
        S += Math.pow(x_value, 2);

        if(!x.isEmpty()){
            return add_running_stdev(summation, count, S, x, historic);
        }

        if(historic){
            this.S_h = S;
            this.count_h = count;
            this.summation_h = summation;
        }else {
            this.S_new = S;
            this.count_new = count;
            this.summation_new = summation;
        }

        Double stdev = Math.sqrt(count*S - Math.pow(summation, 2))/count;
        //add minimum noise to stdev
        if (stdev.isNaN()) {
            stdev = 0.0;
        }

        return stdev;

    }




    @Override
    public void input(double prediction) {

        //prediction += 1;
        this.iteration += 1;
        insert_to_w(prediction);
        this.delay = 0;

        if(w.size() < this.min_window_lenght){
            this.isChangeDetected = false;
            this.isWarningZone = false;
            return;
        }
        //get optimal cuts
        int optimal_cut = this.opt_cut.get(w.size());
        Double optimal_phi = this.opt_phi.get(w.size());

        //update stdev and avg
        if(optimal_cut > this.last_opt_cut){
            //remove elements from window_new and add to window_h
            List<Double> element_list = w.subList(this.last_opt_cut, optimal_cut);
            this.stdev_new = pop_from_running_stdev(this.summation_new, this.count_new, this.S_new, element_list, false);
            this.stdev_h = add_running_stdev(this.summation_h, this.count_h, this.S_h, element_list, true);
        }else if (optimal_cut < this.last_opt_cut){
            //remove elements from window_h and add to window_new
            List<Double> element_list = w.subList(optimal_cut, this.last_opt_cut);
            this.stdev_h = pop_from_running_stdev(this.summation_h, this.count_h, this.S_h, element_list, true);
            this.stdev_new = add_running_stdev(this.summation_new, this.count_new, this.S_new, element_list, false);
        }


        double avg_h = this.summation_h / this.count_h;
        double avg_new = this.summation_new / this.count_new;
        double stdev_h = this.stdev_h;
        double stdev_new = this.stdev_new;
        this.last_opt_cut = optimal_cut;

        //if(stdev_new == 0 || stdev_h == 0){
        stdev_new += this.minimum_noise;
        stdev_h += this.minimum_noise;
        //}

        this.estimation = avg_new;
        //check one side of f and t tests
        //f-test

        //t-test
        double t_test_result = (avg_new-avg_h)/(Math.sqrt((stdev_new/(w.size()-optimal_cut))+(stdev_h/optimal_cut)));
        if( t_test_result > this.t_stat.get(w.size())){
            drift_reaction();
            insert_to_w(prediction);
            return;
        } else if (t_test_result > this.t_stat_warning.get(w.size())) {
            this.isWarningZone = true;
        }else{
            this.isWarningZone = false;
            this.isChangeDetected = false;
        }

        if((stdev_new*stdev_new)/(stdev_h*stdev_h) > optimal_phi) {
            //drift_reaction();
            //insert_to_w(prediction);
            //return;
            if (avg_h - avg_new < 0.0) {
                drift_reaction();
                insert_to_w(prediction);
                return;
            }/*else {
                resetLearning();
                insert_to_w(prediction);
                this.isChangeDetected = false;
                this.isWarningZone = false;
                return;
            }*/
        }

    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
    }

    private void drift_reaction(){

        this.drifts.add(this.iteration);
        resetLearning();
        this.estimation = 0.0;
        this.delay = 0;
        this.isChangeDetected = true;
        this.isWarningZone = false;
        this.isInitialized = true;
    }


    @Override
    protected void prepareForUseImpl(moa.tasks.TaskMonitor monitor, moa.core.ObjectRepository repository) {
        // TODO Auto-generated method stub
    }
}
