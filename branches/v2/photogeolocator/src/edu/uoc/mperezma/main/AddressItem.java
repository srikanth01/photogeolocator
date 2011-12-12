/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.uoc.mperezma.main;

import android.location.Address;

/**
 *
 * @author mperezma
 */
public class AddressItem {

    protected final Address address;

    public AddressItem(Address address) {
        this.address = address;
    }
    
    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            result += address.getAddressLine(i);
            if (i < address.getMaxAddressLineIndex()) {
                result += "\n";
            }
        }
        return result;
    }
}
