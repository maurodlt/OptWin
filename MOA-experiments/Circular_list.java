package moa.classifiers.core.driftdetection;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Drift detection method based in OPTWIN.
 *
 * @author Mauro Dalle Lucca Tosi (mauro.dalleluccatosi@uni.lu)
 * @version $Revision: 1 $
 */

public class Circular_list {
    public double[] w;
    public int lenght;
    public int init;
    public int max_size;

    Circular_list(int max_size){
        this.max_size=max_size;
        this.w = new double[this.max_size];
        this.lenght = 0;
        this.init = 0;
    }

    public int actual_position(int idx){
        if (this.init + idx < this.w.length){
            return this.init+idx;
        }else{
            return this.init+idx-this.w.length;
        }
    }

    public void add(double x){
        int position = actual_position(this.lenght);
        this.lenght += 1;
        this.w[position] = x;
    }

    public double pop_first(){
        double x = this.w[this.init];
        int position = actual_position(1);
        this.init = position;
        this.lenght -= 1;
        return x;
    }

    public double get(int idx){
        int position = actual_position(idx);
        return this.w[position];
    }

    public List<Double> get_interval(int idx1, int idx2){
        int position1 = actual_position(idx1);
        int position2 = actual_position(idx2);
        List<Double> output = (List<Double>) Arrays.stream(this.w);
        if (position1 <= position2){
            return output.subList(position1,position2);

            //return Arrays.asList(this.w).;
            //return Arrays.copyOfRange(this.w, position1, position2);
        }else{
            List<Double> list1 = output.subList(position1,this.w.length);
            list1.addAll(output.subList(0,position2));
            return list1;
        }

    }


}
