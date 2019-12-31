//------------------------------------
// SCMD Project
//  
// MatOp.java 
// Since:  2004/04/16
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.calmorph;

class MatOp {
    public static double[] GS(double[][] A) {
        double[] x = new double[5];
        for (int i = 0; i < 5; i++) {
            x[i] = 0;
        }
        do {
            for (int i = 0; i < 5; i++) {
                double s = 0;
                for (int j = 0; j < 5; j++) {
                    if (j != i) s += x[j] * A[i][j];
                }
                x[i] = (1 - s) / A[i][i];
            }
        } while (!(zannsa(A, x) < 10E-10));
        return x;
    }

    public static double[] GE(double[][] B) {
        boolean flag = false;
        double[] x = new double[5];
        double[] b = {1, 1, 1, 1, 1};
        double[][] A = new double[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                A[i][j] = B[i][j];
            }
        }
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                double s = A[j][i] / A[i][i];
                for (int k = i; k < 5; k++) {
                    A[j][k] -= A[i][k] * s;
                }
                b[j] -= b[i] * s;
            }
        }
        for (int i = 4; i >= 0; i--) {
            double s = 0;
            for (int j = 4; j > i; j--) {
                s += A[i][j] * x[j];
            }
            if (A[i][i] == 0) {
                flag = true;
                break;
            }
            x[i] = (b[i] - s) / A[i][i];
        }
        double s;
        for (int i = 0; i < 5; i++) {
            s = 0;
            for (int j = 0; j < 5; j++) {
                s += B[i][j] * x[j];
            }
        }
        if (flag) {
            x[0] = 0;
            x[1] = 0;
            x[2] = 0;
        }
        return x;
    }

    public static double[] GE3(double[][] B, double[] b) {
        boolean flag = false;
        double[] x = new double[5];
        double[][] A = new double[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                A[i][j] = B[i][j];
            }
        }
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                double s = A[j][i] / A[i][i];
                for (int k = i; k < 5; k++) {
                    A[j][k] -= A[i][k] * s;
                }
                b[j] -= b[i] * s;
            }
        }
        for (int i = 5 - 1; i >= 0; i--) {
            double s = 0;
            for (int j = 5 - 1; j > i; j--) {
                s += A[i][j] * x[j];
            }
            if (A[i][i] == 0) {
                flag = true;
                break;
            }
            x[i] = (b[i] - s) / A[i][i];
        }
        double s;
        for (int i = 0; i < 5; i++) {
            s = 0;
            for (int j = 0; j < 5; j++) {
                s += B[i][j] * x[j];
            }
        }
        if (flag) {
            x[0] = 0;
            x[1] = 0;
            x[2] = 0;
        }
        return x;
    }

    public static double[] GE4(double[][] B, double[] b) {
        int n = 6;
        boolean flag = false;
        double[] x = new double[n];
        double[][] A = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = B[i][j];
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double s = A[j][i] / A[i][i];
                for (int k = i; k < n; k++) {
                    A[j][k] -= A[i][k] * s;
                }
                b[j] -= b[i] * s;
            }
        }
        //for(int i=0;i<n;i++) {
        //for(int j=0;j<n;j++) {
        //System.out.print(A[i][j]+ " ");
        //}
        //System.out.println(b[i]);
        //}
        for (int i = n - 1; i >= 0; i--) {
            double s = 0;
            for (int j = n - 1; j > i; j--) {
                s += A[i][j] * x[j];
            }
            if (A[i][i] == 0) {
                flag = true;
                break;
            }
            x[i] = (b[i] - s) / A[i][i];
        }
        double s;
        for (int i = 0; i < n; i++) {
            s = 0;
            for (int j = 0; j < n; j++) {
                s += B[i][j] * x[j];
            }
            //System.out.println(x[i]);
        }
        if (flag) {
            //System.out.println("aaaaaa");
            x[0] = 0;
            x[1] = 0;
            x[2] = 0;
        }
        return x;
    }

    public static double zannsa(double[][] A, double[] x) {
        double r = 0;
        for (int i = 0; i < 5; i++) {
            double s = 0;
            for (int j = 0; j < 5; j++) {
                s += A[i][j] * x[j];
            }
            r += (1 - s) * (1 - s);
        }
        return Math.sqrt(r);
    }

    public static double[][] seki(double[][] B, int repnum) {
        double[][] A = new double[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < repnum; k++) {
                    A[i][j] += B[k][i] * B[k][j];
                }
            }
        }
        return A;
    }

    public static double[][] BtB(double[][] B, int repnum) {
        int n = 6;
        double[][] A = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < repnum; k++) {
                    A[i][j] += B[k][i] * B[k][j];
                }
            }
        }
        return A;
    }

    public static void invMat(double[][] A) {
        int n = 6;
        for (int i = 0; i < n; i++) {
            double t = A[i][i];
            for (int j = 0; j < n; j++) {
                A[i][j] /= t;
            }
            A[i][i] = 1 / t;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    double u = A[j][i];
                    for (int k = 0; k < n; k++) {
                        if (k != i) A[j][k] -= A[i][k] * u;
                        else A[j][k] = -u / t;
                    }
                }
            }
        }
    }

    public static void invMat(double[][] A, int n) {
        for (int i = 0; i < n; i++) {
            double t = A[i][i];
            for (int j = 0; j < n; j++) {
                A[i][j] /= t;
            }
            A[i][i] = 1 / t;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    double u = A[j][i];
                    for (int k = 0; k < n; k++) {
                        if (k != i) A[j][k] -= A[i][k] * u;
                        else A[j][k] = -u / t;
                    }
                }
            }
        }
    }

    public static void cholesky(double[][] A) {
        int n = 6;
        double[][] L = new double[n][n];
        for (int i = 0; i < n; i++) {
            double s = 0;
            for (int j = 0; j < i; j++) {
                s += L[i][j] * L[i][j];
            }
            L[i][i] = Math.sqrt(A[i][i] - s);
            for (int j = i + 1; j < n; j++) {
                double p = 0;
                for (int k = 0; k < i; k++) {
                    p += L[j][k] * L[i][k];
                }
                L[j][i] = (A[j][i] - p) / L[i][i];
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = L[i][j];
            }
        }
    }

    public static double[][] LLt(double[][] L) {
        int n = 6;
        double[][] s = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    s[i][j] += L[i][k] * L[j][k];
                }
            }
        }
        return s;
    }

    public static double[][] tennchi(double[][] A) {
        int n = 6;
        double[][] B = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                B[i][j] = A[j][i];
            }
        }
        return B;
    }

    public static double[][] mul(double[][] A, double[][] B) {
        int n = 6;
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;
    }

    public static double[] generalEigenVector(double[][] A, double[][] B) {
        int n = 6;
        double[][] Bt1 = new double[n][n];
        double[][] Bt2 = new double[n][n];
        double[] y = new double[n];
        cholesky(B);
        Bt1 = tennchi(B);
        Bt2 = tennchi(B);
        invMat(B);
        invMat(Bt1);
        A = mul(B, A);
        A = mul(A, Bt1);
        y = jacobi(A);
        if (y == null) {
            return null;
        } else {
            y = GE4(Bt2, y);
            return y;
        }
    }

    public static double[] Ax(double[][] A, double[] x) {
        double[] r = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x.length; j++) {
                r[i] += A[i][j] * x[j];
            }
        }
        return r;
    }

    public static double[] jacobi(double[][] A) {
        int n = 6;
        double[][] check = new double[n][n];
        double[][] w = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                check[i][j] = A[i][j];
                w[i][j] = 0;
            }
            w[i][i] = 1;
        }
        int count = 0;
        while (true) {
            if (count > 100) break;
            count++;
            double s = 0;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    s += A[i][j] * A[i][j];
                }
            }
            if (s < 1.0e-16) {
                break;
            }
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (Math.abs(A[i][j]) < 1.0e-20) continue;
                    double tan = (A[j][j] - A[i][i]) / (2 * A[i][j]);
                    if (tan >= 0) tan = 1 / (tan + Math.sqrt(tan * tan + 1));
                    else tan = 1 / (tan - Math.sqrt(tan * tan + 1));
                    double cos = 1 / Math.sqrt(tan * tan + 1);
                    double sin = tan * cos;
                    double t = tan * A[i][j];
                    A[i][i] -= t;
                    A[j][j] += t;
                    A[i][j] = 0;
                    for (int k = 0; k < i; k++) {
                        double x = A[k][i], y = A[k][j];
                        A[k][i] = x * cos - y * sin;
                        A[k][j] = x * sin + y * cos;
                    }
                    for (int k = i + 1; k < j; k++) {
                        double x = A[i][k], y = A[k][j];
                        A[i][k] = x * cos - y * sin;
                        A[k][j] = x * sin + y * cos;
                    }
                    for (int k = j + 1; k < n; k++) {
                        double x = A[i][k], y = A[j][k];
                        A[i][k] = x * cos - y * sin;
                        A[j][k] = x * sin + y * cos;
                    }
                    for (int k = 0; k < n; k++) {
                        double x = w[i][k], y = w[j][k];
                        w[i][k] = x * cos - y * sin;
                        w[j][k] = x * sin + y * cos;
                    }

                }
            }
        }
        //System.out.println(count);
        //print(w,n,n);                                                     
        if (count < 100) {
            for (int i = 0; i < n; i++) {
                //System.out.println(A[i][i]);
            }
            double min = 10;
            int mini = 0;
            for (int i = 0; i < n; i++) {
                if (min > A[i][i]) {
                    min = A[i][i];
                    mini = i;
                }
            }
            //double[] bbbb = new double[6];
            //bbbb = Ax(check,w[mini]);
            //for(int j=0;j<n;j++) {
            //System.out.print(bbbb[j] + "\t");
            //System.out.println(A[i][i]*w[i][j]);
            //}
            return w[mini];
        } else {
            return null;
        }
    }

    public static void print(double[][] A, int n, int m) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(A[i][j] + "\t");
            }
            System.out.println();
        }
    }
}


//--------------------------------------
//$Log: MatOp.java,v $
//Revision 1.3  2004/09/06 13:44:06  sesejun
//CalMorphのおそらく正しい1_0のソース
//