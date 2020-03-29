import java.util.ArrayList;
import java.util.Random;

/**
 * Prints 5 sets of 5 numbers that add up to 100.
 */
public class Shuffle {
    private int[][] array;
    private Random random;
    private ArrayList<Integer> row, col;

    private Shuffle(int size) {
        array = new int[size][size];
        random = new Random();
        row = new ArrayList<>(size);
        col = new ArrayList<>(size);
    }
    private void print() {
        for (int[] ints : array) {
            for (int j = 0; j < array.length; j++)
                System.out.printf("%5d", ints[j]);
            System.out.println();
        }
        System.out.println();
    }
    private void doIteration() {
        for(int i = 0; i < array.length; ++i) {
            row.add(i);
            col.add(i);
        }
        int size = array.length;
        for(int i = 0; i < array.length; ++i) {
            ++array[row.remove(random.nextInt(size))][col.remove(random.nextInt(size))];
            --size;
        }
    }
    public static void main(String[] args) {
        Shuffle s = new Shuffle(5);
        for(int i = 0; i < 100; i++) {
            s.doIteration();
        }
        s.print();
    }
}
