/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.moos_ivp.geodesy;

import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.ProjCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Port of MOOSGeodesy from C++ Library.
 *
 * @author cgagner
 */
public class MOOSGeodesy {

    private static final Logger LOG = LoggerFactory.getLogger(MOOSGeodesy.class);
    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static final double RAD_TO_DEG = 180.0 / Math.PI;

    private double originLatitude;
    private double originLongitude;
    private double originEasting;
    private double originNorthing;

    private int utmZone;
    private CoordinateTransform transformUtmToWgs84;
    private CoordinateTransform transformWgs84ToUtm;

    public MOOSGeodesy() {

    }

    public MOOSGeodesy(double latitude, double longitude) {
        initialize(latitude, longitude);
    }

    /**
     * Initialize MOOSGeodesy
     *
     * @param latitude latitude in decimal degrees
     * @param longitude longitude in decimal degrees
     * @return true if MOOSGeodesy was initialized successfully; false
     * otherwise.
     */
    public final boolean initialize(double latitude, double longitude) {

        CRSFactory factory = new CRSFactory();

        int zone = ((int) Math.floor((longitude + 180.0) / 6.0) + 1) % 60;
        CoordinateReferenceSystem wgs84Crs;

        try {
            wgs84Crs = factory.createFromParameters("WGS84", "+proj=latlong +ellps=WGS84");
        } catch (Exception ex) {
            LOG.error("Failed to create WGS84 CoordinateRefenceSystem", ex);
            return false;
        }

        CoordinateReferenceSystem utmCrs;
        try {
            utmCrs = factory.createFromParameters("UTM", "+proj=utm +ellps=WGS84 +zone=" + zone);
        } catch (Exception ex) {
            LOG.error("Failed to create UTM CoordinateReferenceSystem", ex);
            return false;
        }

        BasicCoordinateTransform wgs84ToUtm;
        try {
            wgs84ToUtm = new BasicCoordinateTransform(wgs84Crs, utmCrs);
        } catch (Exception ex) {
            LOG.error("Failed to create CoordinateTransform from WGS84 to UTM", ex);
            return false;
        }
        BasicCoordinateTransform utmToWgs84;
        try {
            utmToWgs84 = new BasicCoordinateTransform(utmCrs, wgs84Crs);
        } catch (Exception ex) {
            LOG.error("Failed to create CoordinateTransform from UTM to WGS84");
            return false;
        }

        ProjCoordinate srcCoord = new ProjCoordinate(longitude, latitude);
        ProjCoordinate dstCoord = new ProjCoordinate();

        try {
            wgs84ToUtm.transform(srcCoord, dstCoord);
        } catch (Exception ex) {
            LOG.error("Failed to transfor WGS84 origin into UTM.", ex);
            return false;
        }

        originLatitude = latitude;
        originLongitude = longitude;
        originEasting = dstCoord.x;
        originNorthing = dstCoord.y;
        utmZone = zone;
        transformWgs84ToUtm = wgs84ToUtm;
        transformUtmToWgs84 = utmToWgs84;

        return true;
    }

    public final double getOriginLatitude() {
        return originLatitude;
    }

    public final double getOriginLongitude() {
        return originLongitude;
    }

    public final double getOriginNorthing() {
        return originNorthing;
    }

    public final double getOriginEasting() {
        return originEasting;
    }

    public final int getUtmZone() {
        return utmZone;
    }

    public LocalCoord latLon2LocalUtm(double latitude, double longitude) {
        if (transformWgs84ToUtm == null) {
            LOG.error("Must call initialize before calling latLon2LocalUtm");
            return null;
        }

        ProjCoordinate src = new ProjCoordinate(longitude, latitude);
        ProjCoordinate dst = new ProjCoordinate();

        try {
            transformWgs84ToUtm.transform(src, dst);
        } catch (Exception ex) {
            LOG.error("Failed to transform (lat,lon) = ({},{})", latitude, longitude, ex);
            return null;
        }

        return new LocalCoord(dst.x - getOriginEasting(), dst.y - getOriginNorthing());
    }

    public GlobalCoord localUtm2LatLon(double x, double y) {
        if (transformUtmToWgs84 == null) {
            LOG.error("Must call initialize before calling localUtm2LatLon");
            return null;
        }

        ProjCoordinate src = new ProjCoordinate(x + getOriginEasting(), y + getOriginNorthing());
        ProjCoordinate dst = new ProjCoordinate();

        try {
            transformUtmToWgs84.transform(src, dst);
        } catch (Exception ex) {
            LOG.error("Failed to transform (x,y) = ({},{})", x, y, ex);
            return null;
        }

        return new GlobalCoord(dst.y, dst.x);
    }

    public LocalCoord latLon2LocalGrid(double latitude, double longitude) {
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
            return null;
        }
        //(semimajor axis)
        double dfa = 6378137;
        // (semiminor axis)
        double dfb = 6356752;

        double dftanlat2 = Math.pow(Math.tan(latitude * DEG_TO_RAD), 2);
        double dfRadius = dfb * Math.sqrt(1 + dftanlat2) / Math.sqrt((Math.pow(dfb, 2) / Math.pow(dfa, 2)) + dftanlat2);

        //the decimal degrees conversion should take place elsewhere
        double dXArcDeg = (longitude - getOriginLongitude()) * DEG_TO_RAD;
        double dX = dfRadius * Math.sin(dXArcDeg) * Math.cos(latitude * DEG_TO_RAD);

        double dYArcDeg = (latitude - getOriginLatitude()) * DEG_TO_RAD;
        double dY = dfRadius * Math.sin(dYArcDeg);

        return new LocalCoord(dX, dY);
    }

    public GlobalCoord localGrid2LatLon(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            return null;
        }

        //(semimajor axis)
        double dfa = 6378137;
        // (semiminor axis)
        double dfb = 6356752;

        double dftanlat2 = Math.pow(Math.tan(getOriginLatitude() * DEG_TO_RAD), 2);
        double dfRadius = dfb * Math.sqrt(1 + dftanlat2) / Math.sqrt((Math.pow(dfb, 2) / Math.pow(dfa, 2)) + dftanlat2);

        //first calculate lat arc
        double dfYArcRad = Math.asin(y / dfRadius); //returns result in rad
        double dfYArcDeg = dfYArcRad * RAD_TO_DEG;

        double dfXArcRad = Math.asin(x / (dfRadius * Math.cos(getOriginLatitude() * DEG_TO_RAD)));
        double dfXArcDeg = dfXArcRad * RAD_TO_DEG;

        //add the origin to these arc lengths
        return new GlobalCoord(dfYArcDeg + getOriginLatitude(), dfXArcDeg + getOriginLongitude());
    }

    @Override
    public String toString() {
        return String.format("MOOSGeodesy{originLatitude=%.6f, originLongitude=%.6f, originEasting=%.6f, originNorthing=%.6f, utmZone=%d}",
                originLatitude, originLongitude, originEasting, originNorthing, utmZone);
    }

}
