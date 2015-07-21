/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weatherwebwander.gradedstring;

import java.util.Comparator;

/**
 *
 * @author simonkenny
 */
// reverse ordering, largest values to front
public class GradedStringReverseOrderComparitor implements Comparator<GradedString> {
    @Override
    public int compare(GradedString gs1, GradedString gs2) {
        int c1 = gs1.getGrade();
        int c2 = gs2.getGrade();
        // note: uses reverse ordering
        if (c1 > c2) {
            return -1;
        } else if (c1 < c2){
            return 1;
        } else {
            return 0;
        }
    }
}
