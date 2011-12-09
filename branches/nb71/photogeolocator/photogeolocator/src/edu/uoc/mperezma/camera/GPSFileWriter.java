/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma.camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.RationalNumber;
import org.apache.sanselan.common.RationalNumberUtilities;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

/**
 *
 * @author mperezma
 */
public class GPSFileWriter {

    public static boolean update(File gpxfile, double longitude, byte longitudeRef, double latitude, byte latitudeRef, boolean rational, String mapDatum) {    
        boolean result = true;
        try {
            TiffOutputSet outputSet = new TiffOutputSet();
            IImageMetadata metadata = Sanselan.getMetadata(gpxfile);
            JpegImageMetadata jpegMetadata = null;
            if (metadata != null) {
                jpegMetadata = (JpegImageMetadata) metadata;
            }
            TiffImageMetadata exif = null;
            if (jpegMetadata != null) {
                exif = jpegMetadata.getExif();
            }
            if (exif == null) {
                return false;
            }
            outputSet = exif.getOutputSet();
            if (outputSet != null) {
                TiffOutputDirectory exifDirectory = outputSet.getOrCreateRootDirectory();
                TagInfo softwareTag = TiffConstants.EXIF_TAG_SOFTWARE;
                String softwareString = "PhotoGeolocator for Android";
                TiffOutputField softwareField = new TiffOutputField(softwareTag, TiffFieldTypeConstants.FIELD_TYPE_ASCII, softwareString.length(), softwareString.getBytes());
                exifDirectory.removeField(TiffConstants.EXIF_TAG_SOFTWARE);
                exifDirectory.add(softwareField);

                double longitudeMinutes = (longitude - (int) longitude) * 60d;
                double longitudeSeconds = (longitudeMinutes - (int) longitudeMinutes) * 60d;

                double latitudeMinutes = (latitude - (int) latitude) * 60d;
                double latitudeSeconds = (latitudeMinutes - (int) latitudeMinutes) * 60d;
                
                exifDirectory = outputSet.getOrCreateGPSDirectory();
                TagInfo tgpsversion = new TagInfo("GPSVersionID", 0x0000, TiffFieldTypeConstants.FIELD_TYPE_BYTE);
                TiffOutputField gpsversion = new TiffOutputField(tgpsversion, TiffFieldTypeConstants.FIELD_TYPE_BYTE, 4, new byte[]{2, 2, 0, 0});
                exifDirectory.removeField(TiffConstants.GPS_TAG_GPS_VERSION_ID);
                exifDirectory.add(gpsversion);

                // add gpsmapdatum tag
                if (mapDatum != null) {
                    TagInfo tgpsmapdatum = new TagInfo("GPSMapDatum", 0x0012, TiffFieldTypeConstants.FIELD_TYPE_ASCII);
                    TiffOutputField gpsmapdatum = new TiffOutputField(tgpsmapdatum, TiffFieldTypeConstants.FIELD_TYPE_ASCII, mapDatum.length() + 1, (mapDatum + "\0").getBytes("US-ASCII"));
                    exifDirectory.removeField(TiffConstants.GPS_TAG_GPS_MAP_DATUM);
                    exifDirectory.add(gpsmapdatum);
                }
                
                TagInfo tlongitud = new TagInfo("GPSLongitude", 0x0004, TiffFieldTypeConstants.FIELD_TYPE_RATIONAL);

                TiffOutputField longitud;
                if (rational) {
                    longitud = new TiffOutputField(tlongitud, TiffFieldTypeConstants.FIELD_TYPE_RATIONAL, 3, getRationalArray((int) longitude, (int) longitudeMinutes, longitudeSeconds));
                } else {
                    longitud = new TiffOutputField(tlongitud, TiffFieldTypeConstants.FIELD_TYPE_RATIONAL, 3, getRationalArray((int) longitude, (int) longitudeMinutes, (int) Math.round(longitudeSeconds)));
                }
                
                exifDirectory.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE);
                exifDirectory.add(longitud);
                TagInfo tlongitudref = new TagInfo("GPSLongitudeRef", 0x0003, TiffFieldTypeConstants.FIELD_TYPE_ASCII);
                TiffOutputField longitudRef = new TiffOutputField(tlongitudref, TiffFieldTypeConstants.FIELD_TYPE_ASCII, 2, new byte[]{longitudeRef, 0});
                exifDirectory.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
                exifDirectory.add(longitudRef);
                TagInfo tlatitud = new TagInfo("GPSLatitude", 0x0002, TiffFieldTypeConstants.FIELD_TYPE_RATIONAL);

                TiffOutputField latitud;
                if (rational) {
                    latitud = new TiffOutputField(tlatitud, TiffFieldTypeConstants.FIELD_TYPE_RATIONAL, 3, getRationalArray((int) latitude, (int) latitudeMinutes, latitudeSeconds));
                } else {
                    latitud = new TiffOutputField(tlatitud, TiffFieldTypeConstants.FIELD_TYPE_RATIONAL, 3, getRationalArray((int) latitude, (int) latitudeMinutes, (int) Math.round(latitudeSeconds)));
                }
                
                exifDirectory.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE);
                exifDirectory.add(latitud);
                TagInfo tlatitudref = new TagInfo("GPSLatitudeRef", 0x0001, TiffFieldTypeConstants.FIELD_TYPE_ASCII);
                TiffOutputField latitudRef = new TiffOutputField(tlatitudref, TiffFieldTypeConstants.FIELD_TYPE_ASCII, 2, new byte[]{latitudeRef, 0});
                exifDirectory.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
                exifDirectory.add(latitudRef);
            }
            OutputStream os = null;
            File newFile = null;
            try {
                newFile = new File(gpxfile.getAbsolutePath() + "-temp");
                os = new FileOutputStream(newFile);
                os = new BufferedOutputStream(os);

                new ExifRewriter().updateExifMetadataLossless(gpxfile, os, outputSet);
                gpxfile.delete();
                newFile.renameTo(gpxfile);

            } catch (ImageReadException e) {
                result = false;
            } catch (ImageWriteException e) {
                result = false;
            } catch (IOException e) {
                result = false;
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                    }
                }
            }
        } catch (Exception ex) {
            result = false;
        }
        return result;
    }

    private static byte[] getRationalArray(double a, double b, double c) throws ImageWriteException {
        TagInfo taginfo = new TagInfo("GPSLongitude", 0x0004, TiffFieldTypeConstants.FIELD_TYPE_RATIONAL);

        RationalNumber r1 = RationalNumberUtilities.getRationalNumber(a);
        RationalNumber r2 = RationalNumberUtilities.getRationalNumber(b);
        RationalNumber r3 = RationalNumberUtilities.getRationalNumber(c);
        List<Byte> bytes = new ArrayList<Byte>();
        for (Byte aByte : taginfo.encodeValue(TiffFieldTypeConstants.FIELD_TYPE_RATIONAL, r1, 'M')) {
            bytes.add(aByte);
        }
        for (Byte aByte : taginfo.encodeValue(TiffFieldTypeConstants.FIELD_TYPE_RATIONAL, r2, 'M')) {
            bytes.add(aByte);
        }
        for (Byte aByte : taginfo.encodeValue(TiffFieldTypeConstants.FIELD_TYPE_RATIONAL, r3, 'M')) {
            bytes.add(aByte);
        }
        byte[] bytesArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bytesArray[i] = bytes.get(i);
        }
        return bytesArray;
    }

}
