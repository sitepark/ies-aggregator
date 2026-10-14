package com.sitepark.ies.aggregator.port;

import java.util.Optional;

/**
 * Finds the position of a postal address - the geocoding an address without stored coordinates is
 * given when a project switches it on.
 *
 * <p>The lookup belongs to whatever service a project geocodes with; SiteKit's legacy asks the
 * {@code address-service} module ({@code getGeoLocation.spml}). An installation without such a
 * service answers no position for every address, and an aggregator then writes the address without
 * coordinates, as the legacy does when the lookup finds nothing.
 */
public interface Geocoder {

  /**
   * Looks up the position of an address.
   *
   * @param city the city; may be empty
   * @param street the street; may be empty
   * @param houseNumber the house number; may be empty
   * @return the position, or empty when the address is not found or no geocoding is available
   */
  Optional<Position> locate(String city, String street, String houseNumber);

  /**
   * A position on the earth, in decimal degrees (WGS 84).
   *
   * @param latitude the latitude
   * @param longitude the longitude
   */
  record Position(double latitude, double longitude) {}
}
