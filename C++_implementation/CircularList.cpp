#include <stdexcept>
#include <vector>
#include <iostream>

class CircularList {
private:
    float* W;
    int init;
    int maxSize;

    int actualPosition(int idx) const {
        if (init + idx < maxSize) {
            return init + idx;
        } else {
            return init + idx - maxSize;
        }
    }

public:
    int length;

    CircularList(int maxSize=10000) : maxSize(maxSize), length(0), init(0) {
        W = new float[maxSize](); // Allocate and initialize the array with zeros
    }

    ~CircularList() {
        delete[] W; // Release the allocated memory
    }

    void reset(){
        delete[] W;
        W = new float[maxSize]();
        length = 0;
        init = 0;
    }

    float add(float x) {
        int position = actualPosition(length);

        if(length < maxSize){ //list is not full yet
            length++;
            W[position] = x;
            return -1;
        }else{
            float popped = W[init];
            W[init] = x;
            init = actualPosition(1);
            return popped;
        }
        
    }
    /*
    float popFirst() {
        float x = W[init];
        init = actualPosition(1);
        length--;
        return x;
    }*/

    float get(int idx) const {
        int position = actualPosition(idx);
        return W[position];
    }

    std::vector<float> getInterval(int idx1, int idx2) const {
        int position1 = actualPosition(idx1);
        int position2 = actualPosition(idx2);

        std::vector<float> result;

        if (position1 <= position2) {
            for (int i = position1; i < position2; ++i) {
                result.push_back(W[i]);
            }

        } else {
            std::vector<float> result;
            for (int i = position1; i < maxSize; ++i) {
                result.push_back(W[i]);
            }
            for(int i = 0; i < position2; i++){
                result.push_back(W[i]);
            }
        }

        return result;
    }
};
