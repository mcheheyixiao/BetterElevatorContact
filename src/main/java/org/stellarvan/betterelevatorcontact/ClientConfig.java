package org.stellarvan.betterelevatorcontact;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/** Client-only settings; this class intentionally has no Minecraft client references. */
public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> LANGUAGE;

    static {
        var builder = new ForgeConfigSpec.Builder();
        LANGUAGE = builder.comment(
                "UI language: auto follows Minecraft; zh_cn = Simplified Chinese; en_us = English.",
                "界面语言：auto 跟随游戏，zh_cn 简体中文，en_us 英文。修改后重新打开锚点界面。")
                .defineInList("language", "auto", List.of("auto", "zh_cn", "en_us"));
        SPEC = builder.build();
    }

    private ClientConfig() {}
}
