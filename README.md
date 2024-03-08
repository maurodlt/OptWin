# OptWin

OPTWIN, our "OPTimal WINdow" concept drift detector uses a sliding window of events over an incoming data stream to track the errors of an OL algorithm. OPTWIN allows the concept drift detection on both classification and regression problems and achieve excellent precision and recall. In special, it flags considerably less false positives when compared to other detectors. The novelty of OPTWIN is to consider *both the means and the variances* of the error rates produced by a learner in order to split the sliding window into two provably optimal sub-windows, such that the split occurs at the earliest event at which a statistically significant difference according to either the *t*- or the *f*-tests occurred.


# Repository structure

### OPTWIN.py

the main implementation of OPTWIN on python, which extends the [River library](https://riverml.xyz/0.9.0/examples/concept-drift-detection/). 
    
**dependencies**: river, scipy, and numpy.
```sh
pip install river
pip install scipy
pip install numpy
```

**usage**:

```py
from OPTWIN import Optwin_river
 
#define parameters
w_lenght_max = 25000
w_lenght_min=30
rigor=0.5
error = 1e-2
confidence_final=1-error

#init optwin (also possible to use pre-caluclated optimal cuts)
optwin = Optwin_river(w_lenght_max=w_lenght_max, w_lenght_min=w_lenght_min, rigor = rigor, confidence_final=confidence_final)

#detect drift
## please follow the river drift detection framework https://riverml.xyz/0.9.0/examples/concept-drift-detection/

```

**OPTWIN_pre_compute_cuts.ipynb**: jupyter notebook used to pre-caluclate the optimal sliding window cuts based on the sliding window size. The calcualtion of the optimal cut, despite O(1), involves complex numerical operations and we recommend to perform it beforehand (or use one of the pre-caluclated ones we provide in the *pre_computed_cuts* directory).

**pre_computed_cuts**: directory in which we store some pre-computed cuts. 

**MOA-experiments**: directory with OPTWIN java implementation to run on MOA. Additionally, it also contains the scripts, results, and visualization from our experiments on MOA. 

**C++_implementation**: implementation of OPTWIN in C++.

**ANN-experiments**: comparison of OPTWIN and ADWIN in the detection of concept drifts in the CIFAR-10 usecase.

# Cite us

Tosi, Mauro Dalle Lucca, and Martin Theobald. "OPTWIN: Drift identification with optimal sub-windows." arXiv preprint arXiv:2305.11942 (2023). https://arxiv.org/abs/2305.11942


