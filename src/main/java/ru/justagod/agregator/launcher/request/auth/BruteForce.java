package ru.justagod.agregator.launcher.request.auth;

public class BruteForce {

    private static char[] password = new char[8];
    private static long count = 0;
    static
    {
        cleanUp();
        rollForward(210793);
    }

    private synchronized static void rollForward(long i) {
        synchronized (BruteForce.class) {
            for (int j = 0; j < i; j++) {
                getNext();
            }
        }
    }

    public static synchronized char[] getNext() {
        synchronized (BruteForce.class) {
            boolean flag = false;
            for (int i = 0; i < password.length; i++) {
                if (password[i] < 122) {
                    password[i]++;
                    for (int j = 0; j < i; j++) {
                        password[j] = 48;
                    }
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                int index = password.length;
                password = new char[index + 1];
                password[index] = 48;
            }
            System.out.println(++count);
            char[] tmp = new char[password.length];
            System.arraycopy(password, 0, tmp, 0, password.length);
            return tmp;
        }
    }

    public synchronized static void cleanUp() {
        for (int i = 0; i < password.length; i++) {
            password[i] = '0';
        }
        password[0] = 47;
    }
}
