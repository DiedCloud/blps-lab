package com.example.blps.entity;

import lombok.Data;
import lombok.NonNull;

@Data
public class MonetizationInfo {
    @NonNull
    VideoInfo video;
    @NonNull
    Float percent;
    @NonNull
    Boolean isAgreed;
}
