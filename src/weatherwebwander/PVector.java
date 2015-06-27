/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package weatherwebwander;

/**
 *
 * @author simonkenny
 */
public class PVector {
    
    public float x;
    public float y;
    
    public PVector() {
        x = 0.f;
        y = 0.f;
    }
    
    public PVector(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public void sub(PVector pv) {
        this.x -= pv.x;
        this.y -= pv.y;
    }
    
    public void sub(float val) {
        this.x -= val;
        this.y -= val;
    }
    
    public void add(PVector pv) {
        this.x += pv.x;
        this.y += pv.y;
    }
    
    public void add(float val) {
        this.x += val;
        this.y += val;
    }
    
    public void mult(PVector pv) {
        this.x *= pv.x;
        this.y *= pv.y;
    }
    
    public void mult(float val) {
        this.x *= val;
        this.y *= val;
    }
    
    public void set(PVector pv) {
        this.x = pv.x;
        this.y = pv.y;
    }
    
    public float mag() {
        return (float)Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }
    
    public void normalize() {
        float mag = mag();
        this.x /= mag;
        this.y /= mag;
    }
}
