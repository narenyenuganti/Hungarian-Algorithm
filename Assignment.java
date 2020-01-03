import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Berkeley Business Society
 * Driver class afor solving the unbalanced maximization assignment problem.
 * Utilizes the Hungarian/Munkres combinatorial optimization Algorithm
 * to solve the problem in polynomial time.
 * Designed specifically for assigning consultants to divisions.
 *
 * @author Naren Yenuganti
 */

class Assignment {

    private static String[] DIVISIONS = {"Corporate", "Marketing", "Startup", "Finance", "Pro Bono"};
    private static int[][] _assignment;
    private static boolean _manual;
    private static ArrayList<Object> names = new ArrayList<>();
    private static HashMap<Object, int[]> _namesToPoints;
    private int[][] assignmentCopy;
    private int[] rows;
    private int[] occupiedCols;
    private int[][] lines;
    private int numLines;

    /**
     * Constructor for when the algorithm is run a first time.
     * @param manual Boolean of whether to run auto.
     * @param assignment 2D array to run algorithm on.
     * @param nameList Names of all the consultants.
     * @param namesToPoints Hashmap of people's names to preference points.
     */
    Assignment(boolean manual, int[][] assignment, ArrayList<Object> nameList, HashMap<Object, int[]> namesToPoints) {
        _manual = manual;
        _assignment = assignment;
        names.clear();
        names = nameList;
        _namesToPoints = namesToPoints;
        manual();
    }

    /**
     * Constructor for when the algorithm is run again.
     * @param namesToPoints Hashmap of people's names to preference points.
     */
    Assignment(HashMap<Object, int[]> namesToPoints) {
        _manual = false;
        names.clear();
        names.addAll(namesToPoints.keySet());
        _namesToPoints = namesToPoints;
        _assignment = new int[_namesToPoints.keySet().size()][DIVISIONS.length];
        int counter = 0;
        for (Object name : _namesToPoints.keySet()) {
            _assignment[counter][0] = _namesToPoints.get(name)[0];
            _assignment[counter][1] = _namesToPoints.get(name)[1];
            _assignment[counter][2] = _namesToPoints.get(name)[2];
            _assignment[counter][3] = _namesToPoints.get(name)[3];
            _assignment[counter][4] = _namesToPoints.get(name)[4];
            counter++;
        }
        _assignment = SheetsQuickstart.transposeMatrix(_assignment);
        manual();
    }

    /**
     * Runs the manual method or for auto: defaults to the algorithm.
     * Manual method is unfinished.
     */
    private void manual() {
        if (_manual) {
            System.out.println("Manual is not finished");
//            Scanner scanner = new Scanner(System.in);
//            System.out.println();
//            System.out.println("How many consultants need to be assigned?");
//            _assignment = new int[scanner.nextInt()][DIVISIONS1.length];
//            System.out.println("Type \"0\" if there are no more consultants to add.");
//            System.out.println("Enter a consultant's name:");
//            while (!scanner.nextLine().equals("0")) {
//                String name = scanner.nextLine();
//                System.out.println();
//                System.out.println("The more preference points for X division the more an individual wants to be in that division.");
//                System.out.println("Enter preference points for CORPORATE:");
//                int corpNum = scanner.nextInt();
//                System.out.println("Enter preference points for MARKETING:");
//                int markNum = scanner.nextInt();
//                System.out.println("Enter preference points for STARTUP:");
//                int startNum = scanner.nextInt();
//                System.out.println("Enter preference points for FINANCE:");
//                int finNum = scanner.nextInt();
//                System.out.println("Enter preference points for PRO BONO:");
//                int proNum = scanner.nextInt();
//            }
//            System.out.println("Assignment algorithm beginning.");
        } else {
            algorithm();
        }
    }

    /**
     * Adjusted Hungarian/Munkres Algorithm for
     * solving the unbalanced maximization assignment problem.
     */
    private void algorithm() {
        assignmentCopy = new int[_assignment.length][];
        for (int i = 0; i < assignmentCopy.length; ++i) {
            assignmentCopy[i] = new int[_assignment[i].length];
            System.arraycopy(_assignment[i], 0, assignmentCopy[i], 0, assignmentCopy[i].length);
        }
        stepOne();
        occupiedCols = new int[assignmentCopy.length];
        rows = new int[assignmentCopy.length];
        stepTwo();
        stepThree();
        coverZeros();
        while (numLines < assignmentCopy.length) {
            createAdditionalZeros();
            coverZeros();
        }
        optimization(0);
        System.out.println("\nAssignments:");
        int counter = 0;
        for (int row : rows) {
            if (counter < 5) {
                System.out.printf("%s: %s\n", DIVISIONS[counter], names.get(row));
                _namesToPoints.remove(names.get(row));
                counter++;
            }
        }
        System.out.println("Total Preference Points: " + getTotal());
        if (!_namesToPoints.isEmpty() && _namesToPoints.keySet().size() > DIVISIONS.length) {
            Assignment unassigned = new Assignment(_namesToPoints);
        } else if (!_namesToPoints.isEmpty() && _namesToPoints.size() < DIVISIONS.length) {
            System.out.println("\nManually check for these consultant pairings:");
            int max = 0;
            int index = 0;
            for (Object name : _namesToPoints.keySet()) {
                for (int i = 0; i < _namesToPoints.get(name).length; i++) {
                    if (_namesToPoints.get(name)[i] > max) {
                        max = _namesToPoints.get(name)[i];
                        index = i;
                    }
                }
                System.out.printf("%s: %s\n", DIVISIONS[index], name);
                max = 0;
            }
        }
    }

    /**
     * To maximize the total cost we negate all elements.
     * The cost matrix contains negative elements, we add the max value
     * to each entry to make the cost matrix non-negative.
     * Makes the matrix a square by adding zeros (assuming that there will
     * always be more consultants than divisions.
     */
    private void stepOne() {
        int max = 0;
        for (int i = 0; i < assignmentCopy.length; i++) {
            for (int j = 0; j < assignmentCopy[i].length; j++) {
                if (assignmentCopy[i][j] > max) {
                    max = assignmentCopy[i][j];
                    assignmentCopy[i][j] = -assignmentCopy[i][j];
                } else {
                    assignmentCopy[i][j] = -assignmentCopy[i][j];
                }
            }
        }
        for (int i = 0; i < assignmentCopy.length; i++) {
            for (int j = 0; j < assignmentCopy[i].length; j++) {
                assignmentCopy[i][j] += max;
            }
        }
        int[][] squareAssignment = new int[_assignment[0].length][_assignment[0].length];
        for (int i = 0; i < assignmentCopy.length; i++) {
            System.arraycopy(assignmentCopy[i], 0, squareAssignment[i], 0, assignmentCopy[i].length);
        }
        assignmentCopy = squareAssignment;
    }

    /**
     * Subtract Row Minima.
     */
    private void stepTwo() {
        int[] rowMinValue = new int[assignmentCopy.length];
        for (int row = 0; row < assignmentCopy.length; row++) {
            rowMinValue[row] = assignmentCopy[row][0];
            for (int col = 1; col < assignmentCopy.length; col++) {
                if (assignmentCopy[row][col] < rowMinValue[row])
                    rowMinValue[row] = assignmentCopy[row][col];
            }
        }
        for (int row = 0; row < assignmentCopy.length; row++) {
            for (int col = 0; col < assignmentCopy.length; col++) {
                assignmentCopy[row][col] -= rowMinValue[row];
            }
        }

    }

    /**
     * Subtract Column Minima.
     */
    private void stepThree() {
        int[] colMinValue = new int[assignmentCopy.length];
        for (int col = 0; col < assignmentCopy.length; col++) {
            colMinValue[col] = assignmentCopy[0][col];
            for (int row = 1; row < assignmentCopy.length; row++) {
                if (assignmentCopy[row][col] < colMinValue[col])
                    colMinValue[col] = assignmentCopy[row][col];
            }
        }
        for (int col = 0; col < assignmentCopy.length; col++) {
            for (int row = 0; row < assignmentCopy.length; row++) {
                assignmentCopy[row][col] -= colMinValue[col];
            }
        }
    }

    /**
     * Step 4.1
     * Loop through all elements, and run colorNeighbors when the element visited is equal to zero
     */
    private void coverZeros() {
        numLines = 0;
        lines = new int[assignmentCopy.length][assignmentCopy.length];
        for (int row = 0; row < assignmentCopy.length; row++) {
            for (int col = 0; col < assignmentCopy.length; col++) {
                if (assignmentCopy[row][col] == 0)
                    colorNeighbors(row, col, maxVH(row, col));
            }
        }
    }

    /**
     * Step 4.2
     * Checks which direction (vertical,horizontal) contains more zeros.
     *
     * @param row Row index for the target cell
     * @param col Column index for the target cell
     * @return Positive integer = vertical, Zero or Negative = horizontal
     */
    private int maxVH(int row, int col) {
        int result = 0;
        for (int i = 0; i < assignmentCopy.length; i++) {
            if (assignmentCopy[i][col] == 0)
                result++;
            if (assignmentCopy[row][i] == 0)
                result--;
        }
        return result;
    }

    /**
     * Step 4.3
     * Color the neighbors of the cell at index [row][col].
     *
     * @param row   Row index for the target cell
     * @param col   Column index for the target cell
     * @param maxVH Value return by the maxVH method
     */
    private void colorNeighbors(int row, int col, int maxVH) {
        if (lines[row][col] == 2 || maxVH > 0 && lines[row][col] == 1 || maxVH <= 0 && lines[row][col] == -1) {
            return;
        }
        for (int i = 0; i < assignmentCopy.length; i++) {
            if (maxVH > 0) {
                lines[i][col] = lines[i][col] == -1 || lines[i][col] == 2 ? 2 : 1;
            }
            else {
                lines[row][i] = lines[row][i] == 1 || lines[row][i] == 2 ? 2 : -1;
            }
        }
        numLines++;
    }

    /**
     * Step 5
     * Create additional zeros, by coloring the minimum value of uncovered cells.
     */
    private void createAdditionalZeros() {
        int minUncoveredValue = 0;
        for (int row = 0; row < assignmentCopy.length; row++) {
            for (int col = 0; col < assignmentCopy.length; col++) {
                if (lines[row][col] == 0 && (assignmentCopy[row][col] < minUncoveredValue || minUncoveredValue == 0))
                    minUncoveredValue = assignmentCopy[row][col];
            }
        }
        for (int row = 0; row < assignmentCopy.length; row++) {
            for (int col = 0; col < assignmentCopy.length; col++) {
                if (lines[row][col] == 0) {
                    assignmentCopy[row][col] -= minUncoveredValue;
                } else if (lines[row][col] == 2) {
                    assignmentCopy[row][col] += minUncoveredValue;
                }
            }
        }
    }

    /**
     * Step 6
     * Optimization, assign every row a cell in a unique column.
     *
     * @param row row number
     * @return true
     */
    private boolean optimization(int row) {
        if (row == rows.length) {
            return true;
        }
        for (int col = 0; col < assignmentCopy.length; col++) {
            if (assignmentCopy[row][col] == 0 && occupiedCols[col] == 0) {
                rows[row] = col;
                occupiedCols[col] = 1;
                if (optimization(row + 1)) {
                    return true;
                }
                occupiedCols[col] = 0;
            }
        }
        return false;
    }

    /**
     * Get the sum of the value of the assigned cells for all rows using the original passed matrix.
     * @return Total values
     */
    private int getTotal() {
        int total = 0;
        for (int row = 0; row < _assignment.length; row++)
            total += _assignment[row][rows[row]];
        return total;
    }

    /**
     * For testing purposes. Print a 2d Array.
     *
     * @param array 2d-array
     */
    private void printArray(int[][] array) {
        for (int[] ints : array) {
            for (int anInt : ints) {
                System.out.print(anInt + " ");
            }
            System.out.println();
        }
    }
}
