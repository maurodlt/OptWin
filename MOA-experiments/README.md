# MOA Experiments

To use OPTWIN within MOA it is necessary to (1) download MOA, (2) replace the moa-release-2021.07.0-bin/lib/moa.jar file by the one in this directory and (3) set the *OPTWIN_CUT_PATH* environmental variable. 

**(1)** One may download MOA directly on the developers [website](https://moa.cms.waikato.ac.nz/). 

**(2)** Then, it is necessary to unzip the donwload file and replace the moa-release-2021.07.0-bin/lib/moa.jar file with the one provided in this directory.

**(3)** At last, simply set the *OPTWIN_CUT_PATH* with the directory in which the pre-computed OPTWIN cuts are located. We provide a few pre-computed cuts in the OPTWIN/pre_computed_cuts directory.

```sh
export OPTWIN_CUT_PATH="PATH_TO_OPTWIN/OPTWIN/pre_computed_cuts"
```

**Important**: The custom moa.jar provided in this repository was built from the moa-release-2021.07.0. If using a different MOA release, it may not work as expected. 

## Building your custom moa.jar 

It is also possible to use the .java files provided in this directory to build your own custom moa.jar, which may be necessary when using different MOA release versions. To do it one must:

**(1)** Download MOA project on github ([MOA repository](https://github.com/Waikato/moa))

**(2)** copy the .java files (OPTWIN.java and CircularList.java) in the moa/tree/master/moa/src/main/java/moa/classifiers/core/driftdetection folder.

**(3)** Build the moa.jar artifact. In IntelliJ, *Build -> Build Artifacts... -> moa.jar -> build*

**(4)** use the custom moa.jar file as described above (the file is usually located in moa/classes/artifacts/moa_jar).

## Directory structure

**scripts**: folder containing the scripts used to run MOA and generate our results.

**results**: folder with the results returned by MOA scripts. Note that we did not include the classification experiments in the repository because they had over 20Gb of raw data. 

**jupyter-visualization**: folder containing jupyter notebooks with the visualization of the raw results returned by MOA.

**OPTWIN.java**: implementation of OPTWIN used on MOA experiments.

**Circular_list.java**: auxiliary structure used by OPTWIN.

**moa.jar**: jar generated when building moa-release-2021.07.0 with OPTWIN.java and Circular_list.java files.
