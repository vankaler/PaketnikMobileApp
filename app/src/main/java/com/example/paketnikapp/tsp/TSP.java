package com.example.paketnikapp.tsp;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TSP {

    private final Context context;

    enum DistanceType {EUCLIDEAN, EXPLICIT}

    public class City {
        public int index;
        public double x, y;

        @NonNull
        @Override
        public String toString() {
            return index + ":" + x + ":" + y;
        }
    }

    public static class Tour {

        double distance;
        int dimension;
        City[] path;

        public Tour(Tour tour) {
            distance = tour.distance;
            dimension = tour.dimension;
            path = tour.path.clone();
        }

        public Tour(int dimension) {
            this.dimension = dimension;
            path = new City[dimension];
            distance = Double.MAX_VALUE;
        }

        public Tour clone() {
            return new Tour(this);
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public City[] getPath() {
            return path;
        }

        public int getIndex(City of, int start, int end) {
            for (int i = start; i <= end; i ++) {
                if (path[i] == of) {
                    return i;
                }
            }
            return -1;
        }

        public boolean containsCity(City checkCity, int start, int end) {
            for (int i = start; i <= end; i++) {
                if (path[i].equals(checkCity)) {
                    return true;
                }
            }
            return false;
        }

        public void setPath(City[] path) {
            this.path = path.clone();
        }

        public void setCity(int index, City city) {
            path[index] = city;
            distance = Double.MAX_VALUE;
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder returnString = new StringBuilder();

            for (City city : path) {
                returnString.append(city.toString()).append(" ");
            }

            return returnString.toString();
        }
    }

    String name;
    City start;
    List<City> cities = new ArrayList<>();
    int numberOfCities;
    double[][] weights;
    DistanceType distanceType = DistanceType.EUCLIDEAN;
    int numberOfEvaluations, maxEvaluations;

    public TSP(Context context, String path, int maxEvaluations) {
        this.context = context;
        loadData(path);
        Log.d("TSP", "Loaded TSP data from " + path + " with " + numberOfCities + " cities.");
        numberOfEvaluations = 0;
        this.maxEvaluations = maxEvaluations;
    }

    public void evaluate(Tour tour) {
        if (tour == null || tour.getPath().length == 0) {
            System.err.println("Error: Tour is not properly initialized or contains no cities.");
            return;
        }

        double distance = 0;
        distance += calculateDistance(start, tour.getPath()[0]);
        for (int index = 0; index < numberOfCities; index++) {
            if (index + 1 < numberOfCities)
                distance += calculateDistance(tour.getPath()[index], tour.getPath()[index + 1]);
            else
                distance += calculateDistance(tour.getPath()[index], start);
        }
        tour.setDistance(distance);
        numberOfEvaluations++;
    }

    private double calculateDistance(City from, City to) {
        if (distanceType == DistanceType.EUCLIDEAN) {
            return Math.sqrt(Math.pow(from.x - to.x, 2) + Math.pow(from.y - to.y, 2));
        } else if (distanceType == DistanceType.EXPLICIT) {
            return weights[from.index][to.index];
        }
        return Double.MAX_VALUE;
    }

    public Tour generateTour() {
        if (numberOfCities == 0 || cities.isEmpty()) {
            System.err.println("Error: Cannot generate a tour as no city data is available.");
            return null;
        }

        Tour tour = new Tour(numberOfCities);
        List<City> cityList = new ArrayList<>(cities);
        for (int i = 0; i < numberOfCities; i++) {
            int randomIndex = RandomUtils.nextInt(cityList.size());
            tour.setCity(i, cityList.remove(randomIndex));
        }
        return tour;
    }

    private void loadData(String path) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream;
        try {
            inputStream = assetManager.open(path);
        } catch (Exception e) {
            System.err.println("File " + path + " not found!");
            return;
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = br.readLine();
            while (line != null) {
                lines.add(line.strip()); // Strip spaces
                line = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        parseData(lines);

        // Set a default start city if coordinates exist
        if (!cities.isEmpty()) {
            start = cities.get(0);
        } else if (distanceType == DistanceType.EXPLICIT && numberOfCities > 0) {
            // For EXPLICIT weights, create dummy cities
            for (int i = 0; i < numberOfCities; i++) {
                City city = new City();
                city.index = i;
                cities.add(city);
            }
            start = cities.get(0);
        } else {
            System.err.println("No city data found! Ensure the TSP file is formatted correctly.");
        }
    }

    private void parseData(List<String> lines) {
        boolean nodeCoordSection = false;
        boolean edgeWeightSection = false;
        boolean displayDataSection = false;

        int matrixRow = 0;
        String displayDataType = null;

        for (String line : lines) {
            if (line.equals(Keywords.NODE_COORD_SECTION)) {
                nodeCoordSection = true;
                edgeWeightSection = false;
                displayDataSection = false;
                continue;
            }

            if (line.equals(Keywords.DISPLAY_DATA_SECTION)) {
                nodeCoordSection = false;
                edgeWeightSection = false;
                displayDataSection = true;
                continue;
            }

            if (line.equals(Keywords.EDGE_WEIGHT_SECTION)) {
                edgeWeightSection = true;
                nodeCoordSection = false;
                displayDataSection = false;
                matrixRow = 0;
                continue;
            }

            if (line.equals(Keywords.EOF)) {
                break;
            }
            if (nodeCoordSection || (displayDataSection && Objects.equals(displayDataType, "TWOD_DISPLAY"))) {
                String[] coordinates = line.split("\\s+");
                if (coordinates.length >= 3) {
                    City city = new City();
                    city.index = Integer.parseInt(coordinates[0]) - 1; // Assume 1-based index in file
                    city.x = Double.parseDouble(coordinates[1]);
                    city.y = Double.parseDouble(coordinates[2]);
                    cities.add(city);
                }
            } else if (edgeWeightSection ) {
                if (weights == null) {
                    System.err.println("Error: Weights array is not initialized. Ensure 'DIMENSION' is parsed first.");
                    return;
                }

                String[] weightValues = line.split("\\s+");
                for (int col = 0; col < weightValues.length; col++) {
                    if (matrixRow < numberOfCities && col < numberOfCities) {
                        weights[matrixRow][col] = Double.parseDouble(weightValues[col]);
                    }
                }
                matrixRow++;
            } else {
                String[] lineElements = line.split(":");
                if (lineElements.length == 2) {
                    String name = lineElements[0].trim();
                    String value = lineElements[1].trim();
                    switch (name) {
                        case Keywords.NAME:
                            this.name = value;
                            break;
                        case Keywords.DIMENSION:
                            this.numberOfCities = Integer.parseInt(value);
                            if (this.numberOfCities > 0) {
                                weights = new double[numberOfCities][numberOfCities];
                            }
                            break;
                        case Keywords.EDGE_WEIGHT_TYPE:
                            if (value.equals("EUC_2D"))
                                distanceType = DistanceType.EUCLIDEAN;
                            else if (value.equals("EXPLICIT"))
                                distanceType = DistanceType.EXPLICIT;
                            break;
                        case Keywords.COMMENT:
                            break;
                        case Keywords.DISPLAY_DATA_TYPE:
                            displayDataType = value;
                            break;
                    }
                }
            }
        }
    }

    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    public int getNumberOfEvaluations() {
        return numberOfEvaluations;
    }
}
