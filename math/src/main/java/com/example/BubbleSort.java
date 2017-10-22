package com.example;

/**
 * Created by dinghugui on 2017/10/21.
 */

public class BubbleSort {

    public static void main(String[] args) {
        int[] array = {4, 3, 76, 89, 45, 23, 47, 1, 55, 88, 222, 555, 444, 333};
        for (int value : array) {
            System.out.print(String.valueOf(value));
            System.out.print(",");
        }
        System.out.println("");
        BubbleSort(array);
        for (int value : array) {
            System.out.print(String.valueOf(value));
            System.out.print(",");
        }
    }

    private static void BubbleSort(int[] array) {
        int size = array.length;
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - 1 - i; j++) {
                if (array[j] > array[j + 1]) {
                    int des = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = des;
                }
            }
        }
    }
}
