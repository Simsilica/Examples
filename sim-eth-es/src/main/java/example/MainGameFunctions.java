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

import com.jme3.input.KeyInput;
import com.simsilica.lemur.input.Button;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;


/**
 *  Defines a set of global game functions and some default key/control
 *  mappings.
 *
 *  @author    Paul Speed
 */
public class MainGameFunctions {

    public static final String IN_GAME = "In Game";
    public static final FunctionId F_IN_GAME_MENU = new FunctionId(IN_GAME, "Menu");
    
    public static final FunctionId F_IN_GAME_HELP = new FunctionId(IN_GAME, "In-Game Help");

    public static final FunctionId F_PLAYER_LIST = new FunctionId(IN_GAME, "Player List");
    
    public static final FunctionId F_COMMAND_CONSOLE = new FunctionId(IN_GAME, "Command Console");
 
    public static final FunctionId F_TIME_DEBUG = new FunctionId(IN_GAME, "Time Debug");
    
    public static void initializeDefaultMappings( InputMapper inputMapper ) {
    
        inputMapper.map(F_IN_GAME_MENU, KeyInput.KEY_ESCAPE);
        inputMapper.map(F_IN_GAME_MENU, Button.JOYSTICK_SELECT); // the normal one
        inputMapper.map(F_IN_GAME_MENU, Button.JOYSTICK_BUTTON8); // just in case it's not a gamepad
        inputMapper.map(F_IN_GAME_HELP, KeyInput.KEY_F1);

        inputMapper.map(F_PLAYER_LIST, KeyInput.KEY_F2);

        inputMapper.map(F_TIME_DEBUG, KeyInput.KEY_F7);
        
        inputMapper.map(F_COMMAND_CONSOLE, KeyInput.KEY_RETURN);
        inputMapper.map(F_COMMAND_CONSOLE, KeyInput.KEY_NUMPADENTER);
    }
}
