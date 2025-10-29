import java.io.File;
import java.math.BigInteger;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class HashiraInterpolation {

    // Fraction class for precise rational arithmetic using BigInteger
    static class Fraction {
        BigInteger num, den;

        Fraction(BigInteger n, BigInteger d) {
            if (d.signum() == 0) throw new ArithmeticException("Division by zero");
            if (d.signum() < 0) { n = n.negate(); d = d.negate(); }
            BigInteger g = n.gcd(d);
            num = n.divide(g);
            den = d.divide(g);
        }

        Fraction add(Fraction o) {
            return new Fraction(num.multiply(o.den).add(o.num.multiply(den)), den.multiply(o.den));
        }

        Fraction multiply(Fraction o) {
            return new Fraction(num.multiply(o.num), den.multiply(o.den));
        }

        public String toString() {
            if (den.equals(BigInteger.ONE)) return num.toString();
            return num + "/" + den;
        }
    }

    public static void main(String[] args) throws Exception {
        // JSON parser setup
        ObjectMapper mapper = new ObjectMapper();
        
        //Adjust the path for test case file
        Map<String, Object> json = mapper.readValue(new File("test/2.json"), new TypeReference<Map<String, Object>>() {});

        // Extract keys
        Map<String, Object> keys = (Map<String, Object>) json.get("keys");
        int n = (int) keys.get("n");
        int k = (int) keys.get("k");

        // Sort x-values (root indices)
        List<Integer> sortedKeys = new ArrayList<>();
        for (String key : json.keySet()) {
            if (!key.equals("keys")) sortedKeys.add(Integer.parseInt(key));
        }
        Collections.sort(sortedKeys);

        // Use only first k roots
        List<Integer> xVals = new ArrayList<>();
        List<BigInteger> yVals = new ArrayList<>();

        for (int i = 0; i < Math.min(k, sortedKeys.size()); i++) {
            int x = sortedKeys.get(i);
            Map<String, String> node = (Map<String, String>) json.get(String.valueOf(x));
            int base = Integer.parseInt(node.get("base"));
            BigInteger val = new BigInteger(node.get("value"), base);
            xVals.add(x);
            yVals.add(val);
        }

        // Compute f(0) using Lagrange interpolation
        Fraction f0 = new Fraction(BigInteger.ZERO, BigInteger.ONE);
        for (int i = 0; i < k; i++) {
            Fraction term = new Fraction(yVals.get(i), BigInteger.ONE);
            for (int j = 0; j < k; j++) {
                if (i == j) continue;
                BigInteger xi = BigInteger.valueOf(xVals.get(i));
                BigInteger xj = BigInteger.valueOf(xVals.get(j));
                term = term.multiply(new Fraction(xj.negate(), xi.subtract(xj)));
            }
            f0 = f0.add(term);
        }

        System.out.println("Constant term f(0) = " + f0);
    }
}
