/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MOOS.geodesy;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author cgagner
 */
public class MOOSGeodesyTest {

    private static double DEFAULT_LAT = 41.5;
    private static double DEFAULT_LON = -71.3;
    private static double DEFAULT_EASTING = 308029.2473549368;
    private static double DEFAULT_NORTHING = 4596818.096391;
    private static int DEFAULT_ZONE = 19;

    public MOOSGeodesyTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of initialize method, of class MOOSGeodesy.
     */
    @Test
    public void testInitialize() {
        System.out.println("initialize");
        double latitude = DEFAULT_LAT;
        double longitude = DEFAULT_LON;
        MOOSGeodesy instance = new MOOSGeodesy();
        boolean expResult = true;
        boolean result = instance.initialize(latitude, longitude);

        System.out.println("MOOSGeodesy: " + instance);
        Assert.assertEquals(expResult, result);

        Assert.assertEquals(latitude, instance.getOriginLatitude(), 1e-6);
        Assert.assertEquals(latitude, instance.getOriginLatitude(), 1e-6);
        Assert.assertEquals(DEFAULT_EASTING, instance.getOriginEasting(), 1e-2);
        Assert.assertEquals(DEFAULT_NORTHING, instance.getOriginNorthing(), 1e-2);
        Assert.assertEquals(DEFAULT_ZONE, instance.getUtmZone());

    }

    public void testGetOriginLatitude() {
        // NO-OP
    }

    public void testGetOriginLongitude() {
        // NO-OP
    }

    public void testGetOriginNorthing() {
        // NO-OP
    }

    public void testGetOriginEasting() {
        // NO-OP
    }

    public void testGetUtmZone() {
        // NO-OP
    }

    /**
     * Test of latLon2LocalUtm method, of class MOOSGeodesy.
     */
    @Test
    public void testLatLon2LocalUtm() {
        System.out.println("latLon2LocalUtm");
        double latitude = DEFAULT_LAT;
        double longitude = DEFAULT_LON;
        MOOSGeodesy instance = new MOOSGeodesy(DEFAULT_LAT, DEFAULT_LON);
        LocalCoord expResult = LocalCoord.ZERO;
        LocalCoord result = instance.latLon2LocalUtm(latitude, longitude);
        Assert.assertEquals(expResult, result);

        expResult = new LocalCoord(-745.651121019037,3353.179780054837);
        result = instance.latLon2LocalUtm(41.53,-71.31);
        Assert.assertEquals(expResult, result);
        
        expResult = new LocalCoord(1581.570641721715,-3375.129465023056);
        result = instance.latLon2LocalUtm(41.47,-71.28);
        Assert.assertEquals(expResult, result);
        
    }

    /**
     * Test of localUtm2LatLon method, of class MOOSGeodesy.
     */
    @Test
    public void testLocalUtm2LatLon() {
        System.out.println("localUtm2LatLon");
        double x = 0.0;
        double y = 0.0;
        MOOSGeodesy instance = new MOOSGeodesy(DEFAULT_LAT, DEFAULT_LON);
        GlobalCoord expResult = new GlobalCoord(DEFAULT_LAT, DEFAULT_LON);
        GlobalCoord result = instance.localUtm2LatLon(x, y);
        Assert.assertEquals(expResult, result);

        
        expResult = new GlobalCoord(41.529999999989,-71.310000000010);
        result = instance.localUtm2LatLon(-745.651121019037,3353.179780054837);
        Assert.assertEquals(expResult, result);
        
        expResult = new GlobalCoord(41.469999999990,-71.280000000010);
        result = instance.localUtm2LatLon(1581.570641721715,-3375.129465023056);
        Assert.assertEquals(expResult, result);
        
    }

    /**
     * Test of latLon2LocalGrid method, of class MOOSGeodesy.
     */
    @Test
    public void testLatLon2LocalGrid() {
        System.out.println("latLon2LocalGrid");
        double latitude = DEFAULT_LAT;
        double longitude = DEFAULT_LON;
        MOOSGeodesy instance = new MOOSGeodesy(DEFAULT_LAT, DEFAULT_LON);
        LocalCoord expResult = LocalCoord.ZERO;
        LocalCoord result = instance.latLon2LocalGrid(latitude, longitude);

        System.out.println(expResult);
        System.out.println(result);

        Assert.assertEquals(expResult, result);

        expResult = new LocalCoord(-832.115644420294,3334.648566415680);
        result = instance.latLon2LocalGrid(41.53,-71.31);
        Assert.assertEquals(expResult, result);
        
        expResult = new LocalCoord(1665.779675989095,-3334.660211717011);
        result = instance.latLon2LocalGrid(41.47,-71.28);
        Assert.assertEquals(expResult, result);
        
        
        
    }

    /**
     * Test of localGrid2LatLon method, of class MOOSGeodesy.
     */
    @Test
    public void testLocalGrid2LatLon() {
        System.out.println("localGrid2LatLon");
        double x = 0.0;
        double y = 0.0;
        MOOSGeodesy instance = new MOOSGeodesy(DEFAULT_LAT, DEFAULT_LON);
        GlobalCoord expResult = new GlobalCoord(DEFAULT_LAT, DEFAULT_LON);
        GlobalCoord result = instance.localGrid2LatLon(x, y);
        Assert.assertEquals(expResult, result);
        
        
        expResult = new GlobalCoord(41.529999947614,-71.309995348765);
        result = instance.localGrid2LatLon(-832.115644420294,3334.648566415680);
        Assert.assertEquals(expResult, result);
        
        expResult = new GlobalCoord(41.469999947620,-71.279990702984);
        result = instance.localGrid2LatLon(1665.779675989095, -3334.660211717011);
        Assert.assertEquals(expResult, result);
        
    }

    /**
     * Test of toString method, of class MOOSGeodesy.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        MOOSGeodesy instance = new MOOSGeodesy(DEFAULT_LAT, DEFAULT_LON);
        String expResult = String.format("MOOSGeodesy{originLatitude=%.6f, originLongitude=%.6f, originEasting=%.6f, originNorthing=%.6f, utmZone=%d}",
                DEFAULT_LAT, DEFAULT_LON, DEFAULT_EASTING, DEFAULT_NORTHING, DEFAULT_ZONE);
        String result = instance.toString();
        System.out.println(expResult);
        System.out.println(result);
        Assert.assertEquals(expResult, result);
    }

}
