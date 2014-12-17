package ch.ethz.inf.asl.mva;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class MVA {

    private enum Type {
        FIXED_CAPACITY,
        DELAY_CENTER,
        LOAD_DEPENDENT
    }

    private static BigDecimal calculateMuFunction(int n, int m, BigDecimal S) {
        if (n < m) {
            return new BigDecimal(n).divide(S, 200, RoundingMode.HALF_UP);
        } else {
            return new BigDecimal(m).divide(S, 200, RoundingMode.HALF_UP);
        }
    }

    private static BigDecimal calculateMuFunctionDatabase(int n, int m, BigDecimal S) {
        //            System.out.println("Cool!!!");
//        if (i < 12) {
//            return new BigDecimal( (1.00 - i * 0.06) * i).divide(serviceTime, MathContext.DECIMAL128);
//        } else if (i < 18) {
//            return new BigDecimal( (1.00 - 12 * 0.06) * i).divide(serviceTime, MathContext.DECIMAL128);
//        } else if ( i < 25) {
//            return new BigDecimal( (1.00 - 13 * 0.06) * i).divide(serviceTime, MathContext.DECIMAL128);
//        }
//        else {
//            return new BigDecimal(0.3 * numberOfServices).divide(serviceTime, MathContext.DECIMAL128);
//        }
        if (n < 20) {
            return new BigDecimal(n).divide(S, MathContext.DECIMAL128);
        } else {
            return new BigDecimal(m ).divide(S, MathContext.DECIMAL128);
        }
    }

    public static void calculate(BigDecimal thinkTime, Type[] types, BigDecimal[] serviceTimes, int[] numberOfVisits,
                                 int numberOfUsers, BigDecimal[][] serviceRates) {


        int M = numberOfVisits.length; // number of devices (not including terminals)

        BigDecimal[] Q = new BigDecimal[M];
        BigDecimal[][] P = new BigDecimal[M][numberOfUsers + 1];
        for (int i = 0; i < M; ++i) {
            if (types[i] == Type.FIXED_CAPACITY || types[i] == Type.DELAY_CENTER) {
                Q[i] = new BigDecimal(0);
            }
            else {
                assert(types[i] == Type.LOAD_DEPENDENT);
                P[i][0] = new BigDecimal(1);
            }
        }


        BigDecimal XAll = new BigDecimal(0);
        BigDecimal RAll = new BigDecimal(0);
        BigDecimal[] R = new BigDecimal[M];
        for (int n = 1; n <= numberOfUsers; ++n) {

            for (int i = 0; i < M; ++i) {
                if (types[i] == Type.FIXED_CAPACITY) {
                    R[i] = serviceTimes[i].multiply(Q[i].add(new BigDecimal(1)));
                }
                else if (types[i] == Type.DELAY_CENTER) {
                    R[i] = serviceTimes[i];
                }
                else {
                    assert(types[i] == Type.LOAD_DEPENDENT);
                    BigDecimal sum = new BigDecimal(0);
                    for (int j = 1; j <= n; ++j) {
                        sum = sum.add(P[i][j - 1].multiply(new BigDecimal(j).divide(serviceRates[i][j], 200, RoundingMode.HALF_UP)));
                    }
                    R[i] = sum;
                }
            }

            RAll = new BigDecimal(0);
            for (int i = 0; i < M; ++i) {
                RAll = RAll.add(R[i].multiply(new BigDecimal(numberOfVisits[i])));
            }

            XAll = new BigDecimal(n).divide(RAll.add(thinkTime), 200, RoundingMode.HALF_UP);

            for (int i = 0; i < M; ++i) {
                if (types[i] == Type.FIXED_CAPACITY || types[i] == Type.DELAY_CENTER) {
                    Q[i] = XAll.multiply(R[i].multiply(new BigDecimal(numberOfVisits[i])));
                }
                else {
                    assert (types[i] == Type.LOAD_DEPENDENT);

                    for (int j = n; j >= 1; --j) {
                        P[i][j] = (XAll.divide(serviceRates[i][j], 200, RoundingMode.HALF_UP)).multiply(P[i][j - 1]);

                        if (P[i][j].compareTo(new BigDecimal(1.1)) > 0 || P[i][j].compareTo(new BigDecimal(-0.1)) < 0) {
                            System.out.println("PROBABILITY SUCKS!"  + P[i][j]);
                        }
//                        System.out.println(":::> " + (String.format("%.2f", P[i][j])));
                    }

                    BigDecimal sum = new BigDecimal(0);
                    for (int k = 1; k <= n; ++k) {
                        sum = sum.add(P[i][k]);
                    }
                    P[i][0] = new BigDecimal(1).subtract(sum);
                }
            }
        }
        System.out.println(numberOfUsers + ": " + (String.format("%.2f %.2f", XAll.multiply(new BigDecimal(1000)), RAll)));

        BigDecimal[] X = new BigDecimal[M];
        BigDecimal[] U = new BigDecimal[M];
        for (int i = 0; i < M; ++i) {
            X[i] = XAll.multiply(new BigDecimal(numberOfVisits[i]));
        }

        for (int i = 0; i < M; ++i) {
            if (types[i] == Type.FIXED_CAPACITY || types[i] == Type.DELAY_CENTER) {
                U[i] = XAll.multiply(serviceTimes[i].multiply(new BigDecimal(numberOfVisits[i])));
            }
            else {
                U[i] = new BigDecimal(1).subtract(P[i][0]);
            }

            if (U[i].compareTo(new BigDecimal(1.1)) > 0) {
                System.out.println("ERROR!!!: " + i + " ... " + U[i]);
            }
        }

    }


    public static void main(String[] args) {

        for (int n = 2; n <= 200;) {
            BigDecimal thinkTime = new BigDecimal(0);
            Type[] types = new Type[]{Type.FIXED_CAPACITY, Type.LOAD_DEPENDENT};
            BigDecimal[] serviceTimes = new BigDecimal[]{ new BigDecimal(0.000534249), new BigDecimal(1.35318) };
            int[] numberOfVisits = new int[]{1, 1};
            int numberOfUsers = n;

            BigDecimal[] firstDevice = new BigDecimal[numberOfUsers + 1];
            BigDecimal[] secondDevice = new BigDecimal[numberOfUsers + 1];
            for (int i = 0; i <= numberOfUsers; ++i) {
                firstDevice[i] = calculateMuFunction(i, 20, serviceTimes[0]);
                secondDevice[i] = calculateMuFunctionDatabase(i, 6, serviceTimes[1]);
            }
            BigDecimal[][] serviceRates = new BigDecimal[][]{firstDevice, secondDevice};
            calculate(thinkTime, types, serviceTimes, numberOfVisits, numberOfUsers, serviceRates);

            if (n == 2) {
                n = 5;
            }
            else if (n < 50) {
                n += 5;
            }
            else if (n < 100) {
                n += 10;
            }
            else if (n <= 200) {
                n += 20;
            }
        }
    }
}
