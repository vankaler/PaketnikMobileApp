package com.example.paketnikapp.tsp;

import java.util.ArrayList;

public class GA {

    int popSize;
    double cr; // crossover probability
    double pm; // mutation probability

    ArrayList<TSP.Tour> population;
    ArrayList<TSP.Tour> offspring;

    public GA(int popSize, double cr, double pm) {
        this.popSize = popSize;
        this.cr = cr;
        this.pm = pm;
    }

    public TSP.Tour execute(TSP problem) {
        population = new ArrayList<>();
        offspring = new ArrayList<>();
        TSP.Tour best = null;

        // Initialize population
        for (int i = 0; i < popSize; i++) {
            TSP.Tour newTour = problem.generateTour();
            problem.evaluate(newTour);
            population.add(newTour);

            if (best == null || newTour.getDistance() < best.getDistance()) {
                best = newTour.clone();
            }
        }

        while (problem.getNumberOfEvaluations() < problem.getMaxEvaluations()) {
            // Elitism: retain the best individual
            offspring.add(best.clone());

            // Generate offspring
            while (offspring.size() < popSize) {
                TSP.Tour parent1 = tournamentSelection();
                TSP.Tour parent2 = tournamentSelection();

                // Ensure parents are not the same
                while (parent1 == parent2) {
                    parent2 = tournamentSelection();
                }

                if (RandomUtils.nextDouble() < cr) {
                    TSP.Tour[] children = pmx(parent1, parent2);
                    offspring.add(children[0]);
                    if (offspring.size() < popSize)
                        offspring.add(children[1]);
                } else {
                    offspring.add(parent1.clone());
                    if (offspring.size() < popSize)
                        offspring.add(parent2.clone());
                }
            }

            // Apply mutation
            for (TSP.Tour off : offspring) {
                if (RandomUtils.nextDouble() < pm) {
                    swapMutation(off);
                }
            }

            // Evaluate offspring and find the best
            for (TSP.Tour tour : offspring) {
                problem.evaluate(tour);
                if (tour.getDistance() < best.getDistance()) {
                    best = tour.clone();
                }
            }

            // Replace population with offspring
            population = new ArrayList<>(offspring);
            offspring.clear();
        }
        return best;
    }

    private void swapMutation(TSP.Tour tour) {
        int size = tour.getPath().length;
        int index1 = RandomUtils.nextInt(0, size);
        int index2 = RandomUtils.nextInt(0, size);

        // Swap two cities
        TSP.City temp = tour.getPath()[index1];
        tour.setCity(index1, tour.getPath()[index2]);
        tour.setCity(index2, temp);
    }

    private TSP.Tour[] pmx(TSP.Tour parent1, TSP.Tour parent2) {
        int dimension = parent1.getPath().length;

        // Create offspring tours
        TSP.Tour child1 = new TSP.Tour(dimension);
        TSP.Tour child2 = new TSP.Tour(dimension);

        // Initialize children paths
        for (int i = 0; i < dimension; i++) {
            child1.setCity(i, null);
            child2.setCity(i, null);
        }

        // Generate two random crossover points
        int crossoverPoint1 = RandomUtils.nextInt(0, dimension);
        int crossoverPoint2 = RandomUtils.nextInt(0, dimension);

        // Ensure crossoverPoint1 < crossoverPoint2
        if (crossoverPoint1 > crossoverPoint2) {
            int temp = crossoverPoint1;
            crossoverPoint1 = crossoverPoint2;
            crossoverPoint2 = temp;
        }

        // Copy segment between crossover points from parents
        for (int i = crossoverPoint1; i <= crossoverPoint2; i++) {
            child1.setCity(i, parent2.getPath()[i]);
            child2.setCity(i, parent1.getPath()[i]);
        }

        for (int i = 0; i < dimension; i ++) {
            if (i < crossoverPoint1 || i > crossoverPoint2) {
                map(child1, parent1, child2, i, crossoverPoint1, crossoverPoint2);
                map(child2, parent2, child1, i, crossoverPoint1, crossoverPoint2);
            }
        }

        return new TSP.Tour[]{child1, child2};
    }

    void map(TSP.Tour child, TSP.Tour fromParent, TSP.Tour child2, int i, int c1, int c2) {
        TSP.City candidate = fromParent.getPath()[i];

        while (child.containsCity(candidate, c1, c2)) {
            int currentIndex = child.getIndex(candidate, c1, c2);

            candidate = child2.getPath()[currentIndex];
        }

        child.setCity(i, candidate);
    }

    private TSP.Tour tournamentSelection() {
        TSP.Tour individual1 = population.get(RandomUtils.nextInt(population.size()));
        TSP.Tour individual2 = population.get(RandomUtils.nextInt(population.size()));

        return (individual1.getDistance() < individual2.getDistance()) ? individual1 : individual2;
    }
}
