/*
 * @(#) $RCSfile: Matrix4D.java,v $ $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $ $Name: RELEASE_1_3_1_0001b $
 *
 * Center for Computational Genomics and Bioinformatics
 * Academic Health Center, University of Minnesota
 * Copyright (c) 2000-2002. The Regents of the University of Minnesota  
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * see: http://www.gnu.org/copyleft/gpl.html
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 */


package edu.umn.genomics.table.dv;  //DataViewer

import java.io.Serializable;

/** A 4D matrix object that can transform sets of 4D points 
 *  and perform a variety of manipulations on the transform. 
 * @author       J Johnson
 * @version $Revision: 1.2 $ $Date: 2008/08/19 20:03:32 $  $Name: RELEASE_1_3_1_0001b $
 * @since        1.0
 */
class Matrix4D implements Serializable {
    float xx, xy, xz, xw;
    float yx, yy, yz, yw;
    float zx, zy, zz, zw;
    float wx, wy, wz, ww;
    static final double pi = 3.14159265;
    /** Create a new unit matrix */
    public Matrix4D () {
	xx = 1.0f;
	yy = 1.0f;
	zz = 1.0f;
	ww = 1.0f;
    }
    /** Scale by f in all dimensions */
    public void scale(float f) {
	xx *= f;
	xy *= f;
	xz *= f;
	xw *= f;
	yx *= f;
	yy *= f;
	yz *= f;
	yw *= f;
	zx *= f;
	zy *= f;
	zz *= f;
	zw *= f;
    }
    /** Scale along each axis independently */
    public void scale(float xf, float yf, float zf) {
	xx *= xf;
	xy *= xf;
	xz *= xf;
	xw *= xf;
	yx *= yf;
	yy *= yf;
	yz *= yf;
	yw *= yf;
	zx *= zf;
	zy *= zf;
	zz *= zf;
	zw *= zf;
    }
    /** Translate the origin */
    public void translate(float x, float y, float z) {
	xw += x;
	yw += y;
	zw += z;
        //dump();
    }
    public void translate(float[] translation) {
      switch (translation.length) {
      default:
	zw += translation[2];
      case 2:
	yw += translation[1];
      case 1:
	xw += translation[0];
        break;
      case 0:
      }
    }
    public void setTranslation(float[] translation) {
      switch (translation.length) {
      default:
	zw = translation[2];
      case 2:
	yw = translation[1];
      case 1:
	xw = translation[0];
        break;
      case 0:
      }
    }
    public void setTranslation(float x, float y, float z) {
	xw = x;
	yw = y;
	zw = z;
    }
    public float[] getTranslation(float[] translation) {
      float[] t;
      if (translation != null) {
        t = translation;
      } else {
        t = new float[3];
      }
      switch (t.length) {
      default:
        t[2] = zw;
      case 2:
        t[1] = yw;
      case 1:
        t[0] = xw;
        break;
      case 0:
      }
      return t;
    }
    /** rotate theta degrees about the y axis */
    public void yrot(double theta) {
	theta *= (pi / 180);
	double ct = Math.cos(theta);
	double st = Math.sin(theta);

	float Nxx = (float) (xx * ct + zx * st);
	float Nxy = (float) (xy * ct + zy * st);
	float Nxz = (float) (xz * ct + zz * st);
	float Nxw = (float) (xw * ct + zw * st);

	float Nzx = (float) (zx * ct - xx * st);
	float Nzy = (float) (zy * ct - xy * st);
	float Nzz = (float) (zz * ct - xz * st);
	float Nzw = (float) (zw * ct - xw * st);

	xw = Nxw;
	xx = Nxx;
	xy = Nxy;
	xz = Nxz;
	zw = Nzw;
	zx = Nzx;
	zy = Nzy;
	zz = Nzz;
    }
    /** rotate theta degrees about the x axis */
    public void xrot(double theta) {
	theta *= (pi / 180);
	double ct = Math.cos(theta);
	double st = Math.sin(theta);

	float Nyx = (float) (yx * ct + zx * st);
	float Nyy = (float) (yy * ct + zy * st);
	float Nyz = (float) (yz * ct + zz * st);
	float Nyw = (float) (yw * ct + zw * st);

	float Nzx = (float) (zx * ct - yx * st);
	float Nzy = (float) (zy * ct - yy * st);
	float Nzz = (float) (zz * ct - yz * st);
	float Nzw = (float) (zw * ct - yw * st);

	yw = Nyw;
	yx = Nyx;
	yy = Nyy;
	yz = Nyz;
	zw = Nzw;
	zx = Nzx;
	zy = Nzy;
	zz = Nzz;
    }
    /** rotate theta degrees about the z axis */
    public void zrot(double theta) {
	theta *= (pi / 180);
	double ct = Math.cos(theta);
	double st = Math.sin(theta);

	float Nyx = (float) (yx * ct + xx * st);
	float Nyy = (float) (yy * ct + xy * st);
	float Nyz = (float) (yz * ct + xz * st);
	float Nyw = (float) (yw * ct + xw * st);

	float Nxx = (float) (xx * ct - yx * st);
	float Nxy = (float) (xy * ct - yy * st);
	float Nxz = (float) (xz * ct - yz * st);
	float Nxw = (float) (xw * ct - yw * st);

	yw = Nyw;
	yx = Nyx;
	yy = Nyy;
	yz = Nyz;
	xw = Nxw;
	xx = Nxx;
	xy = Nxy;
	xz = Nxz;
    }
    /** Multiply this matrix by a second: M = M*R */
    public void mult(Matrix4D rhs) {
	float lxx = xx * rhs.xx + yx * rhs.xy + zx * rhs.xz + wx * rhs.xw;
	float lxy = xy * rhs.xx + yy * rhs.xy + zy * rhs.xz + wy * rhs.xw;
	float lxz = xz * rhs.xx + yz * rhs.xy + zz * rhs.xz + wz * rhs.xw;
	float lxw = xw * rhs.xx + yw * rhs.xy + zw * rhs.xz + ww * rhs.xw;

	float lyx = xx * rhs.yx + yx * rhs.yy + zx * rhs.yz + wx * rhs.yw;
	float lyy = xy * rhs.yx + yy * rhs.yy + zy * rhs.yz + wy * rhs.yw;
	float lyz = xz * rhs.yx + yz * rhs.yy + zz * rhs.yz + wz * rhs.yw;
	float lyw = xw * rhs.yx + yw * rhs.yy + zw * rhs.yz + ww * rhs.yw;

	float lzx = xx * rhs.zx + yx * rhs.zy + zx * rhs.zz + wx * rhs.zw;
	float lzy = xy * rhs.zx + yy * rhs.zy + zy * rhs.zz + wy * rhs.zw;
	float lzz = xz * rhs.zx + yz * rhs.zy + zz * rhs.zz + wz * rhs.zw;
	float lzw = xw * rhs.zx + yw * rhs.zy + zw * rhs.zz + ww * rhs.zw;

	float lwx = xx * rhs.zx + yx * rhs.zy + zx * rhs.zz + wx * rhs.ww;
	float lwy = xy * rhs.zx + yy * rhs.zy + zy * rhs.zz + wy * rhs.ww;
	float lwz = xz * rhs.zx + yz * rhs.zy + zz * rhs.zz + wz * rhs.ww;
	float lww = xw * rhs.zx + yw * rhs.zy + zw * rhs.zz + ww * rhs.ww;

	xx = lxx;
	xy = lxy;
	xz = lxz;
	xw = lxw;

	yx = lyx;
	yy = lyy;
	yz = lyz;
	yw = lyw;

	zx = lzx;
	zy = lzy;
	zz = lzz;
	zw = lzw;

	wx = lwx;
	wy = lwy;
	wz = lwz;
	ww = lww;
    }

    /** Reinitialize to the unit matrix */
    public void setIdentity() {
	xx = 1;
	xy = 0;
	xz = 0;
	xw = 0;

	yx = 0;
	yy = 1;
	yz = 0;
	yw = 0;

	zx = 0;
	zy = 0;
	zz = 1;
	zw = 0;

	wx = 0;
	wy = 0;
	wz = 0;
	ww = 1;
    }

    /** Set a Perspective Projection */
    public void setFrustum(float l,float r,float b,float t,float n,float f) {
	xx = 2*n / (r-l);
	xy = 0;
	xz = (r+l)/(r-l);
	xw = 0;

	yx = 0;
	yy = 2*n / (t-b);
	yz = (t+b)/(t-b);
	yw = 0;

	zx = 0;
	zy = 0;
	zz = -1*(f+n)/(f-n);
	zw = -2*f*n / (f-n);

	wx = 0;
	wy = 0;
	wz = -1;
	ww = 0;
    }

    /** Set an Orthographic Projection */
    public void setOrtho(float l,float r,float b,float t,float n,float f) {
	xx = 2 / (r-l);
	xy = 0;
	xz = 0;
	xw = -1*(r+l)/(r-l);

	yx = 0;
	yy = 2 / (t-b);
	yz = 0;
	yw = -1*(t+b)/(t-b);

	zx = 0;
	zy = 0;
	zz = -2/(f-n);
	zw = -1*(f+n)/(f-n);

	wx = 0;
	wy = 0;
	wz = 0;
	ww = 1;
    }

    /** Transform nvert points from v into tv.  v contains the input
        coordinates in floating point.  Three successive entries in
	the array constitute a point.  tv ends up holding the transformed
	points as integers; three successive entries per point */
    public void transform(float v[], int tv[], int nvert) {
	float lxx = xx, lxy = xy, lxz = xz, lxw = xw;
	float lyx = yx, lyy = yy, lyz = yz, lyw = yw;
	float lzx = zx, lzy = zy, lzz = zz, lzw = zw;
	for (int i = nvert * 3; (i -= 3) >= 0;) {
	    float x = v[i];
	    float y = v[i + 1];
	    float z = v[i + 2];
	    tv[i    ] = (int) (x * lxx + y * lxy + z * lxz + lxw);
	    tv[i + 1] = (int) (x * lyx + y * lyy + z * lyz + lyw);
	    tv[i + 2] = (int) (x * lzx + y * lzy + z * lzz + lzw);
	}
    }

    /** Transform nvert points from v into tv.  v contains the input
        coordinates in floating point.  Three successive entries in
	the array constitute a point.  tv ends up holding the transformed
	points as integers; three successive entries per point */
    public void transform(float v[], float tv[], int nvert) {
	float lxx = xx, lxy = xy, lxz = xz, lxw = xw;
	float lyx = yx, lyy = yy, lyz = yz, lyw = yw;
	float lzx = zx, lzy = zy, lzz = zz, lzw = zw;
	for (int i = nvert * 3; (i -= 3) >= 0;) {
	    float x = v[i];
	    float y = v[i + 1];
	    float z = v[i + 2];
	    tv[i    ] = (x * lxx + y * lxy + z * lxz + lxw);
	    tv[i + 1] = (x * lyx + y * lyy + z * lyz + lyw);
	    tv[i + 2] = (x * lzx + y * lzy + z * lzz + lzw);
	}
    }

    /** Transform nvert points from v into tv.  v contains the input
        coordinates in floating point.  Three successive entries in
	the array constitute a point.  tv ends up holding the transformed
	points as integers; three successive entries per point */
    public void transform(float v[], int inc, float tv[], int outc, int first, int nvert){
	float lxx = xx, lxy = xy, lxz = xz, lxw = xw;
	float lyx = yx, lyy = yy, lyz = yz, lyw = yw;
	float lzx = zx, lzy = zy, lzz = zz, lzw = zw;
	float lwx = wx, lwy = wy, lwz = wz, lww = ww;
        int fiv = first * inc;
        int fov = first * outc;
	for (int i = (first + nvert) * inc, j = (first + nvert) * outc; 
                 (i -= inc) >= fiv && (j-=outc) >= fov;) {
	    float x = v[i];
	    float y = v[i + 1];
	    float z = v[i + 2];
            if (inc == 3) {
	      tv[j    ] = (x * lxx + y * lxy + z * lxz + lxw);
	      tv[j + 1] = (x * lyx + y * lyy + z * lyz + lyw);
	      tv[j + 2] = (x * lzx + y * lzy + z * lzz + lzw);
              if (outc == 4) {
	        tv[j + 3] = 1f;
              }
            } else if (inc == 4) {
              float w = v[i + 3];
	      tv[j    ] = (x * lxx + y * lxy + z * lxz + w * lxw);
	      tv[j + 1] = (x * lyx + y * lyy + z * lyz + w * lyw);
	      tv[j + 2] = (x * lzx + y * lzy + z * lzz + w * lzw);
              if (outc == 4) {
	        tv[j + 3] = (x * lwx + y * lwy + z * lwz + w * lww);
              }
            }
	}
    }

    @Override
	public String toString() {
	return ("[" + xw + "," + xx + "," + xy + "," + xz + ";"
		+ yw + "," + yx + "," + yy + "," + yz + ";"
		+ zw + "," + zx + "," + zy + "," + zz + "]");
    }
    public void dump() {
       System.err.println(
           "\n" + xx + "\t" + xy + "\t" + xz + "\t" + xw 
         + "\n" + yx + "\t" + yy + "\t" + yz + "\t" + yw 
         + "\n" + zx + "\t" + zy + "\t" + zz + "\t" + zw 
         + "\n" + wx + "\t" + wy + "\t" + wz + "\t" + ww 
         + "\n");
    }
}
