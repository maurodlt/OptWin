#!/bin/bash
OPTWIN_PATH=""
MOA_DIR="PATH_TO_MOA/moa-release-2021.07.0/lib"

RESULT_DIR=$OPTWIN_PATH"/MOA_experiments/results/nb_results/"
N_TOTAL=30
COUNT=0
declare -a drift_detection=("(OPTWIN -r 0.1)" "(OPTWIN -r 0.5)" "(OPTWIN -r 1.0)" "ADWINChangeDetector" "DDM" "EDDM" "STEPD" "EWMAChartDM" "moa.classifiers.rules.core.changedetection.NoChangeDetection")
cd $MOA_DIR

COMMAND1="EvaluatePrequential -l (drift.DriftDetectionMethodClassifier -d "
COMMAND2=") -s (ConceptDriftStream -s (generators.STAGGERGenerator -i "
COMMAND3=" -f "
COMMAND4=") -d (ConceptDriftStream -s (generators.STAGGERGenerator -i "
COMMAND5=") -d (generators.STAGGERGenerator -i "
COMMADN6=") -p 20000 -w "
COMMAND7=" -r "
COMMAND8=") -e BasicClassificationPerformanceEvaluator -i 100000 -f 1 -q 1 -w 1 -d "
COMMAND9=".csv"
COMMAND10=" > "$RESULT_DIR$COUNT".out"


stream="STAGGERGenerator"
#redefine commands to use correct stream generator
COMMAND2=") -s (ConceptDriftStream -s (generators."$stream" -i "
COMMAND4=") -d (ConceptDriftStream -s (generators."$stream" -i "
COMMAND5=") -d (generators."$stream" -i "

for drift_window in {1,1000} #defines if sudden or gradual drift
do
  for drift_det in "${drift_detection[@]}" #defines drift detection algorithm
  do
    for (( i=1; i <= $N_TOTAL; i++)) #run N_TOTAL times to get statistical significance (with different initializations)
    do
      #remove spaces from drfit detection name
      drift_detection_no_space=`echo $drift_det| tr -d ' '` #{echo some text with spaces | tr -d ' '}

      rm $RESULT_DIR"naive_bayes_"$stream"_"$drift_detection_no_space"_"$i"_"$drift_window$COMMAND9 #remove results if they exist
      COUNT=$(( COUNT + 1 ))
      COMMAND10=" > "$RESULT_DIR$COUNT".out"
      rm $RESULT_DIR$COUNT".out"

      #define command that will be run
      COMMAND="$COMMAND1$drift_det$COMMAND2$i$COMMAND3"1"$COMMAND4$i$COMMAND3"2"$COMMAND4$i$COMMAND3"3"$COMMAND4$i$COMMAND3"1"$COMMAND5$i$COMMAND3"2"$COMMADN6$drift_window$COMMAND7"1"$COMMADN6$drift_window$COMMAND7"1"$COMMADN6$drift_window$COMMAND7"1"$COMMADN6$drift_window$COMMAND7"1"$COMMAND8$RESULT_DIR"naive_bayes_"$stream"_"$drift_detection_no_space"_"$i"_"$drift_window$COMMAND9"

      echo $COMMAND
      java -cp moa.jar -javaagent:sizeofag-1.0.4.jar moa.DoTask "$COMMAND" #run
    done
  done
done

stream="AgrawalGenerator"
#redefine commands to use correct stream generator
COMMAND2=") -s (ConceptDriftStream -s (generators."$stream" -i "
COMMAND4=") -d (ConceptDriftStream -s (generators."$stream" -i "
COMMAND5=") -d (generators."$stream" -i "

for drift_window in {1,1000} #defines if sudden or gradual drift
do
  for drift_det in "${drift_detection[@]}" #defines drift detection algorithm
  do
    for (( i=1; i <= $N_TOTAL; i++)) #run N_TOTAL times to get statistical significance (with different initializations)
    do
      #remove spaces from drfit detection name
      drift_detection_no_space=`echo $drift_det| tr -d ' '` #{echo some text with spaces | tr -d ' '}

      rm $RESULT_DIR"naive_bayes_"$stream"_"$drift_detection_no_space"_"$i"_"$drift_window$COMMAND9 #remove results if they exist

      #define command that will be run
      COMMAND="$COMMAND1$drift_det$COMMAND2$i$COMMAND3"5"$COMMAND4$i$COMMAND3"4"$COMMAND4$i$COMMAND3"3"$COMMAND4$i$COMMAND3"2"$COMMAND5$i$COMMAND3"1"$COMMADN6$drift_window$COMMAND7"1"$COMMADN6$drift_window$COMMAND7"1"$COMMADN6$drift_window$COMMAND7"1"$COMMADN6$drift_window$COMMAND7"1"$COMMAND8$RESULT_DIR"naive_bayes_"$stream"_"$drift_detection_no_space"_"$i"_"$drift_window$COMMAND9"
      COUNT=$(( COUNT + 1 ))
      COMMAND10=" > "$RESULT_DIR$COUNT".out"
      rm $RESULT_DIR$COUNT".out"

      echo $COMMAND
      java -cp moa.jar -javaagent:sizeofag-1.0.4.jar moa.DoTask "$COMMAND" #run
    done
  done
done

stream="RandomRBFGenerator"
#redefine commands to use correct stream generator
COMMAND2=") -s (ConceptDriftStream -s (generators."$stream" -i "
COMMAND3=" -i "
COMMAND3_2=" -r "
COMMAND4=") -d (ConceptDriftStream -s (generators."$stream" -i "
COMMAND5=") -d (generators."$stream" -i "

for drift_window in {1,1000} #defines if sudden or gradual drift
do
  for drift_det in "${drift_detection[@]}" #defines drift detection algorithm
  do
    for (( i=1; i <= $N_TOTAL; i++)) #run N_TOTAL times to get statistical significance (with different initializations)
    do
      #remove spaces from drfit detection name
      drift_detection_no_space=`echo $drift_det| tr -d ' '` #{echo some text with spaces | tr -d ' '}

      rm $RESULT_DIR"naive_bayes_"$stream"_"$drift_detection_no_space"_"$i"_"$drift_window$COMMAND9 #remove results if they exist
      COUNT=$(( COUNT + 1 ))
      COMMAND10=" > "$RESULT_DIR$COUNT".out"
      rm $RESULT_DIR$COUNT".out"
      
      #define command that will be run
      COMMAND="$COMMAND1$drift_det$COMMAND2$i$COMMAND3"1"$COMMAND3_2"1"$COMMAND4$i$COMMAND3"2"$COMMAND3_2"2"$COMMAND4$i$COMMAND3"3"$COMMAND3_2"3"$COMMAND4$i$COMMAND3"4"$COMMAND3_2"4"$COMMAND5$i$COMMAND3"5"$COMMAND3_2"5"$COMMADN6$drift_window$COMMAND7"1"$COMMADN6$drift_window$COMMAND7"1"$COMMADN6$drift_window$COMMAND7"1"$COMMADN6$drift_window$COMMAND7"1"$COMMAND8$RESULT_DIR"naive_bayes_"$stream"_"$drift_detection_no_space"_"$i"_"$drift_window$COMMAND9"

      echo $COMMAND
      java -cp moa.jar -javaagent:sizeofag-1.0.4.jar moa.DoTask "$COMMAND" #run
    done
  done
done






stream="Electricity"
COMMAND2=") -s (ArffFileStream -f /Users/mauro.dalleluccatosi/Downloads/electricity-normalized.arff) -e BasicClassificationPerformanceEvaluator -i 100000 -f 1 -q 1 -w 1 -d "
for drift_det in "${drift_detection[@]}" #defines drift detection algorithm
do
  #remove spaces from drfit detection name
  drift_detection_no_space=`echo $drift_det| tr -d ' '` #{echo some text with spaces | tr -d ' '}

  rm $RESULT_DIR"naive_bayes_"$stream"_"$drift_detection_no_space$COMMAND9 #remove results if they exist
  COUNT=$(( COUNT + 1 ))
  COMMAND10=" > "$RESULT_DIR$COUNT".out"
  rm $RESULT_DIR$COUNT".out"
  
  #define command that will be run
  COMMAND="$COMMAND1$drift_det$COMMAND2$RESULT_DIR"naive_bayes_"$stream"_"$drift_detection_no_space$COMMAND9"

  echo $COMMAND
  java -cp moa.jar -javaagent:sizeofag-1.0.4.jar moa.DoTask "$COMMAND" #run

done


stream="Covertype"
COMMAND2=") -s (ArffFileStream -f /Users/mauro.dalleluccatosi/Downloads/covtypeNorm.arff) -e BasicClassificationPerformanceEvaluator -i 600000 -f 1 -q 1 -w 1 -d "
for drift_det in "${drift_detection[@]}" #defines drift detection algorithm
do
  #remove spaces from drfit detection name
  drift_detection_no_space=`echo $drift_det| tr -d ' '` #{echo some text with spaces | tr -d ' '}

  rm $RESULT_DIR"naive_bayes_"$stream"_"$drift_detection_no_space$COMMAND9 #remove results if they exist
  COUNT=$(( COUNT + 1 ))
  COMMAND10=" > "$RESULT_DIR$COUNT".out"
  rm $RESULT_DIR$COUNT".out"

  #define command that will be run
  COMMAND="$COMMAND1$drift_det$COMMAND2$RESULT_DIR"naive_bayes_"$stream"_"$drift_detection_no_space$COMMAND9"

  echo $COMMAND
  java -cp moa.jar -javaagent:sizeofag-1.0.4.jar moa.DoTask "$COMMAND" #run

done

