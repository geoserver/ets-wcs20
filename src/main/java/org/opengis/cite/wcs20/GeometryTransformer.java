package org.opengis.cite.wcs20;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.opengis.cite.wcs20.exception.UnknownCrsException;

/**
 * Modified from ets-ogcapi-features10 GeometryTransformer
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeometryTransformer {

	private final GeometryFactory geometryFactory = new GeometryFactory();

	private final CoordinateTransform transformer;

	/**
	 * <p>
	 * Constructor for GeometryTransformer.
	 * </p>
	 * @param srcCrs source crs, , never <code>null</code>
	 * @param targetCrs target crs, , never <code>null</code>
	 */
	public GeometryTransformer(String srcCrs, String targetCrs) {
		CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
		CRSFactory crsFactory = new CRSFactory();
		this.transformer = coordinateTransformFactory.createTransform(
				crsFactory.createFromName(getCodeWithAuthority(srcCrs)),
				crsFactory.createFromName(getCodeWithAuthority(targetCrs)));
	}

	/**
	 * <p>
	 * transform.
	 * </p>
	 * @param geometryToTransform a {@link org.locationtech.jts.geom.Geometry} object
	 * @return a {@link org.locationtech.jts.geom.Geometry} object
	 */
	public Geometry transform(Geometry geometryToTransform) {
		if (geometryToTransform == null)
			return null;
		if (geometryToTransform instanceof Point) {
			return transform((Point) geometryToTransform);
		}
		else if (geometryToTransform instanceof Polygon) {
			return transform((Polygon) geometryToTransform);
		}
		else if ((geometryToTransform instanceof LineString)) {
			return transform((LineString) geometryToTransform);
		}
		else if ((geometryToTransform instanceof GeometryCollection)) {
			return transform((GeometryCollection) geometryToTransform);
		}
		throw new IllegalArgumentException("Unsupported geometry type: " + geometryToTransform.getClass());
	}

	/**
	 * <p>
	 * transform.
	 * </p>
	 * @param geometryToTransform a {@link org.locationtech.jts.geom.Point} object
	 * @return a {@link org.locationtech.jts.geom.Point} object
	 */
	public Point transform(Point geometryToTransform) {
		Coordinate coordinate = geometryToTransform.getCoordinate();
		Coordinate transformedCoordinate = transform(coordinate);
		return geometryFactory.createPoint(transformedCoordinate);
	}

	/**
	 * <p>
	 * transform.
	 * </p>
	 * @param geometryToTransform a {@link org.locationtech.jts.geom.LineString} object
	 * @return a {@link org.locationtech.jts.geom.LineString} object
	 */
	public LineString transform(LineString geometryToTransform) {
		Coordinate[] coordinates = geometryToTransform.getCoordinates();
		Coordinate[] transformedCoordinates = transform(coordinates);
		return geometryFactory.createLineString(transformedCoordinates);
	}

	/**
	 * <p>
	 * transform.
	 * </p>
	 * @param geometryToTransform a {@link org.locationtech.jts.geom.Polygon} object
	 * @return a {@link org.locationtech.jts.geom.Polygon} object
	 */
	public Polygon transform(Polygon geometryToTransform) {
		Coordinate[] coordinatesExteriorRing = geometryToTransform.getExteriorRing().getCoordinates();
		Coordinate[] transformedCoordinatesExteriorRing = transform(coordinatesExteriorRing);
		LinearRing exteriorRing = geometryFactory.createLinearRing(transformedCoordinatesExteriorRing);
		LinearRing[] interiorRings = new LinearRing[geometryToTransform.getNumInteriorRing()];
		for (int numInteriorRing = 0; numInteriorRing < geometryToTransform.getNumInteriorRing(); numInteriorRing++) {
			LinearRing interiorRingN = geometryToTransform.getInteriorRingN(numInteriorRing);
			Coordinate[] transformedCoordinatesInteriorRingN = transform(interiorRingN.getCoordinates());
			interiorRings[numInteriorRing] = geometryFactory.createLinearRing(transformedCoordinatesInteriorRingN);
		}
		return geometryFactory.createPolygon(exteriorRing, interiorRings);
	}

	/**
	 * <p>
	 * transform.
	 * </p>
	 * @param geometryToTransform a {@link org.locationtech.jts.geom.GeometryCollection}
	 * object
	 * @return a {@link org.locationtech.jts.geom.Geometry} object
	 */
	public Geometry transform(GeometryCollection geometryToTransform) {
		if (geometryToTransform instanceof MultiPoint) {
			return transform((MultiPoint) geometryToTransform);
		}
		else if (geometryToTransform instanceof MultiLineString) {
			return transform((MultiLineString) geometryToTransform);
		}
		else if (geometryToTransform instanceof MultiPolygon) {
			return transform((MultiPolygon) geometryToTransform);
		}
		throw new IllegalArgumentException("Unsupported geometry type: " + geometryToTransform.getClass());
	}

	/**
	 * <p>
	 * transform.
	 * </p>
	 * @param geometryToTransform a {@link org.locationtech.jts.geom.MultiPoint} object
	 * @return a {@link org.locationtech.jts.geom.MultiPoint} object
	 */
	public MultiPoint transform(MultiPoint geometryToTransform) {
		Point[] points = new Point[geometryToTransform.getNumGeometries()];
		for (int numGeometry = 0; numGeometry < geometryToTransform.getNumGeometries(); numGeometry++) {
			Geometry geometryN = geometryToTransform.getGeometryN(numGeometry);
			points[numGeometry] = transform((Point) geometryN);
		}
		return geometryFactory.createMultiPoint(points);
	}

	/**
	 * <p>
	 * transform.
	 * </p>
	 * @param geometryToTransform a {@link org.locationtech.jts.geom.MultiLineString}
	 * object
	 * @return a {@link org.locationtech.jts.geom.MultiLineString} object
	 */
	public MultiLineString transform(MultiLineString geometryToTransform) {
		LineString[] lineStrings = new LineString[geometryToTransform.getNumGeometries()];
		for (int numGeometry = 0; numGeometry < geometryToTransform.getNumGeometries(); numGeometry++) {
			Geometry geometryN = geometryToTransform.getGeometryN(numGeometry);
			lineStrings[numGeometry] = transform((LineString) geometryN);
		}
		return geometryFactory.createMultiLineString(lineStrings);

	}

	/**
	 * <p>
	 * transform.
	 * </p>
	 * @param geometryToTransform a {@link org.locationtech.jts.geom.MultiPolygon} object
	 * @return a {@link org.locationtech.jts.geom.MultiPolygon} object
	 */
	public MultiPolygon transform(MultiPolygon geometryToTransform) {
		Polygon[] polygons = new Polygon[geometryToTransform.getNumGeometries()];
		for (int numGeometry = 0; numGeometry < geometryToTransform.getNumGeometries(); numGeometry++) {
			Geometry geometryN = geometryToTransform.getGeometryN(numGeometry);
			polygons[numGeometry] = transform((Polygon) geometryN);
		}
		return geometryFactory.createMultiPolygon(polygons);
	}

	private Coordinate[] transform(Coordinate[] coordinates) {
		List<Coordinate> transformedCoordinates = Arrays.stream(coordinates)
			.map(coord -> transform(coord))
			.collect(Collectors.toList());
		return transformedCoordinates.toArray(new Coordinate[transformedCoordinates.size()]);
	}

	private Coordinate transform(Coordinate coord) {
		ProjCoordinate srcCoordinate = new ProjCoordinate(coord.x, coord.y, coord.z);
		ProjCoordinate targetCoordinate = new ProjCoordinate();
		transformer.transform(srcCoordinate, targetCoordinate);
		return new Coordinate(targetCoordinate.x, targetCoordinate.y, targetCoordinate.z);
	}

	/**
	 * srid from the passed crs
	 * @param code a {@link java.lang.String} object
	 * @return the parsed srid, -1 if the crs is <code>null</code>
	 */
	public int getSrid(String code) {
		try {
			if (code.startsWith("http://www.opengis.net/def/crs/")) {
				return Integer.parseInt(code.substring(code.lastIndexOf("/") + 1));
			}
			else if (code.startsWith("urn:ogc:def:crs:")) {
				return Integer.parseInt(code.substring(code.lastIndexOf(":") + 1));
			}
		}
		catch (NumberFormatException e) {
			throw new UnknownCrsException(
					String.format("Could not parse srid from crs '%s', crs is not supported.", code));
		}
		throw new UnknownCrsException(String.format("Could not parse srid from crs '%s', crs is not supported.", code));
	}

	/**
	 * <p>
	 * getCodeWithAuthority.
	 * </p>
	 * @param code a {@link java.lang.String} object
	 * @return the code with the authority: EPSG:CODE, may be <code>null</code>
	 */
	public String getCodeWithAuthority(String code) {
		if (code.startsWith("urn:ogc:def:crs:EPSG") || code.startsWith("http://www.opengis.net/def/crs/EPSG")) {
			int srid = getSrid(code);
			return String.format("EPSG:%s", srid);
		}
		else if (code.startsWith("EPSG:")) {
			return code;
		}
		throw new UnknownCrsException(String.format(
				"CRS %s is not supported, only OGC URNs (starting with urn:ogc:def:crs:epsg) and OGC http-URIs (starting with http://www.opengis.net/def/crs/epsg) with EPSG auhority are supported.",
				code));
	}

	/**
	 * <p>
	 * Getter for the field <code>geometryFactory</code>.
	 * </p>
	 * @return a {@link org.locationtech.jts.geom.GeometryFactory} object
	 */
	public GeometryFactory getGeometryFactory() {
		return geometryFactory;
	}

}
