package org.westos.utils;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * @author lwj
 * @date 2020/6/23 13:04
 */
public class InputNumUtil {
    public static int scannerNum() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            try {
                int i = sc.nextInt();
                return i;
            } catch (InputMismatchException e) {
                System.out.println("您输入的类型不正确,请重新输入.");
            }
        }
    }
}
