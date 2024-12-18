package radar.metrics

import radar.scene.Point2D
import kotlin.test.Test
import kotlin.test.assertEquals

class MetricsTest {

    private val MAX_COORDINATE = Double.MAX_VALUE
    private val MIN_COORDINATE = Double.MIN_VALUE

    @Test
    fun testEuclidean() {
        val point1 = Point2D(0.0, 0.0)
        val point2 = Point2D(3.0, 4.0)

        val result = euclidean(point1, point2)
        assertEquals(5.0, result, 0.001) 
    }

    @Test
    fun testEuclideanSamePoint() {
	val point3 = Point2D(1.0, 1.0)
        val resultSamePoint = euclidean(point3, point3)
        assertEquals(0.0, resultSamePoint, 0.001)

    }

    @Test
    fun testEuclideanNegative() {
        val point4 = Point2D(-1.0, -1.0)
        val point5 = Point2D(-4.0, -5.0)
        val resultNegativeCoordinates = euclidean(point4, point5)
        assertEquals(5.0, resultNegativeCoordinates, 0.001) 

    }

    @Test
    fun testEuclideanBounds() {
        val pointMax = Point2D(MAX_COORDINATE, MAX_COORDINATE)
        val pointMin = Point2D(MIN_COORDINATE, MIN_COORDINATE)
        assertFailsWith<ArithmeticException> {
            euclidean(pointMax, pointMin) 
        }
    }


    @Test
    fun testManhattan() {
        val point1 = Point2D(1.0, 2.0)
        val point2 = Point2D(4.0, 6.0)

        val result = manhattan(point1, point2)
        assertEquals(7.0, result)
    }

    @Test
    fun testManhattanEqual() {	
	val point3 = Point2D(5.0, 5.0)
        val resultSamePoint = manhattan(point3, point3)
        assertEquals(0.0, resultSamePoint)
    }

    @Test
    fun testManhattanNegative() {
        val point4 = Point2D(-1.0, -2.0)
        val point5 = Point2D(-4.0, -6.0)
        val resultNegativeCoordinates = manhattan(point4, point5)
        assertEquals(7.0, resultNegativeCoordinates) 
    }

    @Test
    fun testManhattanBounds() {
        val pointMax = Point2D(MAX_COORDINATE, MAX_COORDINATE)
        val pointMin = Point2D(MIN_COORDINATE, MIN_COORDINATE)
        assertFailsWith<ArithmeticException> {
            manhattan(pointMax, pointMin) 
        }
    }


    @Test
    fun testGreatCircle() {
        val point1 = Point2D(0.0, 0.0) 
        val point2 = Point2D(90.0, 0.0)

        val result = greatCircle(point1, point2)
        assertEquals(RADIUS * PI / 2, result, 0.001) 
    }

    @Test
    fun testGreatCircle() {
        val point1 = Point2D(0.0, 0.0) 
        val point2 = Point2D(90.0, 0.0) 

        val result = greatCircle(point1, point2)
        assertEquals(RADIUS * PI / 2, result, 0.001)

    @Test
    fun testGreatCircleSamePoint() {
        val point3 = Point2D(45.0, 45.0)
        val resultSamePoint = greatCircle(point3, point3)
        assertEquals(0.0, resultSamePoint)

    }

    @Test
    fun testGreatCircleEquator() {
        val point4 = Point2D(30.0, 60.0)
        val point5 = Point2D(30.0, -60.0)
        val expectedDistanceEquator = RADIUS * PI / 3 
        val resultEquator = greatCircle(point4, point5)
        assertEquals(expectedDistanceEquator, resultEquator, 0.001)
    }

    @Test
    fun testGreatCircleEquatorOpposite() {
        val point6 = Point2D(0.0, 180.0) 
        val expectedDistanceOpposite = RADIUS * PI 
        val resultOpposite = greatCircle(point1, point6)
        assertEquals(expectedDistanceOpposite, resultOpposite, 0.001)
    }

    @Test
    fun testGreatCircleBounds() {
        assertFailsWith<ArithmeticException> {
            greatCircle(Point2D(MAX_COORDINATE, MAX_COORDINATE), Point2D(-MAX_COORDINATE, -MAX_COORDINATE))

        }
    }


}
