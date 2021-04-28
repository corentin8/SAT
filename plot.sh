#!/bin/bash

# compiling
javac -d classes -cp classes:lib/pddl4j-3.8.3.jar:lib/sat4j-pb.jar:lib/sat4j-sat.jar src/fr/uga/pddl4j/tutorial/satplanner/*.java



# running tests
for d in $(ls pddl); do
  echo "Running planner on $d problem"

  for p in $(ls pddl/$d/p*); do
    echo "running HSP on $p"
    hsp=$(java -cp classes:lib/pddl4j-3.8.3.jar:lib/sat4j-pb.jar:lib/sat4j-sat.jar fr.uga.pddl4j.tutorial.HSPPlanner.HSPPlanner  -o pddl/$d/domain.pddl -f $p | tail -1)
    echo $hsp
    res=$(java -cp classes:lib/pddl4j-3.8.3.jar:lib/sat4j-pb.jar:lib/sat4j-sat.jar fr.uga.pddl4j.tutorial.satplanner.SATPlanner -o pddl/$d/domain.pddl -f $p -n 20)
    echo $res
    echo "$hsp $res" >> .tempfile
  done

  # plotting
  python plotter.py $d
  rm .tempfile
done;

