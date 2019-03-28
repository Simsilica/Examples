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

package rename;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.*;
import com.jme3.scene.*;

/**
 *  Just a spinning silicon dioxide molecule.
 *
 *  @author    Paul Speed
 */
public class SiliconDioxideState extends BaseAppState {

    private Node logo;

    public SiliconDioxideState() {
    }
    
    @Override   
    protected void initialize( Application app ) {
        logo = new Node("LogoHolder");
        Spatial molecule = app.getAssetManager().loadModel("Models/simsilica.j3o");
        molecule.center();
        logo.attachChild(molecule);
        logo.setLocalScale(0.5f);
    }
 
    @Override   
    protected void cleanup( Application app ) {
    }
    
    @Override   
    protected void onEnable() {
        Node root = ((Main)getApplication()).getRootNode();
        root.attachChild(logo);
        
        getApplication().getCamera().setLocation(new Vector3f(0, 0, 10));
        getApplication().getCamera().lookAtDirection(new Vector3f(0, 0, -1), Vector3f.UNIT_Y);
    }
 
    @Override
    public void update( float tpf ) {
        logo.rotate(0, tpf, 0);
    }
    
    @Override   
    protected void onDisable() {
        logo.removeFromParent();
    }
}
