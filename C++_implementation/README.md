# OPTWIN C++ implementation

In this directory there is an implementation of OPTWIN in C++. It relies on the CircularList strucutere to ensure O(1) standard deviation calculation. 

To use this code it is only necessary to instanciate the OPTWIN object, which take as input the file containing the pre-computed optwin cuts, and the maximum window size. Then, one may add new values into the sliding window just by calling the *bool* *update(x)* method, where $x$ is a float of the error rate of the learner. If a drift is detected, the $update$ method will return true and reinit the drift detector automatically.

