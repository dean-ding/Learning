package com.example;

public class BinarySearch {
    private static final int INVALID = -1;

    public static void main(String[] args){
        int[] array = {1,2,3,4,6,7,8,9,10};
        int index = binarySearch(array, 4);
        if(index == INVALID){
            System.out.println("not exist!!!");
        }else{
            System.out.println(String.format("index = %d", index));
        }
    }

    public static int binarySearch(int[] array,int des){
        int low = 0;
        int high = array.length - 1;
        while(low <= high){
            int middle = (high + low) / 2;
            int value = array[middle];
            if(des == value){
                return middle;
            }else if(value < des){
                low = middle + 1;
            }else{
                high = middle - 1;
            }
        }
        return INVALID;
    }
}
