#!/bin/bash
OPTWIN_PATH=""
MOA_DIR="PATH_TO_MOA/moa-release-2021.07.0/lib"

RESULT_DIR=$OPTWIN_PATH"/MOA_experiments/results/abrupt_non_binary/"
N_TOTAL=30
declare -a drift_detection=("(OPTWIN -r 0.1)" "(OPTWIN -r 0.5)" "(OPTWIN -r 1.0)" "ADWINChangeDetector" "DDM" "EDDM" "STEPD" "EWMAChartDM" "moa.classifiers.rules.core.changedetection.NoChangeDetection")
cd $MOA_DIR

COMMAND1="EvaluateConceptDrift -l (ChangeDetectorLearner -d "
COMMAND2=") -s (AbruptChangeGenerator -i "
COMMAND3=" -b -p 2000) -i 10000 -f 1 -d "
COMMAND4="_abrupt_not_binary.csv"



for drift_det in "${drift_detection[@]}"
do
  #OPTWIN
  for (( i=1; i <= $N_TOTAL; i++))
  do
    #remove spaces from drfit detection name
    drift_detection_no_space=`echo $drift_det| tr -d ' '` #{echo some text with spaces | tr -d ' '}

    rm $RESULT_DIR$drift_detection_no_space"_"$i$COMMAND4
    COMMAND="$COMMAND1$drift_det$COMMAND2$i$COMMAND3$RESULT_DIR$drift_detection_no_space"_"$i$COMMAND4"
    echo $COMMAND
    java -cp moa.jar -javaagent:sizeofag-1.0.4.jar moa.DoTask "$COMMAND"
  done
done
