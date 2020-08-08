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

package example;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.util.SafeArrayList;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.style.ElementId;

/**
 *  Pops up various types of messages from the bottom of the screen.
 *
 *  @author    Paul Speed
 */
public class MessageState extends BaseAppState {
 
    public static final ElementId MESSAGE_LABEL_ID = new ElementId("message.label");
 
    private Node messageRoot;
 
    private SafeArrayList<Message> messages = new SafeArrayList<>(Message.class);
 
    private float fadeRate = 1/8f; // 8 seconds
    
    public MessageState() {
    }
    
    public Label addMessage( String message ) {
        return addMessage(new Label(message, MESSAGE_LABEL_ID));
    }

    public Label addMessage( String message, ColorRGBA color ) {
        Label result = new Label(message, MESSAGE_LABEL_ID);
        result.setColor(color);
        addMessage(result);
        return result;
    }
    
    public <T extends Panel> T addMessage( T label ) {
        messages.add(0, new Message(label));
        messageRoot.attachChild(label);
        refreshLayout();
        return label;
    }
    
    @Override
    protected void initialize( Application app ) {
 
        // We keep a root node that we can slide as new
        // messages come in.  Initially we can just pop the
        // new messages up relative to the messageRoot but eventually
        // we can also animate this node to slide up from a lower
        // position to make it look like the messages are sliding
        // up smoothly.
        messageRoot = new Node("MessageRoot");            
    }
    
    @Override
    protected void cleanup( Application app ) {
    }
 
    float nextTime = 1;
    protected void addTestMessages( float tpf ) {
        nextTime -= tpf;
        if( nextTime < 0 ) {
            nextTime = (float)(Math.random() * 1.9) + 0.1f;
            int count = (int)(Math.random() * 4) + 1;
            for( int i = 0; i < count; i++ ) {
                if( (i % 2) == 0 ) {
                    addMessage("> Tick " + System.currentTimeMillis());
                } else { 
                    addMessage("> Tock " + System.currentTimeMillis());
                }
            }
        }
    }
       
    public void update( float tpf ) {

        //addTestMessages(tpf);
        
        for( Message m : messages.getArray() ) {
            m.update(tpf);
        }
    }
    
    @Override
    protected void onEnable() {
        Node gui = ((Main)getApplication()).getGuiNode();
        gui.attachChild(messageRoot);
        
        // Set a small margin for the messages
        messageRoot.setLocalTranslation(5, 5, 0);
    }
    
    @Override
    protected void onDisable() {
        messageRoot.removeFromParent();
    }           
 
    protected void refreshLayout() {
        int height = getApplication().getCamera().getHeight();
        @SuppressWarnings("unused")
        int width = getApplication().getCamera().getWidth();
 
        float y = 0;
        for( Message m : messages.getArray() ) {
            Vector3f pref = m.label.getPreferredSize();
            y += pref.y;
            m.label.setLocalTranslation(0, y, 0);
            if( y > height ) {
                // Off the screen so remove it
                m.label.removeFromParent();
                messages.remove(m);
            }           
        } 
    }
 
    private class Message {
        private float alpha;
        private float overrideAlpha = 0;
        private Panel label;
 
        public Message( Panel label ) {
            this(label, 1);
        }
        
        public Message( Panel label, float initialAlpha ) {
            this.alpha = initialAlpha;
            this.label = label;
        }
        
        public void update( float tpf ) {
            alpha -= tpf * fadeRate;
            if( alpha < 0 ) {
                alpha = 0;
            }            
            label.setAlpha(Math.max(alpha, overrideAlpha));
        }                
    }       
}
