/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.moos_ivp.moosbeans;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author cgagner
 */
public class Utils {

    public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    private static double moosTimeWarp = 1.0;
    /**
     * Allocate a buffer with the correct byte order. Java by default will use
     * big endian. However, MOOS uses little endian.
     *
     * @param size Size of the buffer to allocate.
     * @return A little endian buffer with the specified size.
     */
    public static ByteBuffer allocate(int size) {
        return ByteBuffer.allocate(size).order(BYTE_ORDER);
    }

    public static double currentTime() {
        return currentTime(true);
    }

    /**
     * Get the current MOOS time.
     * @param applyTimeWarp Apply the time warp.
     * @return 
     */
    public static double currentTime(boolean applyTimeWarp) {
        return ((double) System.currentTimeMillis()) / 1000.0d * moosTimeWarp;
    }
    
    /**
     * Get the MOOS time warp.
     * @return 
     */
    public static double getMoosTimeWarp() {
        return moosTimeWarp;
    }
    
    /**
     * Set the MOOS time warp.
     * @param timeWarp 
     */
    public static void setMoosTimeWarp(double timeWarp) {
        moosTimeWarp = timeWarp;
    }
    
}
