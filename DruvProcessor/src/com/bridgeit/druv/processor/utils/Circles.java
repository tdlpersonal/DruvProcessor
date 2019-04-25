package com.bridgeit.druv.processor.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class Circles {
	
	static long startTime= Calendar.getInstance().getTimeInMillis();
	static NumberFormat numberFormat = NumberFormat.getInstance();
	
    public Point[] findCentersOfCircles(Point p1, Point p2, double r) {
        if (r < 0.0) throw new IllegalArgumentException("the radius can't be negative");
        if (r == 0.0 && p1 != p2) throw new IllegalArgumentException("no circles can ever be drawn");
        if (r == 0.0) return new Point[]{p1, p1};
        if (Objects.equals(p1, p2)) throw new IllegalArgumentException("an infinite number of circles can be drawn");
        double distance = p1.distanceFrom(p2);
        double diameter = 2.0 * r;
        if (distance > diameter) throw new IllegalArgumentException("the points are too far apart to draw a circle");
        Point center = new Point((p1.x + p2.x) / 2.0, (p1.y + p2.y) / 2.0);
        if (distance == diameter) return new Point[]{center, center};
        double mirrorDistance = Math.sqrt(r * r - distance * distance / 4.0);
        double dx = (p2.x - p1.x) * mirrorDistance / distance;
        double dy = (p2.y - p1.y) * mirrorDistance / distance;
        return new Point[]{
            new Point(center.x - dy, center.y + dx),
            new Point(center.x + dy, center.y - dx)
        };
    }
 
    public ArrayList<Point> getPointsOfCircle(Point x, Point y, double radius,double increment,String mode)
    {
    	long time = Calendar.getInstance().getTimeInMillis();
    	Point[] centers = findCentersOfCircles(x, y, radius);
    	time = displayTime("Find Cneters", time);
    	
    	System.out.println(numberFormat.format(centers[0].x)+"~"+numberFormat.format(centers[0].y));
    	System.out.println(numberFormat.format(centers[1].x)+"~"+numberFormat.format(centers[1].y));
    	ArrayList<Point> listPointsAtO = getPointsOfCircleWithCenterAtO(radius, increment,mode);
    	time = displayTime("Find Co-Ordinates with circle at Origin", time);
    	ArrayList<Point> actualListPoints_1 = new ArrayList<Point>();
    	ArrayList<Point> actualListPoints_2 = new ArrayList<Point>();
    	Point center = centers[0];
    	boolean startFound = false;
    	boolean endFound = false;
    	Point p = null;
    	Point q = null;
    	
    	for(int i=0;i<listPointsAtO.size();i++)
    	{
    			p = listPointsAtO.get(i);
    			q = new Point(p.x+center.x, p.y+center.y);
    			actualListPoints_1.add(q);
    		
    	}
    	time = displayTime("Find Co-Ordinates  for First Circle", time);
    	
    	
    	if(centers.length==2)
    	{
    		center = centers[1];
    		actualListPoints_2 = new ArrayList<Point>();
    		for(int i=0;i<listPointsAtO.size();i++)
        	{
    				p = listPointsAtO.get(i);
        			q = new Point(p.x+center.x, p.y+center.y);
        			actualListPoints_2.add(q);
        		
        	}
    		
    		
    	}
    	time = displayTime("Find Co-Ordinates  for Second Circle", time);
    	
    	System.out.println("2:" + actualListPoints_1.size());
    	System.out.println("3:" + actualListPoints_2.size());
    	
    	ArrayList<Point>  rearrangedList1 = rearrangeList(actualListPoints_1, x, y);
    	time = displayTime("Re-arrange for First Circle", time);
    	ArrayList<Point>  rearrangedList2 = rearrangeList(actualListPoints_2, x, y);
    	time = displayTime("Re-arrange for Second  Circle", time);
    	
    	if(rearrangedList2.size()==0)
    		return rearrangedList1;
    	else 
    	{
    		/*for(int i=0;i<rearrangedList1.size();i++)
    		{
    			p = rearrangedList1.get(i);
    			System.out.println(numberFormat.format(p.x) +"~" + numberFormat.format(p.y));
    		}
    		
    		for(int i=0;i<rearrangedList2.size();i++)
    		{
    			p = rearrangedList2.get(i);
    			System.out.println(numberFormat.format(p.x) +"~" + numberFormat.format(p.y));
    		}
    		*/
    		if(rearrangedList1.size()<=rearrangedList2.size())
    			return rearrangedList1;
    		else 
    			return rearrangedList2;
    	}
    	
    }
    
    private long displayTime(String str, long prev) {
		// TODO Auto-generated method stub
    	long curr = Calendar.getInstance().getTimeInMillis();
    	System.out.println("Time Taken for : " + str + ": " + (curr-prev) + " ms");
    	return curr;
	}

	private ArrayList<Point> rearrangeList (ArrayList<Point> actualListPoints_1, Point x, Point y )
    {
    	Point p=null;
    	int startIndex = -1 ;
    	int endIndex = -1 ;
    	boolean foundStart = false;
    	boolean foundEnd = false;
    	double xdistance = x.distanceFrom(y);
    	double ydistance = x.distanceFrom(y);
    	// find first point - abs x and abs Y should match with x 
    	for(int i=0;i<actualListPoints_1.size();i++)
		{
    		p = actualListPoints_1.get(i);
    		if(xdistance>p.distanceFrom(x))
    		{
    			xdistance = p.distanceFrom(x);
    			startIndex = i;
    		}
    		if(ydistance>p.distanceFrom(y))
    		{
    			ydistance = p.distanceFrom(y);
    			endIndex = i;
    		}
    		
		}
    	
    	
    	System.out.println("start index: " + startIndex + ":" + xdistance + "-->" + actualListPoints_1.get(startIndex));
    	System.out.println("end index: " + endIndex + ":" + ydistance + "-->" + actualListPoints_1.get(endIndex));
    	
    	ArrayList<Point> arrangedList_1 = new ArrayList<Point>();
    	int stop = startIndex < endIndex ? endIndex : actualListPoints_1.size()-1;
    	for(int i=startIndex;i<=stop;i++)
    	{
    		arrangedList_1.add(actualListPoints_1.get(i));
    	}
    	
    	if(endIndex<startIndex)
    	{
    		for(int i=0;i<=endIndex;i++)
        	{
        		arrangedList_1.add(actualListPoints_1.get(i));
        	}
    		
    	}
    	
    	return arrangedList_1;
    }
    
    public ArrayList<Point> getPointsOfCircleWithCenterAtO(double radius, double increment,String mode)
	{
		ArrayList<Point> listPoints = new ArrayList<Point>();
		double x  , y ;
		for ( x=-1*radius ; x<=radius; x+=increment ) {
			y = Math.sqrt(Math.abs(Math.pow(radius,2) - Math.pow(x,2))) ;
			if(mode.equals("G2"))
				listPoints.add(new Point(x, y));
			else 
				listPoints.add(new Point(x, y*-1));
		}
		for ( x=radius ; x>=-1*radius; x-=increment ) {
			y = -1* Math.sqrt(Math.abs(Math.pow(radius,2) - Math.pow(x,2))) ;
			if(mode.equals("G2"))
				listPoints.add(new Point(x, y));
			else 
				listPoints.add(new Point(x, y*-1));
		}
	    return listPoints;
	}
    
    public Point ArcCenter(Point P1, Point P2, Double Radius, int Direction)
    {
        // returns arc center based on start and end points, radius and arc direction (2=CW, 3=CCW)
        // Radius can be negative (for arcs over 180 degrees)
        double Angle = 0, AdditionalAngle = 0, L1 = 0, L2 = 0, Diff=0;
        double AllowedError = 0.002;
        Point Center = new Point(0.0, 0.0);
        Point T1, T2;

        // Sort points depending of direction
        if (Direction == 3)
        {
            T1 = new Point(P2.x, P2.y);
            T2 = new Point(P1.x, P1.y);
        }
        else // 03
        {
            T1 = new Point(P1.x, P1.y);
            T2 = new Point(P2.x, P2.y);
        }

        // find angle arc covers
        Angle = CalculateAngle(T1, T2);

        L1 = PointDistance(T1, T2) / 2;
        Diff = L1 - Math.abs(Radius);

        if (Math.abs(Radius) < L1 && Diff > AllowedError)
        {
            return Center;
        }

        L2 = Math.sqrt(Math.abs(Math.pow(Radius,2) - Math.pow(L1,2)));

        if (L1 == 0)
            AdditionalAngle = Math.PI / 2;
        else
            AdditionalAngle = Math.atan(L2 / L1);

        // Add or subtract from angle (depending of radius sign)
        if (Radius < 0)
            Angle -= AdditionalAngle;
        else
            Angle += AdditionalAngle;

        // calculate center (from T1)
        Center = new Point((double) (T1.x + Math.abs(Radius) * Math.cos(Angle)), (double) (T1.y + Math.abs(Radius) * Math.sin(Angle)));
        return Center;
    }

    public double CalculateAngle(Point P1, Point P2)
    {
        // returns Angle of line between 2 points and X axis (according to quadrants)
        double Angle = 0;

        if (P1 == P2) // same points
            return 0;
        else if (P1.x == P2.x) // 90 or 270
        {
            Angle = Math.PI / 2;
            if (P1.y > P2.y) Angle += Math.PI;
        }
        else if (P1.y == P2.y) // 0 or 180
        {
            Angle = 0;
            if (P1.x > P2.x) Angle += Math.PI;
        }
        else
        {
            Angle = Math.atan(Math.abs((P2.y - P1.y) / (P2.x - P1.x))); // 1. quadrant
            if (P1.x > P2.x && P1.y < P2.y) // 2. quadrant
                Angle = Math.PI - Angle;
            else if (P1.x > P2.x && P1.y > P2.y) // 3. quadrant
                Angle += Math.PI;
            else if (P1.x < P2.x && P1.y > P2.y) // 4. quadrant
                Angle = 2 * Math.PI - Angle;
        }
        return Angle;
    }

    public double PointDistance(Point P1, Point P2)
    {
        return Math.sqrt(Math.pow((P2.x - P1.x), 2) + Math.pow((P2.y - P1.y), 2));
    }

    public static void main(String[] args) throws IOException {
    	numberFormat.setMaximumFractionDigits(4);
		numberFormat.setMinimumFractionDigits(4);
		numberFormat.setMaximumIntegerDigits(5);
		numberFormat.setMinimumIntegerDigits(5);
		numberFormat.setGroupingUsed(false);
		
		Circles c = new Circles();
		
		ArrayList<Point> points = c.getPointsOfCircle(new Point(143.0, 200.0),new Point(105.0, 232.0),  32, .001,"G3");
		
		for(int i=0;i<points.size();i++)
		{
			Point p = points.get(i);
			//System.out.println(numberFormat.format(p.x) +"~" + numberFormat.format(p.y));
		}
		System.out.println("Total Time Taken : " +(Calendar.getInstance().getTimeInMillis() - startTime + " ms"));
	}
    
    
}
