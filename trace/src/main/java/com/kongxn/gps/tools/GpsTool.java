package com.kongxn.gps.tools;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;

public class GpsTool {

    public static double calDistance(double oneLatitude, double oneLongitude, double twoLatitude, double twoLongitude){
        GlobalCoordinates source = new GlobalCoordinates(oneLatitude, oneLongitude);
        GlobalCoordinates target = new GlobalCoordinates(twoLatitude, twoLongitude);

        GeodeticCurve geoCurve = new GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.Sphere, source, target);

        return geoCurve.getEllipsoidalDistance();
    }
}
