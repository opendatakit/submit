package org.opendatakit.submit.flags;

/**
 * Radio
 * Flags indicating radio availability.
 * Note: Although WiFi and WiFi-Direct both use the same
 * radio, we treat them as different radio resources.
 * @author mvigil
 *
 */
public enum Radio {
	LOW_BAND_CELL, // 2G for SMS
	HIGH_BAND_CELL, // 3G+ for IP 
	WIFI, // 802.11 for IP
	P2P_WIFI, // 802.11 ad-hoc for IP
	NFC // NFC for tags
}
