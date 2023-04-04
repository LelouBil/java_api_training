package fr.lernejo.navy_battle.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FireConsequence {
    @JsonProperty("miss")
    MISS,
    @JsonProperty("hit")
    HIT,
    @JsonProperty("sunk")
    SUNK

}
