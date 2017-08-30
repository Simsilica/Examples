/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.map;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 *
 * @author Asser
 */
public class BitMapLoader implements AssetLoader {

    @Override
    public BitMap load(AssetInfo assetInfo) throws IOException {
        BitMap bmp = new BitMap(new BufferedInputStream(assetInfo.openStream()));
        bmp.readBitMap(false);
        return bmp;
    }
}
