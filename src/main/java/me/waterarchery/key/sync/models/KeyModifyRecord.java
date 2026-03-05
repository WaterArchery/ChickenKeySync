package me.waterarchery.key.sync.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class KeyModifyRecord {

    private final UUID player;
    private final String keyId;
    private int keyAmount;
}
