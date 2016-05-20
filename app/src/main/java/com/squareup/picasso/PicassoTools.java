package com.squareup.picasso;

import com.squareup.picasso.Picasso;

public class PicassoTools {

    public static void clearCache(Picasso p) {
        p.cache.clear();
    }

}
