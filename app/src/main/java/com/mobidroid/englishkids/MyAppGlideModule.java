package com.mobidroid.englishkids;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;

@GlideModule
public class MyAppGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // Register FirebaseImageLoader to handle StorageReference


        registry.append(StorageReference.class, InputStream.class,
                (ResourceDecoder<StorageReference, InputStream>) new Factory());
    }

    private class Factory implements com.bumptech.glide.load.ResourceDecoder<StorageReference, InputStream>, com.bumptech.glide.load.model.ModelLoaderFactory<StorageReference, InputStream> {
        @Override
        public boolean handles(@NonNull StorageReference source, @NonNull Options options) throws IOException {
            return false;
        }

        @Nullable
        @Override
        public Resource<InputStream> decode(@NonNull StorageReference source, int width, int height, @NonNull Options options) throws IOException {
            return null;
        }

        @NonNull
        @Override
        public ModelLoader<StorageReference, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return null;
        }

        @Override
        public void teardown() {

        }
    }
}
