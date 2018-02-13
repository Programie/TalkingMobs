package com.selfcoders.talkingmobs;

enum Permission {
    RECEIVE("talkingmobs.receive"),
    RELOAD("talkingmobs.reload");

    private String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String permission() {
        return permission;
    }
}
