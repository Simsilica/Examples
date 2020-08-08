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

package example.debug;

import java.nio.FloatBuffer;
import java.util.Arrays;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Line;
import com.jme3.util.BufferUtils;
import com.simsilica.ethereal.Statistics;
import com.simsilica.ethereal.Statistics.Sequence;
import com.simsilica.ethereal.Statistics.Tracker;
import com.simsilica.ethereal.SynchedTimeSource;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.input.InputMapper;

import example.Main;
import example.MainGameFunctions;
import example.TimeState;



/**
 *  Renders some frame timing related sequences as a graphic.
 *
 *  @author    Paul Speed
 */
public class TimeSequenceState extends BaseAppState {

    private SequenceEntry[] sequences = {
                /**
                 *  syncTime is the timestamps on the update messages we
                 *  received from the server.  If we are receiving messages
                 *  regularly these should coincide evenly with the servers
                 *  send interval.  (Default is 20 FPS)
                 */
                new SequenceEntry("syncTime", ColorRGBA.Cyan, 24 * 3),
                
                /**
                 *  stateTime is the time of received frame states in server
                 *  time.  These should roughly be stepped equivalently to the
                 *  game simulations update time.
                 */
                new SequenceEntry("stateTime", ColorRGBA.Green, 24 * 3 * 3),
                
                /**
                 *  frameTime is the time as we see it every update frame.
                 *  This includes server drift and the current time offset.
                 */
                new SequenceEntry("frameTime", ColorRGBA.White, 60 * 3)
            };

    private TimeState timeState;
    private long baseTime;
    private Sequence frameTime;
    private Node root;
    
    private Geometry realTime;
    private Geometry offsetTime;

    // Keep track of the delta between current time and 'frame time'.
    // We need a consistent base do track ups and downs in the drift.
    private long driftLock;
    private boolean resetDrift;

    private Graph driftGraph;
    private Geometry driftGeom;

    private Graph msgGraph;
    private Geometry msgGeom;
    private Tracker msgSize;

    public TimeSequenceState() {
        setEnabled(false);        
    }

    public void toggleEnabled() {
        setEnabled(!isEnabled());
    }

    @Override
    protected void initialize( Application app ) {
        
        this.timeState = getState(TimeState.class);
        this.frameTime = Statistics.getSequence("frameTime", true);
        this.msgSize = Statistics.getTracker("messageSize", 5, true);

        this.root = new Node("TimeSequenceHud");
 
        float offset = 0;       
        for( SequenceEntry e : sequences ) {
            e.initialize();
            
            root.attachChild(e.geom);
            e.geom.setLocalTranslation(0, offset, 0);
            e.geom.setLocalScale(1, 10, 1);
            e.label.setLocalTranslation(50, offset + 15, 0);
            root.attachChild(e.label);
            
            offset += 10;
        }
 
        Camera cam = app.getCamera();        
        
        root.setLocalTranslation(cam.getWidth() - 200, cam.getHeight() - 100, 0);
 
        Node graphRoot = new Node("graphRoot");
        graphRoot.setLocalTranslation(root.getLocalTranslation().negate());
        root.attachChild(graphRoot);
 
        realTime = createTimeMarker(ColorRGBA.Yellow);
        offsetTime = createTimeMarker(ColorRGBA.Magenta); 
 
        driftGraph = new Graph(ColorRGBA.Green, cam.getWidth());
        
        driftGeom = new Geometry("drift", driftGraph.mesh);
        Material mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.White, false).getMaterial();
        mat.setBoolean("VertexColor", true);
        driftGeom.setMaterial(mat);
        driftGeom.setLocalTranslation(0, cam.getHeight() * 0.75f, -10);
        driftGeom.setLocalScale(1, 10f/1000000, 1); // 1 ms = 10 pixels
        graphRoot.attachChild(driftGeom);

        Label label;
        label = new Label("time drift");
        label.setFontSize(10);
        label.setLocalTranslation(0, cam.getHeight() * 0.75f, -10);
        graphRoot.attachChild(label);

        msgGraph = new Graph(ColorRGBA.Blue, cam.getWidth());
        
        msgGeom = new Geometry("msgSize", msgGraph.mesh);
        mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.White, false).getMaterial();
        mat.setBoolean("VertexColor", true);
        msgGeom.setMaterial(mat);
        msgGeom.setLocalTranslation(0, cam.getHeight() * 0.80f, -10);
        msgGeom.setLocalScale(1, 50f/1500, 1); // 50 pixels = 1500 bytes
        graphRoot.attachChild(msgGeom);

        label = new Label("avg msg size");
        label.setFontSize(10);
        label.setLocalTranslation(0, cam.getHeight() * 0.80f, -10);
        graphRoot.attachChild(label);
                
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.addDelegate(MainGameFunctions.F_TIME_DEBUG, this, "toggleEnabled");
    }

    @Override
    protected void cleanup( Application app ) {
        for( SequenceEntry e : sequences ) {
            e.cleanup();
        }
        
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.removeDelegate(MainGameFunctions.F_TIME_DEBUG, this, "toggleEnabled");
    }

    @Override
    protected void onEnable() {
        ((Main)getApplication()).getGuiNode().attachChild(root);
        resetDrift = true;
        driftGraph.reset();                
        msgGraph.reset();                
    }

    @Override
    protected void onDisable() {
        root.removeFromParent();
    }

    @Override
    public void update( float tpf ) {       
        long frame = timeState.getTime();
        frameTime.add(frame);
                        
        for( SequenceEntry e : sequences ) {
            e.updateArray();
        }
        
        // Base time is the latest server sync time
        // Base the time on the perceived frame time
        this.baseTime = frame; //sequences[0].latest;
 
        // Move the 'real time' bar as appropriate
        // This will show us what the current drift is from the server...
        // which unless our times are very closely synched is likely to send us
        // off the screen.  Still, might as well show it.
        long delta = ((timeState.getRealTime() - baseTime) / 1000000) / 3;
        realTime.setLocalTranslation(delta, 0, -1);

        delta = (-((SynchedTimeSource) timeState.getTimeSource()).getOffset() / 1000000) / 3;
        offsetTime.setLocalTranslation(delta, 0, -1);

        for( SequenceEntry e : sequences ) {
            e.updateMesh();
        }

        long time = timeState.getRealTime();
        if( resetDrift ) {
            driftLock = frame - time;
            resetDrift = false;               
        }        
        // Calculate this frame's relative drift
        long drift = frame - time - driftLock;
        //System.out.println("Drift:" + drift);
        driftGraph.addValue(drift);
 
        long averageMessageSize = msgSize.get();
        msgGraph.addValue(averageMessageSize);
    }
 
    protected Geometry createTimeMarker( ColorRGBA color ) {
        Mesh mesh = new Line(new Vector3f(0, 1, 0), new Vector3f(0, sequences.length * 10, 0));
        Geometry result = new Geometry("timeMarker", mesh);
        Material mat = GuiGlobals.getInstance().createMaterial(color, false).getMaterial();
        result.setMaterial(mat);
        root.attachChild(result);
        return  result;
    }
    
    private class SequenceEntry {
        private String name;
        private Sequence sequence;
        private ColorRGBA color;
        private long[] times;
        private int end = 0;
        private long latest = 0;
        
        private Mesh mesh;
        private Geometry geom;
        private Material mat;
        private Label label;
        
        @SuppressWarnings("unused")
        public SequenceEntry( String name, ColorRGBA color ) {
            this(name, color, 12);
        }
        
        public SequenceEntry( String name, ColorRGBA color, int size ) {
            this.name = name;
            this.sequence = Statistics.getSequence(name, true);
            this.times = new long[size];
            this.color = color;
            this.label = new Label(name);
            label.setFontSize(10);
            label.setColor(color);            
        }
        
        private int next( int i ) {
            return (i + 1) % times.length;
        }
        
        public void initialize() {
            this.mat = GuiGlobals.getInstance().createMaterial(color, false).getMaterial();
            this.mesh = new Mesh();
            mesh.setMode(Mode.Lines);
            
            float[] temp = new float[times.length * 3 * 2];
            mesh.setBuffer(Type.Position, 3, temp);
            temp = new float[times.length * 4 * 2];
            mesh.setBuffer(Type.Color, 4, temp);
            mesh.updateBound();
            
            this.geom = new Geometry(name, mesh);
            geom.setMaterial(mat);                        
        }

        public void cleanup() {
        }

        public void updateArray() {
            
            // Update the array with the latest entries
            Long value;
            while( (value = sequence.poll()) != null ) {
                if( value > latest ) {
                    latest = value;
                }
                times[end] = value;
                end = next(end);
            }
        }

        public void updateMesh() {
            
            VertexBuffer pb = mesh.getBuffer(Type.Position);
            VertexBuffer cb = mesh.getBuffer(Type.Color);
            FloatBuffer pos = (FloatBuffer)pb.getData();
            pos.rewind();
            FloatBuffer colors = (FloatBuffer)cb.getData();
            colors.rewind();

            int index = end;           
            for( int i = 0; i < times.length; i++, index = next(index) ) {
                float time = ((times[index] - baseTime) / 1000000) / 3;
                
                pos.put(time);
                pos.put(0);
                pos.put(0);
 
                pos.put(time);
                pos.put(1);
                pos.put(0);
                               
                if( time > 0 ) {
                    colors.put(1).put(0).put(0).put(1);
                } else {
                    colors.put(color.r);
                    colors.put(color.g);
                    colors.put(color.b);
                    colors.put(color.a);
                }
                
                colors.put(color.r);
                colors.put(color.g);
                colors.put(color.b);
                colors.put(color.a);
            }
            
            pos.rewind();
            pb.updateData(pos);
            colors.rewind();
            cb.updateData(colors);
            //mesh.updateBound();
            //geom.updateModelBound();              
        }
        
    }
    
    private class Graph {
        private ColorRGBA color;
        private int size;
        private int frameIndex = 0;
        private long[] frames;
        private long updateInterval = 1000000L; // once a millisecond
        private long lastUpdate = 0;
        
        private Mesh mesh;

        @SuppressWarnings("unused")
        public Graph( ColorRGBA color ) {
            this(color, 1280);
        }
                
        public Graph( ColorRGBA color, int size ) {
            this.color = color;
            setFrameCount(size);
        }
 
        public void reset() {
            frameIndex = 0;
            Arrays.fill(frames, 0);
        }
        
        /**
         *  Sets the number of frames to display and track.  By default
         *  this is 1280.
         */
        public final void setFrameCount( int size ) {
            if( this.size == size ) {
                return;
            }
        
            this.size = size;
            this.frames = new long[size];
    
            createMesh();
            
            if( frameIndex >= size ) {
                frameIndex = 0;
            }       
        }
    
        @SuppressWarnings("unused")
        public int getFrameCount() {
            return size;
        }

        /**
         *  Sets the number of nanoseconds to wait before updating the
         *  mesh.  By default this is once a millisecond, ie: 1000000 nanoseconds.
         */
        @SuppressWarnings("unused")
        public void setUpdateInterval( long nanos ) {
            this.updateInterval = nanos;
        }
    
        @SuppressWarnings("unused")
        public long getUpdateInterval() {
            return updateInterval;
        }

        /**
         *  Returns the mesh that contains the bar chart of tracked frame
         *  timings.
         */
        @SuppressWarnings("unused")
        public Mesh getMesh() {
            return mesh;
        }

        protected final void createMesh() {
            if( mesh == null ) {
                mesh = new Mesh();
                mesh.setMode(Mesh.Mode.Lines);
            }
        
            mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(size * 2 * 3));

            FloatBuffer cb = BufferUtils.createFloatBuffer(size * 2 * 4);
            for( int i = 0; i < size; i++ ) {
                // For each index we add 2 colors, one for each line
                // endpoint for one layers.
                cb.put(0.5f).put(0.5f).put(0.5f).put(1);
                cb.put(1).put(1).put(1).put(1);
            }         
            mesh.setBuffer(Type.Color, 4, cb);
        }
    
        protected void updateMesh() {
            FloatBuffer pb = (FloatBuffer)mesh.getBuffer(Type.Position).getData();
            pb.rewind();
            FloatBuffer cb = (FloatBuffer)mesh.getBuffer(Type.Color).getData();
            cb.rewind();
             
            for( int i = 0; i < size; i++ ) {
                float t = frames[i];
                
                pb.put(i-1).put(0).put(-1);
                pb.put(i).put(t).put(0);
 
                int tail = frameIndex + 2;
 
                if( i >= frameIndex && i <= tail ) {
                    cb.put(0.0f).put(0.0f).put(0.0f).put(1);
                    cb.put(0.0f).put(0.0f).put(0.0f).put(1);
                } else if( frames[i] == 0 ) {
                    cb.put(0.5f).put(0.5f).put(0.5f).put(1);
                    cb.put(0.75f).put(0.75f).put(0.75f).put(1);
                } else {
                    cb.put(color.r * 0.5f).put(color.g * 0.5f).put(color.b * 0.5f).put(color.a);
                    cb.put(color.r).put(color.g).put(color.b).put(color.a);
                }
            }
            mesh.setBuffer(Type.Position, 3, pb);
            mesh.setBuffer(Type.Color, 4, cb);
        }

        public void addValue( long value ) {
            long time = System.nanoTime();
            frames[frameIndex] = value;
            frameIndex++;
            if( frameIndex >= size ) {
                frameIndex = 0;
            }
            if( time - lastUpdate > updateInterval ) {
                updateMesh();
                lastUpdate = time;
            } 
        }
    }      
}


