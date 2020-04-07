package com.server;

public class Storage {
    private long storageAll;
    private long storageFill;

    public void setStorageAll(long storageAll) {
        this.storageAll = storageAll;
    }

    public void setStorageFill(long storageFill) {
        this.storageFill = storageFill;
    }

    public long getStorageAll() {
        return storageAll;
    }

    public long getStorageFill() {
        return storageFill;
    }
}
