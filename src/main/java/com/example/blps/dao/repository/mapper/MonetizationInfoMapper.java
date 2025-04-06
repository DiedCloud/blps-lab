package com.example.blps.dao.repository.mapper;

import lombok.NonNull;

public class MonetizationInfoMapper {
    static public com.example.blps.entity.MonetizationInfo getMonetizationInfo(
            @NonNull com.example.blps.dao.repository.model.MonetizationInfo monetizationInfo
    ) {
        return new com.example.blps.entity.MonetizationInfo(
                VideoInfoMapper.getVideoInfo(monetizationInfo.getVideo()),
                monetizationInfo.getPercent(),
                monetizationInfo.getIsAgreed()
        );
    }
}
