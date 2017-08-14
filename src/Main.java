import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // write your code here
        System.out.println("Hello World");
        Scanner scanner = new Scanner(System.in);
        System.out.println("how many numbers");
        int l = scanner.nextInt();
        //System.out.println(String.format("hello! %s", name));
        ArrayList<Integer> numbers =  new ArrayList<Integer>();
        Random rand = new Random(42);

        for(int i=0;i<=l;i++){
            numbers.add(rand.nextInt(10));
            System.out.println(numbers.get(i));
        }


    }
}
