/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MOOS.geodesy;

/**
 *
 * @author cgagner
 */
public class LocalCoord {

    public static final LocalCoord ZERO = new LocalCoord(0.0, 0.0);
    public static final double EPS = 1e-6;
    private final double x;
    private final double y;

    public LocalCoord(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LocalCoord other = (LocalCoord) obj;
        if (Math.abs(this.x - other.x) > EPS) {
            return false;
        }
        if (Math.abs(this.y - other.y) > EPS) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LocalCoord{" + "x=" + x + ", y=" + y + '}';
    }

}
