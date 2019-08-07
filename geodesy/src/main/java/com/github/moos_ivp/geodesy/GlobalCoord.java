/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.moos_ivp.geodesy;

/**
 *
 * @author cgagner
 */
public class GlobalCoord {

    public static final GlobalCoord ZERO = new GlobalCoord(0.0, 0.0);
    public static final double EPS = 1e-6;

    private final double latitude;
    private final double longitude;

    public GlobalCoord(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.latitude) ^ (Double.doubleToLongBits(this.latitude) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.longitude) ^ (Double.doubleToLongBits(this.longitude) >>> 32));
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
        final GlobalCoord other = (GlobalCoord) obj;

        if (Math.abs(this.latitude - other.latitude) > EPS) {
            return false;
        }
        if (Math.abs(this.longitude - other.longitude) > EPS) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Wgs84Coord{" + "latitude=" + latitude + ", longitude=" + longitude + '}';
    }

}
