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

import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.simsilica.lemur.input.Axis;
import com.simsilica.lemur.input.Button;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a set of player movement functions and their default control
 * mappings.
 *
 * @author Paul Speed
 */
public class PlayerMovementFunctions {

    public static final String G_MOVEMENT = "Movement";
    public static final String G_MAP = "Map";
    public static final String G_SHIPSELECTION = "Ship";
    public static final String G_TOWER = "Tower";

    public static final FunctionId F_THRUST = new FunctionId(G_MOVEMENT, "Thrust");

    /**
     * Turns the ship left or right. Default controls are setup as 'a' and 'd'.
     */
    public static final FunctionId F_TURN = new FunctionId(G_MOVEMENT, "Turn");

    /**
     * Shoots the pimary weapon of the ship. Default control mapping is the
     * space bar.
     */
    public static final FunctionId F_STOP = new FunctionId(G_MOVEMENT, "Stop");
    public static final FunctionId F_SHOOT = new FunctionId(G_MOVEMENT, "Shoot");
    public static final FunctionId F_MOUSELEFTCLICK = new FunctionId(G_MAP, "UpdateTile");
    public static final FunctionId F_MOUSERIGHTCLICK = new FunctionId(G_MAP, "RemoveTile");
    public static final FunctionId F_BOMB = new FunctionId(G_MOVEMENT, "Bomb");
    public static final FunctionId F_GRAVBOMB = new FunctionId(G_MOVEMENT, "GravBomb");
    public static final FunctionId F_REPEL = new FunctionId(G_MOVEMENT, "Repel");
    public static final FunctionId F_MINE = new FunctionId(G_MOVEMENT, "Mine");
    public static final FunctionId F_WARP = new FunctionId(G_MOVEMENT, "Warp");
    public static final FunctionId F_TOWER = new FunctionId(G_TOWER, "Tower");
    
    
    public static final FunctionId F_WARBIRD = new FunctionId(G_SHIPSELECTION, "Warbird");
    public static final FunctionId F_JAVELIN = new FunctionId(G_SHIPSELECTION, "Javelin");
    public static final FunctionId F_SPIDER = new FunctionId(G_SHIPSELECTION, "Spider");
    public static final FunctionId F_LEVI = new FunctionId(G_SHIPSELECTION, "Leviathan");
    public static final FunctionId F_TERRIER = new FunctionId(G_SHIPSELECTION, "Terrier");
    public static final FunctionId F_LANC = new FunctionId(G_SHIPSELECTION, "Lancaster");
    public static final FunctionId F_WEASEL = new FunctionId(G_SHIPSELECTION, "Weasel");
    public static final FunctionId F_SHARK = new FunctionId(G_SHIPSELECTION, "Shark");

    public static void initializeDefaultMappings(InputMapper inputMapper) {

        // Default key mappings
        if (!inputMapper.hasMappings(F_TURN)) {
            inputMapper.map(F_TURN, KeyInput.KEY_A);
            inputMapper.map(F_TURN, InputState.Negative, KeyInput.KEY_D);

        }

        if (!inputMapper.hasMappings(F_THRUST)) {
            inputMapper.map(F_THRUST, KeyInput.KEY_W);
            inputMapper.map(F_THRUST, InputState.Negative, KeyInput.KEY_S);
        }

        if (!inputMapper.hasMappings(F_REPEL)) {
            inputMapper.map(F_REPEL, KeyInput.KEY_LSHIFT, KeyInput.KEY_LCONTROL);
        }

        if (!inputMapper.hasMappings(F_MINE)) {
            inputMapper.map(F_MINE, KeyInput.KEY_TAB, KeyInput.KEY_LSHIFT);
        }
        
        if (!inputMapper.hasMappings(F_SHOOT)) {
            inputMapper.map(F_SHOOT, KeyInput.KEY_LCONTROL);
        }

        if (!inputMapper.hasMappings(F_BOMB)) {
            inputMapper.map(F_BOMB, KeyInput.KEY_TAB);
        }

        if (!inputMapper.hasMappings(F_STOP)) {
            inputMapper.map(F_STOP, KeyInput.KEY_SPACE);
        }

        if (!inputMapper.hasMappings(F_MOUSELEFTCLICK)) {
            inputMapper.map(F_MOUSELEFTCLICK, MouseInput.BUTTON_LEFT);
        }

        if (!inputMapper.hasMappings(F_MOUSERIGHTCLICK)) {
            inputMapper.map(F_MOUSERIGHTCLICK, MouseInput.BUTTON_RIGHT);
        }

        if (!inputMapper.hasMappings(F_GRAVBOMB)) {
            inputMapper.map(F_GRAVBOMB, KeyInput.KEY_LSHIFT);
        }
        
        if (!inputMapper.hasMappings(F_WARP)) {
            inputMapper.map(F_WARP, KeyInput.KEY_INSERT);
        }
        
        if (!inputMapper.hasMappings(F_TOWER)) {
            inputMapper.map(F_TOWER, KeyInput.KEY_T);
        }
        
        /**
         * Ship selection keys
         */
        if (!inputMapper.hasMappings(F_WARBIRD)) {
            inputMapper.map(F_WARBIRD, KeyInput.KEY_1);
        }
        if (!inputMapper.hasMappings(F_JAVELIN)) {
            inputMapper.map(F_JAVELIN, KeyInput.KEY_2);
        }
        if (!inputMapper.hasMappings(F_SPIDER)) {
            inputMapper.map(F_SPIDER, KeyInput.KEY_3);
        }
        if (!inputMapper.hasMappings(F_LEVI)) {
            inputMapper.map(F_LEVI, KeyInput.KEY_4);
        }
        if (!inputMapper.hasMappings(F_TERRIER)) {
            inputMapper.map(F_TERRIER, KeyInput.KEY_5);
        }
        if (!inputMapper.hasMappings(F_WEASEL)) {
            inputMapper.map(F_WEASEL, KeyInput.KEY_6);
        }
        if (!inputMapper.hasMappings(F_LANC)) {
            inputMapper.map(F_LANC, KeyInput.KEY_7);
        }
        if (!inputMapper.hasMappings(F_SHARK)) {
            inputMapper.map(F_SHARK, KeyInput.KEY_8);
        }
    }
}
