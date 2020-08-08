/*
 * $Id$
 * 
 * Copyright (c) 2016, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package example.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.simsilica.lemur.GuiGlobals;

import example.Main;

/**
 *  Presents a 3D grid around the current camera position.  It will
 *  look like this grid moves with the player but it's actually stationary.
 *
 *  @author    Paul Speed
 */
public class SpaceGridState extends BaseAppState {
 
    private int cellSize;
    private int gridRadius;
    private ColorRGBA gridColor;
 
    private Geometry grid;
    
    public SpaceGridState( int cellSize, int gridRadius, ColorRGBA gridColor ) {
        this.cellSize = cellSize;
        this.gridRadius = gridRadius;
        this.gridColor = gridColor;
    }
    
    @Override
    protected void initialize( Application app ) {
        
        // I have in mind a grid where the lines fade out
        // as the inverse square or something.  I think if it's
        // gradual enough it will look ok even though it's stationary.
        
        // Keep track of the values in two layers then connect them
        int size = gridRadius * 2 + 1;
        float[][] last = new float[size][size];
        float[][] current = new float[size][size];
        
        for( int i = 0; i < size; i++ ) {
            Arrays.fill(last[i], -1);
        }
        
        List<Segment> segs = new ArrayList<>();
 
        float xCenter = cellSize * 0.5f;
        float yCenter = cellSize * 0.5f;
        float zCenter = cellSize * 0.5f;
        float maxDist = gridRadius * cellSize; // just short of full grid
        @SuppressWarnings("unused")
        float maxDistSq = maxDist * maxDist;
        //float maxDist = gridRadius; //maxDistSq * maxDistSq;
        
        for( int j = 0; j < size; j++ ) {
            float y = -(gridRadius * cellSize) + j * cellSize;
            for( int i = 0; i < size; i++ ) {
                float x = -(gridRadius * cellSize) + i * cellSize;
                for( int k = 0; k < size; k++ ) {
                    float z = -(gridRadius * cellSize) + k * cellSize;
                    float dx = x - xCenter;
                    float dy = y - yCenter;
                    float dz = z - zCenter;
                    float dSquared = dx * dx + dy * dy + dz * dz;
                    float d = (float)Math.sqrt(dSquared); 
 
                    float value = (maxDist - d) / maxDist;
                    if( value > 0 ) {
                        value = value * value * value;
                    }  
                    
                    current[i][k] = value;
                    if( j > 0 ) {
                        // May need to draw a line to the previous layer
                        float neighbor = last[i][k];
                        if( value >= 0 && neighbor >= 0 ) {
                            // Then we can emit a span
                            segs.add(new Segment(x, y, z, value, x, y-cellSize, z, neighbor));
                        }
                    }
                    if( i > 0 ) {
                        // May need to draw a line to the west (-x)
                        float neighbor = current[i-1][k];
                        if( value >= 0 && neighbor >= 0 ) {
                            // Then we can emit a span
                            segs.add(new Segment(x, y, z, value, x-cellSize, y, z, neighbor));
                        }
                    }
                    if( k > 0 ) {
                        // May need to draw a line to north (-z)
                        float neighbor = current[i][k-1];
                        if( value >= 0 && neighbor >= 0 ) {
                            // Then we can emit a span
                            segs.add(new Segment(x, y, z, value, x, y, z-cellSize, neighbor));
                        }
                    }
                                   
                }
                
            }
             
            // Swap the current and last
            float[][] temp = current;
            current = last;
            last = temp; 
        }
        
        // Now make a mesh
        float[] pos = new float[segs.size() * 2 * 3];
        float[] color = new float[segs.size() * 2 * 4];
        int posIndex = 0;
        int colorIndex = 0; 
        for( Segment seg : segs ) {
            pos[posIndex++] = seg.end1.x;
            pos[posIndex++] = seg.end1.y;
            pos[posIndex++] = seg.end1.z;
            pos[posIndex++] = seg.end2.x;
            pos[posIndex++] = seg.end2.y;
            pos[posIndex++] = seg.end2.z;
            
            color[colorIndex++] = 1; 
            color[colorIndex++] = 1;  
            color[colorIndex++] = 1;  
            color[colorIndex++] = seg.value1;  
            color[colorIndex++] = 1; 
            color[colorIndex++] = 1;  
            color[colorIndex++] = 1;  
            color[colorIndex++] = seg.value2;  
        }  
 
        Mesh mesh = new Mesh();
        mesh.setMode(Mesh.Mode.Lines);
        mesh.setBuffer(VertexBuffer.Type.Position, 3, pos);
        mesh.setBuffer(VertexBuffer.Type.Color, 4, color);
        mesh.updateBound();
 
        grid = new Geometry("SpaceGrid", mesh);
        Material mat = GuiGlobals.getInstance().createMaterial(gridColor, false).getMaterial();
        mat.setBoolean("VertexColor", true);
        //mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.AlphaAdditive);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        grid.setMaterial(mat);
        grid.setQueueBucket(Bucket.Transparent);             
    }
    
    @Override
    protected void cleanup( Application app ) {
    }
    
    @Override
    protected void onEnable() {
        ((Main)getApplication()).getRootNode().attachChild(grid);
    }
 
    public void update( float tpf ) {
        /*
        Camera cam = getApplication().getCamera();
        Vector3f pos = cam.getLocation();
        if( pos.x < 0 ) {
            pos.x += cellSize;
        } else if( pos.x >= cellSize ) {
            pos.x -= cellSize;
        }
        if( pos.y < 0 ) {
            pos.y += cellSize;
        } else if( pos.y >= cellSize ) {
            pos.y -= cellSize;
        }
        if( pos.z < 0 ) {
            pos.z += cellSize;
        } else if( pos.z >= cellSize ) {
            pos.z -= cellSize;
        }
        cam.setLocation(pos);*/
    }
    
    @Override
    protected void onDisable() {
        grid.removeFromParent();
    }
    
    private class Segment {
        Vector3f end1 = new Vector3f();
        Vector3f end2 = new Vector3f();
        float value1;
        float value2;
        
        public Segment( float x1, float y1, float z1, float value1,
                        float x2, float y2, float z2, float value2 ) {
            this.end1.set(x1, y1, z1);
            this.end2.set(x2, y2, z2);
            this.value1 = Math.max(0, value1);
            this.value2 = Math.max(0, value2); 
        }
                        
    }
}
